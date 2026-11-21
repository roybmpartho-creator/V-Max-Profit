package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true), Index(value = ["referralCode"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    val id: String, // e.g. "USR-782910"
    val name: String,
    val email: String,
    val passwordHash: String,
    val country: String,
    val districtState: String,
    val cityUpazila: String,
    val fullAddress: String,
    val avatarId: Int = 1, // 1 to 9
    val isEmailVerified: Boolean = false,
    val emailOtp: String = "849201",
    val kycStatus: String = "UNVERIFIED", // UNVERIFIED, PENDING, VERIFIED, REJECTED
    val kycSubmittedAt: Long? = null,
    val availableBalance: Double = 0.0,
    val holdingBalance: Double = 0.0,
    val bonusBalance: Double = 0.0, // promotional bonus ledger
    val referralCode: String = "",
    val referredBy: String? = null,
    val rewardPoints: Int = 0,
    val tier100Spins: Int = 0, // Unlocked by $100 - $999.99 deposit (3 spins, max $1.00)
    val tier1000Spins: Int = 0, // Unlocked by $1,000+ deposit (6 spins, max $9.00)
    val totalSpinWinnings: Double = 0.0,
    val role: String = "USER", // USER or ADMIN
    val isBanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deposits",
    indices = [Index(value = ["txHash"], unique = true)] // Prevent duplicate transaction processing!
)
data class DepositEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val amount: Double,
    val bonusAmount: Double,
    val currency: String = "USDT",
    val network: String = "TRON TRC20",
    val txHash: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val verificationType: String = "AUTOMATIC",
    val timestamp: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null,
    val adminNote: String? = null
)

@Entity(
    tableName = "used_transactions",
    indices = [Index(value = ["txHash"], unique = true)]
)
data class UsedTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val txHash: String,
    val userId: String,
    val amount: Double,
    val network: String = "BSC BNB Smart Chain (BEP20)",
    val usedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val amount: Double,
    val feeAmount: Double,
    val netAmount: Double,
    val method: String, // NORMAL (5%), EXPRESS (10%)
    val destinationWallet: String,
    val status: String = "PENDING", // PENDING, PROCESSING, APPROVED, COMPLETED, REJECTED
    val timestamp: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val rejectReason: String? = null
)

@Entity(tableName = "nft_levels")
data class NftLevelEntity(
    @PrimaryKey
    val level: Int, // 1 to 9
    val name: String,
    val description: String,
    val investmentPrice: Double,
    val holdingDays: Int,
    val targetReturnRate: Double, // 0.10 to 0.90
    val contractAddress: String,
    val tokenId: String,
    val rarityRank: String,
    val avatarVariation: Int,
    val isActive: Boolean = true
)

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val level: Int,
    val nftName: String,
    val investmentAmount: Double,
    val proposedRewardRate: Double,
    val targetRewardAmount: Double,
    val dailyProfitAmount: Double = 0.0,
    val lastClaimDate: String = "",
    val claimsCount: Int = 0,
    val totalClaimedReward: Double = 0.0,
    val startTime: Long,
    val durationDays: Int,
    val maturityTime: Long,
    val status: String = "HOLDING_ACTIVE", // HOLDING_ACTIVE, HOLDING_COMPLETED, CLAIMED
    val claimedAt: Long? = null
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val type: String, // DEPOSIT, DEPOSIT_BONUS, SHARE_ACTIVATION, DAILY_PROFIT, HOLDING_RELEASE, WITHDRAWAL, REFERRAL_COMMISSION
    val amount: Double,
    val referenceId: String,
    val description: String,
    val balanceAfter: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val userEmail: String,
    val subject: String,
    val category: String,
    val message: String,
    val priority: String = "NORMAL", // NORMAL, HIGH, URGENT
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val adminReply: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_config")
data class PlatformConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val adminTrc20Wallet: String = "TYDzsYUEpvnYmQk4zGP9sWWcTEd2MiAtW6",
    val adminBscWallet: String = "0xe59dca55642127ebac2fb92c1f0cffdfdccdd840",
    val depositBonusPercent: Double = 0.0,
    val minDepositAmount: Double = 20.0,
    val minWithdrawAmount: Double = 10.0,
    val maxWithdrawAmount: Double = 10000.0,
    val normalWithdrawFeePercent: Double = 10.0,
    val expressWithdrawFeePercent: Double = 0.0,
    val totalPlatformReserves: Double = 1250000.0,
    val announcements: String = "V-Max Profit Share Active | Withdrawal Fast (10% Fee) | 0% Fee Manual Payout Available",
    val withdrawStartHourBD: Int = 8, // 08:00 AM BD Time
    val withdrawEndHourBD: Int = 24, // 12:00 AM midnight BD Time
    val isWithdrawWindowEnforced: Boolean = true,
    val withdrawWindowNotice: String = "Withdrawal Window: 08:00 AM – 12:00 AM (Bangladesh Time) / 09:00 PM – 01:00 PM (US Time)"
)

@Entity(tableName = "spin_history")
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val tierLabel: String, // "100_TIER" or "1000_TIER"
    val prizeLabel: String, // e.g. "$0.80"
    val prizeAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

