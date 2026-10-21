package com.example.canteenconnect

import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val ProfileForest = Color(0xFF123817)
private val ProfileGreen = Color(0xFF2F6B3A)
private val ProfilePaleGreen = Color(0xFFE6F2E7)
private val ProfilePage = Color(0xFFF6F8FA)
private val ProfileMuted = Color(0xFF647080)
private val ProfileBorder = Color(0xFFE1E6E3)

@Composable
fun ProfileScreen(
    prefs: SharedPreferences,
    onBack: () -> Unit
) {
    val name = prefs.getString("account_name", "Arun Kumar")
        ?.takeIf { it.isNotBlank() } ?: "Arun Kumar"
    val email = prefs.getString("account_email", "arun@example.com")
        ?.takeIf { it.isNotBlank() } ?: "arun@example.com"
    val initials = name.trim().split(Regex("\\s+"))
        .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("").ifBlank { "AK" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfilePage)
    ) {
        // Compact app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Text("‹", color = ProfileForest, fontSize = 28.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "CanteenConnect",
                color = ProfileForest,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text("⊙", color = ProfileForest, fontSize = 20.sp, modifier = Modifier.padding(end = 7.dp))
        }
        HorizontalDivider(color = ProfileBorder, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 9.dp, vertical = 10.dp)
        ) {
            Surface(
                color = Color(0xFFE9EEEA),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "  ◉  IDENTITY & ENTITLEMENT VERIFICATION  ",
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                    color = Color(0xFF45554A),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.25.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Confirm your profile",
                color = Color(0xFF202823),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "Verify your authorised canteen eligibility details before\naccessing inventory and scheduling store pick-ups.",
                color = ProfileMuted,
                fontSize = 8.5.sp,
                lineHeight = 12.sp
            )
            Spacer(Modifier.height(10.dp))

            // Verified identity card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProfileBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFE9ECEA), RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, color = ProfileForest, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(7.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    name,
                                    color = Color(0xFF222B25),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("●", color = ProfileGreen, fontSize = 9.sp)
                            }
                            Text("Primary Dependent", color = ProfileMuted, fontSize = 8.sp)
                            Text("Card", color = ProfileMuted, fontSize = 8.sp)
                        }
                        Surface(
                            color = ProfilePaleGreen,
                            shape = RoundedCornerShape(3.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0E6D2))
                        ) {
                            Text(
                                "✓  Defence Card Holder\n    (Active)",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
                                color = ProfileForest,
                                fontSize = 7.sp,
                                lineHeight = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = ProfileBorder)
                    Spacer(Modifier.height(8.dp))
                    DetailRow("Canteen Smart Card ID", "▣  DEF-88291-K", "VERIFIED")
                    Spacer(Modifier.height(6.dp))
                    DetailRow("Card Category", "Service Personnel Dependent /\nEx-Servicemen", null)
                    Spacer(Modifier.height(6.dp))
                    DetailRow("Attached Station", "Tambaram URC\n(Air Force Station)", null)
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFF5F7F8),
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ProfileBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▣  Station Code: TAM-AF-04", color = ProfileMuted, fontSize = 8.sp)
                            Spacer(Modifier.weight(1f))
                            Text("Sub-Depot Direct", color = ProfileGreen, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quota categories card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProfileBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        "Eligible Quota Categories",
                        color = Color(0xFF222B25),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Monthly quota limits for fiscal\nallocation", color = ProfileMuted, fontSize = 8.sp, lineHeight = 10.sp)
                    Spacer(Modifier.height(7.dp))
                    HorizontalDivider(color = ProfileBorder)
                    Spacer(Modifier.height(7.dp))

                    QuotaRow("Grocery Quota", "₹7,000 / month authorised limit", "Active", true)
                    Spacer(Modifier.height(6.dp))
                    QuotaRow("Household & Toiletries", "Combined limit with monthly dry rations", "Authorised", true)
                    Spacer(Modifier.height(6.dp))
                    QuotaRow("General Store Rations", "Standard subsidised civilian MRP rebate\naccess", "Eligible", true)

                    Spacer(Modifier.height(7.dp))
                    Surface(
                        color = Color(0xFFEDEFEF),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("ⓘ", color = ProfileForest, fontSize = 10.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Entitlement limits refresh on the 1st of every\ncalendar month. Smart card verification is\nrequired at physical POS billing counter.",
                                color = Color(0xFF4E5852),
                                fontSize = 7.5.sp,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(34.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfileForest),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("Continue to Dashboard  →", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }

        // Bottom navigation to match the reference image
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(top = 5.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("⌂", "Home", false, onBack)
            BottomNavItem("▤", "Canteens", false, onBack)
            BottomNavItem("♧", "Shop", false, onBack)
            BottomNavItem("◴", "History", false, onBack)
            BottomNavItem("♟", "Profile", true, {})
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, badge: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, modifier = Modifier.weight(0.9f), color = ProfileMuted, fontSize = 7.5.sp)
        Spacer(Modifier.width(5.dp))
        Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
            Text(value, color = Color(0xFF252E28), fontSize = 7.5.sp, lineHeight = 10.sp)
            if (badge != null) {
                Spacer(Modifier.height(2.dp))
                Surface(color = Color(0xFFE8ECEA), shape = RoundedCornerShape(3.dp)) {
                    Text(badge, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), color = ProfileMuted, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuotaRow(title: String, description: String, status: String, active: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF5F7F5),
        shape = RoundedCornerShape(5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6EAE6))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(13.dp).background(ProfilePaleGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (active) "✓" else "i", color = ProfileGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color(0xFF26302A), fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                Text(description, color = ProfileMuted, fontSize = 7.2.sp, lineHeight = 9.sp)
            }
            Spacer(Modifier.width(3.dp))
            Surface(
                color = if (status == "Active") ProfilePaleGreen else Color(0xFFEDEFF0),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(status, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = if (status == "Active") ProfileGreen else ProfileMuted, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    glyph: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = glyph,
            color = if (selected) ProfileForest else ProfileMuted,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) ProfileForest else ProfileMuted, fontSize = 7.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
