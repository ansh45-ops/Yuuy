package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TelegramBotCheckResult(
    val isSuccess: Boolean,
    val botId: Long? = null,
    val botName: String? = null,
    val username: String? = null,
    val webhookUrl: String? = null,
    val pendingUpdateCount: Int = 0,
    val latencyMs: Long = 0,
    val errorMessage: String? = null
)

class TelegramApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun verifyBotToken(token: String): TelegramBotCheckResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val cleanedToken = token.trim()

        if (cleanedToken.isBlank() || !cleanedToken.contains(":")) {
            return@withContext TelegramBotCheckResult(
                isSuccess = false,
                errorMessage = "Invalid token format. Telegram tokens look like 123456789:ABCdef-GHIjkl..."
            )
        }

        try {
            val getMeRequest = Request.Builder()
                .url("https://api.telegram.org/bot$cleanedToken/getMe")
                .get()
                .build()

            client.newCall(getMeRequest).execute().use { response ->
                val elapsed = System.currentTimeMillis() - startTime
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful || body.isBlank()) {
                    return@withContext TelegramBotCheckResult(
                        isSuccess = false,
                        latencyMs = elapsed,
                        errorMessage = "Telegram API error (${response.code}): ${response.message}"
                    )
                }

                val json = JSONObject(body)
                val ok = json.optBoolean("ok", false)
                if (!ok) {
                    val desc = json.optString("description", "Unauthorized or invalid bot token")
                    return@withContext TelegramBotCheckResult(
                        isSuccess = false,
                        latencyMs = elapsed,
                        errorMessage = desc
                    )
                }

                val result = json.getJSONObject("result")
                val botId = result.optLong("id")
                val firstName = result.optString("first_name", "Telegram Bot")
                val username = result.optString("username", "")

                // Try fetching webhook info
                var webhookUrl = ""
                var pendingUpdates = 0
                try {
                    val hookReq = Request.Builder()
                        .url("https://api.telegram.org/bot$cleanedToken/getWebhookInfo")
                        .get()
                        .build()
                    client.newCall(hookReq).execute().use { hookRes ->
                        if (hookRes.isSuccessful) {
                            val hookBody = hookRes.body?.string() ?: ""
                            val hookJson = JSONObject(hookBody).optJSONObject("result")
                            if (hookJson != null) {
                                webhookUrl = hookJson.optString("url", "")
                                pendingUpdates = hookJson.optInt("pending_update_count", 0)
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore non-fatal webhook check
                }

                TelegramBotCheckResult(
                    isSuccess = true,
                    botId = botId,
                    botName = firstName,
                    username = if (username.isNotBlank()) "@$username" else null,
                    webhookUrl = webhookUrl.ifBlank { "No webhook set (using Long Polling)" },
                    pendingUpdateCount = pendingUpdates,
                    latencyMs = elapsed
                )
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            TelegramBotCheckResult(
                isSuccess = false,
                latencyMs = elapsed,
                errorMessage = e.localizedMessage ?: "Failed to connect to Telegram servers"
            )
        }
    }

    suspend fun dispatchWebhookTest(
        url: String,
        payloadJson: String
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = payloadJson.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("User-Agent", "NexusControl-BotOps/1.0")
                .addHeader("X-Nexus-Trigger", "Manual-Admin-Test")
                .build()

            client.newCall(request).execute().use { res ->
                val resBody = res.body?.string() ?: ""
                Pair(res.code, resBody.take(300))
            }
        } catch (e: Exception) {
            Pair(0, e.localizedMessage ?: "Network connection failed")
        }
    }
}
