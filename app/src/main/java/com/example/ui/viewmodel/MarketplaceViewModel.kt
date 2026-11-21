package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DepositEntity
import com.example.data.db.HoldingEntity
import com.example.data.db.NftLevelEntity
import com.example.data.db.PlatformConfigEntity
import com.example.data.db.SpinHistoryEntity
import com.example.data.db.SupportTicketEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.UserEntity
import com.example.data.db.WithdrawalEntity
import com.example.data.model.SpinOutcome
import com.example.data.network.LiveApiClient
import com.example.data.repository.MarketplaceRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    CLAIM,
    NFT,
    PROFILE,
    DEPOSIT,
    WITHDRAW,
    REFERRAL,
    AUTH_LOGIN,
    AUTH_REGISTER,
    AUTH_OTP,
    KYC_VERIFICATION,
    SUPPORT_CENTER,
    NEW_TICKET,
    TRANSACTION_HISTORY,
    ADMIN_PANEL,
    TOP_LEADERBOARD,
    REWARDS
}

sealed class DepositUiState {
    object Idle : DepositUiState()
    object Verifying : DepositUiState()
    data class Success(val deposit: DepositEntity) : DepositUiState()
    data class Error(val message: String) : DepositUiState()
}

sealed class SpinUiState {
    object Idle : SpinUiState()
    data class Spinning(val outcome: SpinOutcome) : SpinUiState()
    data class Completed(val outcome: SpinOutcome) : SpinUiState()
    data class Error(val message: String) : SpinUiState()
}

data class PendingRegistrationData(
    val name: String,
    val email: String,
    val pass: String,
    val country: String,
    val district: String,
    val city: String,
    val address: String,
    val refCode: String?
)

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarketplaceRepository
    val platformConfig: StateFlow<PlatformConfigEntity?>
    val nftLevels: StateFlow<List<NftLevelEntity>>
    val totalRegisteredUsers: StateFlow<Int>
    val allUsers: StateFlow<List<UserEntity>>
    val allDeposits: StateFlow<List<DepositEntity>>
    val allWithdrawals: StateFlow<List<WithdrawalEntity>>
    val allHoldings: StateFlow<List<HoldingEntity>>
    val allTickets: StateFlow<List<SupportTicketEntity>>
    val allTransactions: StateFlow<List<TransactionEntity>>

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.AUTH_LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenBackStack = MutableStateFlow<List<Screen>>(listOf(Screen.AUTH_LOGIN))

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _pendingRegistration = MutableStateFlow<PendingRegistrationData?>(null)
    val pendingRegistration: StateFlow<PendingRegistrationData?> = _pendingRegistration.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _depositUiState = MutableStateFlow<DepositUiState>(DepositUiState.Idle)
    val depositUiState: StateFlow<DepositUiState> = _depositUiState.asStateFlow()

    private val _spinUiState = MutableStateFlow<SpinUiState>(SpinUiState.Idle)
    val spinUiState: StateFlow<SpinUiState> = _spinUiState.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getUser(uid) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userHoldings: StateFlow<List<HoldingEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getActiveAndMaturedHoldings(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val claimedHoldings: StateFlow<List<HoldingEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getClaimedHoldings(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDeposits: StateFlow<List<DepositEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getDepositsForUser(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getWithdrawalsForUser(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.dao.getTransactionsByUserId(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTickets: StateFlow<List<SupportTicketEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getTicketsForUser(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSpinHistory: StateFlow<List<SpinHistoryEntity>> = _currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getSpinHistoryForUser(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referredUsers: StateFlow<List<UserEntity>> = currentUser.flatMapLatest { user ->
        if (user != null && user.referralCode.isNotBlank()) {
            repository.getReferralsForCode(user.referralCode)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qualifiedReferralIds: StateFlow<List<String>> = currentUser.flatMapLatest { user ->
        if (user != null && user.referralCode.isNotBlank()) {
            repository.getQualifiedReferralUserIds(user.referralCode)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MarketplaceRepository(db.appDao())

        platformConfig = repository.platformConfig.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        nftLevels = repository.allNftLevels.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        totalRegisteredUsers = repository.allRegisteredUsersCount.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 1
        )
        allUsers = repository.allUsers.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allDeposits = repository.allDeposits.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allWithdrawals = repository.allWithdrawals.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allHoldings = repository.allHoldings.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allTickets = repository.allTickets.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allTransactions = repository.allTransactions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
        viewModelScope.launch {
            LiveApiClient.wakeUpServer()
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value == screen) return
        val updated = _screenBackStack.value.toMutableList()
        updated.add(screen)
        _screenBackStack.value = updated
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        val stack = _screenBackStack.value
        if (stack.size > 1) {
            val updated = stack.dropLast(1)
            _screenBackStack.value = updated
            _currentScreen.value = updated.last()
            return true
        }
        return false
    }

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }

    // Purchases
    fun purchaseNft(level: Int, onSuccess: () -> Unit = {}) {
        val uid = _currentUserId.value ?: run {
            showMessage("Please log in to activate Shares.")
            navigateTo(Screen.AUTH_LOGIN)
            return
        }

        viewModelScope.launch {
            val result = repository.purchaseNft(uid, level)
            result.onSuccess {
                showMessage("Successfully activated ${it.nftName}! Added to Holding Balance.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Failed to activate Share.")
            }
        }
    }

    // Claims
    fun claimDailyProfit(holdingId: Long) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.claimDailyProfit(uid, holdingId)
            result.onSuccess { profit ->
                showMessage("Daily profit claimed! +$${String.format(Locale.US, "%.2f", profit)} USDT added directly to your profile balance.")
            }.onFailure {
                showMessage(it.message ?: "Daily profit claim failed.")
            }
        }
    }

    fun claimHolding(holdingId: Long) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.claimMaturedHolding(uid, holdingId)
            result.onSuccess { payout ->
                showMessage("Cycle completed! Released principal & reward: +$${String.format(Locale.US, "%.2f", payout)} USDT to Available Balance.")
            }.onFailure {
                showMessage(it.message ?: "Claim failed.")
            }
        }
    }

    // Testing accelerator
    fun accelerateHoldingForTesting(holdingId: Long) {
        viewModelScope.launch {
            repository.accelerateHoldingForTesting(holdingId)
            showMessage("Holding accelerated to maturity for testing! Claim is now active.")
        }
    }

    // Deposits (BscScan BEP20 Automatic Verification)
    fun resetDepositUiState() {
        _depositUiState.value = DepositUiState.Idle
    }

    fun verifyAndSubmitDeposit(
        txHash: String,
        expectedAmount: Double? = null
    ) {
        val uid = _currentUserId.value ?: run {
            val msg = "Please sign in first."
            _depositUiState.value = DepositUiState.Error(msg)
            showMessage(msg)
            return
        }

        if (_depositUiState.value is DepositUiState.Verifying) return

        viewModelScope.launch {
            _depositUiState.value = DepositUiState.Verifying
            val result = repository.submitAutoVerifiedDeposit(
                userId = uid,
                txHash = txHash,
                expectedAmount = expectedAmount
            )
            result.onSuccess { dep ->
                _depositUiState.value = DepositUiState.Success(dep)
                showMessage("Deposit verified! +$${String.format(Locale.US, "%.2f", dep.amount)} USDT credited instantly to your balance.")
                val userEmail = currentUser.value?.email ?: ""
                viewModelScope.launch {
                    LiveApiClient.syncDeposit(uid, userEmail, dep.amount, dep.txHash)
                }
            }.onFailure { err ->
                val msg = err.message ?: "Deposit verification failed."
                _depositUiState.value = DepositUiState.Error(msg)
                showMessage(msg)
            }
        }
    }

    fun submitDeposit(
        amount: Double,
        txHash: String,
        autoVerify: Boolean = false,
        network: String = "BSC BNB Smart Chain (BEP20)",
        onSuccess: () -> Unit = {}
    ) {
        val uid = _currentUserId.value ?: run {
            showMessage("Please sign in first.")
            return
        }

        viewModelScope.launch {
            val result = repository.submitDeposit(
                userId = uid,
                amount = amount,
                txHash = txHash,
                automaticVerification = autoVerify,
                network = network
            )
            result.onSuccess { dep ->
                if (dep.status == "APPROVED") {
                    showMessage("Deposit of $${dep.amount} USDT verified on network!")
                } else {
                    showMessage("Deposit submitted! Status: PROCESSING (Pending Admin Approval).")
                }
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Deposit failed.")
            }
        }
    }

    // Withdrawals
    fun submitWithdrawal(
        amount: Double,
        method: String,
        destinationWallet: String,
        passwordPlain: String,
        onSuccess: () -> Unit = {}
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.submitWithdrawal(uid, amount, method, destinationWallet, passwordPlain)
            result.onSuccess { wd ->
                showMessage("Withdrawal request for $${wd.amount} USDT submitted (${wd.method} - Net: $${wd.netAmount} USDT). Status: Processing (Awaiting Admin Approval).")
                val userEmail = currentUser.value?.email ?: ""
                viewModelScope.launch {
                    LiveApiClient.syncWithdrawal(uid, userEmail, wd.amount, wd.method, wd.destinationWallet)
                }
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Withdrawal request failed.")
            }
        }
    }

    // KYC
    fun submitKyc(fullAddress: String, district: String, city: String, onSuccess: () -> Unit = {}) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.submitKyc(uid, fullAddress, district, city)
            result.onSuccess {
                showMessage("KYC verification documents submitted. Status updated to Pending.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "KYC submission failed.")
            }
        }
    }

    // Avatar
    fun updateAvatar(avatarId: Int) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.updateUserAvatar(uid, avatarId)
            showMessage("Profile avatar updated successfully.")
        }
    }

    // Support Tickets
    fun submitSupportTicket(
        subject: String,
        category: String,
        message: String,
        priority: String,
        onSuccess: () -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.submitSupportTicket(user.id, user.email, subject, category, message, priority)
            showMessage("Support ticket submitted. Ticket ID generated.")
            onSuccess()
        }
    }

    // Auth
    fun register(
        name: String,
        email: String,
        pass: String,
        confirmPass: String,
        country: String,
        district: String,
        city: String,
        address: String,
        refCode: String?
    ) {
        val cleanEmail = email.trim().lowercase()
        if (name.isBlank() || cleanEmail.isBlank() || pass.isBlank()) {
            showMessage("Please fill in all required fields.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            showMessage("Please enter a valid Gmail address.")
            return
        }
        if (pass != confirmPass) {
            showMessage("Passwords do not match.")
            return
        }
        if (pass.length < 6) {
            showMessage("Password must be at least 6 characters.")
            return
        }
        if (_isAuthLoading.value) return

        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                if (repository.isEmailAlreadyRegistered(cleanEmail)) {
                    showMessage("An account with this Gmail already exists. Only one account per Gmail is allowed.")
                    return@launch
                }

                val otpResponse = LiveApiClient.requestRegistrationOtp(
                    name = name,
                    email = cleanEmail,
                    passwordPlain = pass,
                    country = country,
                    referralCode = refCode
                )

                if (otpResponse.success) {
                    _pendingRegistration.value = PendingRegistrationData(
                        name = name.trim(),
                        email = cleanEmail,
                        pass = pass,
                        country = country,
                        district = district,
                        city = city,
                        address = address,
                        refCode = refCode
                    )
                    showMessage("OTP sent successfully to $cleanEmail! Check your Gmail inbox or Spam folder.")
                    navigateTo(Screen.AUTH_OTP)
                } else {
                    showMessage(otpResponse.message)
                }
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun resendOtp() {
        val pending = _pendingRegistration.value ?: return
        if (_isAuthLoading.value) return

        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                val res = LiveApiClient.resendRegistrationOtp(pending.email)
                showMessage(res.message)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun cancelOtpVerification() {
        _pendingRegistration.value = null
        navigateBack()
    }

    fun verifyOtp(enteredOtp: String) {
        val pending = _pendingRegistration.value ?: run {
            showMessage("Registration session expired. Please register again.")
            navigateTo(Screen.AUTH_REGISTER)
            return
        }
        if (enteredOtp.trim().length != 6) {
            showMessage("Please enter the 6-digit OTP sent to your Gmail.")
            return
        }
        if (_isAuthLoading.value) return

        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                val verifyRes = LiveApiClient.verifyRegistrationOtp(pending.email, enteredOtp.trim())
                if (!verifyRes.success) {
                    showMessage(verifyRes.message)
                    return@launch
                }

                val saveRes = repository.finalizeVerifiedRegistration(
                    name = pending.name,
                    email = pending.email,
                    passwordPlain = pending.pass,
                    country = pending.country,
                    districtState = pending.district,
                    cityUpazila = pending.city,
                    fullAddress = pending.address,
                    referredByCode = pending.refCode,
                    serverUserId = verifyRes.userId,
                    serverReferralCode = verifyRes.referralCode
                )
                saveRes.onSuccess { user ->
                    _currentUserId.value = user.id
                    _pendingRegistration.value = null
                    showMessage("Email verified & registration complete! Welcome to V-Max Profit.")
                    navigateTo(Screen.HOME)
                }.onFailure { err ->
                    showMessage(err.message ?: "Account could not be finalized.")
                }
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            showMessage("Please enter both email and password.")
            return
        }
        viewModelScope.launch {
            val res = repository.login(email, pass)
            res.onSuccess { user ->
                _currentUserId.value = user.id
                if (user.role == "ADMIN") {
                    showMessage("Admin Logged In: Welcome ${user.name}!")
                    navigateTo(Screen.ADMIN_PANEL)
                } else {
                    showMessage("Welcome back, ${user.name}!")
                    navigateTo(Screen.HOME)
                }
            }.onFailure {
                showMessage(it.message ?: "Login failed.")
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
        showMessage("Logged out successfully.")
        navigateTo(Screen.AUTH_LOGIN)
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
        showMessage("Switched user session.")
    }

    fun adminToggleUserBan(userId: String) {
        viewModelScope.launch {
            val res = repository.adminToggleUserBan(userId)
            res.onSuccess { isBanned ->
                showMessage(if (isBanned) "User has been BANNED from platform." else "User unbanned successfully.")
            }.onFailure {
                showMessage(it.message ?: "Failed to update user status.")
            }
        }
    }

    fun adminAdjustUserBalance(userId: String, available: Double, holding: Double, bonus: Double) {
        viewModelScope.launch {
            repository.adminAdjustUserBalance(userId, available, holding, bonus)
            showMessage("User balance updated.")
        }
    }

    // Admin Panel Actions
    fun adminApproveDeposit(depositId: Long) {
        viewModelScope.launch {
            repository.adminApproveDeposit(depositId)
            showMessage("Deposit #$depositId approved and balances credited.")
        }
    }

    fun adminRejectDeposit(depositId: Long, reason: String) {
        viewModelScope.launch {
            repository.adminRejectDeposit(depositId, reason)
            showMessage("Deposit #$depositId rejected.")
        }
    }

    fun adminUpdateWithdrawal(id: Long, status: String, reason: String? = null) {
        viewModelScope.launch {
            repository.adminUpdateWithdrawalStatus(id, status, reason)
            showMessage("Withdrawal #$id updated to $status.")
        }
    }

    fun adminReplyTicket(id: Long, reply: String, status: String) {
        viewModelScope.launch {
            repository.adminReplyTicket(id, reply, status)
            showMessage("Support ticket #$id replied.")
        }
    }

    fun adminVerifyKyc(userId: String, isApproved: Boolean) {
        viewModelScope.launch {
            repository.adminUpdateKyc(userId, isApproved)
            showMessage("User KYC ${if (isApproved) "VERIFIED" else "REJECTED"}.")
        }
    }

    fun adminUpdateConfig(config: PlatformConfigEntity) {
        viewModelScope.launch {
            repository.adminUpdatePlatformConfig(config)
            showMessage("Platform settings updated successfully.")
        }
    }

    fun adminUpdateNftLevel(level: NftLevelEntity) {
        viewModelScope.launch {
            repository.adminUpdateNftLevel(level)
            showMessage("Share Level ${level.level} settings updated.")
        }
    }

    // Lucky Spin Lottery
    fun playLuckySpin() {
        val uid = _currentUserId.value ?: return
        if (_spinUiState.value is SpinUiState.Spinning) return

        viewModelScope.launch {
            val result = repository.executeLuckySpin(uid)
            result.onSuccess { outcome ->
                _spinUiState.value = SpinUiState.Spinning(outcome)
            }.onFailure { err ->
                val msg = err.message ?: "Unable to spin right now."
                _spinUiState.value = SpinUiState.Error(msg)
                showMessage(msg)
            }
        }
    }

    fun onSpinAnimationFinished() {
        val current = _spinUiState.value
        if (current is SpinUiState.Spinning) {
            _spinUiState.value = SpinUiState.Completed(current.outcome)
            showMessage("Congratulations! You won ${current.outcome.prizeLabel} USDT!")
        }
    }

    fun clearSpinState() {
        _spinUiState.value = SpinUiState.Idle
    }
}
