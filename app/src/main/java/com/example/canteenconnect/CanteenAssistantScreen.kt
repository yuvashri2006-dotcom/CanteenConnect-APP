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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

private val AssistantGreen = Color(0xFF17421D)
private val AssistantGreen2 = Color(0xFF285C2D)
private val AssistantPage = Color(0xFFF6F7F8)
private val AssistantMuted = Color(0xFF657181)
private val AssistantBorder = Color(0xFFE0E5E8)
private val AssistantPaleGreen = Color(0xFFE7F3E8)

private data class AssistantGrocery(
    val name: String,
    val price: Int
)

/**
 * Canteen assistant screen styled to match the supplied screenshot.
 * This is a UI demo: responses and quota figures are sample values.
 */
@Composable
fun CanteenAssistantScreen(
    onBack: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var message by remember { mutableStateOf("") }
    var conversation by remember {
        mutableStateOf(
            listOf(
                "assistant" to "Hello Arun! I can help you plan your grocery shopping to ensure you stay within your ₹3,000 monthly entitlement before visiting the canteen.",
                "user" to "I have ₹3,000 remaining. Help me plan my groceries."
            )
        )
    }

    val groceries = listOf(
        AssistantGrocery("Rice (5kg Bag)", 850),
        AssistantGrocery("Cooking Oil (1L)", 600),
        AssistantGrocery("Toor Dal (1kg)", 250),
        AssistantGrocery("Refined Sugar (1kg)", 200),
        AssistantGrocery("Detergent Powder (2kg)", 400)
    )
    val total = groceries.sumOf { it.price }
    val remaining = 3000 - total

    Column(
        modifier = Modifier.fillMaxSize().background(AssistantPage)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, AssistantBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = AssistantGreen, fontSize = 27.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Canteen Assistant", color = AssistantGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Plan your shopping within your demo balance.", color = AssistantMuted, fontSize = 7.sp)
            }
            Text("⊙", color = AssistantMuted, fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = onOpenProfile))
        }

        // Entitlement strip
        Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 11.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("● Monthly Entitlement Quota", color = Color(0xFF30373D), fontSize = 7.5.sp,
                    modifier = Modifier.weight(1f))
                Text("₹3,000 left", color = AssistantGreen, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFFE0E5E8), RoundedCornerShape(20.dp))) {
                Box(Modifier.fillMaxWidth(0.44f).height(5.dp).background(AssistantGreen2, RoundedCornerShape(20.dp)))
            }
            Spacer(Modifier.height(3.dp))
            Text("Used: ₹2,500     Balance: ₹3,000 grocery quota remaining",
                color = AssistantMuted, fontSize = 6.5.sp)
        }

        // Conversation area
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            conversation.forEachIndexed { index, entry ->
                if (entry.first == "assistant") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        AssistantAvatar()
                        Spacer(Modifier.width(7.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(0.88f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 9.dp, bottomEnd = 9.dp, bottomStart = 9.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AssistantBorder)
                            ) {
                                Text(entry.second, modifier = Modifier.padding(9.dp),
                                    color = Color(0xFF30363B), fontSize = 8.5.sp, lineHeight = 12.sp)
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(if (index == 0) "09:41 AM" else "09:42 AM",
                                color = AssistantMuted, fontSize = 6.sp)
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.86f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Surface(
                                color = AssistantGreen,
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 3.dp, bottomEnd = 8.dp, bottomStart = 8.dp)
                            ) {
                                Text(entry.second, modifier = Modifier.padding(9.dp),
                                    color = Color.White, fontSize = 8.5.sp, lineHeight = 12.sp)
                            }
                            Spacer(Modifier.height(3.dp))
                            Text("09:42 AM", color = AssistantMuted, fontSize = 6.sp)
                        }
                    }
                }
            }

            Text("SUGGESTED PROMPTS", color = AssistantMuted, fontSize = 6.5.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp,
                modifier = Modifier.padding(start = 27.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 27.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                PromptChip("“I have ₹3,000 remaining.”") {
                    message = "I have ₹3,000 remaining. Help me plan my groceries."
                }
                PromptChip("“Help me create a grocery list.”") {
                    message = "Help me create a grocery list."
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                AssistantAvatar()
                Spacer(Modifier.width(7.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AssistantBorder),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("◉  Tambaram Unit Run Canteen", color = AssistantGreen,
                                fontSize = 7.2.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Surface(color = AssistantPaleGreen, shape = RoundedCornerShape(3.dp)) {
                                Text("IN STOCK", color = AssistantGreen, fontSize = 5.8.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Based on sample products available at Tambaram Canteen, here is a suggested shopping plan:",
                            color = Color(0xFF30363B), fontSize = 7.5.sp, lineHeight = 10.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF7F8F9),
                            shape = RoundedCornerShape(5.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AssistantBorder)
                        ) {
                            Column(Modifier.padding(7.dp)) {
                                groceries.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("•  ${item.name}", color = Color(0xFF3A424A),
                                            fontSize = 7.3.sp, modifier = Modifier.weight(1f),
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("₹${item.price}", color = Color(0xFF20252A), fontSize = 7.3.sp)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                androidx.compose.material3.HorizontalDivider(color = AssistantBorder)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Estimated Total:", color = AssistantMuted, fontSize = 7.2.sp,
                                        modifier = Modifier.weight(1f))
                                    Text("₹${"%,d".format(total)}", color = Color(0xFF20252A),
                                        fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Surface(color = AssistantPaleGreen, shape = RoundedCornerShape(4.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("Estimated Remaining:", color = AssistantGreen, fontSize = 7.2.sp,
                                            modifier = Modifier.weight(1f))
                                        Text("₹${"%,d".format(remaining)}", color = AssistantGreen,
                                            fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(3.dp))
                                        Text("(Safe buffer)", color = AssistantGreen, fontSize = 6.sp)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = onOpenShoppingList,
                            modifier = Modifier.fillMaxWidth().height(31.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AssistantGreen),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
                        ) {
                            Text("☷  Add All to Shopping List", color = Color.White,
                                fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Chat composer
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, AssistantBorder)
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f).height(43.dp),
                placeholder = { Text("What do you want to plan?...", fontSize = 8.sp, color = AssistantMuted) },
                singleLine = true,
                shape = RoundedCornerShape(7.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AssistantGreen,
                    unfocusedBorderColor = Color(0xFFCCD3D7),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                modifier = Modifier.size(34.dp).clickable {
                    if (message.isNotBlank()) {
                        conversation = conversation + ("user" to message.trim())
                        conversation = conversation + ("assistant" to "I can help you plan within your ₹3,000 demo grocery balance. Your sample list totals ₹${"%,d".format(total)}, leaving ₹${"%,d".format(remaining)}. Tap “Add All to Shopping List” to continue.")
                        message = ""
                    }
                },
                color = AssistantGreen,
                shape = RoundedCornerShape(5.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("➤", color = Color.White, fontSize = 15.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .navigationBarsPadding().border(0.5.dp, AssistantBorder).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistantNav("⌂", "Home", false) { onNavigate("Home") }
            AssistantNav("▤", "Canteens", false) { onNavigate("Canteens") }
            AssistantNav("♧", "Shop", true) { onNavigate("Shop") }
            AssistantNav("◴", "History", false) { onNavigate("History") }
            AssistantNav("♟", "Profile", false) { onNavigate("Profile") }
        }
    }
}

@Composable
private fun AssistantAvatar() {
    Box(
        modifier = Modifier.size(19.dp).background(AssistantGreen, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("✿", color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun PromptChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD5DCCF))
    ) {
        Text(text, color = Color(0xFF424B43), fontSize = 6.7.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AssistantNav(glyph: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = if (selected) AssistantGreen else AssistantMuted,
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) AssistantGreen else AssistantMuted,
            fontSize = 6.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (selected) Box(Modifier.padding(top = 1.dp).size(3.dp).background(AssistantGreen, CircleShape))
    }
}
