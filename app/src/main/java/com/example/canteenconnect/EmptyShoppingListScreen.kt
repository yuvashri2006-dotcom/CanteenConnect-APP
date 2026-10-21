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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EmptyListGreen = Color(0xFF173F1B)
private val EmptyListMuted = Color(0xFF667181)
private val EmptyListBorder = Color(0xFFE2E6E9)
private val EmptyListPage = Color(0xFFF7F8F9)
private val EmptyListPaleGreen = Color(0xFFE7F3E8)

/**
 * Empty shopping-list state matching the supplied CanteenConnect screenshot.
 * Connect Browse Products and View Saved Canteens to the app's existing navigation.
 */
@Composable
fun EmptyShoppingListScreen(
    onBack: () -> Unit,
    onBrowseProducts: () -> Unit,
    onViewSavedCanteens: () -> Unit,
    onNavigate: (String) -> Unit = {},
    canteenName: String = "Delhi Canteen",
    quotaRemaining: Int = 3000,
    quotaLimit: Int = 5500,
    itemCount: Int = 0
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = EmptyListGreen, fontSize = 27.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(12.dp))
            Text(
                "My Shopping List",
                modifier = Modifier.weight(1f),
                color = EmptyListGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text("⊙", color = EmptyListMuted, fontSize = 18.sp)
        }
        Divider(color = EmptyListBorder, thickness = 1.dp)

        // Station strip
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F2F4))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("▣  Station Canteen: $canteenName", color = EmptyListMuted,
                fontSize = 7.5.sp, modifier = Modifier.weight(1f))
            Surface(color = Color(0xFFE0E4E8), shape = RoundedCornerShape(5.dp)) {
                Text("$itemCount Items", color = EmptyListMuted, fontSize = 6.5.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
        }
        Divider(color = EmptyListBorder, thickness = 1.dp)

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(58.dp))

            // Empty basket illustration
            Box(
                modifier = Modifier.size(78.dp)
                    .background(Color(0xFFF4F6F4), CircleShape)
                    .border(1.5.dp, Color(0xFFCCD7CA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(Color.White, CircleShape)
                        .border(1.dp, EmptyListBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("♧", color = EmptyListGreen, fontSize = 29.sp)
                }
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                        .background(Color(0xFFFFF2D9), CircleShape)
                        .border(1.dp, Color(0xFFF0A52B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = Color(0xFF9A5700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(17.dp))
            Text(
                "You haven't added anything yet",
                color = Color(0xFF20252A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Search groceries or explore canteen inventory\nto plan your monthly quota allowance before\nvisiting.",
                color = EmptyListMuted,
                fontSize = 8.5.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Quota card
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                    .background(Color.White, RoundedCornerShape(9.dp))
                    .border(1.dp, EmptyListBorder, RoundedCornerShape(9.dp))
                    .padding(9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▣ Grocery Quota Status", color = Color(0xFF20252A),
                        fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Surface(color = EmptyListPaleGreen, shape = RoundedCornerShape(5.dp)) {
                        Text("Active Smart Card", color = EmptyListGreen, fontSize = 6.2.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Available Balance", color = EmptyListMuted, fontSize = 7.2.sp,
                        modifier = Modifier.weight(1f))
                    Text("₹${"%,d".format(quotaRemaining)}", color = EmptyListGreen,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(" / ₹${"%,d".format(quotaLimit)}", color = EmptyListMuted, fontSize = 7.sp)
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(5.dp)
                    .background(Color(0xFFE2E6E8), RoundedCornerShape(20.dp))) {
                    val usedFraction = if (quotaLimit > 0) (1f - quotaRemaining.toFloat() / quotaLimit).coerceIn(0f, 1f) else 0f
                    Box(Modifier.fillMaxWidth(usedFraction).height(5.dp)
                        .background(EmptyListGreen, RoundedCornerShape(20.dp)))
                }
                Spacer(Modifier.height(6.dp))
                Text("✓ ₹${"%,d".format(quotaRemaining)} grocery quota remaining for this month",
                    color = EmptyListMuted, fontSize = 7.sp)
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = onBrowseProducts,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp).height(34.dp),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmptyListGreen),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("⌕  Browse Products", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(7.dp))
            OutlinedButton(
                onClick = onViewSavedCanteens,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp).height(32.dp),
                shape = RoundedCornerShape(7.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("▤  View Saved Canteens", color = Color(0xFF394554), fontSize = 8.5.sp)
            }
        }

        // Bottom navigation
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .navigationBarsPadding().border(0.5.dp, EmptyListBorder)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmptyListNavItem("⌂", "Home", false) { onNavigate("Home") }
            EmptyListNavItem("▤", "Canteens", false) { onNavigate("Canteens") }
            EmptyListNavItem("♧", "Shop", true) { onNavigate("Shop") }
            EmptyListNavItem("◴", "History", false) { onNavigate("History") }
            EmptyListNavItem("♟", "Profile", false) { onNavigate("Profile") }
        }
    }
}

@Composable
private fun EmptyListNavItem(
    glyph: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = if (selected) EmptyListGreen else EmptyListMuted,
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) EmptyListGreen else EmptyListMuted,
            fontSize = 6.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (selected) Box(Modifier.padding(top = 1.dp).size(3.dp).background(EmptyListGreen, CircleShape))
    }
}
