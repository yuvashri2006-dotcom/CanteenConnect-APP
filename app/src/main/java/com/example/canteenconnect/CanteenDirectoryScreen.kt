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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DirectoryGreen = Color(0xFF17451F)
private val DirectoryMuted = Color(0xFF667383)
private val DirectoryBorder = Color(0xFFDCE2E0)
private val DirectoryPage = Color(0xFFF6F7F8)
private val DirectoryPaleGreen = Color(0xFFE7F3E8)

private data class DirectoryCanteen(
    val name: String,
    val distance: String,
    val openHours: String,
    val savedItems: Int,
    val match: Int,
    val isSelected: Boolean = false
)

/**
 * Find a Canteen directory UI based on the supplied reference.
 * Map, distances, opening hours and inventory are demo content, not live GPS data.
 */
@Composable
fun CanteenDirectoryScreen(
    onBack: () -> Unit,
    onViewDetails: (String) -> Unit,
    onDirections: (String) -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("Chennai Garrison Zone") }
    var nearbyOnly by remember { mutableStateOf(true) }
    var openNowOnly by remember { mutableStateOf(false) }
    var sortByDistance by remember { mutableStateOf(true) }

    val canteens = listOf(
        DirectoryCanteen("Tambaram Canteen", "4.2 km", "8 AM - 8 PM", 18, 100, true),
        DirectoryCanteen("Avadi Canteen", "8.7 km", "Open", 12, 67),
        DirectoryCanteen("St. Thomas Mount Canteen", "11.5 km", "Open", 8, 54)
    )
    val visibleCanteens = canteens.filter {
        it.name.contains(searchText, ignoreCase = true) ||
                searchText.equals("Chennai Garrison Zone", ignoreCase = true) ||
                searchText.isBlank()
    }.let { if (sortByDistance) it.sortedBy { c -> c.distance.substringBefore(" ").toDoubleOrNull() ?: 999.0 } else it }

    Column(Modifier.fillMaxSize().background(DirectoryPage)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, DirectoryBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = DirectoryGreen, fontSize = 26.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(10.dp))
            Text("Find a Canteen", color = Color(0xFF20252A),
                fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Surface(color = Color(0xFFE9EDF1), shape = RoundedCornerShape(12.dp)) {
                Text("LIVE DB", color = DirectoryMuted, fontSize = 6.5.sp,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("⊙", color = DirectoryMuted, fontSize = 17.sp)
        }

        // Search and filters
        Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 10.dp, vertical = 7.dp)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().height(39.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 8.5.sp, color = Color(0xFF303841)),
                placeholder = { Text("Search canteen or station", fontSize = 8.sp) },
                leadingIcon = { Text("⌕", color = DirectoryMuted, fontSize = 16.sp) },
                trailingIcon = {
                    Text("⊗", color = DirectoryMuted, fontSize = 13.sp,
                        modifier = Modifier.clickable { searchText = "" }.padding(end = 7.dp))
                },
                shape = RoundedCornerShape(7.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DirectoryGreen,
                    unfocusedBorderColor = Color(0xFFD4DADF),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                DirectoryFilter("➤ Nearby (<10km)", nearbyOnly) { nearbyOnly = !nearbyOnly }
                DirectoryFilter("Open Now", openNowOnly) { openNowOnly = !openNowOnly }
            }
        }

        // Stylized map panel (illustrative only)
        Box(
            modifier = Modifier.fillMaxWidth().height(151.dp)
                .background(Color(0xFFE2EAE5))
        ) {
            Box(Modifier.fillMaxWidth().height(42.dp).align(Alignment.TopStart)
                .background(Color(0xFFDDE7E1)))
            Box(Modifier.fillMaxWidth(0.72f).height(48.dp).align(Alignment.BottomStart)
                .background(Color(0xFFD5E2D9)))
            Box(Modifier.fillMaxWidth().height(7.dp).align(Alignment.Center)
                .background(Color(0xFFB7D0E0)))
            Box(Modifier.fillMaxWidth().height(2.dp).align(Alignment.Center)
                .background(Color(0xFF91B5CA))
            )
            Row(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f).height(2.dp).background(Color(0xFFFFD14A)))
                Box(Modifier.weight(1f).height(2.dp).background(Color(0xFFFFD14A)))
            }
            // Diagonal route is represented as short dashed-style segments.
            Row(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(9) {
                    Box(Modifier.width(12.dp).height(2.dp).background(Color(0xFFE9A800)))
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 10.dp),
                color = Color(0xFFF7F8F5), shape = RoundedCornerShape(3.dp)
            ) {
                Text("AIR FORCE STATION", color = DirectoryMuted, fontSize = 5.5.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
            }
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 12.dp),
                color = Color.White, shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DirectoryBorder)
            ) {
                Text("Avadi (8.7\nkm)", color = Color(0xFF303841), fontSize = 6.5.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
            }
            Surface(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp, top = 8.dp),
                color = Color.White, shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DirectoryBorder)
            ) {
                Text("● St. Thomas (11.5 km)", color = Color(0xFF303841), fontSize = 6.2.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
            }
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(top = 5.dp),
                color = DirectoryGreen, shape = RoundedCornerShape(4.dp),
                shadowElevation = 2.dp
            ) {
                Text("▦ Tambaram URC", color = Color.White, fontSize = 7.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp))
            }
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("+", "−", "⌾").forEach {
                    Surface(color = Color.White, shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, DirectoryBorder)) {
                        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                            Text(it, color = DirectoryGreen, fontSize = 15.sp)
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 9.dp, bottom = 8.dp),
                color = Color.White, shape = RoundedCornerShape(8.dp)
            ) {
                Text("● GPS Accurate to 15m", color = DirectoryMuted, fontSize = 5.8.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 42.dp, bottom = 8.dp),
                color = Color(0xFFF7F8F5), shape = RoundedCornerShape(3.dp)
            ) {
                Text("DEFENCE COMPLEX", color = DirectoryMuted, fontSize = 5.4.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
            }
        }

        // List header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("AUTHORIZED CANTEENS NEARBY", color = DirectoryMuted,
                    fontSize = 7.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                Text("(${visibleCanteens.size})", color = DirectoryMuted, fontSize = 7.sp)
            }
            Text("☷", color = DirectoryGreen, fontSize = 11.sp,
                modifier = Modifier.clickable { sortByDistance = !sortByDistance })
            Spacer(Modifier.width(8.dp))
            Text(if (sortByDistance) "Sort by\nDistance" else "Default\norder",
                color = DirectoryGreen, fontSize = 6.5.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { sortByDistance = !sortByDistance })
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (visibleCanteens.isEmpty()) {
                Text("No canteens match your search.", color = DirectoryMuted, fontSize = 9.sp,
                    modifier = Modifier.padding(14.dp))
            }
            visibleCanteens.forEach { canteen ->
                val selected = canteen.name == "Tambaram Canteen"
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onViewDetails(canteen.name) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        if (selected) 1.2.dp else 0.8.dp,
                        if (selected) DirectoryGreen else DirectoryBorder
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(canteen.name, color = Color(0xFF20252A), fontSize = 9.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (selected) {
                                Surface(color = DirectoryPaleGreen, shape = RoundedCornerShape(4.dp)) {
                                    Text("Selected", color = DirectoryGreen, fontSize = 5.8.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                                }
                                Spacer(Modifier.width(5.dp))
                                Box(Modifier.size(17.dp).background(DirectoryPaleGreen, CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Text("✓", color = DirectoryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(Modifier.size(19.dp).border(1.dp, DirectoryBorder, CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Text("›", color = DirectoryMuted, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♧ ${canteen.distance} away", color = DirectoryMuted, fontSize = 6.5.sp,
                                modifier = Modifier.weight(1f))
                            Surface(color = DirectoryPaleGreen, shape = RoundedCornerShape(5.dp)) {
                                Text("● Open (${canteen.openHours})", color = DirectoryGreen, fontSize = 6.2.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF5F7F8),
                            shape = RoundedCornerShape(5.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, DirectoryBorder)
                        ) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("▣  ${canteen.savedItems} saved items available", color = DirectoryMuted,
                                    fontSize = 6.5.sp, modifier = Modifier.weight(1f))
                                Text("${canteen.match}% Match", color = DirectoryGreen,
                                    fontSize = 6.3.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (selected) {
                            Spacer(Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onViewDetails(canteen.name) },
                                    modifier = Modifier.weight(1f).height(29.dp),
                                    shape = RoundedCornerShape(5.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DirectoryGreen),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
                                ) {
                                    Text("ⓘ  View Details", color = Color.White, fontSize = 7.3.sp)
                                }
                                OutlinedButton(
                                    onClick = { onDirections(canteen.name) },
                                    modifier = Modifier.weight(1f).height(29.dp),
                                    shape = RoundedCornerShape(5.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
                                ) {
                                    Text("◇  Directions", color = DirectoryGreen, fontSize = 7.3.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
        }

        // Bottom navigation
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, DirectoryBorder).padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectoryNav("⌂", "Home", false) { onNavigate("Home") }
            DirectoryNav("▤", "Canteens", true) { onNavigate("Canteens") }
            DirectoryNav("♧", "Shop", false) { onNavigate("Shop") }
            DirectoryNav("◴", "History", false) { onNavigate("History") }
            DirectoryNav("♟", "Profile", false) { onNavigate("Profile") }
        }
    }
}

@Composable
private fun DirectoryFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) DirectoryGreen else Color(0xFFF0F2F4),
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (selected) DirectoryGreen else DirectoryBorder)
    ) {
        Text(label, color = if (selected) Color.White else DirectoryMuted,
            fontSize = 6.5.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
private fun DirectoryNav(glyph: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = if (selected) DirectoryGreen else DirectoryMuted,
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) DirectoryGreen else DirectoryMuted,
            fontSize = 6.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}