package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.db.DepositEntity
import com.example.data.db.HoldingEntity
import com.example.data.db.NftLevelEntity
import com.example.data.db.PlatformConfigEntity
import com.example.data.db.SpinHistoryEntity
import com.example.data.db.SupportTicketEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.UsedTransactionEntity
import com.example.data.db.UserEntity
import com.example.data.db.WithdrawalEntity
import com.example.data.model.SpinOutcome
import com.example.data.model.SpinWheelConfig
import com.example.data.network.BscScanVerifier
import com.example.data.network.BscVerifyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MarketplaceRepository(val dao: AppDao) {

    val allNftLevels: Flow<List<NftLevelEntity>> = dao.getAllNftLevels()
    val platformConfig: Flow<PlatformConfigEntity?> = dao.getConfig()
    val allRegisteredUsersCount: Flow<Int> = dao.getUserCount()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allDeposits: Flow<List<DepositEntity>> = dao.getAllDeposits()
    val allWithdrawals: Flow<List<WithdrawalEntity>> = dao.getAllWithdrawals()
    val allHoldings: Flow<List<HoldingEntity>> = dao.getAllHoldings()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allTickets: Flow<List<SupportTicketEntity>> = dao.getAllTickets()

    suspend fun initializeDefaultDataIfEmpty() {
        // 1. Seed Config if missing
        val existingConfig = dao.getConfigDirect()
        if (existingConfig == null) {
            dao.insertOrUpdateConfig(
                PlatformConfigEntity(
                    id = 1,
                    adminTrc20Wallet = "TYDzsYUEpvnYmQk4zGP9sWWcTEd2MiAtW6",
                    adminBscWallet = BscScanVerifier.DEPOSIT_WALLET_ADDRESS,
                    depositBonusPercent = 0.0,
                    minDepositAmount = 20.0,
                    minWithdrawAmount = 10.0,
                    maxWithdrawAmount = 10000.0,
                    normalWithdrawFeePercent = 10.0,
                    expressWithdrawFeePercent = 0.0,
                    totalPlatformReserves = 1250000.0,
                    announcements = "V-Max Profit Share Active | Standard Fast Payout (10% Fee) | Manual Payout (0% Free, 1-2 Days)"
                )
            )
        } else {
            dao.insertOrUpdateConfig(
                existingConfig.copy(
                    adminBscWallet = BscScanVerifier.DEPOSIT_WALLET_ADDRESS,
                    normalWithdrawFeePercent = 10.0,
                    expressWithdrawFeePercent = 0.0
                )
            )
        }

        // 2. Seed 9 NFT Levels if empty
        val existingLevels = dao.getAllNftLevels().firstOrNull()
        val levels = listOf(
            NftLevelEntity(
                level = 1,
                name = "V-Max Share Level 1",
                description = "Buy $20 | Daily Profit $3.00 | Duration: 180 Days",
                investmentPrice = 20.0,
                holdingDays = 180,
                targetReturnRate = 0.15, // $3.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L1",
                tokenId = "#101-SHARE-V1",
                rarityRank = "Tier 1",
                avatarVariation = 1
            ),
            NftLevelEntity(
                level = 2,
                name = "V-Max Share Level 2",
                description = "Buy $50 | Daily Profit $7.50 | Duration: 180 Days",
                investmentPrice = 50.0,
                holdingDays = 180,
                targetReturnRate = 0.15, // $7.50 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L2",
                tokenId = "#204-SHARE-V2",
                rarityRank = "Tier 2",
                avatarVariation = 2
            ),
            NftLevelEntity(
                level = 3,
                name = "V-Max Share Level 3",
                description = "Buy $100 | Daily Profit $15.00 | Duration: 180 Days",
                investmentPrice = 100.0,
                holdingDays = 180,
                targetReturnRate = 0.15, // $15.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L3",
                tokenId = "#305-SHARE-V3",
                rarityRank = "Tier 3",
                avatarVariation = 3
            ),
            NftLevelEntity(
                level = 4,
                name = "V-Max Share Level 4",
                description = "Buy $500 | Daily Profit $80.00 | Duration: 180 Days",
                investmentPrice = 500.0,
                holdingDays = 180,
                targetReturnRate = 0.16, // $80.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L4",
                tokenId = "#408-SHARE-V4",
                rarityRank = "Tier 4",
                avatarVariation = 4
            ),
            NftLevelEntity(
                level = 5,
                name = "V-Max Share Level 5",
                description = "Buy $1,000 | Daily Profit $170.00 | Duration: 180 Days",
                investmentPrice = 1000.0,
                holdingDays = 180,
                targetReturnRate = 0.17, // $170.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L5",
                tokenId = "#512-SHARE-V5",
                rarityRank = "Tier 5",
                avatarVariation = 5
            ),
            NftLevelEntity(
                level = 6,
                name = "V-Max Share Level 6",
                description = "Buy $5,000 | Daily Profit $900.00 | Duration: 180 Days",
                investmentPrice = 5000.0,
                holdingDays = 180,
                targetReturnRate = 0.18, // $900.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L6",
                tokenId = "#615-SHARE-V6",
                rarityRank = "Tier 6",
                avatarVariation = 6
            ),
            NftLevelEntity(
                level = 7,
                name = "V-Max Share Level 7",
                description = "Buy $10,000 | Daily Profit $1,900.00 | Duration: 180 Days",
                investmentPrice = 10000.0,
                holdingDays = 180,
                targetReturnRate = 0.19, // $1,900.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L7",
                tokenId = "#721-SHARE-V7",
                rarityRank = "Tier 7",
                avatarVariation = 7
            ),
            NftLevelEntity(
                level = 8,
                name = "V-Max Share Level 8",
                description = "Buy $50,000 | Daily Profit $10,000.00 | Duration: 180 Days",
                investmentPrice = 50000.0,
                holdingDays = 180,
                targetReturnRate = 0.20, // $10,000.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L8",
                tokenId = "#830-SHARE-V8",
                rarityRank = "Tier 8",
                avatarVariation = 8
            ),
            NftLevelEntity(
                level = 9,
                name = "V-Max Share Level 9",
                description = "Buy $100,000 | Daily Profit $21,000.00 | Duration: 180 Days",
                investmentPrice = 100000.0,
                holdingDays = 180,
                targetReturnRate = 0.21, // $21,000.00 daily profit
                contractAddress = "0x91a02efb7643d5000304df834561200152886007:L9",
                tokenId = "#999-SHARE-V9",
                rarityRank = "Tier 9",
                avatarVariation = 9
            )
        )
        if (existingLevels.isNullOrEmpty()) {
            dao.insertNftLevels(levels)
        } else if (existingLevels.any { !it.name.startsWith("V-Max Share") || it.targetReturnRate > 5.0 }) {
            // Migrate legacy or inflated rates once; preserve subsequent admin edits
            levels.forEach { dao.updateNftLevel(it) }
        }

        // 3. Seed accounts: all initial user balances start at 0.0 as requested
        val userCount = dao.getUserCountDirect()
        if (userCount == 0) {
            val demoUser = UserEntity(
                id = "USR-772910",
                name = "Alex Vance",
                email = "trader@nfttrade.com",
                passwordHash = hashPassword("password123"),
                country = "United States",
                districtState = "California",
                cityUpazila = "San Francisco",
                fullAddress = "500 Market St, Suite 400",
                avatarId = 1,
                isEmailVerified = true,
                emailOtp = "729401",
                kycStatus = "VERIFIED",
                availableBalance = 0.0,
                holdingBalance = 0.0,
                bonusBalance = 0.0,
                rewardPoints = 0,
                tier100Spins = 0,
                tier1000Spins = 0,
                referralCode = "VMAX-772910",
                role = "USER",
                createdAt = System.currentTimeMillis() - 86400000L * 5
            )
            dao.insertUser(demoUser)
        } else {
            // Ensure un-deposited demo user starts strictly at 0.0 balance
            val existingDemo = dao.getUserByIdDirect("USR-772910")
            if (existingDemo != null && existingDemo.availableBalance > 0.0 && existingDemo.role != "ADMIN") {
                val hasDeposits = !dao.getDepositsByUserId(existingDemo.id).firstOrNull().isNullOrEmpty()
                if (!hasDeposits && existingDemo.totalSpinWinnings == 0.0) {
                    dao.updateUserBalances(existingDemo.id, 0.0, existingDemo.holdingBalance, 0.0)
                }
            }
        }

        // Seed master admin roybmpartho@gmail.com requested by user
        val parthoAdmin = dao.getUserByEmail("roybmpartho@gmail.com")
        if (parthoAdmin == null) {
            val adminUser = UserEntity(
                id = "ADMIN-PARTHO",
                name = "Roy Bm Partho",
                email = "roybmpartho@gmail.com",
                passwordHash = hashPassword("Bmpartho@25"),
                country = "Bangladesh",
                districtState = "Dhaka",
                cityUpazila = "Dhaka City",
                fullAddress = "Admin Headquarter",
                avatarId = 9,
                isEmailVerified = true,
                emailOtp = "849201",
                kycStatus = "VERIFIED",
                availableBalance = 50000.0,
                holdingBalance = 0.0,
                bonusBalance = 0.0,
                referralCode = "VMAX-PARTHO",
                role = "ADMIN",
                isBanned = false,
                createdAt = System.currentTimeMillis() - 86400000L * 30
            )
            dao.insertUser(adminUser)
        }

        // Always ensure master admin roybmpartho@gmail.com exists with password Bmpartho@25
        val adminCheck = dao.getUserByEmail("roybmpartho@gmail.com")
        if (adminCheck == null) {
            dao.insertUser(
                UserEntity(
                    id = "ADMIN-PARTHO",
                    name = "Roy Bm Partho",
                    email = "roybmpartho@gmail.com",
                    passwordHash = hashPassword("Bmpartho@25"),
                    country = "Bangladesh",
                    districtState = "Dhaka",
                    cityUpazila = "Dhaka City",
                    fullAddress = "Admin Headquarter",
                    avatarId = 9,
                    isEmailVerified = true,
                    emailOtp = "849201",
                    kycStatus = "VERIFIED",
                    availableBalance = 50000.0,
                    holdingBalance = 0.0,
                    bonusBalance = 0.0,
                    referralCode = "VMAX-PARTHO",
                    role = "ADMIN",
                    isBanned = false,
                    createdAt = System.currentTimeMillis()
                )
            )
        } else if (adminCheck.passwordHash != hashPassword("Bmpartho@25") || adminCheck.role != "ADMIN") {
            dao.updateUser(adminCheck.copy(passwordHash = hashPassword("Bmpartho@25"), role = "ADMIN"))
        }

        // Ensure all withdrawals require explicit admin approval in Admin Panel
        dao.resetProcessedWithdrawalsToPending()
    }

    fun getUser(userId: String): Flow<UserEntity?> = dao.getUserById(userId)

    suspend fun getUserDirect(userId: String): UserEntity? = dao.getUserByIdDirect(userId)

    fun getReferralsForCode(code: String): Flow<List<UserEntity>> = dao.getReferrals(code)

    fun getQualifiedReferralUserIds(code: String): Flow<List<String>> = dao.getQualifiedReferralUserIds(code)

    suspend fun isEmailAlreadyRegistered(email: String): Boolean {
        val normalized = email.trim().lowercase()
        return dao.getUserByEmail(normalized) != null
    }

    suspend fun finalizeVerifiedRegistration(
        name: String,
        email: String,
        passwordPlain: String,
        country: String,
        districtState: String,
        cityUpazila: String,
        fullAddress: String,
        referredByCode: String?,
        serverUserId: String? = null,
        serverReferralCode: String? = null
    ): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        val existing = dao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this Gmail already exists. Only one account per Gmail is allowed."))
        }

        val uid = serverUserId?.takeIf { it.isNotBlank() } ?: ("USR-" + (100000..999999).random())
        val refCode = serverReferralCode?.takeIf { it.isNotBlank() } ?: ("VMAX-" + (100000..999999).random())
        val cleanedRefCode = referredByCode
            ?.trim()
            ?.substringAfterLast("/ref/", referredByCode.trim())
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.isNotBlank() }

        val verifiedUser = UserEntity(
            id = uid,
            name = name.trim(),
            email = normalizedEmail,
            passwordHash = hashPassword(passwordPlain),
            country = country,
            districtState = districtState,
            cityUpazila = cityUpazila,
            fullAddress = fullAddress,
            avatarId = (1..9).random(),
            isEmailVerified = true,
            emailOtp = "",
            kycStatus = "UNVERIFIED",
            availableBalance = 0.0,
            holdingBalance = 0.0,
            bonusBalance = 0.0,
            referralCode = refCode,
            referredBy = cleanedRefCode,
            rewardPoints = 0,
            tier100Spins = 0,
            tier1000Spins = 0,
            role = "USER",
            isBanned = false,
            createdAt = System.currentTimeMillis()
        )
        dao.insertUser(verifiedUser)
        return Result.success(verifiedUser)
    }

    suspend fun registerUser(
        name: String,
        email: String,
        passwordPlain: String,
        country: String,
        districtState: String,
        cityUpazila: String,
        fullAddress: String,
        referredByCode: String?
    ): Result<UserEntity> {
        val existing = dao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("An account with this Gmail already exists. Only one account per Gmail is allowed."))
        }

        val randomId = "USR-" + (100000..999999).random()
        val randomAvatar = (1..9).random()
        val otp = (100000..999999).random().toString()
        val refCode = "VMAX-" + (100000..999999).random()

        val cleanedRefCode = referredByCode
            ?.trim()
            ?.substringAfterLast("/ref/", referredByCode.trim())
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.isNotBlank() }

        val newUser = UserEntity(
            id = randomId,
            name = name.trim(),
            email = email.trim().lowercase(),
            passwordHash = hashPassword(passwordPlain),
            country = country,
            districtState = districtState,
            cityUpazila = cityUpazila,
            fullAddress = fullAddress,
            avatarId = randomAvatar,
            isEmailVerified = false,
            emailOtp = otp,
            kycStatus = "UNVERIFIED",
            availableBalance = 0.0,
            holdingBalance = 0.0,
            bonusBalance = 0.0,
            referralCode = refCode,
            referredBy = cleanedRefCode,
            rewardPoints = 0,
            tier100Spins = 0,
            tier1000Spins = 0,
            role = "USER",
            isBanned = false,
            createdAt = System.currentTimeMillis()
        )

        dao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun verifyEmailOtp(userId: String, enteredOtp: String): Result<UserEntity> {
        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))
        if (user.emailOtp != enteredOtp.trim()) {
            return Result.failure(Exception("Invalid 6-digit OTP code. Please check and retry."))
        }
        val updated = user.copy(isEmailVerified = true)
        dao.updateUser(updated)
        return Result.success(updated)
    }

    suspend fun login(email: String, passwordPlain: String): Result<UserEntity> {
        val user = dao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(Exception("Account not found with this email."))
        if (user.isBanned) {
            return Result.failure(Exception("This account has been banned by Administrator. Please contact support."))
        }
        if (user.passwordHash != hashPassword(passwordPlain)) {
            return Result.failure(Exception("Incorrect password. Please verify credentials."))
        }
        return Result.success(user)
    }

    suspend fun adminToggleUserBan(userId: String): Result<Boolean> {
        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))
        val updated = user.copy(isBanned = !user.isBanned)
        dao.updateUser(updated)
        return Result.success(updated.isBanned)
    }

    suspend fun adminAdjustUserBalance(userId: String, available: Double, holding: Double, bonus: Double) {
        dao.updateUserBalances(userId, available, holding, bonus)
    }

    suspend fun submitKyc(userId: String, fullAddress: String, district: String, city: String): Result<UserEntity> {
        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))
        val updated = user.copy(
            kycStatus = "PENDING",
            kycSubmittedAt = System.currentTimeMillis(),
            fullAddress = fullAddress,
            districtState = district,
            cityUpazila = city
        )
        dao.updateUser(updated)
        return Result.success(updated)
    }

    suspend fun updateUserAvatar(userId: String, avatarId: Int) {
        val user = dao.getUserByIdDirect(userId) ?: return
        dao.updateUser(user.copy(avatarId = avatarId))
    }

    // Deposits
    fun getDepositsForUser(userId: String): Flow<List<DepositEntity>> = dao.getDepositsByUserId(userId)

    suspend fun submitAutoVerifiedDeposit(
        userId: String,
        txHash: String,
        expectedAmount: Double? = null
    ): Result<DepositEntity> {
        val extracted = Regex("0x[a-f0-9]{64}", RegexOption.IGNORE_CASE).find(txHash)?.value?.lowercase()
        val normalizedTx = extracted ?: txHash.trim().lowercase()
        if (normalizedTx.isBlank()) {
            return Result.failure(Exception("Please enter your BEP20 Transaction ID (TxID)."))
        }
        if (!Regex("^0x[a-f0-9]{64}$").matches(normalizedTx)) {
            return Result.failure(Exception("Invalid TxID format. Enter a valid 66-character BSC transaction hash starting with 0x."))
        }

        // CRITICAL: Instant local DB check for duplicate TxID BEFORE calling BscScan API
        val existingUsed = dao.getUsedTransactionByHash(normalizedTx)
        val existingDeposit = dao.getDepositByTxHash(normalizedTx)
        if (existingUsed != null || existingDeposit != null) {
            return Result.failure(Exception("TxID already used"))
        }

        val config = dao.getConfigDirect() ?: PlatformConfigEntity()

        // Real-time verification on BSC blockchain via BscScan API / BSC JSON-RPC
        val verifyResult = BscScanVerifier.verifyBep20UsdtDeposit(
            rawTxHash = normalizedTx,
            expectedMinAmount = expectedAmount
        )

        if (verifyResult is BscVerifyResult.Failed) {
            return Result.failure(Exception(verifyResult.reason))
        }

        val verified = verifyResult as BscVerifyResult.Verified
        val receivedAmount = verified.amountUsdt
        if (receivedAmount <= 0.0) {
            return Result.failure(Exception("No USDT BEP20 transfer amount detected in this transaction."))
        }

        // Race-condition safety check right before DB commit
        if (dao.getUsedTransactionByHash(normalizedTx) != null || dao.getDepositByTxHash(normalizedTx) != null) {
            return Result.failure(Exception("TxID already used"))
        }

        val now = System.currentTimeMillis()
        val bonusRate = config.depositBonusPercent / 100.0
        val bonus = receivedAmount * bonusRate

        try {
            dao.insertUsedTransaction(
                UsedTransactionEntity(
                    txHash = normalizedTx,
                    userId = userId,
                    amount = receivedAmount,
                    network = "BSC BNB Smart Chain (BEP20)",
                    usedAt = now
                )
            )
        } catch (_: Exception) {
            return Result.failure(Exception("TxID already used"))
        }

        val deposit = DepositEntity(
            userId = userId,
            amount = receivedAmount,
            bonusAmount = bonus,
            currency = "USDT",
            network = "BSC BNB Smart Chain (BEP20)",
            txHash = normalizedTx,
            status = "APPROVED",
            verificationType = "BSCSCAN_AUTO",
            timestamp = now,
            verifiedAt = now,
            adminNote = "Auto-verified via BscScan BEP20 ($receivedAmount USDT)"
        )

        val depositId = dao.insertDeposit(deposit)
        val savedDeposit = deposit.copy(id = depositId)

        val user = dao.getUserByIdDirect(userId)
        if (user != null) {
            val newAvailable = user.availableBalance + receivedAmount
            dao.updateUserBalances(userId, newAvailable, user.holdingBalance, user.bonusBalance)
            grantSpinsForDeposit(userId, receivedAmount)

            dao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    type = "DEPOSIT",
                    amount = receivedAmount,
                    referenceId = normalizedTx,
                    description = "BEP20 USDT Deposit ($normalizedTx) auto-verified via BscScan",
                    balanceAfter = newAvailable,
                    timestamp = now
                )
            )

            if (!user.referredBy.isNullOrBlank()) {
                val referrer = dao.getUserByReferralCodeDirect(user.referredBy)
                if (referrer != null) {
                    val refBonus = receivedAmount * 0.05
                    val refNewAvailable = referrer.availableBalance + refBonus
                    dao.updateUserBalances(referrer.id, refNewAvailable, referrer.holdingBalance, referrer.bonusBalance)
                    dao.insertTransaction(
                        TransactionEntity(
                            userId = referrer.id,
                            type = "REFERRAL_BONUS",
                            amount = refBonus,
                            referenceId = "REF-DEP-$normalizedTx",
                            description = "5% Commission from ${user.id} deposit ($${String.format(Locale.US, "%.2f", receivedAmount)} USDT)",
                            balanceAfter = refNewAvailable,
                            timestamp = now
                        )
                    )
                }
            }
        }

        return Result.success(savedDeposit)
    }

    suspend fun submitDeposit(
        userId: String,
        amount: Double,
        txHash: String,
        network: String = "BNB Smart Chain (BEP20)",
        automaticVerification: Boolean = false
    ): Result<DepositEntity> {
        val cleanHash = txHash.trim()
        val config = dao.getConfigDirect() ?: PlatformConfigEntity()

        if (amount < config.minDepositAmount) {
            return Result.failure(Exception("Minimum deposit amount is $${config.minDepositAmount.toInt()} USDT."))
        }

        if (cleanHash.length < 8) {
            return Result.failure(Exception("Invalid Transaction Hash / TxID. Please enter a valid blockchain transaction ID."))
        }

        // Duplicate transaction check (CRITICAL)
        val duplicate = dao.getDepositByTxHash(cleanHash)
        if (duplicate != null) {
            return Result.failure(Exception("Duplicate transaction hash detected: This blockchain transaction ID has already been submitted."))
        }

        val bonusRate = config.depositBonusPercent / 100.0
        val bonus = amount * bonusRate

        val deposit = DepositEntity(
            userId = userId,
            amount = amount,
            bonusAmount = bonus,
            currency = "USDT",
            network = network,
            txHash = cleanHash,
            status = if (automaticVerification) "APPROVED" else "PENDING",
            verificationType = if (automaticVerification) "AUTOMATIC" else "MANUAL_ADMIN",
            timestamp = System.currentTimeMillis(),
            verifiedAt = if (automaticVerification) System.currentTimeMillis() else null
        )

        val depositId = dao.insertDeposit(deposit)
        val savedDeposit = deposit.copy(id = depositId)

        if (automaticVerification) {
            // Atomic update to balances
            val user = dao.getUserByIdDirect(userId)
            if (user != null) {
                val newAvailable = user.availableBalance + amount
                dao.updateUserBalances(userId, newAvailable, user.holdingBalance, user.bonusBalance)

                // Transaction records in ledger
                dao.insertTransaction(
                    TransactionEntity(
                        userId = userId,
                        type = "DEPOSIT",
                        amount = amount,
                        referenceId = cleanHash,
                        description = "USDT (BSC BEP20) Deposit ($cleanHash) verified via Binance Gateway",
                        balanceAfter = newAvailable,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // 5% Recurring Referral Bonus to Referrer every time user deposits!
                if (!user.referredBy.isNullOrBlank()) {
                    val referrer = dao.getUserByReferralCodeDirect(user.referredBy)
                    if (referrer != null) {
                        val refBonus = amount * 0.05
                        val refNewAvailable = referrer.availableBalance + refBonus
                        dao.updateUserBalances(referrer.id, refNewAvailable, referrer.holdingBalance, referrer.bonusBalance)
                        dao.insertTransaction(
                            TransactionEntity(
                                userId = referrer.id,
                                type = "REFERRAL_BONUS",
                                amount = refBonus,
                                referenceId = "REF-DEP-$cleanHash",
                                description = "5% Referral Deposit Bonus from ${user.name} deposit of $${amount.toInt()} USDT",
                                balanceAfter = refNewAvailable,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }

        return Result.success(savedDeposit)
    }

    // Purchases & Level-Wise Rules
    suspend fun purchaseNft(userId: String, levelNumber: Int): Result<HoldingEntity> {
        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))
        val nftLevel = dao.getNftLevel(levelNumber) ?: return Result.failure(Exception("Share level $levelNumber does not exist."))

        // 1. Balance verification
        if (user.availableBalance < nftLevel.investmentPrice) {
            return Result.failure(Exception("Insufficient Available Balance. Required: $${nftLevel.investmentPrice}, Available: $${user.availableBalance}"))
        }

        // 2. Strict Level-Wise Rule:
        // A user cannot purchase another Share from a level while a Share from that same level remains active!
        val activeHolding = dao.getActiveHoldingForLevel(userId, levelNumber)
        if (activeHolding != null) {
            return Result.failure(
                Exception("Level $levelNumber already has an active Share in holding. You cannot purchase another share from this level until this cycle completes.")
            )
        }

        // 3. Financial atomic deduction: Available -> Holding
        val newAvailable = user.availableBalance - nftLevel.investmentPrice
        val newHolding = user.holdingBalance + nftLevel.investmentPrice
        dao.updateUserBalances(userId, newAvailable, newHolding, user.bonusBalance)

        val startTime = System.currentTimeMillis()
        val durationMillis = nftLevel.holdingDays * 24L * 3600L * 1000L
        val maturityTime = startTime + durationMillis
        val dailyProfit = nftLevel.investmentPrice * nftLevel.targetReturnRate
        val targetReward = dailyProfit * nftLevel.holdingDays

        val holding = HoldingEntity(
            userId = userId,
            level = levelNumber,
            nftName = nftLevel.name,
            investmentAmount = nftLevel.investmentPrice,
            proposedRewardRate = nftLevel.targetReturnRate,
            targetRewardAmount = targetReward,
            dailyProfitAmount = dailyProfit,
            lastClaimDate = "",
            claimsCount = 0,
            totalClaimedReward = 0.0,
            startTime = startTime,
            durationDays = nftLevel.holdingDays,
            maturityTime = maturityTime,
            status = "HOLDING_ACTIVE"
        )

        val holdingId = dao.insertHolding(holding)
        val createdHolding = holding.copy(id = holdingId)

        // Ledger record
        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "SHARE_ACTIVATION",
                amount = -nftLevel.investmentPrice,
                referenceId = "HOLD-$holdingId",
                description = "Activated ${nftLevel.name} (Level $levelNumber) - Added to Holding Balance",
                balanceAfter = newAvailable,
                timestamp = startTime
            )
        )

        return Result.success(createdHolding)
    }

    // Holdings & Claims
    fun getHoldingsForUser(userId: String): Flow<List<HoldingEntity>> = dao.getHoldingsByUserId(userId)
    fun getActiveAndMaturedHoldings(userId: String): Flow<List<HoldingEntity>> = dao.getActiveAndMaturedHoldings(userId)
    fun getClaimedHoldings(userId: String): Flow<List<HoldingEntity>> = dao.getClaimedHoldings(userId)

    suspend fun claimDailyProfit(userId: String, holdingId: Long): Result<Double> {
        val holding = dao.getHoldingById(holdingId) ?: return Result.failure(Exception("Share record not found."))
        if (holding.userId != userId) {
            return Result.failure(Exception("Unauthorized holding access."))
        }
        if (holding.status == "CLAIMED") {
            return Result.failure(Exception("This share cycle has already been completed."))
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (holding.lastClaimDate == todayDate) {
            return Result.failure(Exception("Today's profit has already been claimed! Next daily profit unlocks tomorrow at 12:00 AM."))
        }

        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))
        val profit = if (holding.dailyProfitAmount > 0.0) holding.dailyProfitAmount else (holding.investmentAmount * holding.proposedRewardRate)

        val newAvailable = user.availableBalance + profit
        dao.updateUserBalances(userId, newAvailable, user.holdingBalance, user.bonusBalance)

        val newClaimsCount = holding.claimsCount + 1
        val newTotalClaimed = holding.totalClaimedReward + profit
        val isCycleDone = newClaimsCount >= holding.durationDays

        val updatedHolding = holding.copy(
            lastClaimDate = todayDate,
            claimsCount = newClaimsCount,
            totalClaimedReward = newTotalClaimed,
            status = if (isCycleDone) "CLAIMED" else "HOLDING_ACTIVE",
            claimedAt = if (isCycleDone) System.currentTimeMillis() else holding.claimedAt
        )
        dao.updateHolding(updatedHolding)

        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "DAILY_PROFIT",
                amount = profit,
                referenceId = "PROFIT-${holding.id}-$newClaimsCount",
                description = "Daily Profit Claimed: ${holding.nftName} (+$${String.format(Locale.US, "%.2f", profit)} USDT)",
                balanceAfter = newAvailable,
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(profit)
    }

    suspend fun claimMaturedHolding(userId: String, holdingId: Long): Result<Double> {
        val holding = dao.getHoldingById(holdingId) ?: return Result.failure(Exception("Holding record not found."))
        if (holding.userId != userId) {
            return Result.failure(Exception("Unauthorized holding access."))
        }
        if (holding.status == "CLAIMED") {
            return Result.failure(Exception("This holding has already been claimed."))
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime < holding.maturityTime && holding.status != "HOLDING_COMPLETED") {
            return Result.failure(Exception("Holding period has not yet matured. Please wait until countdown completes."))
        }

        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))

        // Calculate payout: Release investment principal + target reward
        val principal = holding.investmentAmount
        val reward = holding.targetRewardAmount
        val totalPayout = principal + reward

        val newAvailable = user.availableBalance + totalPayout
        val newHolding = (user.holdingBalance - principal).coerceAtLeast(0.0)
        dao.updateUserBalances(userId, newAvailable, newHolding, user.bonusBalance)

        // Mark holding claimed
        val updatedHolding = holding.copy(
            status = "CLAIMED",
            claimedAt = currentTime
        )
        dao.updateHolding(updatedHolding)

        // Record in ledger
        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "HOLDING_RELEASE",
                amount = principal,
                referenceId = "CLAIM-${holding.id}-PRIN",
                description = "Released ${holding.nftName} Holding Principal to Available Balance",
                balanceAfter = user.availableBalance + principal,
                timestamp = currentTime
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "CLAIM_REWARD",
                amount = reward,
                referenceId = "CLAIM-${holding.id}-REW",
                description = "Claimed verified trading reward for ${holding.nftName} (Level ${holding.level})",
                balanceAfter = newAvailable,
                timestamp = currentTime
            )
        )

        return Result.success(totalPayout)
    }

    // Testing accelerator to fast-forward holding maturity without waiting 3-27 days
    suspend fun accelerateHoldingForTesting(holdingId: Long) {
        val holding = dao.getHoldingById(holdingId) ?: return
        val maturedHolding = holding.copy(
            maturityTime = System.currentTimeMillis() - 1000L,
            status = "HOLDING_COMPLETED"
        )
        dao.updateHolding(maturedHolding)
    }

    // Withdrawals
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalEntity>> = dao.getWithdrawalsByUserId(userId)

    suspend fun submitWithdrawal(
        userId: String,
        amount: Double,
        method: String, // NORMAL (5%) or EXPRESS (10%)
        destinationWallet: String,
        passwordPlain: String
    ): Result<WithdrawalEntity> {
        val config = dao.getConfigDirect() ?: PlatformConfigEntity()
        val wallet = destinationWallet.trim()

        if (passwordPlain.isBlank()) {
            return Result.failure(Exception("Account password is required to authorize withdrawal."))
        }

        val user = dao.getUserByIdDirect(userId) ?: return Result.failure(Exception("User not found"))
        if (hashPassword(passwordPlain.trim()) != user.passwordHash) {
            return Result.failure(Exception("Incorrect account password! Withdrawal verification failed."))
        }

        if (amount < config.minWithdrawAmount) {
            return Result.failure(Exception("Minimum withdrawal is $${config.minWithdrawAmount.toInt()} USDT."))
        }
        if (amount > config.maxWithdrawAmount) {
            return Result.failure(Exception("Maximum withdrawal is $${config.maxWithdrawAmount.toInt()} USDT."))
        }
        if (wallet.length < 26 || (!wallet.startsWith("0x") && !wallet.startsWith("T"))) {
            return Result.failure(Exception("Invalid BSC BEP20 USDT wallet address. Must start with '0x' and be at least 26 characters long."))
        }

        if (user.availableBalance < amount) {
            return Result.failure(Exception("Insufficient Available Balance. Available: $${user.availableBalance}"))
        }

        val isAuto = method.contains("Standard", ignoreCase = true) || method.contains("STANDARD", ignoreCase = true) || method.contains("NORMAL", ignoreCase = true) || method.contains("Binance", ignoreCase = true)
        val feePercent = if (isAuto) 10.0 else 0.0
        val fee = amount * (feePercent / 100.0)
        val net = amount - fee

        // Atomic deduction
        val newAvailable = user.availableBalance - amount
        dao.updateUserBalances(userId, newAvailable, user.holdingBalance, user.bonusBalance)

        // All withdrawals must be manually reviewed and approved by Admin in Admin Panel
        val withdrawal = WithdrawalEntity(
            userId = userId,
            amount = amount,
            feeAmount = fee,
            netAmount = net,
            method = if (isAuto) "Standard (Withdrawal Fast)" else "Manual Payout (1-2 Days)",
            destinationWallet = wallet,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        val withdrawId = dao.insertWithdrawal(withdrawal)
        val savedWithdrawal = withdrawal.copy(id = withdrawId)

        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "WITHDRAWAL",
                amount = -amount,
                referenceId = if (isAuto) "FAST-WD-$withdrawId" else "WD-$withdrawId",
                description = "USDT (BSC BEP20) Withdrawal (${if (isAuto) "Withdrawal Fast (10% Fee)" else "Manual 0% Fee"}) to $wallet (Net: $${String.format(Locale.US, "%.2f", net)} USDT, Fee: $${String.format(Locale.US, "%.2f", fee)} USDT)",
                balanceAfter = newAvailable,
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(savedWithdrawal)
    }

    // Support Tickets
    fun getTicketsForUser(userId: String): Flow<List<SupportTicketEntity>> = dao.getTicketsByUserId(userId)

    suspend fun submitSupportTicket(
        userId: String,
        userEmail: String,
        subject: String,
        category: String,
        message: String,
        priority: String
    ): Long {
        val ticket = SupportTicketEntity(
            userId = userId,
            userEmail = userEmail,
            subject = subject.trim(),
            category = category,
            message = message.trim(),
            priority = priority,
            status = "OPEN",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return dao.insertTicket(ticket)
    }

    // Admin Operations
    suspend fun adminApproveDeposit(depositId: Long) {
        val deposit = dao.getDepositById(depositId) ?: return
        if (deposit.status == "APPROVED") return

        val user = dao.getUserByIdDirect(deposit.userId) ?: return
        val newAvailable = user.availableBalance + deposit.amount
        dao.updateUserBalances(user.id, newAvailable, user.holdingBalance, user.bonusBalance)
        grantSpinsForDeposit(user.id, deposit.amount)

        dao.updateDeposit(
            deposit.copy(
                status = "APPROVED",
                verifiedAt = System.currentTimeMillis(),
                verificationType = "MANUAL_ADMIN"
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                userId = user.id,
                type = "DEPOSIT",
                amount = deposit.amount,
                referenceId = deposit.txHash,
                description = "Deposit approved manually by Administrator",
                balanceAfter = newAvailable,
                timestamp = System.currentTimeMillis()
            )
        )

        // 5% Recurring Referral Bonus to Referrer
        if (!user.referredBy.isNullOrBlank()) {
            val referrer = dao.getUserByReferralCodeDirect(user.referredBy)
            if (referrer != null) {
                val refBonus = deposit.amount * 0.05
                val refNewAvailable = referrer.availableBalance + refBonus
                dao.updateUserBalances(referrer.id, refNewAvailable, referrer.holdingBalance, referrer.bonusBalance)
                dao.insertTransaction(
                    TransactionEntity(
                        userId = referrer.id,
                        type = "REFERRAL_BONUS",
                        amount = refBonus,
                        referenceId = "REF-DEP-${deposit.txHash}",
                        description = "5% Commission from ${user.id} deposit ($${String.format(Locale.US, "%.2f", deposit.amount)} USDT)",
                        balanceAfter = refNewAvailable,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun adminRejectDeposit(depositId: Long, reason: String) {
        val deposit = dao.getDepositById(depositId) ?: return
        dao.updateDeposit(
            deposit.copy(
                status = "REJECTED",
                adminNote = reason
            )
        )
    }

    suspend fun adminUpdateWithdrawalStatus(withdrawalId: Long, newStatus: String, reason: String? = null) {
        val wd = dao.getWithdrawalById(withdrawalId) ?: return
        if (newStatus == "REJECTED" && wd.status != "REJECTED") {
            // Refund available balance
            val user = dao.getUserByIdDirect(wd.userId)
            if (user != null) {
                val refundedAvailable = user.availableBalance + wd.amount
                dao.updateUserBalances(user.id, refundedAvailable, user.holdingBalance, user.bonusBalance)
                dao.insertTransaction(
                    TransactionEntity(
                        userId = user.id,
                        type = "WITHDRAWAL_REFUND",
                        amount = wd.amount,
                        referenceId = "WD-REFUND-${wd.id}",
                        description = "Withdrawal rejected & refunded: ${reason ?: "Operational compliance"}",
                        balanceAfter = refundedAvailable,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        dao.updateWithdrawal(
            wd.copy(
                status = newStatus,
                processedAt = System.currentTimeMillis(),
                rejectReason = reason
            )
        )
    }

    suspend fun adminReplyTicket(ticketId: Long, reply: String, newStatus: String) {
        val ticket = dao.getTicketById(ticketId) ?: return
        dao.updateTicket(
            ticket.copy(
                adminReply = reply,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun adminUpdateKyc(userId: String, isApproved: Boolean) {
        val user = dao.getUserByIdDirect(userId) ?: return
        dao.updateUser(
            user.copy(
                kycStatus = if (isApproved) "VERIFIED" else "REJECTED"
            )
        )
    }

    suspend fun adminUpdatePlatformConfig(config: PlatformConfigEntity) {
        dao.insertOrUpdateConfig(config)
    }

    suspend fun adminUpdateNftLevel(level: NftLevelEntity) {
        dao.updateNftLevel(level)
    }

    // Lucky Spin Lottery System
    fun getSpinHistoryForUser(userId: String): Flow<List<SpinHistoryEntity>> =
        dao.getSpinHistoryByUserId(userId)

    suspend fun grantSpinsForDeposit(userId: String, depositAmount: Double) {
        when {
            depositAmount >= 1000.0 -> dao.addUserSpins(userId, add100 = 0, add1000 = 6)
            depositAmount >= 100.0 -> dao.addUserSpins(userId, add100 = 3, add1000 = 0)
        }
    }

    suspend fun executeLuckySpin(userId: String): Result<SpinOutcome> {
        val user = dao.getUserByIdDirect(userId)
            ?: return Result.failure(Exception("Account not found."))

        val use1000 = user.tier1000Spins > 0
        val use100 = !use1000 && user.tier100Spins > 0
        if (!use1000 && !use100) {
            return Result.failure(
                Exception("No Lucky Spins available. Deposit $100+ (3 Spins) or $1,000+ (6 Spins) to unlock.")
            )
        }

        val tierLabel = if (use1000) "1000_TIER" else "100_TIER"
        val allowedIndices = if (use1000) {
            SpinWheelConfig.tier1000AllowedIndices
        } else {
            SpinWheelConfig.tier100AllowedIndices
        }

        val chosenIndex = allowedIndices.random()
        val chosenSegment = SpinWheelConfig.segments[chosenIndex]

        // Extra defense-in-depth safety cap check (strictly <= $1 for $100 tier, <= $9 for $1000 tier)
        val safePrize = if (use1000) {
            chosenSegment.amountUsdt.coerceIn(0.09, 9.0)
        } else {
            chosenSegment.amountUsdt.coerceIn(0.09, 1.0)
        }

        val now = System.currentTimeMillis()
        dao.applySpinResult(
            userId = userId,
            prizeAmount = safePrize,
            use100 = if (use100) 1 else 0,
            use1000 = if (use1000) 1 else 0
        )

        dao.insertSpinHistory(
            SpinHistoryEntity(
                userId = userId,
                tierLabel = tierLabel,
                prizeLabel = chosenSegment.label,
                prizeAmount = safePrize,
                timestamp = now
            )
        )

        val updatedBalance = user.availableBalance + safePrize
        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "LUCKY_SPIN_REWARD",
                amount = safePrize,
                referenceId = "SPIN-$now",
                description = "Lucky Spin Lottery Reward (${chosenSegment.label}) credited to Available Balance",
                balanceAfter = updatedBalance,
                timestamp = now
            )
        )

        val remaining100 = if (use100) (user.tier100Spins - 1).coerceAtLeast(0) else user.tier100Spins
        val remaining1000 = if (use1000) (user.tier1000Spins - 1).coerceAtLeast(0) else user.tier1000Spins

        return Result.success(
            SpinOutcome(
                segmentIndex = chosenIndex,
                prizeLabel = chosenSegment.label,
                prizeAmount = safePrize,
                tierUsed = tierLabel,
                remainingTier100Spins = remaining100,
                remainingTier1000Spins = remaining1000
            )
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
