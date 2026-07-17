package com.zhengde.chronicles.agent

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token 消耗追踪
 *
 * 记录每次 LLM 调用的 Token 消耗和费用，
 * 供 Token 统计界面展示。
 */
@Singleton
class TokenTracker @Inject constructor() {

    /** 最后一次调用的消耗 */
    @Volatile
    var lastCost: TokenCost? = null
        private set

    /** 会话累计消耗 */
    private val _totalCost = SessionCost()
    val totalCost: SessionCost get() = _totalCost

    // 模型价格（每百万 Token 的元数，可配置）
    @Volatile
    var inputPricePerM: Double = 0.5   // DeepSeek V3 参考价
    @Volatile
    var outputPricePerM: Double = 2.0

    /**
     * 记录一次 LLM 调用
     */
    fun track(prompt: String, response: String) {
        // 估算 Token 数（粗略：中文约 1.5 字/token，英文约 4 字符/token）
        val inputTokens = estimateTokens(prompt)
        val outputTokens = estimateTokens(response)

        val inputCost = inputTokens * inputPricePerM / 1_000_000
        val outputCost = outputTokens * outputPricePerM / 1_000_000

        lastCost = TokenCost(
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            totalTokens = inputTokens + outputTokens,
            inputCost = inputCost,
            outputCost = outputCost,
            totalCost = inputCost + outputCost
        )

        _totalCost.totalInputTokens += inputTokens
        _totalCost.totalOutputTokens += outputTokens
        _totalCost.totalCost += inputCost + outputCost
        _totalCost.callCount++
    }

    /**
     * 预估 Token 数（粗略估计）
     */
    fun estimateTokens(text: String): Int {
        // 中文占多数时粗略估算：1 token ≈ 1.5 个汉字
        // 英文：1 token ≈ 4 个字符
        val chineseChars = text.count { it in '\u4e00'..'\u9fff' }
        val otherChars = text.length - chineseChars
        return (chineseChars / 1.5 + otherChars / 4).toInt().coerceAtLeast(1)
    }

    /**
     * 重置（开新档时调用）
     */
    fun reset() {
        lastCost = null
        _totalCost.reset()
    }
}

data class TokenCost(
    val inputTokens: Int,
    val outputTokens: Int,
    val totalTokens: Int,
    val inputCost: Double,
    val outputCost: Double,
    val totalCost: Double          // 元
)

class SessionCost {
    var totalInputTokens: Int = 0
    var totalOutputTokens: Int = 0
    var totalCost: Double = 0.0
    var callCount: Int = 0

    fun reset() {
        totalInputTokens = 0
        totalOutputTokens = 0
        totalCost = 0.0
        callCount = 0
    }
}
