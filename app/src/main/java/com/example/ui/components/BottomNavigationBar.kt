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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.Screen

data class NavItem(
    val screen: Screen,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
)

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    activeHoldingCount: Int,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        NavItem(Screen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavItem(Screen.CLAIM, "Claim", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard, "nav_claim"),
        NavItem(Screen.TOP_LEADERBOARD, "Top", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_top"),
        NavItem(Screen.NFT, "Task", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, "nav_task"),
        NavItem(Screen.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, ambientColor = Color.Black, spotColor = Color.Black)
            .background(Color(0xFF070B14))
            .border(width = 0.5.dp, color = Color(0xFF16233B))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen

                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(item.screen) }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BadgedBox(
                        badge = {
                            if (item.screen == Screen.CLAIM && activeHoldingCount > 0) {
                                Badge(
                                    containerColor = GoldAccent,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = activeHoldingCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                            contentDescription = item.title,
                            tint = if (isSelected) CyanAccent else Color(0xFF708099),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanAccent else Color(0xFF708099)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Small indicator dot under selected tab
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .background(CyanAccent, CircleShape)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(3.5.dp))
                    }
                }
            }
        }
    }
}
