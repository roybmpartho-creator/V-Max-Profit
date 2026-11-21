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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Info
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
import com.example.data.db.HoldingEntity
import com.example.data.db.NftLevelEntity
import com.example.data.db.UserEntity
import com.example.ui.components.MonkeyArtwork
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun NftMarketplaceScreen(
    currentUser: UserEntity?,
    nftLevels: List<NftLevelEntity>,
    activeHoldings: List<HoldingEntity>,
    onPurchaseNft: (Int) -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All Levels, 1: Active Levels, 2: Available to Purchase
    var detailsDialogNft by remember { mutableStateOf<NftLevelEntity?>(null) }
    var confirmPurchaseNft by remember { mutableStateOf<NftLevelEntity?>(null) }

    val activeLevels = remember(activeHoldings) {
        activeHoldings.map { it.level }.toSet()
    }

    val filteredList = when (selectedFilter) {
        1 -> nftLevels.filter { activeLevels.contains(it.level) }
        2 -> nftLevels.filter { !activeLevels.contains(it.level) }
        else -> nftLevels
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Title
            Text(
                text = "V-Max Share & Task Marketplace",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = "Select a Share level to earn guaranteed daily profit",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Filter Chips Row (All Levels, Active Levels, Available to Purchase)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = "All Levels",
                    isSelected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 }
                )
                FilterChip(
                    label = "Active Levels",
                    isSelected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 }
                )
                FilterChip(
                    label = "Available to Buy",
                    isSelected = selectedFilter == 2,
                    onClick = { selectedFilter = 2 }
                )
            }
        }

        // Amber Legal / Target Return Disclaimer Box
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A11)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF523E1C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Target Return Disclaimer",
                        tint = GoldAccent,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "V-Max Profit Share returns are credited daily. Each Share runs for a duration of 180 Days. You can claim daily profits instantly into your account balance.",
                        fontSize = 11.sp,
                        color = Color(0xFFD4B16A),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Subheader: 9 levels • One active holding per level
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "9 Share Levels",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "One active holding per level",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Share Level Cards
        items(filteredList, key = { it.level }) { nft ->
            val isActiveHoldingForThisLevel = activeLevels.contains(nft.level)
            val canAfford = (currentUser?.availableBalance ?: 0.0) >= nft.investmentPrice

            // Daily profit is investmentPrice * targetReturnRate (matches MarketplaceRepository.purchaseNft)
            val dailyProfit = nft.investmentPrice * nft.targetReturnRate

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1426)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActiveHoldingForThisLevel) GoldAccent.copy(alpha = 0.5f) else Color(0xFF19253F)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_card_level_${nft.level}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Avatar (88x88)
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        MonkeyArtwork(level = nft.level, size = 88.dp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Information and Actions
                    Column(modifier = Modifier.weight(1f)) {
                        // Title + Level + Info Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nft.name, // e.g. V-Max Share Level 1
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Buy $${nft.investmentPrice.toInt()} | Daily Profit $${String.format(Locale.US, "%.2f", dailyProfit)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }

                            IconButton(
                                onClick = { detailsDialogNft = nft },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Details",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Stats Row: Investment | Daily Profit | Duration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Investment", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "$${nft.investmentPrice.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(text = "Daily Profit", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "+$${String.format(Locale.US, "%.2f", dailyProfit)} / day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                            Column {
                                Text(text = "Duration", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "${nft.holdingDays} Days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Row: Status Pill + Purchase button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status Pill
                            if (isActiveHoldingForThisLevel) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoldAccent.copy(alpha = 0.15f))
                                        .border(1.dp, GoldAccent, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "In Holding",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.15f))
                                        .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Available",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            }

                            // Purchase Button
                            Button(
                                onClick = { confirmPurchaseNft = nft },
                                enabled = !isActiveHoldingForThisLevel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Color(0xFF090D16),
                                    disabledContainerColor = Color(0xFF263554),
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("purchase_share_level_${nft.level}")
                            ) {
                                Text(
                                    text = if (isActiveHoldingForThisLevel) "Active" else "Buy Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
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

    // Purchase Confirmation Dialog
    confirmPurchaseNft?.let { nft ->
        val canAfford = (currentUser?.availableBalance ?: 0.0) >= nft.investmentPrice
        val dailyProfit = nft.investmentPrice * nft.targetReturnRate
        val totalProfit = dailyProfit * nft.holdingDays

        AlertDialog(
            onDismissRequest = { confirmPurchaseNft = null },
            title = {
                Text(
                    text = "Confirm Share Purchase",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are activating Level ${nft.level}: ${nft.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Buy Price: $${nft.investmentPrice.toInt()} (Deducted from Available Balance and placed into Holding Balance)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• Daily Profit: +$${String.format(Locale.US, "%.2f", dailyProfit)} (Claimable once every 24 hours directly to profile balance)",
                        fontSize = 11.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Duration: ${nft.holdingDays} Days (Total Cycle Profit: +$${String.format(Locale.US, "%.2f", totalProfit)})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• One Active Share: Only one active contract of Level ${nft.level} can run simultaneously.",
                        fontSize = 10.sp,
                        color = GoldAccent
                    )

                    if (!canAfford) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Insufficient Available Balance ($${String.format(Locale.US, "%.2f", currentUser?.availableBalance ?: 0.0)} USDT). Please make a USDT deposit first.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPurchaseNft(nft.level)
                        confirmPurchaseNft = null
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Confirm Purchase", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmPurchaseNft = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Share Details Dialog
    detailsDialogNft?.let { nft ->
        val dailyProfit = nft.investmentPrice * nft.targetReturnRate
        val totalProfit = dailyProfit * nft.holdingDays
        AlertDialog(
            onDismissRequest = { detailsDialogNft = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Level ${nft.level}: ${nft.name}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        MonkeyArtwork(level = nft.level, size = 120.dp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = nft.description, fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Buy Price: $${nft.investmentPrice.toInt()}", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                    Text(text = "Daily Profit: +$${String.format(Locale.US, "%.2f", dailyProfit)} / day", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    Text(text = "Duration: ${nft.holdingDays} Days", fontSize = 11.sp, color = TextPrimary)
                    Text(text = "Total Cycle Profit: +$${String.format(Locale.US, "%.2f", totalProfit)} (${(nft.targetReturnRate * 100).toInt()}% Daily)", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { detailsDialogNft = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF133242) else Color(0xFF0F182A))
            .border(
                1.dp,
                if (isSelected) CyanAccent else Color(0xFF1E2B45),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CyanAccent else TextSecondary
        )
    }
}
