package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.UsedTransactionEntity
import com.example.data.db.UserEntity
import com.example.data.network.BscScanVerifier
import com.example.data.network.BscVerifyResult
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BscScanDepositTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: MarketplaceRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MarketplaceRepository(db.appDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun parseReceipt_verifiesValidBep20UsdtTransferToHardcodedWallet() {
        // 50 USDT = 50 * 10^18 in hex
        val fiftyUsdtWeiHex = "0x" + BigInteger("50000000000000000000").toString(16)
        val receipt = JSONObject().apply {
            put("status", "0x1")
            put("blockNumber", "0x2a1b3c")
            put("from", "0x1111111111111111111111111111111111111111")
            put(
                "logs",
                JSONArray().put(
                    JSONObject().apply {
                        put("address", BscScanVerifier.BSC_USDT_CONTRACT)
                        put(
                            "topics",
                            JSONArray()
                                .put("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
                                .put("0x0000000000000000000000001111111111111111111111111111111111111111")
                                .put("0x000000000000000000000000e59dca55642127ebac2fb92c1f0cffdfdccdd840")
                        )
                        put("data", fiftyUsdtWeiHex)
                    }
                )
            )
        }

        val result = BscScanVerifier.parseReceiptForBep20Usdt(receipt, expectedMinAmount = 50.0)
        assertTrue(result is BscVerifyResult.Verified)
        val verified = result as BscVerifyResult.Verified
        assertEquals(50.0, verified.amountUsdt, 0.0001)
        assertEquals("0xe59dca55642127ebac2fb92c1f0cffdfdccdd840", verified.receiverAddress)
    }

    @Test
    fun parseReceipt_rejectsFailedTxAndWrongReceiver() {
        val failedReceipt = JSONObject().apply {
            put("status", "0x0")
            put("logs", JSONArray())
        }
        val failedResult = BscScanVerifier.parseReceiptForBep20Usdt(failedReceipt)
        assertTrue(failedResult is BscVerifyResult.Failed)

        val wrongReceiverReceipt = JSONObject().apply {
            put("status", "0x1")
            put(
                "logs",
                JSONArray().put(
                    JSONObject().apply {
                        put("address", BscScanVerifier.BSC_USDT_CONTRACT)
                        put(
                            "topics",
                            JSONArray()
                                .put("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
                                .put("0x0000000000000000000000001111111111111111111111111111111111111111")
                                .put("0x0000000000000000000000009999999999999999999999999999999999999999")
                        )
                        put("data", "0x" + BigInteger("25000000000000000000").toString(16))
                    }
                )
            )
        }
        val wrongResult = BscScanVerifier.parseReceiptForBep20Usdt(wrongReceiverReceipt)
        assertTrue(wrongResult is BscVerifyResult.Failed)
    }

    @Test
    fun submitAutoVerifiedDeposit_rejectsDuplicateTxIdBeforeNetworkCall() = runBlocking {
        val user = UserEntity(
            id = "USR-TEST",
            name = "Test User",
            email = "test@vmax.com",
            passwordHash = "hash",
            country = "United States",
            districtState = "NY",
            cityUpazila = "NYC",
            fullAddress = "100 Main St"
        )
        db.appDao().insertUser(user)

        val duplicateTx = "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        db.appDao().insertUsedTransaction(
            UsedTransactionEntity(
                txHash = duplicateTx,
                userId = user.id,
                amount = 50.0
            )
        )

        // Submit with uppercase variant to verify case-insensitive pre-API DB guard
        val result = repository.submitAutoVerifiedDeposit(user.id, duplicateTx.uppercase())
        assertTrue(result.isFailure)
        assertEquals("TxID already used", result.exceptionOrNull()?.message)
    }
}
