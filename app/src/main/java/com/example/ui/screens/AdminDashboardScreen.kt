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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DepositEntity
import com.example.data.db.HoldingEntity
import com.example.data.db.NftLevelEntity
import com.example.data.db.PlatformConfigEntity
import com.example.data.db.SupportTicketEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.UserEntity
import com.example.data.db.WithdrawalEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    config: PlatformConfigEntity?,
    users: List<UserEntity>,
    deposits: List<DepositEntity>,
    withdrawals: List<WithdrawalEntity>,
    holdings: List<HoldingEntity>,
    tickets: List<SupportTicketEntity>,
    nftLevels: List<NftLevelEntity>,
    transactions: List<TransactionEntity>,
    onApproveDeposit: (Long) -> Unit,
    onRejectDeposit: (Long, String) -> Unit,
    onUpdateWithdrawal: (Long, String, String?) -> Unit,
    onToggleUserBan: (String) -> Unit,
    onVerifyKyc: (String, Boolean) -> Unit,
    onReplyTicket: (Long, String, String) -> Unit,
    onUpdateConfig: (PlatformConfigEntity) -> Unit,
    onUpdateNftLevel: (NftLevelEntity) -> Unit,
    onAdjustUserBalance: ((String, Double, Double, Double) -> Unit)? = null
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    // 0: Overview & Approvals, 1: User Management, 2: 9 Levels, 3: Settings, 4: Support

    var userSearchQuery by remember { mutableStateOf("") }
    var editLevelDialog by remember { mutableStateOf<NftLevelEntity?>(null) }
    var replyTicketDialog by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var ticketReplyInput by remember { mutableStateOf("") }
    var adjustBalanceUser by remember { mutableStateOf<UserEntity?>(null) }
    var newAvailableInput by remember { mutableStateOf("") }
    var newHoldingInput by remember { mutableStateOf("") }
    var newBonusInput by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Calculated Real Metrics
    val totalUsersCount = users.size
    val depositingUserIds = deposits.map { it.userId }.distinct()
    val depositingUsersCount = depositingUserIds.size

    val pendingDeposits = deposits.filter { it.status == "PENDING" }
    val approvedDeposits = deposits.filter { it.status == "APPROVED" }
    val rejectedDeposits = deposits.filter { it.status == "REJECTED" }
    val totalDepositVolume = approvedDeposits.sumOf { it.amount }

    val pendingWithdrawals = withdrawals.filter { it.status == "PENDING" || it.status == "PROCESSING" }
    val completedWithdrawals = withdrawals.filter { it.status == "COMPLETED" || it.status == "APPROVED" || it.status == "SUCCESS" }
    val rejectedWithdrawals = withdrawals.filter { it.status == "REJECTED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MASTER ADMIN PANEL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Logged in as roybmpartho@gmail.com • Full System Control",
                        fontSize = 11.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("SUPERUSER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                }
            }
        }

        // Section Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = SurfaceCard,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = CyanAccent
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Approvals", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("Users (${users.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    text = { Text("Share Levels", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    text = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedSection) {
            0 -> {
                // SECTION 0: COMPREHENSIVE METRICS & PENDING APPROVALS
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "LIVE PLATFORM METRICS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatCard("Total Users", "$totalUsersCount", CyanAccent, Modifier.weight(1f))
                            AdminStatCard("Depositing Users", "$depositingUsersCount", EmeraldGreen, Modifier.weight(1f))
                            AdminStatCard("Deposit Volume", "$${String.format("%.0f", totalDepositVolume)}", GoldAccent, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatCard("Pending Deposits", "${pendingDeposits.size}", GoldAccent, Modifier.weight(1f))
                            AdminStatCard("Approved Deposits", "${approvedDeposits.size}", EmeraldGreen, Modifier.weight(1f))
                            AdminStatCard("Cancelled Deposits", "${rejectedDeposits.size}", CoralRed, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatCard("Withdraw Requests", "${withdrawals.size}", CyanAccent, Modifier.weight(1f))
                            AdminStatCard("Pending Withdrawals", "${pendingWithdrawals.size}", GoldAccent, Modifier.weight(1f))
                            AdminStatCard("Done Withdrawals", "${completedWithdrawals.size}", EmeraldGreen, Modifier.weight(1f))
                        }
                    }
                }

                // Quick User Search on Admin Dashboard (by UID, Name, or Email) with Edit & Ban
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "QUICK USER SEARCH (UID / NAME / EMAIL)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                placeholder = {
                                    Text(
                                        "Enter User UID (e.g. USR-...), name, or email",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "Search Users", tint = CyanAccent)
                                },
                                trailingIcon = {
                                    if (userSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { userSearchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = TextMuted)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_dashboard_user_search")
                            )
                        }
                    }
                }

                if (userSearchQuery.isNotBlank()) {
                    val quickMatches = users.filter { u ->
                        u.id.contains(userSearchQuery, ignoreCase = true) ||
                            u.name.contains(userSearchQuery, ignoreCase = true) ||
                            u.email.contains(userSearchQuery, ignoreCase = true)
                    }
                    if (quickMatches.isEmpty()) {
                        item {
                            Text(
                                text = "No users matched \"$userSearchQuery\".",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    } else {
                        items(quickMatches, key = { "quick-${it.id}" }) { u ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (u.isBanned) CoralRed else CyanAccent.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                            Text(text = u.email, fontSize = 11.sp, color = TextSecondary)
                                            Text(
                                                text = "UID: ${u.id}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = GoldAccent
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (u.isBanned) CoralRed.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (u.isBanned) "BANNED" else "ACTIVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (u.isBanned) CoralRed else EmeraldGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Avail: $${String.format(Locale.US, "%.2f", u.availableBalance)} • Hold: $${String.format(Locale.US, "%.2f", u.holdingBalance)}",
                                        fontSize = 11.sp,
                                        color = CyanAccent
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                adjustBalanceUser = u
                                                newAvailableInput = String.format(Locale.US, "%.2f", u.availableBalance)
                                                newHoldingInput = String.format(Locale.US, "%.2f", u.holdingBalance)
                                                newBonusInput = String.format(Locale.US, "%.2f", u.bonusBalance)
                                            },
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                        }
                                        Button(
                                            onClick = { onToggleUserBan(u.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (u.isBanned) EmeraldGreen else CoralRed,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (u.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (u.isBanned) "Unban" else "Ban Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pending Deposits Queue
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PENDING DEPOSITS (${pendingDeposits.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "1-Click Verify & Credit",
                            fontSize = 11.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (pendingDeposits.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "All deposits processed! No pending transactions requiring approval.",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(pendingDeposits, key = { it.id }) { dep ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "$${dep.amount} USDT",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "User: ${dep.userId} • Network: ${dep.network}",
                                            fontSize = 11.sp,
                                            color = CyanAccent
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EmeraldGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+$${String.format(Locale.US, "%.2f", dep.bonusAmount)} Bonus",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Transaction ID Box with Copy Action
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF070B14))
                                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "TRANSACTION ID (TxID)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldAccent
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = dep.txHash,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 15.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(dep.txHash))
                                                Toast.makeText(context, "TxID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SurfaceCardElevated)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy TxID",
                                                tint = CyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Submitted: ${dateFormat.format(Date(dep.timestamp))}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onApproveDeposit(dep.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve & Credit Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onRejectDeposit(dep.id, "Invalid transaction hash on blockchain") },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed),
                                        modifier = Modifier.weight(0.5f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 12.sp, color = CoralRed)
                                    }
                                }
                            }
                        }
                    }
                }

                // Approved / Auto-Verified Deposits History
                val approvedDeposits = deposits.filter { it.status == "APPROVED" }
                if (approvedDeposits.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "VERIFIED & APPROVED DEPOSITS (${approvedDeposits.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                    items(approvedDeposits.take(15), key = { "approved-${it.id}" }) { dep ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", dep.amount)} USDT",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "User: ${dep.userId} • ${dep.verificationType}",
                                            fontSize = 10.sp,
                                            color = CyanAccent
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EmeraldGreen.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "APPROVED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TxID: ${dep.txHash}",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(dep.txHash))
                                            Toast.makeText(context, "TxID copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy TxID",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Pending Withdrawals Queue
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PENDING WITHDRAWALS (${pendingWithdrawals.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (pendingWithdrawals.isEmpty()) {
                    item {
                        Text(text = "No pending withdrawal requests.", fontSize = 11.sp, color = TextMuted)
                    }
                } else {
                    items(pendingWithdrawals, key = { it.id }) { wd ->
                        val withdrawingUser = users.firstOrNull { it.id == wd.userId }
                        val netPayable = (wd.amount - wd.feeAmount).coerceAtLeast(0.0)
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Requested: $${String.format(Locale.US, "%.2f", wd.amount)} USDT",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 17.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "User: ${withdrawingUser?.name ?: wd.userId} (${withdrawingUser?.email ?: "N/A"})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(GoldAccent.copy(alpha = 0.14f))
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(wd.userId))
                                                    Toast.makeText(context, "UID copied: ${wd.userId}", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "UID: ${wd.userId}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = GoldAccent
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy UID",
                                                tint = GoldAccent,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Method: ${wd.method}",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldAccent.copy(alpha = 0.2f))
                                                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "PROCESSING",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Fee: -$${String.format(Locale.US, "%.2f", wd.feeAmount)}",
                                            fontSize = 11.sp,
                                            color = CoralRed
                                        )
                                        Text(
                                            text = "Net Payout: $${String.format(Locale.US, "%.2f", netPayable)} USDT",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Destination Wallet Address Box with Copy Action
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF070B14))
                                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "DESTINATION WALLET ADDRESS (BSC BEP20)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanAccent
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = wd.destinationWallet,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 15.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(wd.destinationWallet))
                                                Toast.makeText(context, "Wallet address copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SurfaceCardElevated)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Wallet Address",
                                                tint = CyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Requested: ${dateFormat.format(Date(wd.timestamp))}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onUpdateWithdrawal(wd.id, "APPROVED", null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve (Success)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onUpdateWithdrawal(wd.id, "REJECTED", "Admin security check failed") },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed),
                                        modifier = Modifier.weight(0.6f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject & Refund", fontSize = 12.sp, color = CoralRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // SECTION 1: USER MANAGEMENT WITH BAN / UNBAN
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "USER MANAGEMENT & SECURITY CONTROLS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "View user balances, holdings, referrals, or instantly Ban/Unban user accounts.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search by UID, name, or email...", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                            trailingIcon = {
                                if (userSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { userSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = TextMuted)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_users_search_input")
                        )
                    }
                }

                val filteredUsers = users.filter { u ->
                    userSearchQuery.isBlank() ||
                            u.name.contains(userSearchQuery, ignoreCase = true) ||
                            u.email.contains(userSearchQuery, ignoreCase = true) ||
                            u.id.contains(userSearchQuery, ignoreCase = true)
                }

                items(filteredUsers, key = { it.id }) { u ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (u.isBanned) CoralRed else BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (u.isBanned) CoralRed.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (u.isBanned) Icons.Default.Block else Icons.Default.People,
                                            contentDescription = null,
                                            tint = if (u.isBanned) CoralRed else CyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text(text = u.email, fontSize = 11.sp, color = TextSecondary)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GoldAccent.copy(alpha = 0.14f))
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(u.id))
                                                    Toast.makeText(context, "UID copied: ${u.id}", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "UID: ${u.id}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = GoldAccent
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy UID",
                                                tint = GoldAccent,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }

                                // Status Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (u.isBanned) CoralRed.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (u.isBanned) "BANNED" else "ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (u.isBanned) CoralRed else EmeraldGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Financial Overview for User
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCardElevated)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Available", fontSize = 10.sp, color = TextMuted)
                                    Text("$${String.format("%.2f", u.availableBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Holding", fontSize = 10.sp, color = TextMuted)
                                    Text("$${String.format("%.2f", u.holdingBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Referral Code", fontSize = 10.sp, color = TextMuted)
                                    Text(u.referralCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("KYC", fontSize = 10.sp, color = TextMuted)
                                    Text(u.kycStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (u.kycStatus == "VERIFIED") EmeraldGreen else GoldAccent)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons: Adjust Balance & Ban/Unban
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        adjustBalanceUser = u
                                        newAvailableInput = String.format("%.2f", u.availableBalance)
                                        newHoldingInput = String.format("%.2f", u.holdingBalance)
                                        newBonusInput = String.format("%.2f", u.bonusBalance)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Adjust Balance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CyanAccent
                                    )
                                }

                                Button(
                                    onClick = { onToggleUserBan(u.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (u.isBanned) EmeraldGreen else CoralRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (u.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (u.isBanned) "Unban" else "Ban Account",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // SECTION 2: 9 V-MAX SHARE LEVELS CONFIGURATION
                item {
                    Text(
                        text = "EDIT 9 V-MAX SHARE LEVELS (BUY PRICE, DAILY PROFIT & DURATION)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(nftLevels, key = { it.level }) { lvl ->
                    val dailyProfit = lvl.investmentPrice * lvl.targetReturnRate
                    val totalProfit = dailyProfit * lvl.holdingDays
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(54.dp)) {
                                MonkeyArtwork(level = lvl.level, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = lvl.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(
                                    text = "Buy $${lvl.investmentPrice.toInt()} | Daily Profit $${String.format(Locale.US, "%.2f", dailyProfit)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = "Duration: ${lvl.holdingDays} Days • Total Profit: +$${String.format(Locale.US, "%.2f", totalProfit)}",
                                    fontSize = 11.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = { editLevelDialog = lvl }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Level", tint = GoldAccent)
                            }
                        }
                    }
                }
            }

            3 -> {
                // SECTION 3: PLATFORM WALLETS, WITHDRAWAL TIME WINDOW & FEES CONFIGURATION
                item {
                    var editNormalFee by remember { mutableStateOf((config?.normalWithdrawFeePercent ?: 10.0).toInt().toString()) }
                    var editExpressFee by remember { mutableStateOf((config?.expressWithdrawFeePercent ?: 0.0).toInt().toString()) }
                    var editMinDeposit by remember { mutableStateOf((config?.minDepositAmount ?: 20.0).toInt().toString()) }
                    var editMinWithdraw by remember { mutableStateOf((config?.minWithdrawAmount ?: 10.0).toInt().toString()) }
                    var editBscWallet by remember { mutableStateOf(config?.adminBscWallet ?: "0xe59dca55642127ebac2fb92c1f0cffdfdccdd840") }
                    var editStartHourBD by remember { mutableStateOf((config?.withdrawStartHourBD ?: 8).toString()) }
                    var editEndHourBD by remember { mutableStateOf((config?.withdrawEndHourBD ?: 24).toString()) }
                    var editWindowEnforced by remember { mutableStateOf(config?.isWithdrawWindowEnforced ?: true) }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "V-Max Platform & Withdrawal Time Window", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Withdrawal Operating Hours (USA Time EST)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCardElevated)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "WITHDRAWAL TIME RESTRICTIONS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                    Text(
                                        text = "Users can only withdraw within this window. Defaults: 09:00 PM – 01:00 PM (USA Time EST).",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Start Hour (USA EST 0-23):", fontSize = 11.sp, color = TextSecondary)
                                    OutlinedTextField(
                                        value = editStartHourBD,
                                        onValueChange = { editStartHourBD = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = { Text("21 (09:00 PM)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "End Hour (USA EST 0-23):", fontSize = 11.sp, color = TextSecondary)
                                    OutlinedTextField(
                                        value = editEndHourBD,
                                        onValueChange = { editEndHourBD = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = { Text("13 (01:00 PM)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enforce Withdrawal Time Window",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                androidx.compose.material3.Switch(
                                    checked = editWindowEnforced,
                                    onCheckedChange = { editWindowEnforced = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "Binance BSC Address (USDT BEP20):", fontSize = 11.sp, color = TextSecondary)
                            OutlinedTextField(
                                value = editBscWallet,
                                onValueChange = { editBscWallet = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Standard Fee (%) [Fast]:", fontSize = 11.sp, color = TextSecondary)
                                    OutlinedTextField(
                                        value = editNormalFee,
                                        onValueChange = { editNormalFee = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Manual Fee (%) [1–2 Days]:", fontSize = 11.sp, color = TextSecondary)
                                    OutlinedTextField(
                                        value = editExpressFee,
                                        onValueChange = { editExpressFee = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Min Deposit (USDT):", fontSize = 11.sp, color = TextSecondary)
                                    OutlinedTextField(
                                        value = editMinDeposit,
                                        onValueChange = { editMinDeposit = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Min Withdraw (USDT):", fontSize = 11.sp, color = TextSecondary)
                                    OutlinedTextField(
                                        value = editMinWithdraw,
                                        onValueChange = { editMinWithdraw = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val currentCfg = config ?: PlatformConfigEntity()
                                    val updatedCfg = currentCfg.copy(
                                        adminBscWallet = editBscWallet.trim(),
                                        normalWithdrawFeePercent = editNormalFee.toDoubleOrNull() ?: 10.0,
                                        expressWithdrawFeePercent = editExpressFee.toDoubleOrNull() ?: 0.0,
                                        minDepositAmount = editMinDeposit.toDoubleOrNull() ?: 20.0,
                                        minWithdrawAmount = editMinWithdraw.toDoubleOrNull() ?: 10.0,
                                        withdrawStartHourBD = editStartHourBD.toIntOrNull() ?: 8,
                                        withdrawEndHourBD = editEndHourBD.toIntOrNull() ?: 24,
                                        isWithdrawWindowEnforced = editWindowEnforced
                                    )
                                    onUpdateConfig(updatedCfg)
                                    Toast.makeText(context, "Settings & Withdrawal Window saved successfully!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Platform & Time Configuration", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Share Level Dialog
    editLevelDialog?.let { lvl ->
        EditShareLevelDialog(
            level = lvl,
            onDismiss = { editLevelDialog = null },
            onSave = { updated ->
                onUpdateNftLevel(updated)
                editLevelDialog = null
                Toast.makeText(context, "Level updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Adjust Balance Modal for Admin
    adjustBalanceUser?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { adjustBalanceUser = null },
            title = {
                Text(
                    text = "Manual Balance Control: ${targetUser.name}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "User: ${targetUser.name} (${targetUser.email})\nDirectly modify available, holding, or bonus balances.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = newAvailableInput,
                        onValueChange = { newAvailableInput = it },
                        label = { Text("Available Balance ($ USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newHoldingInput,
                        onValueChange = { newHoldingInput = it },
                        label = { Text("Holding Balance ($ USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newBonusInput,
                        onValueChange = { newBonusInput = it },
                        label = { Text("Bonus Balance ($ USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cur = newAvailableInput.toDoubleOrNull() ?: targetUser.availableBalance
                                newAvailableInput = String.format(Locale.US, "%.2f", cur + 100.0)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ $100", fontSize = 10.sp, color = EmeraldGreen)
                        }
                        OutlinedButton(
                            onClick = {
                                val cur = newAvailableInput.toDoubleOrNull() ?: targetUser.availableBalance
                                newAvailableInput = String.format(Locale.US, "%.2f", cur + 500.0)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ $500", fontSize = 10.sp, color = EmeraldGreen)
                        }
                        OutlinedButton(
                            onClick = {
                                val cur = newAvailableInput.toDoubleOrNull() ?: targetUser.availableBalance
                                newAvailableInput = String.format(Locale.US, "%.2f", (cur - 100.0).coerceAtLeast(0.0))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("- $100", fontSize = 10.sp, color = CoralRed)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val avail = newAvailableInput.toDoubleOrNull() ?: targetUser.availableBalance
                        val hold = newHoldingInput.toDoubleOrNull() ?: targetUser.holdingBalance
                        val bonus = newBonusInput.toDoubleOrNull() ?: targetUser.bonusBalance
                        onAdjustUserBalance?.invoke(targetUser.id, avail, hold, bonus)
                        adjustBalanceUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Apply Balance Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { adjustBalanceUser = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun AdminStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun EditShareLevelDialog(
    level: NftLevelEntity,
    onDismiss: () -> Unit,
    onSave: (NftLevelEntity) -> Unit
) {
    val currentDailyProfit = level.investmentPrice * level.targetReturnRate
    val formattedDailyInitial = if (currentDailyProfit % 1.0 == 0.0) {
        currentDailyProfit.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", currentDailyProfit)
    }

    var editName by remember(level.level) { mutableStateOf(level.name) }
    var editPrice by remember(level.level) { mutableStateOf(level.investmentPrice.toInt().toString()) }
    var editDailyProfit by remember(level.level) { mutableStateOf(formattedDailyInitial) }
    var editDays by remember(level.level) { mutableStateOf(level.holdingDays.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Level ${level.level}: ${level.name}", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Level Name:", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Buy Price ($):", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = editPrice,
                    onValueChange = { input ->
                        editPrice = input.filter { it.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = false
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Daily Profit ($ / day):", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = editDailyProfit,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        val firstDot = filtered.indexOf('.')
                        editDailyProfit = if (firstDot >= 0) {
                            filtered.substring(0, firstDot + 1) +
                                filtered.substring(firstDot + 1).replace(".", "")
                        } else {
                            filtered
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        autoCorrectEnabled = false
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Duration (Days):", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = editDays,
                    onValueChange = { input ->
                        editDays = input.filter { it.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = false
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPrice = (editPrice.toDoubleOrNull() ?: level.investmentPrice).coerceAtLeast(1.0)
                    val newDays = (editDays.toIntOrNull() ?: level.holdingDays).coerceAtLeast(1)
                    val newDailyProfit = (editDailyProfit.toDoubleOrNull() ?: currentDailyProfit).coerceAtLeast(0.0)
                    val newDailyRate = newDailyProfit / newPrice
                    val newDescription = "Buy $${newPrice.toInt()} | Daily Profit $${String.format(Locale.US, "%.2f", newDailyProfit)} | Duration: $newDays Days"

                    onSave(
                        level.copy(
                            name = editName.trim().ifBlank { level.name },
                            description = newDescription,
                            investmentPrice = newPrice,
                            holdingDays = newDays,
                            targetReturnRate = newDailyRate
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceCard
    )
}
