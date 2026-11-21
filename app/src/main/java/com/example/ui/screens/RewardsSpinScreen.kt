package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SpinHistoryEntity
import com.example.data.db.UserEntity
import com.example.data.model.SpinWheelConfig
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SpinUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RewardsSpinScreen(
    currentUser: UserEntity?,
    spinUiState: SpinUiState,
    spinHistory: List<SpinHistoryEntity>,
    onPlaySpin: () -> Unit,
    onSpinAnimationFinished: () -> Unit,
    onDismissResult: () -> Unit,
    onNavigateTo: (Screen) -> Unit
) {
    val tier100Spins = currentUser?.tier100Spins ?: 0
    val tier1000Spins = currentUser?.tier1000Spins ?: 0
    val totalSpinsAvailable = tier100Spins + tier1000Spins
    val totalWinnings = currentUser?.totalSpinWinnings ?: 0.0
    val isSpinning = spinUiState is SpinUiState.Spinning

    val wheelRotation = remember { Animatable(0f) }
    var bulbPhase by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    // Blinking rim marquee lights (faster while spinning)
    LaunchedEffect(isSpinning) {
        while (true) {
            bulbPhase = (bulbPhase + 1) % 2
            delay(if (isSpinning) 120L else 550L)
        }
    }

    // Drive smooth deceleration animation whenever a spin starts
    LaunchedEffect(spinUiState) {
        if (spinUiState is SpinUiState.Spinning) {
            val segCount = SpinWheelConfig.segments.size
            val sweep = 360f / segCount
            val targetIdx = spinUiState.outcome.segmentIndex
            // Slice i is drawn starting at (-90 + i * sweep + rotation).
            // Its center aligns with top pointer (-90 deg) when (i * sweep + sweep / 2 + rotation) % 360 == 0.
            val targetMod = (360f - (targetIdx * sweep + sweep / 2f) + 360f) % 360f
            val currentMod = ((wheelRotation.value % 360f) + 360f) % 360f
            val deltaToTarget = (targetMod - currentMod + 360f) % 360f
            val jitter = (-6..6).random() * 0.5f // stays well within +-18 deg slice
            val finalAngle = wheelRotation.value + (360f * 7f) + deltaToTarget + jitter

            wheelRotation.animateTo(
                targetValue = finalAngle,
                animationSpec = tween(
                    durationMillis = 4200,
                    easing = CubicBezierEasing(0.12f, 0.85f, 0.22f, 1.0f)
                )
            )
            onSpinAnimationFinished()
        }
    }

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
                    Text(
                        text = "LUCKY SPIN LOTTERY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Deposit $100+ USDT for 3 Spins • Deposit $1,000+ for 6 Spins",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldAccent.copy(alpha = 0.16f))
                        .border(1.dp, GoldAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$totalSpinsAvailable Spins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }
            }
        }

        // Eligibility Tier Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(
                        1.dp,
                        if (tier100Spins > 0) CyanAccent else BorderDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "$100+ DEPOSIT TIER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$tier100Spins Spins Left",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "3 Lucky Spins / Deposit\nWin Up to $999,999",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF201604)),
                    border = BorderStroke(
                        1.dp,
                        if (tier1000Spins > 0) GoldAccent else BorderDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "$1,000+ VIP TIER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$tier1000Spins Spins Left",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "6 VIP Spins / Deposit\nGrand Jackpot Pool",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Animated Lottery Wheel Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF101930), Color(0xFF0A1020))
                            )
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRIZE WHEEL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Total Won: $${String.format(Locale.US, "%.2f", totalWinnings)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wheel Container with Top Pointer & Center Hub
                    Box(
                        modifier = Modifier
                            .widthIn(max = 310.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val segments = SpinWheelConfig.segments
                        val currentRot = wheelRotation.value

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .testTag("lucky_spin_wheel_canvas")
                        ) {
                            val diameter = size.minDimension
                            val radius = diameter / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val sweep = 360f / segments.size

                            // Outer glowing bezel ring
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFD54F), Color(0xFFB45309)),
                                    center = center,
                                    radius = radius + 14.dp.toPx()
                                ),
                                radius = radius + 8.dp.toPx(),
                                center = center
                            )

                            // Rotating slices
                            val sliceRadius = radius - 6.dp.toPx()
                            val topLeft = Offset(center.x - sliceRadius, center.y - sliceRadius)
                            val arcSize = Size(sliceRadius * 2f, sliceRadius * 2f)

                            segments.forEachIndexed { idx, seg ->
                                val startDeg = -90f + idx * sweep + currentRot
                                val fillColor = Color(seg.accentColorHex).copy(
                                    alpha = if (idx % 2 == 0) 0.92f else 0.80f
                                )
                                drawArc(
                                    color = fillColor,
                                    startAngle = startDeg,
                                    sweepAngle = sweep,
                                    useCenter = true,
                                    topLeft = topLeft,
                                    size = arcSize
                                )
                                drawArc(
                                    color = Color(0xFF0A0F1E),
                                    startAngle = startDeg,
                                    sweepAngle = sweep,
                                    useCenter = true,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Draw prize label oriented radially inside the slice
                                val midAngle = startDeg + sweep / 2f
                                rotate(degrees = midAngle + 90f, pivot = center) {
                                    drawIntoCanvas { canvas ->
                                        val paint = Paint().apply {
                                            isAntiAlias = true
                                            color = android.graphics.Color.WHITE
                                            textAlign = Paint.Align.CENTER
                                            textSize = when {
                                                seg.label.length >= 7 -> 9.5.sp.toPx()
                                                seg.label.length >= 6 -> 10.5.sp.toPx()
                                                seg.label.length >= 5 -> 11.5.sp.toPx()
                                                else -> 13.5.sp.toPx()
                                            }
                                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                            setShadowLayer(4f, 0f, 1.5f, android.graphics.Color.BLACK)
                                        }
                                        val labelY = center.y - sliceRadius * 0.71f
                                        canvas.nativeCanvas.drawText(
                                            seg.label,
                                            center.x,
                                            labelY,
                                            paint
                                        )
                                    }
                                }
                            }

                            // 20 Animated Marquee Lights around outer bezel
                            val bulbCount = 20
                            val bulbRingRadius = radius + 2.dp.toPx()
                            for (b in 0 until bulbCount) {
                                val angleRad = Math.toRadians((b * (360.0 / bulbCount)))
                                val bx = center.x + (bulbRingRadius * cos(angleRad)).toFloat()
                                val by = center.y + (bulbRingRadius * sin(angleRad)).toFloat()
                                val lit = (b + bulbPhase) % 2 == 0
                                drawCircle(
                                    color = if (lit) Color.White else Color(0xFF78350F),
                                    radius = if (lit) 3.5.dp.toPx() else 2.5.dp.toPx(),
                                    center = Offset(bx, by)
                                )
                            }
                        }

                        // Fixed Top Pointer Triangle (12 o'clock)
                        Canvas(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(30.dp)
                        ) {
                            val ptr = Path().apply {
                                moveTo(size.width * 0.5f, size.height)
                                lineTo(0f, 0f)
                                lineTo(size.width, 0f)
                                close()
                            }
                            drawPath(ptr, color = Color(0xFFFFD54F))
                            drawPath(ptr, color = Color.White, style = Stroke(width = 1.5.dp.toPx()))
                        }

                        // Center Interactive Spin Hub
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFE082), Color(0xFFF59E0B), Color(0xFFB45309))
                                    )
                                )
                                .border(3.dp, Color.White, CircleShape)
                                .clickable(enabled = totalSpinsAvailable > 0 && !isSpinning) {
                                    onPlaySpin()
                                }
                                .testTag("wheel_center_spin_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSpinning) "..." else "SPIN",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B0E17)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (totalSpinsAvailable > 0) {
                        Button(
                            onClick = onPlaySpin,
                            enabled = !isSpinning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color.Black,
                                disabledContainerColor = Color(0xFF334155),
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("spin_wheel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpinning) {
                                    "Spinning Lucky Wheel..."
                                } else {
                                    "Spin Lucky Wheel ($totalSpinsAvailable Left)"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCardElevated)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Deposit $100+ USDT to Unlock 3 Lucky Spins\nDeposit $1,000+ USDT to Unlock 6 VIP Spins • Win Up to $999,999!",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onNavigateTo(Screen.DEPOSIT) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("spin_unlock_deposit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Deposit $100+ Now to Unlock", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Prize History Header
        item {
            Text(
                text = "MY LUCKY SPIN HISTORY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (spinHistory.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "No spins played yet. Deposit $100+ USDT to unlock your Lucky Spins and win instant USDT rewards!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(spinHistory, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Win",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Won ${item.prizeLabel} USDT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = if (item.tierLabel == "1000_TIER") "$1,000+ VIP Tier" else "$100+ Deposit Tier",
                                    fontSize = 11.sp,
                                    color = CyanAccent
                                )
                            }
                        }
                        Text(
                            text = dateFormat.format(Date(item.timestamp)),
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Celebration Win Modal
    if (spinUiState is SpinUiState.Completed) {
        val outcome = spinUiState.outcome
        AlertDialog(
            onDismissRequest = onDismissResult,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Congratulations! You Won!",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = GoldAccent,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "+$${String.format(Locale.US, "%.2f", outcome.prizeAmount)} USDT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Added directly to your Available Balance!",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Remaining Spins: ${outcome.remainingTier100Spins + outcome.remainingTier1000Spins}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissResult,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("spin_win_dismiss_button")
                ) {
                    Text("Collect & Continue", fontWeight = FontWeight.Black)
                }
            },
            containerColor = SurfaceCard
        )
    }
}
