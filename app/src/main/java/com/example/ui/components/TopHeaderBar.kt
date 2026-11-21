package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserEntity
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
fun TopHeaderBar(
    currentScreen: Screen,
    currentUser: UserEntity?,
    onNavigateBack: () -> Unit,
    onNavigateTo: (Screen) -> Unit
) {
    val isRootScreen = currentScreen in listOf(Screen.HOME, Screen.CLAIM, Screen.NFT, Screen.PROFILE)
    var showNotificationsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF070B14))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Hexagon Brand Logo & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateTo(Screen.HOME) }
            ) {
                if (!isRootScreen) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Geometric Cyan Hexagon Icon
                HexagonLogo(modifier = Modifier.size(32.dp))

                Spacer(modifier = Modifier.width(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "V-Max ",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "PROFIT SHARE",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        color = CyanAccent
                    )
                }
            }

            // Right: Notification Bell (with red badge 3) + You pill button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Admin Panel Quick Shortcut strictly for authenticated Admin sessions
                if (currentUser?.role == "ADMIN") {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f))
                            .border(1.dp, GoldAccent, CircleShape)
                            .clickable { onNavigateTo(Screen.ADMIN_PANEL) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Notification Bell with Badge 3
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { showNotificationsDialog = true }
                        .testTag("notification_bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = CoralRed,
                                contentColor = Color.White,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Text(
                                    text = "3",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // "You >" Pill Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0C2433))
                        .border(1.dp, Color(0xFF145366), RoundedCornerShape(20.dp))
                        .clickable { onNavigateTo(Screen.PROFILE) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("header_you_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(CyanAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = Color(0xFF070B14),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "You",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Profile",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Text(
                    text = "Announcements & Alerts",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotificationItem(
                        title = "Lucky Spin Lottery Live",
                        time = "Just now",
                        desc = "Deposit $100+ USDT (BEP20) for 3 Lucky Spins or $1,000+ for 6 VIP Spins in Profile Rewards.",
                        color = EmeraldGreen
                    )
                    NotificationItem(
                        title = "BEP20 Auto Verification Live",
                        time = "1 hour ago",
                        desc = "BSC BNB Smart Chain (BEP20) instant TxID verification is active for USDT deposits.",
                        color = CyanAccent
                    )
                    NotificationItem(
                        title = "New 9-Level V-Max Shares",
                        time = "3 hours ago",
                        desc = "Explore Level 1 to Level 9 profit sharing tiers with daily profit claims and 180-day durations.",
                        color = GoldAccent
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun NotificationItem(title: String, time: String, desc: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCardElevated)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
            Text(text = time, fontSize = 10.sp, color = TextMuted)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
    }
}

@Composable
fun HexagonLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer hexagon path
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.75f)
            lineTo(0f, h * 0.25f)
            close()
        }

        // Draw hexagon border in Cyan
        drawPath(
            path = path,
            color = Color(0xFF00F0FF),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Inner glowing cube / center hexagon
        val innerPath = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.38f)
            lineTo(w * 0.75f, h * 0.62f)
            lineTo(w * 0.5f, h * 0.75f)
            lineTo(w * 0.25f, h * 0.62f)
            lineTo(w * 0.25f, h * 0.38f)
            close()
        }
        drawPath(
            path = innerPath,
            color = Color(0xFF00F0FF).copy(alpha = 0.5f)
        )
    }
}
