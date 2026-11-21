package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.util.concurrent.TimeUnit

sealed class BscVerifyResult {
    data class Verified(
        val amountUsdt: Double,
        val fromAddress: String,
        val receiverAddress: String,
        val blockNumberHex: String
    ) : BscVerifyResult()

    data class Failed(val reason: String) : BscVerifyResult()
}

object BscScanVerifier {

    const val DEPOSIT_WALLET_ADDRESS = "0xe59dca55642127ebac2fb92c1f0cffdfdccdd840"
    const val BSC_USDT_CONTRACT = "0x55d398326f99059ff775485246999027b3197955"
    private const val TRANSFER_EVENT_TOPIC =
        "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"

    private val TX_HASH_REGEX = Regex("^0x[a-fA-F0-9]{64}$")
    private val WEI_DIVISOR = BigDecimal("1000000000000000000") // 10^18 for BSC BEP20 USDT

    private val rpcEndpoints = listOf(
        "https://bsc-dataseed.binance.org/",
        "https://bsc-dataseed1.defibit.io/",
        "https://bsc-dataseed1.ninicoin.io/"
    )

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    suspend fun verifyBep20UsdtDeposit(
        rawTxHash: String,
        expectedMinAmount: Double? = null
    ): BscVerifyResult = withContext(Dispatchers.IO) {
        val txHash = rawTxHash.trim().lowercase()
        if (!TX_HASH_REGEX.matches(txHash)) {
            return@withContext BscVerifyResult.Failed(
                "Invalid TxID format. Enter a valid 66-character BSC transaction hash starting with 0x."
            )
        }

        // 1. Try BscScan Public API first, fallback to official BSC JSON-RPC nodes if key/rate-limited
        val receipt = fetchTransactionReceiptFromBscScan(txHash)
            ?: fetchTransactionReceiptFromBscRpc(txHash)
            ?: return@withContext BscVerifyResult.Failed(
                "Transaction not found on BSC network yet, or network unavailable. Verify your TxID and internet connection."
            )

        parseReceiptForBep20Usdt(receipt, expectedMinAmount)
    }

    internal fun parseReceiptForBep20Usdt(
        receipt: JSONObject,
        expectedMinAmount: Double? = null
    ): BscVerifyResult {
        // 2. Verify transaction succeeded (status == "0x1")
        val status = receipt.optString("status", "").lowercase()
        if (status != "0x1") {
            return BscVerifyResult.Failed(
                "Transaction failed or was reverted on BNB Smart Chain (status: ${status.ifBlank { "pending" }})."
            )
        }

        // 3. Parse Transfer logs for BEP20 USDT sent to our hardcoded Binance address
        val targetAddressNoPrefix = DEPOSIT_WALLET_ADDRESS.removePrefix("0x").lowercase()
        val logs = receipt.optJSONArray("logs") ?: JSONArray()

        var totalMatchedUsdt = BigDecimal.ZERO
        var detectedFromAddress = receipt.optString("from", "")
        var sawWrongTokenToOurWallet = false

        for (i in 0 until logs.length()) {
            val logObj = logs.optJSONObject(i) ?: continue
            val contractAddr = logObj.optString("address", "").lowercase()
            val topics = logObj.optJSONArray("topics") ?: continue
            if (topics.length() < 3) continue

            val eventSig = topics.optString(0, "").lowercase()
            if (eventSig != TRANSFER_EVENT_TOPIC) continue

            val toTopic = topics.optString(2, "").lowercase().removePrefix("0x")
            if (toTopic.length < 40) continue
            val receiverAddrNoPrefix = toTopic.takeLast(40)

            if (receiverAddrNoPrefix == targetAddressNoPrefix) {
                if (contractAddr != BSC_USDT_CONTRACT) {
                    sawWrongTokenToOurWallet = true
                    continue
                }

                val fromTopic = topics.optString(1, "").lowercase().removePrefix("0x")
                if (fromTopic.length >= 40) {
                    detectedFromAddress = "0x" + fromTopic.takeLast(40)
                }

                val dataHex = logObj.optString("data", "0x0").removePrefix("0x").ifBlank { "0" }
                val rawAmount = try {
                    BigInteger(dataHex, 16)
                } catch (_: Exception) {
                    BigInteger.ZERO
                }
                if (rawAmount > BigInteger.ZERO) {
                    val tokenAmount = BigDecimal(rawAmount).divide(WEI_DIVISOR, MathContext.DECIMAL128)
                    totalMatchedUsdt = totalMatchedUsdt.add(tokenAmount)
                }
            }
        }

        if (totalMatchedUsdt <= BigDecimal.ZERO) {
            return if (sawWrongTokenToOurWallet) {
                BscVerifyResult.Failed(
                    "Wrong token detected. Only USDT (BEP20) transfers to our Binance wallet are accepted."
                )
            } else {
                BscVerifyResult.Failed(
                    "Receiver address does not match our BEP20 USDT wallet ($DEPOSIT_WALLET_ADDRESS)."
                )
            }
        }

        val amountUsdt = totalMatchedUsdt.toDouble()
        if (amountUsdt <= 0.0) {
            return BscVerifyResult.Failed("Invalid transfer amount detected on blockchain.")
        }

        if (expectedMinAmount != null && expectedMinAmount > 0.0 && amountUsdt + 0.0001 < expectedMinAmount) {
            return BscVerifyResult.Failed(
                "Received amount ($amountUsdt USDT) is less than expected ($expectedMinAmount USDT)."
            )
        }

        return BscVerifyResult.Verified(
            amountUsdt = amountUsdt,
            fromAddress = detectedFromAddress,
            receiverAddress = DEPOSIT_WALLET_ADDRESS,
            blockNumberHex = receipt.optString("blockNumber", "")
        )
    }

    private fun fetchTransactionReceiptFromBscScan(txHash: String): JSONObject? {
        val url = "https://api.bscscan.com/api?module=proxy&action=eth_getTransactionReceipt&txhash=$txHash"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val result = json.opt("result")
                if (result is JSONObject && result.has("status") && result.has("logs")) {
                    result
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchTransactionReceiptFromBscRpc(txHash: String): JSONObject? {
        val payload = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "eth_getTransactionReceipt")
            put("params", JSONArray().put(txHash))
        }.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()

        for (endpoint in rpcEndpoints) {
            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .post(payload.toRequestBody(mediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    val result = json.opt("result")
                    if (result is JSONObject && result.has("status") && result.has("logs")) {
                        return result
                    }
                }
            } catch (_: Exception) {
                // Try next BSC dataseed endpoint
            }
        }
        return null
    }
}
