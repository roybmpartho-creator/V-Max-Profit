package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveFeedTx
import com.example.data.model.LiveMarketDataGenerator
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TopLeaderboardScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Dynamic streaming withdrawal transactions
    var withdrawalsList by remember {
        mutableStateOf(
            listOf(
                LiveFeedTx("W-99120", "USR-882***91", "WITHDRAWAL", 1250.0, "BSC BEP20", "Just now"),
                LiveFeedTx("W-99119", "USR-104***33", "WITHDRAWAL", 840.0, "BSC BEP20", "6s ago"),
                LiveFeedTx("W-99118", "USR-551***82", "WITHDRAWAL", 2500.0, "BSC BEP20", "12s ago"),
                LiveFeedTx("W-99117", "USR-342***17", "WITHDRAWAL", 420.0, "BSC BEP20", "18s ago"),
                LiveFeedTx("W-99116", "USR-992***44", "WITHDRAWAL", 1780.0, "BSC BEP20", "24s ago"),
                LiveFeedTx("W-99115", "USR-771***05", "WITHDRAWAL", 630.0, "BSC BEP20", "30s ago"),
                LiveFeedTx("W-99114", "USR-401***99", "WITHDRAWAL", 300.0, "BSC BEP20", "36s ago"),
                LiveFeedTx("W-99113", "USR-619***12", "WITHDRAWAL", 1920.0, "BSC BEP20", "42s ago"),
                LiveFeedTx("W-99112", "USR-208***54", "WITHDRAWAL", 510.0, "BSC BEP20", "48s ago"),
                LiveFeedTx("W-99111", "USR-841***70", "WITHDRAWAL", 980.0, "BSC BEP20", "54s ago")
            )
        )
    }

    // Cumulative Total Paid Out Today - increments live with each streamed withdrawal!
    var totalPaidOutToday by remember { mutableDoubleStateOf(142895.0) }
    var lastRecordedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) }

    // 24-Hour Rotating Country Cycle (Canada 🇨🇦, America 🇺🇸, Bangladesh 🇧🇩)
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val rotatingFlags = listOf("🇨🇦", "🇺🇸", "🇧🇩")
    val initialTopCountry = rotatingFlags[dayOfYear % rotatingFlags.size]
    val initialSecondCountry = rotatingFlags[(dayOfYear + 1) % rotatingFlags.size]
    val initialThirdCountry = rotatingFlags[(dayOfYear + 2) % rotatingFlags.size]

    // Top highest leaderboard earners - dynamically increasing, #1 country rotates every 24 hours!
    var topEarners by remember {
        mutableStateOf(
            LiveMarketDataGenerator.generate100Earners().mapIndexed { idx, item ->
                when (idx) {
                    0 -> item.copy(countryFlag = initialTopCountry)
                    1 -> item.copy(countryFlag = initialSecondCountry)
                    2 -> item.copy(countryFlag = initialThirdCountry)
                    else -> item
                }
            }
        )
    }

    // Automatic stream coroutine: new withdrawal every 6 seconds
    LaunchedEffect(Unit) {
        val amounts = listOf(150.0, 320.0, 500.0, 750.0, 1200.0, 2400.0, 3800.0, 5100.0, 8900.0)
        while (true) {
            delay(6000L)
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            if (currentDay != lastRecordedDay) {
                lastRecordedDay = currentDay
                totalPaidOutToday = 0.0 // Daily reset
            }

            val newAmount = amounts.random()
            totalPaidOutToday += newAmount

            val newTx = LiveFeedTx(
                id = "W-" + (10000..99999).random(),
                userMasked = "USR-" + (100..999).random() + "***" + (10..99).random(),
                type = "WITHDRAWAL",
                amount = newAmount,
                network = "BSC BEP20",
                timeLabel = "Just now"
            )
            withdrawalsList = (listOf(newTx) + withdrawalsList).take(30)
        }
    }

    // Live Leaderboard Earner Increases (Amounts increase live, Top 1 Country rotates every 24 hours)
    LaunchedEffect(Unit) {
        val boostAmounts = listOf(2300.0, 2500.0, 7700.0, 3200.0, 4800.0, 6200.0, 7700.0)
        val rotatingFlags24h = listOf("🇨🇦", "🇺🇸", "🇧🇩")

        while (true) {
            delay(3500L)
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val boost = boostAmounts.random()
            val targetIdx = (0..8).random()
            val currentList = topEarners.toMutableList()

            if (currentList.isNotEmpty() && targetIdx < currentList.size) {
                val earner = currentList[targetIdx]
                val updatedEarner = earner.copy(
                    income = earner.income + boost,
                    delta = boost
                )
                currentList[targetIdx] = updatedEarner

                // Re-sort descending so amounts and ranks change live
                val sorted = currentList.sortedByDescending { it.income }

                // The #1 position country changes after 24 hours based on calendar day
                val top1Country = rotatingFlags24h[currentDay % rotatingFlags24h.size]
                val top2Country = rotatingFlags24h[(currentDay + 1) % rotatingFlags24h.size]
                val top3Country = rotatingFlags24h[(currentDay + 2) % rotatingFlags24h.size]

                topEarners = sorted.mapIndexed { idx, item ->
                    val flag = when (idx) {
                        0 -> top1Country   // Remains fixed for the entire 24h day!
                        1 -> top2Country
                        2 -> top3Country
                        else -> item.countryFlag
                    }
                    item.copy(rank = idx + 1, countryFlag = flag)
                }
            }
        }
    }

    // Pulse animation for live badge
    val transition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val filteredWithdrawals = withdrawalsList.filter {
        it.userMasked.contains(searchQuery, ignoreCase = true) ||
            it.network.contains(searchQuery, ignoreCase = true) ||
            it.amount.toString().contains(searchQuery)
    }

    val filteredTopEarners = topEarners.filter {
        it.handle.contains(searchQuery, ignoreCase = true) ||
            it.rank.toString().contains(searchQuery)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar & Back Button
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TOP WITHDRAWALS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFFFFB800)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0C2B22))
                                .border(0.5.dp, EmeraldGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AUTOMATIC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                        }
                    }
                    Text(
                        text = "Live Automatic Crypto Payouts & Global Leaderboard",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Live Metric Cards Row (Clean, without any "10/min" display)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1628)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B2B48)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = alphaAnim))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE PAYOUT FEED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }

                        Text(
                            text = "Streaming Active",
                            fontSize = 10.sp,
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Paid Out Today", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${numberFormatter.format(totalPaidOutToday.toInt())}.00",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text("USDT BSC BEP20 (Daily Reset)", fontSize = 10.sp, color = CyanAccent)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Auto Payout", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instant",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                            Text("Verified 24/7", fontSize = 10.sp, color = EmeraldGreen)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Success Rate", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "100%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                            Text("Zero Failures", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Tabs: Live Stream vs Top 100 Earners
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceCard,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 0) CoralRed else GoldAccent,
                        height = 3.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = if (selectedTab == 0) CoralRed else TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Withdrawals", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = if (selectedTab == 1) GoldAccent else TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Top 100 Leaderboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (selectedTab == 0) "Search by wallet/user ID or network..." else "Search top earner rank or username...",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_search_input")
            )
        }

        if (selectedTab == 0) {
            // Live Streaming Withdrawals List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● Streaming Live Payouts (Showing ${filteredWithdrawals.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Live Automatic Blockchain Stream",
                        fontSize = 10.sp,
                        color = EmeraldGreen
                    )
                }
            }

            items(filteredWithdrawals, key = { it.id }) { tx ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1829)),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1B2C4B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CoralRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.North,
                                    contentDescription = "Withdrawal",
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.userMasked,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF0C2B22))
                                            .border(0.5.dp, EmeraldGreen, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PAID AUTO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${tx.network} • ${tx.timeLabel}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-$${numberFormatter.format(tx.amount.toInt())}.00",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CoralRed
                            )
                            Text(
                                text = "USDT Sent",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        } else {
            // Top 100 Leaderboard List with live increments and rank switching
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 Top 100 Profit Share Earners",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "● Live Updating",
                        fontSize = 10.sp,
                        color = EmeraldGreen
                    )
                }
            }

            items(filteredTopEarners, key = { it.handle }) { earner ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (earner.rank <= 3) Color(0xFF16253F) else Color(0xFF0F1829)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (earner.rank == 1) 1.5.dp else 0.5.dp,
                        color = when (earner.rank) {
                            1 -> GoldAccent
                            2 -> Color(0xFFE2E8F0)
                            3 -> Color(0xFFCD7F32)
                            else -> Color(0xFF1B2C4B)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${earner.rank}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = when (earner.rank) {
                                    1 -> GoldAccent
                                    2 -> Color(0xFFE2E8F0)
                                    3 -> Color(0xFFCD7F32)
                                    else -> TextSecondary
                                },
                                modifier = Modifier.width(36.dp)
                            )
                            Text(text = earner.countryFlag, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = earner.handle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (earner.rank == 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Tier ${earner.avatarLevel} V-Max Share Earner",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${numberFormatter.format(earner.income.toInt())}.00",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (earner.delta > 0) "+$${numberFormatter.format(earner.delta.toInt())}.00 USDT" else "+$2,300.00 USDT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
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
}
