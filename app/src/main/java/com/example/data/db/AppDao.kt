package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdDirect(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCountDirect(): Int

    @Query("SELECT COUNT(*) FROM users WHERE createdAt >= :startOfDay")
    suspend fun getNewUsersToday(startOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(referredBy) = LOWER(:referralCode)")
    fun getReferralCount(referralCode: String): Flow<Int>

    @Query("SELECT * FROM users WHERE LOWER(referredBy) = LOWER(:referralCode) ORDER BY createdAt DESC")
    fun getReferrals(referralCode: String): Flow<List<UserEntity>>

    @Query("SELECT DISTINCT d.userId FROM deposits d INNER JOIN users u ON d.userId = u.id WHERE LOWER(u.referredBy) = LOWER(:referralCode) AND d.status = 'APPROVED'")
    fun getQualifiedReferralUserIds(referralCode: String): Flow<List<String>>

    @Query("SELECT * FROM users WHERE LOWER(referralCode) = LOWER(:referralCode) LIMIT 1")
    suspend fun getUserByReferralCodeDirect(referralCode: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET availableBalance = :available, holdingBalance = :holding, bonusBalance = :bonus WHERE id = :userId")
    suspend fun updateUserBalances(userId: String, available: Double, holding: Double, bonus: Double)

    @Query("UPDATE users SET tier100Spins = tier100Spins + :add100, tier1000Spins = tier1000Spins + :add1000 WHERE id = :userId")
    suspend fun addUserSpins(userId: String, add100: Int, add1000: Int)

    @Query(
        "UPDATE users SET " +
            "availableBalance = availableBalance + :prizeAmount, " +
            "totalSpinWinnings = totalSpinWinnings + :prizeAmount, " +
            "tier100Spins = tier100Spins - :use100, " +
            "tier1000Spins = tier1000Spins - :use1000 " +
            "WHERE id = :userId"
    )
    suspend fun applySpinResult(userId: String, prizeAmount: Double, use100: Int, use1000: Int)

    // Lucky Spin History
    @Query("SELECT * FROM spin_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSpinHistoryByUserId(userId: String): Flow<List<SpinHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpinHistory(spin: SpinHistoryEntity): Long

    // Deposits
    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY timestamp DESC")
    fun getDepositsByUserId(userId: String): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits ORDER BY timestamp DESC")
    fun getAllDeposits(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE LOWER(txHash) = LOWER(:txHash) LIMIT 1")
    suspend fun getDepositByTxHash(txHash: String): DepositEntity?

    @Query("SELECT * FROM deposits WHERE id = :id")
    suspend fun getDepositById(id: Long): DepositEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)

    // Unique TxID Prevention Table
    @Query("SELECT * FROM used_transactions WHERE LOWER(txHash) = LOWER(:txHash) LIMIT 1")
    suspend fun getUsedTransactionByHash(txHash: String): UsedTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsedTransaction(entry: UsedTransactionEntity): Long

    // Withdrawals
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWithdrawalsByUserId(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("UPDATE withdrawals SET status = 'PENDING' WHERE status = 'PROCESSED'")
    suspend fun resetProcessedWithdrawalsToPending()

    // NFT Levels
    @Query("SELECT * FROM nft_levels ORDER BY level ASC")
    fun getAllNftLevels(): Flow<List<NftLevelEntity>>

    @Query("SELECT * FROM nft_levels WHERE level = :level LIMIT 1")
    suspend fun getNftLevel(level: Int): NftLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNftLevels(levels: List<NftLevelEntity>)

    @Update
    suspend fun updateNftLevel(level: NftLevelEntity)

    // Holdings
    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY startTime DESC")
    fun getHoldingsByUserId(userId: String): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE userId = :userId AND level = :level AND status = 'HOLDING_ACTIVE' LIMIT 1")
    suspend fun getActiveHoldingForLevel(userId: String, level: Int): HoldingEntity?

    @Query("SELECT * FROM holdings WHERE userId = :userId AND status != 'CLAIMED' ORDER BY startTime DESC")
    fun getActiveAndMaturedHoldings(userId: String): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE userId = :userId AND status = 'CLAIMED' ORDER BY claimedAt DESC")
    fun getClaimedHoldings(userId: String): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE id = :id LIMIT 1")
    suspend fun getHoldingById(id: Long): HoldingEntity?

    @Query("SELECT * FROM holdings ORDER BY startTime DESC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity): Long

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    // Transactions
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    // Support Tickets
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getTicketsByUserId(userId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Long): SupportTicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    // Platform Config
    @Query("SELECT * FROM platform_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<PlatformConfigEntity?>

    @Query("SELECT * FROM platform_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): PlatformConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: PlatformConfigEntity)
}
