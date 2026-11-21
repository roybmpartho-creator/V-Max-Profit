package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.UsedTransactionEntity
import com.example.data.db.UserEntity
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LuckySpinAndSecurityTest {

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
    fun registeredUser_startsWithZeroBalanceAndZeroSpins() = runBlocking {
        val result = repository.registerUser(
            name = "New Investor",
            email = "investor@example.com",
            passwordPlain = "secret123",
            country = "United States",
            districtState = "NY",
            cityUpazila = "NYC",
            fullAddress = "101 Wall St",
            referredByCode = null
        )
        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals(0.0, user.availableBalance, 0.0001)
        assertEquals(0.0, user.holdingBalance, 0.0001)
        assertEquals(0.0, user.bonusBalance, 0.0001)
        assertEquals(0, user.rewardPoints)
        assertEquals(0, user.tier100Spins)
        assertEquals(0, user.tier1000Spins)
    }

    @Test
    fun deposit100_grants3Spins_andEachRewardStrictlyCappedAt1DollarWithout020Or080() = runBlocking {
        val user = UserEntity(
            id = "USR-SPIN100",
            name = "Spin 100 User",
            email = "spin100@example.com",
            passwordHash = "hash",
            country = "Canada",
            districtState = "ON",
            cityUpazila = "Toronto",
            fullAddress = "10 Bay St"
        )
        db.appDao().insertUser(user)

        // Sub-$100 deposit grants 0 spins
        repository.grantSpinsForDeposit(user.id, 99.0)
        var updated = db.appDao().getUserByIdDirect(user.id)!!
        assertEquals(0, updated.tier100Spins)
        assertEquals(0, updated.tier1000Spins)

        // Deposit $100 -> 3 spins
        repository.grantSpinsForDeposit(user.id, 100.0)
        updated = db.appDao().getUserByIdDirect(user.id)!!
        assertEquals(3, updated.tier100Spins)
        assertEquals(0, updated.tier1000Spins)

        var totalWon = 0.0
        repeat(3) {
            val outcome = repository.executeLuckySpin(user.id).getOrThrow()
            assertTrue("Prize ${outcome.prizeAmount} must be >= 0.09", outcome.prizeAmount >= 0.09)
            assertTrue("Prize ${outcome.prizeAmount} must never exceed 1.00", outcome.prizeAmount <= 1.00)
            assertTrue("Removed $0.20 and $0.80 must not appear", outcome.prizeAmount != 0.20 && outcome.prizeAmount != 0.80)
            assertEquals("100_TIER", outcome.tierUsed)
            totalWon += outcome.prizeAmount
        }

        val afterUser = db.appDao().getUserByIdDirect(user.id)!!
        assertEquals(0, afterUser.tier100Spins)
        assertEquals(totalWon, afterUser.availableBalance, 0.0001)

        // 4th spin must fail when 0 spins left
        val fourthAttempt = repository.executeLuckySpin(user.id)
        assertTrue(fourthAttempt.isFailure)
    }

    @Test
    fun deposit1000_grants6Spins_andEachRewardStrictlyCappedAt9DollarsWithout020Or080() = runBlocking {
        val user = UserEntity(
            id = "USR-SPIN1000",
            name = "Spin 1000 User",
            email = "spin1000@example.com",
            passwordHash = "hash",
            country = "United States",
            districtState = "TX",
            cityUpazila = "Austin",
            fullAddress = "200 Congress Ave"
        )
        db.appDao().insertUser(user)

        // Run 5 cycles (30 spins total) to statistically verify ceiling never exceeds $9.00
        repeat(5) {
            repository.grantSpinsForDeposit(user.id, 1000.0)
            val seeded = db.appDao().getUserByIdDirect(user.id)!!
            assertEquals(6, seeded.tier1000Spins)
            repeat(6) {
                val outcome = repository.executeLuckySpin(user.id).getOrThrow()
                assertTrue("VIP prize ${outcome.prizeAmount} must never exceed 9.00", outcome.prizeAmount <= 9.00)
                assertTrue("VIP prize ${outcome.prizeAmount} must be >= 0.09", outcome.prizeAmount >= 0.09)
                assertTrue("Removed $0.20 and $0.80 must not appear", outcome.prizeAmount != 0.20 && outcome.prizeAmount != 0.80)
                assertEquals("1000_TIER", outcome.tierUsed)
            }
        }
    }

    @Test
    fun submitAutoVerifiedDeposit_rejectsUsedTxEvenInsideBscScanUrl() = runBlocking {
        val user = UserEntity(
            id = "USR-FRAUD",
            name = "Fraud Check",
            email = "fraud@example.com",
            passwordHash = "hash",
            country = "Bangladesh",
            districtState = "Dhaka",
            cityUpazila = "Dhaka",
            fullAddress = "Gulshan"
        )
        db.appDao().insertUser(user)

        val usedHash = "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        db.appDao().insertUsedTransaction(
            UsedTransactionEntity(
                txHash = usedHash,
                userId = user.id,
                amount = 150.0
            )
        )

        val urlAttempt = "https://bscscan.com/tx/${usedHash.uppercase()}  "
        val res = repository.submitAutoVerifiedDeposit(user.id, urlAttempt)
        assertTrue(res.isFailure)
        assertEquals("TxID already used", res.exceptionOrNull()?.message)
    }

    @Test
    fun referralSystem_countsSignupsWithZeroIncomeUntilDeposit_thenCredits5PercentCommission() = runBlocking {
        val referrer = repository.registerUser(
            name = "Referrer User",
            email = "ref_owner@example.com",
            passwordPlain = "pass123",
            country = "United States",
            districtState = "CA",
            cityUpazila = "LA",
            fullAddress = "100 Sunset",
            referredByCode = null
        ).getOrThrow()

        // Initially 0 referrals, 0 qualified, 0 balance
        assertEquals(0, repository.getReferralsForCode(referrer.referralCode).first().size)
        assertEquals(0, repository.getQualifiedReferralUserIds(referrer.referralCode).first().size)
        assertEquals(0.0, referrer.availableBalance, 0.0001)

        // Referred user registers using lowercase link format
        val friend = repository.registerUser(
            name = "Referred Friend",
            email = "friend@example.com",
            passwordPlain = "pass123",
            country = "Canada",
            districtState = "ON",
            cityUpazila = "Toronto",
            fullAddress = "20 King St",
            referredByCode = "https://vmaxprofitshare.com/ref/${referrer.referralCode.lowercase()}"
        ).getOrThrow()

        // Total referrals = 1, qualified = 0, rewards still $0.00 before deposit!
        assertEquals(1, repository.getReferralsForCode(referrer.referralCode).first().size)
        assertEquals(0, repository.getQualifiedReferralUserIds(referrer.referralCode).first().size)
        val referrerBeforeDeposit = db.appDao().getUserByIdDirect(referrer.id)!!
        assertEquals(0.0, referrerBeforeDeposit.availableBalance, 0.0001)
        val bonusTxBefore = db.appDao().getTransactionsByUserId(referrer.id).first().filter { it.type == "REFERRAL_BONUS" }
        assertTrue(bonusTxBefore.isEmpty())

        // Friend deposits $200.00 -> Referrer receives 5% ($10.00) directly in balance & qualified = 1
        repository.submitDeposit(
            userId = friend.id,
            amount = 200.0,
            txHash = "0xcccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
            automaticVerification = true
        ).getOrThrow()

        assertEquals(1, repository.getQualifiedReferralUserIds(referrer.referralCode).first().size)
        val referrerAfterDeposit = db.appDao().getUserByIdDirect(referrer.id)!!
        assertEquals(10.0, referrerAfterDeposit.availableBalance, 0.0001)
        val bonusTxAfter = db.appDao().getTransactionsByUserId(referrer.id).first().filter { it.type == "REFERRAL_BONUS" }
        assertEquals(1, bonusTxAfter.size)
        assertEquals(10.0, bonusTxAfter.first().amount, 0.0001)
    }

    @Test
    fun finalizeVerifiedRegistration_allowsOnlyOneAccountPerGmail() = runBlocking {
        val first = repository.finalizeVerifiedRegistration(
            name = "User One",
            email = "SingleAccount@Gmail.com",
            passwordPlain = "pass123",
            country = "United States",
            districtState = "CA",
            cityUpazila = "SF",
            fullAddress = "100 Main",
            referredByCode = null,
            serverUserId = "USR-111222",
            serverReferralCode = "VMAX-111222"
        )
        assertTrue(first.isSuccess)
        val created = first.getOrThrow()
        assertTrue(created.isEmailVerified)
        assertEquals(0.0, created.availableBalance, 0.0001)
        assertEquals("USR-111222", created.id)

        val duplicate = repository.finalizeVerifiedRegistration(
            name = "Duplicate Attempt",
            email = "  singleaccount@gmail.com ",
            passwordPlain = "pass456",
            country = "Canada",
            districtState = "ON",
            cityUpazila = "Toronto",
            fullAddress = "200 King",
            referredByCode = null
        )
        assertTrue(duplicate.isFailure)
        assertTrue(duplicate.exceptionOrNull()?.message?.contains("Only one account per Gmail") == true)
    }
}
