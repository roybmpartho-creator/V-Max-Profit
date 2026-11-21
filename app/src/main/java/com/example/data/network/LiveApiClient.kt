package com.example.data.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ApiOtpRequestResult(
    val success: Boolean,
    val message: String
)

data class ApiOtpVerifyResult(
    val success: Boolean,
    val message: String,
    val userId: String? = null,
    val referralCode: String? = null
)

object LiveApiClient {

    val baseUrl: String
        get() = BuildConfig.BACKEND_BASE_URL.trimEnd('/')

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(65, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun wakeUpServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$baseUrl/api/health")
                .get()
                .build()
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun requestRegistrationOtp(
        name: String,
        email: String,
        passwordPlain: String,
        country: String,
        referralCode: String?
    ): ApiOtpRequestResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("fullName", name.trim())
                put("email", email.trim().lowercase())
                put("password", passwordPlain)
                put("country", country)
                if (!referralCode.isNullOrBlank()) {
                    put("referralCode", referralCode.trim())
                }
            }
            val (code, body) = executePost("$baseUrl/api/auth/register", payload)
            val json = parseJsonSafely(body)
            val success = json?.optBoolean("success", false) == true && code in 200..299
            val msg = json?.optString("message")?.takeIf { it.isNotBlank() }
                ?: if (success) "OTP sent successfully to $email." else "Unable to send OTP to your Gmail (HTTP $code)."
            ApiOtpRequestResult(success = success, message = msg)
        } catch (e: Exception) {
            ApiOtpRequestResult(
                success = false,
                message = "Cannot reach V-Max Profit server to send Gmail OTP. Please check internet and try again."
            )
        }
    }

    suspend fun resendRegistrationOtp(email: String): ApiOtpRequestResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
            }
            val (code, body) = executePost("$baseUrl/api/auth/resend-otp", payload)
            val json = parseJsonSafely(body)
            val success = json?.optBoolean("success", false) == true && code in 200..299
            val msg = json?.optString("message")?.takeIf { it.isNotBlank() }
                ?: if (success) "OTP resent to $email." else "Failed to resend OTP (HTTP $code)."
            ApiOtpRequestResult(success = success, message = msg)
        } catch (_: Exception) {
            ApiOtpRequestResult(
                success = false,
                message = "Network error while resending OTP to Gmail."
            )
        }
    }

    suspend fun verifyRegistrationOtp(
        email: String,
        otp: String
    ): ApiOtpVerifyResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("otp", otp.trim())
            }
            val (code, body) = executePost("$baseUrl/api/auth/verify-otp", payload)
            val json = parseJsonSafely(body)
            val success = json?.optBoolean("success", false) == true && code in 200..299
            val msg = json?.optString("message")?.takeIf { it.isNotBlank() }
                ?: if (success) "Email verified successfully!" else "OTP verification failed."
            val userObj = json?.optJSONObject("user")
            ApiOtpVerifyResult(
                success = success,
                message = msg,
                userId = userObj?.optString("id")?.takeIf { it.isNotBlank() },
                referralCode = userObj?.optString("referralCode")?.takeIf { it.isNotBlank() }
            )
        } catch (_: Exception) {
            ApiOtpVerifyResult(
                success = false,
                message = "Network error while verifying OTP. Please try again."
            )
        }
    }

    suspend fun syncRegistration(
        name: String,
        email: String,
        passwordPlain: String,
        country: String,
        referralCode: String?
    ): Boolean = requestRegistrationOtp(name, email, passwordPlain, country, referralCode).success

    suspend fun syncDeposit(
        userId: String,
        email: String,
        amount: Double,
        txHash: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("userId", userId)
                put("email", email)
                put("amount", amount)
                put("txHash", txHash)
            }
            executePost("$baseUrl/api/deposit/verify-bep20", payload).first in 200..299
        } catch (_: Exception) {
            false
        }
    }

    suspend fun syncWithdrawal(
        userId: String,
        email: String,
        amount: Double,
        method: String,
        wallet: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("userId", userId)
                put("email", email)
                put("amount", amount)
                put("method", method)
                put("destinationWallet", wallet)
            }
            executePost("$baseUrl/api/wallet/withdraw", payload).first in 200..299
        } catch (_: Exception) {
            false
        }
    }

    private fun executePost(url: String, payload: JSONObject): Pair<Int, String> {
        val req = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { resp ->
            return resp.code to (resp.body?.string() ?: "")
        }
    }

    private fun parseJsonSafely(raw: String): JSONObject? {
        return try {
            if (raw.isBlank()) null else JSONObject(raw)
        } catch (_: Exception) {
            null
        }
    }
}
