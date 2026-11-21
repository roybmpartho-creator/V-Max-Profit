package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClaimScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KycScreen
import com.example.ui.screens.NftMarketplaceScreen
import com.example.ui.screens.OtpVerificationDialog
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.RewardsSpinScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TopLeaderboardScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.NFTTradeTheme
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NFTTradeTheme {
                NFTTradeApp()
            }
        }
    }
}

@Composable
fun NFTTradeApp(viewModel: MarketplaceViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val platformConfig by viewModel.platformConfig.collectAsStateWithLifecycle()
    val nftLevels by viewModel.nftLevels.collectAsStateWithLifecycle()
    val totalRegisteredUsers by viewModel.totalRegisteredUsers.collectAsStateWithLifecycle()
    val userHoldings by viewModel.userHoldings.collectAsStateWithLifecycle()
    val claimedHoldings by viewModel.claimedHoldings.collectAsStateWithLifecycle()
    val userDeposits by viewModel.userDeposits.collectAsStateWithLifecycle()
    val userWithdrawals by viewModel.userWithdrawals.collectAsStateWithLifecycle()
    val userTickets by viewModel.userTickets.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()

    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()
    val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
    val allHoldings by viewModel.allHoldings.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val depositUiState by viewModel.depositUiState.collectAsStateWithLifecycle()
    val spinUiState by viewModel.spinUiState.collectAsStateWithLifecycle()
    val userSpinHistory by viewModel.userSpinHistory.collectAsStateWithLifecycle()
    val referredUsers by viewModel.referredUsers.collectAsStateWithLifecycle()
    val qualifiedReferralIds by viewModel.qualifiedReferralIds.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val isAuthScreen = currentUser == null ||
        currentScreen == Screen.AUTH_LOGIN ||
        currentScreen == Screen.AUTH_REGISTER ||
        currentScreen == Screen.AUTH_OTP

    // Hardware back button handler
    BackHandler(enabled = !isAuthScreen && currentScreen != Screen.HOME) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgDark,
        topBar = {
            if (!isAuthScreen) {
                TopHeaderBar(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    onNavigateBack = { viewModel.navigateBack() },
                    onNavigateTo = { viewModel.navigateTo(it) }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    activeHoldingCount = userHoldings.size,
                    onTabSelected = { viewModel.navigateTo(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(innerPadding)
        ) {
            if (currentUser == null) {
                when (currentScreen) {
                    Screen.AUTH_REGISTER -> AuthScreen(
                        initialTab = 1,
                        onLogin = { email, pass -> viewModel.login(email, pass) },
                        onRegister = { name, email, pass, confirm, country, district, city, addr, ref ->
                            viewModel.register(name, email, pass, confirm, country, district, city, addr, ref)
                        }
                    )
                    Screen.AUTH_OTP -> OtpVerificationDialog(
                        userEmail = "your registered email",
                        onVerifyOtp = { otp -> viewModel.verifyOtp(otp) }
                    )
                    else -> AuthScreen(
                        initialTab = 0,
                        onLogin = { email, pass -> viewModel.login(email, pass) },
                        onRegister = { name, email, pass, confirm, country, district, city, addr, ref ->
                            viewModel.register(name, email, pass, confirm, country, district, city, addr, ref)
                        }
                    )
                }
            } else {
                when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    currentUser = currentUser,
                    config = platformConfig,
                    nftLevels = nftLevels,
                    totalRegisteredUsers = totalRegisteredUsers,
                    onNavigateTo = { viewModel.navigateTo(it) },
                    onSelectNftLevel = { viewModel.navigateTo(Screen.NFT) }
                )

                Screen.CLAIM -> ClaimScreen(
                    activeHoldings = userHoldings,
                    claimedHoldings = claimedHoldings,
                    onClaimDailyProfit = { viewModel.claimDailyProfit(it) },
                    onClaimHolding = { viewModel.claimHolding(it) }
                )

                Screen.NFT -> NftMarketplaceScreen(
                    currentUser = currentUser,
                    nftLevels = nftLevels,
                    activeHoldings = userHoldings,
                    onPurchaseNft = { viewModel.purchaseNft(it) }
                )

                Screen.PROFILE -> ProfileScreen(
                    currentUser = currentUser,
                    onNavigateTo = { viewModel.navigateTo(it) },
                    onChangeAvatar = { viewModel.updateAvatar(it) },
                    onLogout = { viewModel.logout() }
                )

                Screen.DEPOSIT -> DepositScreen(
                    config = platformConfig,
                    depositHistory = userDeposits,
                    depositUiState = depositUiState,
                    onVerifyAndSubmitDeposit = { txHash, expectedAmount ->
                        viewModel.verifyAndSubmitDeposit(txHash, expectedAmount)
                    },
                    onDismissResult = { viewModel.resetDepositUiState() }
                )

                Screen.WITHDRAW -> WithdrawScreen(
                    currentUser = currentUser,
                    config = platformConfig,
                    withdrawals = userWithdrawals,
                    onWithdrawSubmitted = { amount: Double, method: String, wallet: String, pass: String ->
                        viewModel.submitWithdrawal(amount, method, wallet, pass)
                    }
                )

                Screen.REFERRAL -> ReferralScreen(
                    currentUser = currentUser,
                    referredUsers = referredUsers,
                    qualifiedReferralIds = qualifiedReferralIds,
                    referralTransactions = userTransactions.filter { it.type == "REFERRAL_BONUS" }
                )

                Screen.AUTH_LOGIN -> AuthScreen(
                    initialTab = 0,
                    onLogin = { email, pass -> viewModel.login(email, pass) },
                    onRegister = { name, email, pass, confirm, country, district, city, addr, ref ->
                        viewModel.register(name, email, pass, confirm, country, district, city, addr, ref)
                    }
                )

                Screen.AUTH_REGISTER -> AuthScreen(
                    initialTab = 1,
                    onLogin = { email, pass -> viewModel.login(email, pass) },
                    onRegister = { name, email, pass, confirm, country, district, city, addr, ref ->
                        viewModel.register(name, email, pass, confirm, country, district, city, addr, ref)
                    }
                )

                Screen.AUTH_OTP -> {
                    // Show OTP Verification dialog
                    OtpVerificationDialog(
                        userEmail = currentUser?.email ?: "your registered email",
                        onVerifyOtp = { otp -> viewModel.verifyOtp(otp) }
                    )
                }

                Screen.KYC_VERIFICATION -> KycScreen(
                    currentUser = currentUser,
                    onSubmitKyc = { address, district, city ->
                        viewModel.submitKyc(address, district, city)
                    }
                )

                Screen.SUPPORT_CENTER, Screen.NEW_TICKET -> SupportScreen(
                    tickets = userTickets,
                    onSubmitTicket = { sub, cat, msg, prio ->
                        viewModel.submitSupportTicket(sub, cat, msg, prio)
                    }
                )

                Screen.ADMIN_PANEL -> {
                    if (currentUser?.role == "ADMIN") {
                        AdminDashboardScreen(
                            config = platformConfig,
                            users = allUsers,
                            deposits = allDeposits,
                            withdrawals = allWithdrawals,
                            holdings = allHoldings,
                            tickets = allTickets,
                            nftLevels = nftLevels,
                            transactions = userTransactions,
                            onApproveDeposit = { viewModel.adminApproveDeposit(it) },
                            onRejectDeposit = { id, reason -> viewModel.adminRejectDeposit(id, reason) },
                            onUpdateWithdrawal = { id, status, reason -> viewModel.adminUpdateWithdrawal(id, status, reason) },
                            onToggleUserBan = { viewModel.adminToggleUserBan(it) },
                            onVerifyKyc = { uid, approved -> viewModel.adminVerifyKyc(uid, approved) },
                            onReplyTicket = { id, reply, status -> viewModel.adminReplyTicket(id, reply, status) },
                            onUpdateConfig = { viewModel.adminUpdateConfig(it) },
                            onUpdateNftLevel = { viewModel.adminUpdateNftLevel(it) },
                            onAdjustUserBalance = { uid, avail, hold, bonus -> viewModel.adminAdjustUserBalance(uid, avail, hold, bonus) }
                        )
                    } else {
                        HomeScreen(
                            currentUser = currentUser,
                            config = platformConfig,
                            nftLevels = nftLevels,
                            totalRegisteredUsers = totalRegisteredUsers,
                            onNavigateTo = { viewModel.navigateTo(it) },
                            onSelectNftLevel = { viewModel.navigateTo(Screen.NFT) }
                        )
                    }
                }

                Screen.TOP_LEADERBOARD -> TopLeaderboardScreen(
                    onNavigateBack = { viewModel.navigateBack() }
                )

                Screen.REWARDS -> RewardsSpinScreen(
                    currentUser = currentUser,
                    spinUiState = spinUiState,
                    spinHistory = userSpinHistory,
                    onPlaySpin = { viewModel.playLuckySpin() },
                    onSpinAnimationFinished = { viewModel.onSpinAnimationFinished() },
                    onDismissResult = { viewModel.clearSpinState() },
                    onNavigateTo = { viewModel.navigateTo(it) }
                )

                Screen.TRANSACTION_HISTORY -> {
                    // Falls back cleanly to Profile or Claim
                    ProfileScreen(
                        currentUser = currentUser,
                        onNavigateTo = { viewModel.navigateTo(it) },
                        onChangeAvatar = { viewModel.updateAvatar(it) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}
}


