package com.example.data.model

data class SpinWheelSegment(
    val index: Int,
    val label: String,
    val amountUsdt: Double,
    val accentColorHex: Long
)

data class SpinOutcome(
    val segmentIndex: Int,
    val prizeLabel: String,
    val prizeAmount: Double,
    val tierUsed: String, // "100_TIER" or "1000_TIER"
    val remainingTier100Spins: Int,
    val remainingTier1000Spins: Int
)

object SpinWheelConfig {
    // 10 enticing lottery segments shown on the spinning wheel ($0.20 & $0.80 removed; $99999 & $999999 added)
    val segments: List<SpinWheelSegment> = listOf(
        SpinWheelSegment(0, "$999999", 999999.0, 0xFFFFB800),
        SpinWheelSegment(1, "$0.50", 0.50, 0xFF00D4FF),
        SpinWheelSegment(2, "$99999", 99999.0, 0xFF9C27B0),
        SpinWheelSegment(3, "$0.90", 0.90, 0xFF10B981),
        SpinWheelSegment(4, "$9999", 9999.0, 0xFFFF6B35),
        SpinWheelSegment(5, "$1", 1.0, 0xFF0EA5E9),
        SpinWheelSegment(6, "$999", 999.0, 0xFFF59E0B),
        SpinWheelSegment(7, "$9", 9.0, 0xFF6366F1),
        SpinWheelSegment(8, "$99", 99.0, 0xFFEC4899),
        SpinWheelSegment(9, "$0.09", 0.09, 0xFF00F0FF)
    )

    // Strict prize caps enforced silently behind the scenes:
    // - $100+ deposit tier (3 spins): randomly selected from $0.09, $0.50, $0.90, $1 (never > $1.00)
    // - $1000+ deposit tier (6 spins): randomly selected from $0.09, $0.50, $0.90, $1, $9 (never > $9.00)
    val tier100AllowedIndices: List<Int> = listOf(1, 3, 5, 9)
    val tier1000AllowedIndices: List<Int> = listOf(1, 3, 5, 7, 9)
}
