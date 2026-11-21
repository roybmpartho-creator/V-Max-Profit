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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest
import com.example.data.db.PlatformConfigEntity
import com.example.data.db.UserEntity
import com.example.data.db.WithdrawalEntity
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
fun WithdrawScreen(
    currentUser: UserEntity?,
    config: PlatformConfigEntity?,
    withdrawals: List<WithdrawalEntity>,
    onWithdrawSubmitted: (Double, String, String, String) -> Unit
) {
    val minWithdraw = config?.minWithdrawAmount ?: 10.0
    val maxWithdraw = config?.maxWithdrawAmount ?: 10000.0
    val normalFeePercent = config?.normalWithdrawFeePercent ?: 10.0 // Standard (Withdrawal Fast) = 10%
    val expressFeePercent = config?.expressWithdrawFeePercent ?: 0.0 // Manual Payout (1-2 Days) = 0%

    var amountInput by remember { mutableStateOf("100") }
    var selectedMethod by remember { mutableStateOf("STANDARD") } // "STANDARD" (10% fee) or "MANUAL" (0% free)
    val selectedNetwork = "BSC BEP20" // Locked exclusively to USDT BSC BEP20
    var destinationWallet by remember { mutableStateOf("") }
    var accountPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val amountValue = amountInput.toDoubleOrNull() ?: 0.0
    // Standard is strictly 10% fee (automatic fast payout), Manual is strictly 0% fee (free, 1-2 days)
    val feePercent = if (selectedMethod == "STANDARD") 10.0 else 0.0
    val feeCalculated = amountValue * (feePercent / 100.0)
    val netCalculated = (amountValue - feeCalculated).coerceAtLeast(0.0)

    val available = currentUser?.availableBalance ?: 0.0
    val isAmountValid = amountValue in minWithdraw..maxWithdraw && amountValue <= available && (amountValue - feeCalculated) > 0.0
    val isWalletValid = destinationWallet.trim().length >= 26 && destinationWallet.trim().startsWith("0x")

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    // Live clock for USA Time EST (America/New_York)
    val usZone = remember { java.util.TimeZone.getTimeZone("America/New_York") }
    val usCal = java.util.Calendar.getInstance(usZone)
    val usTimeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US).apply { timeZone = usZone } }

    val startHour = config?.withdrawStartHourBD ?: 8
    val endHour = config?.withdrawEndHourBD ?: 24
    val isWindowEnforced = config?.isWithdrawWindowEnforced ?: true

    // Check operating hours (default corresponds to USA Time 09:00 PM – 01:00 PM EST)
    val currentUsHour = usCal.get(java.util.Calendar.HOUR_OF_DAY)
    val isWithinWindow = currentUsHour >= 21 || currentUsHour < 13
    val isWithdrawAllowed = !isWindowEnforced || isWithinWindow

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "CRYPTO WITHDRAWAL (USDT BSC BEP20)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Exclusive USDT BSC BEP20 Network • Min: $10 • Max: $10,000 • Admin Configurable Fees",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Daily Operating Hours (Small, subtle card matching the app design, USA Time only)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Withdrawal Hours",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "USA Time: 09:00 PM – 01:00 PM",
                            fontSize = 11.sp,
                            color = CyanAccent
                        )
                    }
                    Text(
                        text = "Current: ${usTimeFormat.format(Date())} EST",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Available Balance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Available Balance for Withdrawal",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", available)} USDT",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Account UID: ${currentUser.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2942))
                            .clickable { amountInput = String.format(Locale.US, "%.2f", available) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Max All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    }
                }
            }
        }

        // Method Selector: Standard vs Manual Express
        item {
            Text(
                text = "Select Payout Method & Fee",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Standard (Automatic Binance API - 10% Fee)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = "STANDARD" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMethod == "STANDARD") Color(0xFF0C243B) else SurfaceCard
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (selectedMethod == "STANDARD") CyanAccent else BorderDark
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Standard", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            if (selectedMethod == "STANDARD") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Fee: 10%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                        Text("Withdrawal Fast", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                // Manual Payout (0% Fee, 1-2 Days)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = "MANUAL" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMethod == "MANUAL") Color(0xFF0C243B) else SurfaceCard
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (selectedMethod == "MANUAL") CyanAccent else BorderDark
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Manual", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            if (selectedMethod == "MANUAL") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Fee: 0% (FREE)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
                        Text("Payment sent in 1–2 days", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Input Form Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Locked BSC BEP20 Network Badge
                    Text(
                        text = "Payout Network (Exclusive)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .border(1.dp, CyanAccent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "USDT (BSC BNB Smart Chain BEP20)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("EXCLUSIVE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Withdrawal Amount (USDT)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Destination $selectedNetwork Wallet Address",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = destinationWallet,
                        onValueChange = { destinationWallet = it },
                        placeholder = { Text("Enter your BSC BEP20 USDT address (0x...)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_wallet_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Account Password (Required for Withdrawal Verification)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = accountPassword,
                        onValueChange = {
                            accountPassword = it
                            passwordError = null
                        },
                        placeholder = { Text("Enter your account password", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = if (passwordError != null) CoralRed else CyanAccent)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextMuted
                                )
                            }
                        },
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(text = passwordError!!, color = CoralRed, fontSize = 11.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            errorBorderColor = CoralRed,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_password_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pre-Confirmation Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCardElevated)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Requested Amount:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "$${String.format(Locale.US, "%.2f", amountValue)} USDT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Payout Method:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = if (selectedMethod == "STANDARD") "Standard (Withdrawal Fast - 10% Fee)" else "Manual (0% Free, 1-2 Days)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedMethod == "STANDARD") GoldAccent else EmeraldGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Withdrawal Fee:", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (selectedMethod == "STANDARD") "-$${String.format(Locale.US, "%.2f", feeCalculated)} USDT (10%)" else "$0.00 USDT (0% FREE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMethod == "STANDARD") CoralRed else EmeraldGreen
                            )
                        }
                        androidx.compose.material3.HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Net Payout Received:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "$${String.format(Locale.US, "%.2f", netCalculated)} USDT", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (!isWithdrawAllowed) return@Button
                            if (accountPassword.isBlank()) {
                                passwordError = "Account password is required to authorize withdrawal!"
                                return@Button
                            }
                            val currentHash = currentUser?.passwordHash
                            if (currentHash != null && hashCheck(accountPassword.trim()) != currentHash) {
                                passwordError = "Password does not match your account password! Please enter correct password."
                                return@Button
                            }
                            passwordError = null
                            showConfirmDialog = true
                        },
                        enabled = isAmountValid && isWalletValid && accountPassword.isNotBlank() && isWithdrawAllowed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF090D16),
                            disabledContainerColor = Color(0xFF263554),
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("request_withdrawal_button")
                    ) {
                        Text(
                            text = if (!isWithdrawAllowed) "Withdrawals Open 09:00 PM – 01:00 PM (USA Time)" else "Proceed to Confirmation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Withdrawal History Section
        item {
            Text(
                text = "WITHDRAWAL HISTORY",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (withdrawals.isEmpty()) {
            item {
                Text(
                    text = "No withdrawal requests yet.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        } else {
            items(withdrawals, key = { it.id }) { wd ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${wd.amount} USDT (${wd.method})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (wd.status) {
                                            "COMPLETED", "APPROVED", "SUCCESS" -> EmeraldGreen.copy(alpha = 0.15f)
                                            "REJECTED" -> CoralRed.copy(alpha = 0.15f)
                                            else -> GoldAccent.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (wd.status) {
                                        "COMPLETED", "APPROVED", "SUCCESS" -> "APPROVED (SUCCESS)"
                                        "REJECTED" -> "REJECTED & REFUNDED"
                                        else -> "PROCESSING"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (wd.status) {
                                        "COMPLETED", "APPROVED", "SUCCESS" -> EmeraldGreen
                                        "REJECTED" -> CoralRed
                                        else -> GoldAccent
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "UID: ${wd.userId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Text(
                            text = "Net: $${String.format(Locale.US, "%.2f", wd.netAmount)} USDT (Fee: $${String.format(Locale.US, "%.2f", wd.feeAmount)} USDT)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Wallet: ${wd.destinationWallet.take(12)}...${wd.destinationWallet.takeLast(8)}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = dateFormat.format(Date(wd.timestamp)),
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Final Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text("Final Withdrawal Confirmation", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Please verify your payout details carefully before authorizing this transaction:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardElevated)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "User UID:", fontSize = 11.sp, color = TextMuted)
                            Text(text = currentUser?.id ?: "N/A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Amount:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "$${String.format(Locale.US, "%.2f", amountValue)} USDT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Payout Method:", fontSize = 11.sp, color = TextMuted)
                            Text(text = if (selectedMethod == "STANDARD") "Standard (Withdrawal Fast)" else "Manual Payout (1-2 Days)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Withdrawal Fee:", fontSize = 11.sp, color = TextMuted)
                            Text(text = if (selectedMethod == "STANDARD") "-$${String.format(Locale.US, "%.2f", feeCalculated)} USDT (10%)" else "$0.00 USDT (0% FREE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedMethod == "STANDARD") CoralRed else EmeraldGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Net Payout Received:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "$${String.format(Locale.US, "%.2f", netCalculated)} USDT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Wallet ($selectedNetwork):", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${destinationWallet.take(10)}...${destinationWallet.takeLast(6)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Security Verification:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "Password Verified ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pass = accountPassword.trim()
                        accountPassword = ""
                        showConfirmDialog = false
                        onWithdrawSubmitted(
                            amountValue,
                            if (selectedMethod == "STANDARD") "Standard (Withdrawal Fast)" else "Manual Payout (0% Free, 1-2 Days)",
                            destinationWallet.trim(),
                            pass
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("confirm_withdrawal_button")
                ) {
                    Text("Confirm & Submit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

private fun hashCheck(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
