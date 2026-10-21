package com.example.canteenconnect
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NoticeGreen = Color(0xFF17421D)
private val NoticeMuted = Color(0xFF687382)
private val NoticeBorder = Color(0xFFDCE2DE)
private val NoticePage = Color(0xFFF7F8F9)
private val NoticePaleGreen = Color(0xFFE7F3E8)

private data class NoticeItem(
    val category: String,
    val title: String,
    val body: String,
    val time: String,
    val icon: String,
    val accent: Color,
    val tag: String? = null
)

/**
 * Notifications screen styled after the provided CanteenConnect reference.
 * Notifications and timestamps are demo content.
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenNotification: (String) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val notifications = listOf(
        NoticeItem(
            category = "Stock Alerts",
            title = "Your saved item is available",
            body = "Rice (5kg Bag) is currently marked available at Tambaram Canteen.",
            time = "15m ago",
            icon = "▣",
            accent = Color(0xFFE4F2E5),
            tag = "● Stock Alert • Demo notification"
        ),
        NoticeItem(
            category = "Quota Updates",
            title = "Monthly entitlement updated",
            body = "Your September entitlement information has been updated. ₹3,000 balance remaining.",
            time = "Yesterday",
            icon = "▣",
            accent = Color(0xFFDDE6F8)
        ),
        NoticeItem(
            category = "All",
            title = "Shopping list ready",
            body = "Your saved shopping list is ready to check before your upcoming canteen visit.",
            time = "2 days ago",
            icon = "☷",
            accent = Color(0xFFE8ECEB)
        ),
        NoticeItem(
            category = "All",
            title = "Visit reminder",
            body = "Check your canteen information before visiting. Tambaram Canteen closes at 8:00 PM today.",
            time = "Sep 22",
            icon = "◷",
            accent = Color(0xFFFFEFC9)
        )
    )

    val readIndices = remember { mutableStateListOf<Int>() }
    val selectedTab = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("All") }
    val filtered = when (selectedTab.value) {
        "Stock Alerts" -> notifications.filter { it.category == "Stock Alerts" }
        "Quota Updates" -> notifications.filter { it.category == "Quota Updates" }
        else -> notifications
    }

    Column(Modifier.fillMaxSize().background(NoticePage)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, NoticeBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = NoticeGreen, fontSize = 26.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(10.dp))
            Text("Notifications", color = NoticeGreen, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                "Mark all as read",
                color = NoticeGreen,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { readIndices.clear(); readIndices.addAll(notifications.indices) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 11.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            listOf("All", "Stock Alerts", "Quota Updates").forEach { tab ->
                val selected = selectedTab.value == tab
                Surface(
                    modifier = Modifier.clickable { selectedTab.value = tab },
                    color = if (selected) NoticeGreen else Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) NoticeGreen else NoticeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tab, color = if (selected) Color.White else NoticeMuted,
                            fontSize = 7.2.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        if (tab == "All") {
                            Spacer(Modifier.width(4.dp))
                            Surface(color = if (selected) Color(0xFF315C35) else Color(0xFFE7EAED), shape = CircleShape) {
                                Text("${notifications.size}", color = if (selected) Color.White else NoticeMuted,
                                    fontSize = 6.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            filtered.forEach { item ->
                val index = notifications.indexOf(item)
                val isRead = index in readIndices
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, if (!isRead && index == 0) Color(0xFFC4D8C5) else NoticeBorder, RoundedCornerShape(8.dp))
                        .clickable { readIndices.add(index); onOpenNotification(item.title) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(25.dp).background(item.accent, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.icon, color = NoticeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(item.title, color = Color(0xFF252B31), fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(4.dp))
                            Text(item.time, color = NoticeMuted, fontSize = 6.2.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(item.body, color = Color(0xFF59636C), fontSize = 7.6.sp, lineHeight = 11.sp)
                        if (item.tag != null) {
                            Spacer(Modifier.height(5.dp))
                            Surface(color = NoticePaleGreen, shape = RoundedCornerShape(4.dp)) {
                                Text(item.tag, color = NoticeGreen, fontSize = 6.1.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text("›", color = NoticeGreen, fontSize = 15.sp)
                            }
                        }
                        if (isRead) {
                            Spacer(Modifier.height(3.dp))
                            Text("Read", color = NoticeMuted, fontSize = 5.8.sp)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .navigationBarsPadding().border(0.5.dp, NoticeBorder)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NoticeNavItem("⌂", "Home", true) { onNavigate("Home") }
            NoticeNavItem("▤", "Canteens", false) { onNavigate("Canteens") }
            NoticeNavItem("♧", "Shop", false) { onNavigate("Shop") }
            NoticeNavItem("◴", "History", false) { onNavigate("History") }
            NoticeNavItem("♟", "Profile", false) { onNavigate("Profile") }
        }
    }
}

@Composable
private fun NoticeNavItem(glyph: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = if (selected) NoticeGreen else NoticeMuted,
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) NoticeGreen else NoticeMuted,
            fontSize = 6.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
