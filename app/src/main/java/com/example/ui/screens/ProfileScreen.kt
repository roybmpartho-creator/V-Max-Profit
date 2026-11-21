package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserEntity
import com.example.ui.components.MonkeyArtwork
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    onNavigateTo: (Screen) -> Unit,
    onChangeAvatar: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var showAvatarPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to V-Max Profit Share",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Sign in or register an account to manage your V-Max Share holdings, USDT balance, and daily profit claims.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onNavigateTo(Screen.AUTH_LOGIN) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sign In / Register", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Profile Top Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable { showAvatarPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        MonkeyArtwork(level = currentUser.avatarId, size = 76.dp)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CyanAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Avatar",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentUser.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            if (currentUser.role == "ADMIN") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoldAccent)
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                }
                            }
                        }

                        Text(
                            text = currentUser.email,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyanAccent.copy(alpha = 0.14f))
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("User UID", currentUser.id))
                                        Toast.makeText(context, "UID copied: ${currentUser.id}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "UID: ${currentUser.id}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy UID",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (currentUser.kycStatus) {
                                            "VERIFIED" -> EmeraldGreen.copy(alpha = 0.2f)
                                            "PENDING" -> GoldAccent.copy(alpha = 0.2f)
                                            else -> TextMuted.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "KYC: ${currentUser.kycStatus}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentUser.kycStatus) {
                                        "VERIFIED" -> EmeraldGreen
                                        "PENDING" -> GoldAccent
                                        else -> TextSecondary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Balances Ledger Overview Card (My Balance, Available Balance, Holding Balance, Bonus Balance)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balances_overview_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MY BALANCE (TOTAL NET WORTH)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", currentUser.availableBalance + currentUser.holdingBalance + currentUser.bonusBalance)} USDT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Available Balance
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Text(text = "Available Balance", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${String.format("%.2f", currentUser.availableBalance)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }

                        // Holding Balance
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Text(text = "Holding Balance", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${String.format("%.2f", currentUser.holdingBalance)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }

                        // Bonus Balance
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Text(text = "Promotional Bonus", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${String.format("%.2f", currentUser.bonusBalance)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deposit & Withdraw Quick Actions (matching Home Screen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateTo(Screen.DEPOSIT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = Color(0xFF070B14)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_deposit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.South,
                                contentDescription = "Deposit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Deposit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onNavigateTo(Screen.WITHDRAW) },
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_withdraw_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.North,
                                contentDescription = "Withdraw",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Withdraw",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // PROMINENT LUCKY SPIN & REWARDS FEATURED CARD
        item {
            val totalSpins = currentUser.tier100Spins + currentUser.tier1000Spins
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1504)),
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.REWARDS) }
                    .testTag("profile_lucky_spin_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.22f))
                                .border(1.5.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Lucky Spin Rewards",
                                tint = GoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "LUCKY SPIN & REWARDS",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (totalSpins > 0) {
                                    "$totalSpins Lucky Spins Available • Spin & Win!"
                                } else {
                                    "Win Jackpots Up to $999,999 USDT"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (totalSpins > 0) "SPIN" else "OPEN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // PROMINENT REFERRAL CENTER SHORTCUT
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261900)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.REFERRAL) }
                    .testTag("profile_referral_center_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Referral", tint = GoldAccent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "REFERRAL CENTER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Invite & earn rewards • Ref Code: ${currentUser.referralCode}",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = GoldAccent)
                }
            }
        }

        // Quick Navigation Menu Items
        item {
            Text(
                text = "FINANCIAL & TRANSACTION HISTORIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Deposit History & Recharge Records",
                    subtitle = "USDT BSC BEP20 auto-verified deposits",
                    onClick = { onNavigateTo(Screen.DEPOSIT) }
                )
                ProfileDivider()
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Share Activation & Holding History",
                    subtitle = "All active and completed 9-level investments",
                    onClick = { onNavigateTo(Screen.CLAIM) }
                )
                ProfileDivider()
                ProfileMenuItem(
                    icon = Icons.Default.ReceiptLong,
                    title = "Claim History & Payout Records",
                    subtitle = "Matured principal & trading target rewards",
                    onClick = { onNavigateTo(Screen.CLAIM) }
                )
                ProfileDivider()
                ProfileMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Withdrawal History",
                    subtitle = "Standard & Manual BSC BEP20 payout records",
                    onClick = { onNavigateTo(Screen.WITHDRAW) }
                )
                ProfileDivider()
                val totalSpins = currentUser.tier100Spins + currentUser.tier1000Spins
                ProfileMenuItem(
                    icon = Icons.Default.Stars,
                    title = "Lucky Spin & Lottery Rewards",
                    subtitle = if (totalSpins > 0) "$totalSpins Lucky Spins ready to play!" else "Deposit $100+ to unlock Lucky Spins",
                    tint = GoldAccent,
                    onClick = { onNavigateTo(Screen.REWARDS) }
                )
            }
        }

        // Account Services
        item {
            Text(
                text = "ACCOUNT & SECURITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Account Verification (KYC)",
                    subtitle = "Status: ${currentUser.kycStatus}",
                    onClick = { onNavigateTo(Screen.KYC_VERIFICATION) }
                )
                ProfileDivider()
                ProfileMenuItem(
                    icon = Icons.Default.HeadsetMic,
                    title = "Customer Care & Support Tickets",
                    subtitle = "Submit tickets & view admin replies",
                    onClick = { onNavigateTo(Screen.SUPPORT_CENTER) }
                )
                ProfileDivider()
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Security Settings & Password",
                    subtitle = "Two-factor verification and credentials",
                    onClick = {}
                )
                if (currentUser.role == "ADMIN") {
                    ProfileDivider()
                    ProfileMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Dashboard Panel",
                        subtitle = "Full platform operations & ledger manager",
                        tint = GoldAccent,
                        onClick = { onNavigateTo(Screen.ADMIN_PANEL) }
                    )
                }
                ProfileDivider()
                ProfileMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    subtitle = "Sign out of this session",
                    tint = CoralRed,
                    onClick = onLogout
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Avatar Picker Dialog
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = {
                Text("Select Monkey Avatar", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "Choose your profile avatar:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items((1..9).toList()) { avatarId ->
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        2.dp,
                                        if (currentUser.avatarId == avatarId) CyanAccent else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onChangeAvatar(avatarId)
                                        showAvatarPicker = false
                                    }
                            ) {
                                MonkeyArtwork(level = avatarId, size = 68.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAvatarPicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = CyanAccent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = TextMuted)
    }
}

@Composable
private fun ProfileDivider() {
    androidx.compose.material3.HorizontalDivider(
        color = BorderDark,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
