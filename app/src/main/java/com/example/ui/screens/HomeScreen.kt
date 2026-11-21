package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.NftLevelEntity
import com.example.data.db.PlatformConfigEntity
import com.example.data.db.UserEntity
import com.example.data.model.CountryRegistry
import com.example.data.model.LeaderboardEarner
import com.example.data.model.LiveFeedTx
import com.example.data.model.LiveMarketDataGenerator
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    config: PlatformConfigEntity?,
    nftLevels: List<NftLevelEntity>,
    totalRegisteredUsers: Int,
    onNavigateTo: (Screen) -> Unit,
    onSelectNftLevel: (Int) -> Unit
) {
    var showCountriesDialog by remember { mutableStateOf(false) }
    var countrySearchQuery by remember { mutableStateOf("") }
    var showLeaderboardModal by remember { mutableStateOf(false) }
    var showAllTxsModal by remember { mutableStateOf(false) }

    val countries = CountryRegistry.supportedCountries
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    val available = currentUser?.availableBalance ?: 0.0
    val holding = currentUser?.holdingBalance ?: 0.0

    // Match exact screenshot registered users figure base (2,487,693)
    val registeredUsersCountString = "2,487,693"

    // Live Streaming Transactions Feed: exactly 30 transactions kept, 1 added every 2 seconds
    var liveTransactions by remember { mutableStateOf(LiveMarketDataGenerator.generateInitial30Transactions()) }
    var currentTickerIndex by remember { mutableIntStateOf(0) }

    // Dynamic Top Income Leaderboard: Top 100 earners sampled $590,100 down to $538,377
    var earnersList by remember { mutableStateOf(LiveMarketDataGenerator.generate100Earners()) }

    // Coroutine 1: Strictly 10-12 Deposit/Withdrawal transactions per minute (every 5 seconds)
    LaunchedEffect(Unit) {
        val types = listOf("DEPOSIT", "WITHDRAWAL", "DEPOSIT", "WITHDRAWAL")
        val networks = listOf("Binance Pay", "BSC BEP20", "Binance Auto Payout", "BSC BEP20")
        while (true) {
            delay(5000L) // 12 transactions per minute (every 5 seconds)
            val type = types.random()
            val amt = when (type) {
                "DEPOSIT" -> listOf(20.0, 50.0, 100.0, 250.0, 500.0, 1000.0, 2000.0).random()
                else -> listOf(35.0, 75.0, 120.0, 250.0, 600.0, 1200.0).random()
            }
            val newTx = LiveFeedTx(
                id = "TX-" + (100000..999999).random(),
                userMasked = "USR-" + (100..999).random() + "***",
                type = type,
                amount = amt,
                network = networks.random(),
                timeLabel = "Just now"
            )
            liveTransactions = (listOf(newTx) + liveTransactions).take(30)
            currentTickerIndex = (currentTickerIndex + 1) % 30
        }
    }

    // Coroutine 2: Dynamic Top 100 Income Leaderboard (Top 1 country changes every 24 hours)
    LaunchedEffect(Unit) {
        val rotatingFlags24h = listOf("🇨🇦", "🇺🇸", "🇧🇩")
        while (true) {
            delay(3500L) // every 3.5 seconds
            val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            val top1Country = rotatingFlags24h[currentDay % rotatingFlags24h.size]
            val updated = earnersList.toMutableList()
            val indicesToMutate = (0 until 100).shuffled().take((2..4).random())
            for (idx in indicesToMutate) {
                val item = updated[idx]
                val delta = (-5.0 + Math.random() * 22.0)
                val newIncome = (item.income + delta).coerceIn(530000.0, 610000.0)
                updated[idx] = item.copy(income = newIncome, delta = delta)
            }
            val sorted = updated.sortedByDescending { it.income }.mapIndexed { index, earner ->
                val flag = if (index == 0) top1Country else earner.countryFlag
                earner.copy(rank = index + 1, countryFlag = flag)
            }
            earnersList = sorted
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // 1. HERO ADVERTISEMENT CARD (Matching Screenshot)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1426)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF19253F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_ad_banner")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF091122),
                                    Color(0xFF0E1A33),
                                    Color(0xFF131D38)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Text Block
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "V-MAX —",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "PROFIT SHARE",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF00F0FF)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "PLATFORM",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }

                        // Right Cyber Monkey with Gold Visor Artwork
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .aspectRatio(1.05f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            MonkeyArtwork(level = 2, size = 135.dp)
                        }
                    }
                }
            }
        }

        // 1.5. 24/7 RUNNING WITHDRAW/DEPOSIT TICKER
        item {
            val activeTx = liveTransactions.getOrNull(currentTickerIndex) ?: liveTransactions[0]
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF091222)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF162C4E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAllTxsModal = true }
                    .testTag("live_tx_ticker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "● LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldGreen
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${activeTx.userMasked} ${if (activeTx.type == "DEPOSIT") "deposited" else if (activeTx.type == "WITHDRAWAL") "withdrew" else "claimed"} $${String.format(Locale.US, "%.2f", activeTx.amount)} USDT (${activeTx.network})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeTx.type == "DEPOSIT") "+$${activeTx.amount.toInt()}" else if (activeTx.type == "WITHDRAWAL") "-$${activeTx.amount.toInt()}" else "+$${activeTx.amount.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (activeTx.type == "WITHDRAWAL") CoralRed else EmeraldGreen
                    )
                }
            }
        }

        // 2. AVAILABLE BALANCE & HOLDING BALANCE CARD (Matching Screenshot)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF17233D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_action_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left: Available Balance & Deposit Button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Available Balance",
                                fontSize = 12.sp,
                                color = Color(0xFF8696B0),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format(Locale.US, "%,.2f", available)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "USDT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8696B0)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Solid Cyan Deposit Button
                            Button(
                                onClick = { onNavigateTo(Screen.DEPOSIT) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00F0FF),
                                    contentColor = Color(0xFF070B14)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("shortcut_deposit")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
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
                            }
                        }

                        // Right: Holding Balance & Withdraw Button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Holding Balance",
                                fontSize = 12.sp,
                                color = Color(0xFF8696B0),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format(Locale.US, "%,.2f", holding)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "USDT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8696B0)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Cyan Outlined Withdraw Button
                            OutlinedButton(
                                onClick = { onNavigateTo(Screen.WITHDRAW) },
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00F0FF)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("shortcut_withdraw")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.North,
                                        contentDescription = "Withdraw",
                                        tint = Color(0xFF00F0FF),
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
            }
        }

        // 2.5 PROMINENT CENTER BUTTON: TOP WITHDRAWALS LEADERBOARD (MATCHING USER DEMAND)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B30)),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFB800), Color(0xFF00F0FF), Color(0xFF10B981))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.TOP_LEADERBOARD) }
                    .testTag("center_top_withdrawals_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF13223D), Color(0xFF0C172B))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFB800), Color(0xFFFF8A00))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Top Withdrawals",
                                    tint = Color(0xFF080E1C),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TOP WITHDRAWALS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.5.sp,
                                        color = Color(0xFFFFB800)
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
                                            text = "AUTOMATIC",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Live Automatic Withdrawals • $1,428,950+ Today",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Right prominent 'TOP' button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00F0FF), Color(0xFF00A3FF))
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TOP",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color(0xFF080E1C)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF080E1C),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. STATS CARD (Supported Countries & Registered Users)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF17233D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Supported Countries (Clickable)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCountriesDialog = true }
                            .testTag("supported_countries_card"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A2B3B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Supported Countries",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Supported Countries",
                                fontSize = 11.sp,
                                color = Color(0xFF8696B0),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "124+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "supported countries",
                                fontSize = 10.sp,
                                color = Color(0xFF5D6E87)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: Registered Users
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("registered_users_card"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A2B3B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Registered Users",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Registered Users",
                                fontSize = 11.sp,
                                color = Color(0xFF8696B0),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = registeredUsersCountString,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "total registered accounts",
                                fontSize = 10.sp,
                                color = Color(0xFF5D6E87)
                            )
                        }
                    }
                }
            }
        }

        // 4. REFERRAL REWARDS BANNER (Matching Screenshot)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1C38)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF193B6E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.REFERRAL) }
                    .testTag("referral_rewards_banner")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0B2144),
                                    Color(0xFF081833),
                                    Color(0xFF061124)
                                )
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Blue Group Badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0E2E5C))
                                .border(1.dp, Color(0xFF225AA6), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Referral",
                                tint = Color(0xFF00D2FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "REFERRAL REWARDS (5%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Earn 5% bonus every time your referred friends deposit",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Circuit accent & arrow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(28.dp, 16.dp)) {
                                drawLine(
                                    color = Color(0xFF00D2FF).copy(alpha = 0.4f),
                                    start = Offset(0f, size.height * 0.5f),
                                    end = Offset(size.width * 0.7f, size.height * 0.5f),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawLine(
                                    color = Color(0xFF00D2FF).copy(alpha = 0.4f),
                                    start = Offset(size.width * 0.7f, size.height * 0.5f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Go",
                                tint = Color(0xFF00D2FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. LIVE TRANSACTIONS FEED (Deposit & Withdrawal)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Transactions Feed",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0D253B))
                        .clickable { showAllTxsModal = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "View All History",
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(liveTransactions.take(5), key = { it.id }) { tx ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF17233D)),
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (tx.type == "WITHDRAWAL") CoralRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tx.type == "WITHDRAWAL") Icons.Default.North else Icons.Default.South,
                                contentDescription = null,
                                tint = if (tx.type == "WITHDRAWAL") CoralRed else EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = tx.userMasked, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF16233B))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(tx.type, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (tx.type == "WITHDRAWAL") CoralRed else EmeraldGreen)
                                }
                            }
                            Text(text = "${tx.network} • ${tx.timeLabel}", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (tx.type == "WITHDRAWAL") "-" else "+"}$${String.format(Locale.US, "%.2f", tx.amount)} USDT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (tx.type == "WITHDRAWAL") CoralRed else EmeraldGreen
                        )
                        Text(text = "Auto-Settled", fontSize = 9.sp, color = TextMuted)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // 124+ Supported Countries Modal Dialog
    if (showCountriesDialog) {
        val filtered = if (countrySearchQuery.isBlank()) countries else countries.filter {
            it.name.contains(countrySearchQuery, ignoreCase = true) || it.code.contains(countrySearchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showCountriesDialog = false },
            title = {
                Column {
                    Text(
                        text = "124+ Supported Countries",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Genuinely supported countries across global regions",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.height(350.dp)) {
                    OutlinedTextField(
                        value = countrySearchQuery,
                        onValueChange = { countrySearchQuery = it },
                        placeholder = { Text("Search country...", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filtered) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCardElevated)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = item.flagEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = item.dialCode,
                                    fontSize = 11.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCountriesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Modal: Stream of All 30 Live Transactions (1 tx every 2s)
    if (showAllTxsModal) {
        AlertDialog(
            onDismissRequest = { showAllTxsModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Live Transactions (30 Txs/Min)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Continuous 24/7 stream • 1 transaction every 2 seconds",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.height(380.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(liveTransactions, key = { it.id }) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCardElevated)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = tx.userMasked, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (tx.type == "WITHDRAWAL") CoralRed.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(tx.type, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (tx.type == "WITHDRAWAL") CoralRed else EmeraldGreen)
                                        }
                                    }
                                    Text(text = "${tx.network} • ${tx.timeLabel}", fontSize = 10.sp, color = TextMuted)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (tx.type == "WITHDRAWAL") "-" else "+"}$${String.format(Locale.US, "%.2f", tx.amount)} USDT",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (tx.type == "WITHDRAWAL") CoralRed else EmeraldGreen
                                    )
                                    Text(text = "Completed", fontSize = 9.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAllTxsModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Modal: Full Top 100 Income Leaderboard ($590,100 to $538,377)
    if (showLeaderboardModal) {
        var earnerQuery by remember { mutableStateOf("") }
        val filteredEarners = if (earnerQuery.isBlank()) earnersList else earnersList.filter {
            it.handle.contains(earnerQuery, ignoreCase = true) || it.rank.toString() == earnerQuery
        }

        AlertDialog(
            onDismissRequest = { showLeaderboardModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Top 100 Income Earners",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Dynamic ranking ($590,100 down to $538,377) • Updates live",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.height(420.dp)) {
                    OutlinedTextField(
                        value = earnerQuery,
                        onValueChange = { earnerQuery = it },
                        placeholder = { Text("Search rank or username...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filteredEarners, key = { it.rank }) { earner ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (earner.rank <= 3) Color(0xFF16253E) else SurfaceCardElevated
                                    )
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${earner.rank}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when (earner.rank) {
                                            1 -> GoldAccent
                                            2 -> Color(0xFFE2E8F0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Text(text = earner.countryFlag, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = earner.handle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "Tier ${earner.avatarLevel} Collector", fontSize = 9.sp, color = TextMuted)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${numberFormatter.format(earner.income.toInt())}.00",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (earner.delta >= 0) "+$${String.format(Locale.US, "%.2f", earner.delta)} USDT" else "-$${String.format(Locale.US, "%.2f", -earner.delta)} USDT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (earner.delta >= 0) EmeraldGreen else CoralRed
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLeaderboardModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }
}


