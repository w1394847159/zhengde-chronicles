package com.zhengde.chronicles.agent

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LLM API 调用客户端
 *
 * 兼容 OpenAI 格式的 API（DeepSeek / 千问 / GLM-5 / Claude / OpenRouter 等）
 * 用户自配 API Key，纯本地调用，不经我方服务器。
 */
@Singleton
class LlmClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json".toMediaType()

    /** 当前 API 配置 */
    @Volatile
    var config: LlmConfig = LlmConfig()

    /**
     * 调用 LLM 生成文本
     *
     * @param prompt 系统指令 + 用户消息
     * @return LLM 返回的原始文本，失败返回 null
     */
    suspend fun generate(prompt: String): String? = withContext(Dispatchers.IO) {
        if (!config.isValid) return@withContext null

        try {
            val requestBody = buildJsonPayload(prompt)
            val request = Request.Builder()
                .url(config.apiBaseUrl)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "无响应体"
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            extractContent(responseBody)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查 API 配置是否可用（测试连接）
     */
    suspend fun checkConnection(): ConnectionResult = withContext(Dispatchers.IO) {
        if (!config.isValid) {
            return@withContext ConnectionResult(false, "API Key 或 Base URL 未配置")
        }

        try {
            val testBody = gson.toJson(mapOf(
                "model" to config.model,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to "ping")
                ),
                "max_tokens" to 5
            ))

            val request = Request.Builder()
                .url(config.apiBaseUrl)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .post(testBody.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ConnectionResult(true, "连接成功")
            } else {
                ConnectionResult(false, "API 返回 ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            ConnectionResult(false, "连接失败: ${e.message}")
        }
    }

    // ========== 内部 ==========

    private fun buildJsonPayload(prompt: String): String {
        return gson.toJson(mapOf(
            "model" to config.model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "你是一个专业的历史推演引擎。你的回答必须严格遵循用户指定的输出格式。"),
                mapOf("role" to "user", "content" to prompt)
            ),
            "temperature" to config.temperature,
            "max_tokens" to config.maxTokens
        ))
    }

    /**
     * 从 OpenAI 格式的响应中提取 content
     */
    private fun extractContent(responseBody: String): String? {
        return try {
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val choices = json.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                message?.get("content")?.asString
            } else {
                null
            }
        } catch (e: Exception) {
            // 如果不是标准格式，直接返回原文
            responseBody
        }
    }
}

// ========== 配置与结果类型 ==========

data class LlmConfig(
    val apiBaseUrl: String = "https://api.deepseek.com/chat/completions",
    val apiKey: String = "",
    val model: String = "deepseek-chat",
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048
) {
    val isValid: Boolean get() = apiKey.isNotBlank() && apiBaseUrl.isNotBlank()
}

data class ConnectionResult(
    val success: Boolean,
    val message: String
)
