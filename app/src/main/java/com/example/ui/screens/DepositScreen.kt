package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.DepositEntity
import com.example.data.db.PlatformConfigEntity
import com.example.data.network.BscScanVerifier
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
import com.example.ui.viewmodel.DepositUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DepositScreen(
    config: PlatformConfigEntity?,
    depositHistory: List<DepositEntity>,
    depositUiState: DepositUiState,
    onVerifyAndSubmitDeposit: (txHash: String, expectedAmount: Double?) -> Unit,
    onDismissResult: () -> Unit
) {
    val context = LocalContext.current

    // Hardcoded permanent personal Binance BEP20 USDT wallet address
    val bscAddress = BscScanVerifier.DEPOSIT_WALLET_ADDRESS
    val minDeposit = config?.minDepositAmount ?: 20.0

    // 2-Step Flow:
    // Step 1: Select deposit amount (display guide before showing address & QR)
    // Step 2: Prompt payment + show hardcoded Address & static QR + TxID submission box only
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedAmountInput by remember { mutableStateOf("50") }
    var txHashInput by remember { mutableStateOf("") }
    var copyNotice by remember { mutableStateOf(false) }

    val selectedAmountValue = selectedAmountInput.toDoubleOrNull() ?: 0.0
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val isVerifying = depositUiState is DepositUiState.Verifying

    BackHandler(enabled = currentStep == 2 && !isVerifying) {
        currentStep = 1
        if (depositUiState is DepositUiState.Error) {
            onDismissResult()
        }
    }

    LaunchedEffect(depositUiState) {
        if (depositUiState is DepositUiState.Success) {
            txHashInput = ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEPOSIT USDT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = if (currentStep == 1) {
                            "Step 1 of 2: Select Deposit Amount"
                        } else {
                            "Step 2 of 2: Make Payment & Verify TxID"
                        },
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreen.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AUTO VERIFY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }
        }

        if (currentStep == 1) {
            // ==========================================
            // STEP 1: SELECT DEPOSIT AMOUNT
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Deposit Amount (USDT)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose how much USDT you want to deposit. Final credited balance will match the exact verified USDT amount from your TxID.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("20", "50", "100", "500", "1000", "5000").forEach { chip ->
                                val isSelected = selectedAmountInput == chip
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyanAccent else SurfaceCardElevated)
                                        .clickable { selectedAmountInput = chip },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$$chip",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Or Enter Amount (USDT):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = selectedAmountInput,
                            onValueChange = { selectedAmountInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Text(
                                    "$",
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_amount_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F1B2F))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BSC BNB Smart Chain (BEP20)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = "Minimum guide: $${minDeposit.toInt()} USDT • Instant Auto Credit",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "BEP20",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (selectedAmountValue > 0.0) {
                                    currentStep = 2
                                }
                            },
                            enabled = selectedAmountValue > 0.0,
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
                                .testTag("deposit_next_button")
                        ) {
                            Text(
                                text = "Proceed to Payment ($${String.format(Locale.US, "%.2f", selectedAmountValue)} USDT)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // STEP 2: PAYMENT PROMPT + ADDRESS + QR + TXID ONLY
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!isVerifying) {
                                currentStep = 1
                                if (depositUiState is DepositUiState.Error) {
                                    onDismissResult()
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Change Amount",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Amount", fontSize = 11.sp)
                    }

                    Text(
                        text = "Selected: $${String.format(Locale.US, "%.2f", selectedAmountValue)} USDT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }

            // Payment Instruction Prompt Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2027)),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Make Payment",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Send $${String.format(Locale.US, "%.2f", selectedAmountValue)} USDT (BEP20) Now",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Transfer USDT on BNB Smart Chain (BEP20) to the address or QR code below, then submit your TxID.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // HARDCODED BINANCE BEP20 DEPOSIT ADDRESS & STATIC QR CARD
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("binance_deposit_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF8E9297))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Deposit USDT to Binance",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2329)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr_deposit_bep20),
                                contentDescription = "BEP20 USDT Deposit QR Code",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("deposit_qr_image")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Network",
                                fontSize = 13.sp,
                                color = Color(0xFF707A8A)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BSC",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E2329)
                                )
                                Text(
                                    text = "BNB Smart Chain (BEP20)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF707A8A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Address",
                                fontSize = 13.sp,
                                color = Color(0xFF707A8A),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("USDT Deposit Wallet", bscAddress))
                                        copyNotice = true
                                    },
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = bscAddress,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF1E2329),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .testTag("deposit_wallet_address")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("USDT Deposit Wallet", bscAddress))
                                        copyNotice = true
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("copy_deposit_address_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy BEP20 Wallet Address",
                                        tint = if (copyNotice) Color(0xFF0ECB81) else Color(0xFF707A8A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (copyNotice) {
                            Text(
                                text = "BEP20 USDT address copied to clipboard!",
                                fontSize = 11.sp,
                                color = Color(0xFF0ECB81),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Send only USDT on BNB Smart Chain (BEP20) to this address.",
                                fontSize = 11.sp,
                                color = Color(0xFF707A8A)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width / 2f, 0f)
                                    lineTo(size.width, size.height / 2f)
                                    lineTo(size.width / 2f, size.height)
                                    lineTo(0f, size.height / 2f)
                                    close()
                                }
                                drawPath(path, color = Color(0xFFF0B90B))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BINANCE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF0B90B),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // TXID SUBMISSION FORM (NO OPTIONAL AMOUNT INPUT)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Submit Transaction ID (TxID)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "BscScan Verification",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Paste your BSC transaction hash (TxID). Exact USDT sent will be verified on BscScan and credited automatically.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = txHashInput,
                            onValueChange = {
                                txHashInput = it
                                if (depositUiState is DepositUiState.Error) {
                                    onDismissResult()
                                }
                            },
                            label = { Text("Transaction ID / TxHash (0x...)", fontSize = 12.sp) },
                            placeholder = {
                                Text(
                                    "Paste 66-character BSC TxID",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            },
                            enabled = !isVerifying,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_tx_hash_input")
                        )

                        if (depositUiState is DepositUiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CoralRed.copy(alpha = 0.14f))
                                    .padding(12.dp)
                                    .testTag("deposit_error_banner"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Verification Error",
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = depositUiState.message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CoralRed,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                // Pass null for expectedAmount so the exact USDT received on blockchain is credited
                                onVerifyAndSubmitDeposit(txHashInput, null)
                            },
                            enabled = !isVerifying && txHashInput.isNotBlank(),
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
                                .testTag("submit_deposit_button")
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    color = CyanAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Verifying on BscScan...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            } else {
                                Text(
                                    text = "Verify TxID & Credit Balance",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // DEPOSIT HISTORY SECTION
        item {
            Text(
                text = "DEPOSIT HISTORY & VERIFICATION STATUS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (depositHistory.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No deposits yet. Select an amount above to start your BEP20 USDT deposit.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(depositHistory, key = { it.id }) { dep ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (dep.status) {
                                        "APPROVED" -> Icons.Default.CheckCircle
                                        "REJECTED" -> Icons.Default.Error
                                        else -> Icons.Default.HourglassTop
                                    },
                                    contentDescription = dep.status,
                                    tint = when (dep.status) {
                                        "APPROVED" -> EmeraldGreen
                                        "REJECTED" -> CoralRed
                                        else -> GoldAccent
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", dep.amount)} USDT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (dep.status) {
                                            "APPROVED" -> EmeraldGreen.copy(alpha = 0.15f)
                                            "REJECTED" -> CoralRed.copy(alpha = 0.15f)
                                            else -> GoldAccent.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (dep.status == "PENDING") "PROCESSING" else dep.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (dep.status) {
                                        "APPROVED" -> EmeraldGreen
                                        "REJECTED" -> CoralRed
                                        else -> GoldAccent
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "TxID: ${if (dep.txHash.length > 28) "${dep.txHash.take(16)}...${dep.txHash.takeLast(10)}" else dep.txHash}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BSC BEP20 (${dep.verificationType})",
                                fontSize = 11.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = dateFormat.format(Date(dep.timestamp)),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (depositUiState is DepositUiState.Success) {
        val dep = depositUiState.deposit
        AlertDialog(
            onDismissRequest = {
                currentStep = 1
                onDismissResult()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Deposit Approved!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "+$${String.format(Locale.US, "%.2f", dep.amount)} USDT credited instantly to your Available Balance.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCardElevated)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Credited:", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "$${String.format(Locale.US, "%.2f", dep.amount)} USDT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Network:", fontSize = 11.sp, color = TextMuted)
                            Text("BSC BEP20", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status:", fontSize = 11.sp, color = TextMuted)
                            Text("APPROVED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentStep = 1
                        onDismissResult()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }
}
