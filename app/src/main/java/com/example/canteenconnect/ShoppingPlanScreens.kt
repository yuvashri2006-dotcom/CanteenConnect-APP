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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val PlanGreen = Color(0xFF19451E)
private val PlanSoftGreen = Color(0xFFE7F3E8)
private val PlanPage = Color(0xFFF6F7F9)
private val PlanMuted = Color(0xFF657080)
private val PlanBorder = Color(0xFFE1E5E9)
private val PlanOrange = Color(0xFFFFF0C7)
private val PlanRed = Color(0xFFB3261E)

private fun planMoney(value: Double): String =
    "₹" + String.format(Locale("en", "IN"), "%,.0f", value)

/**
 * Shopping list screen based on the supplied design.
 * Connect the callbacks to the existing app navigation and shopping-list state.
 */
@Composable
fun ShoppingListPlannerScreen(
    items: List<ShoppingItem>,
    quotaRemaining: Double = 3000.0,
    onBack: () -> Unit,
    onCheckAvailability: () -> Unit,
    onAddMoreProducts: () -> Unit,
    onContinue: () -> Unit,
    onItemSelectionChanged: (String, Boolean) -> Unit = { _, _ -> }
) {
    val selectedNames = remember(items) {
        mutableStateListOf<String>().apply {
            addAll(items.map { it.product.name })
        }
    }
    val total = items.sumOf { it.product.price * it.quantity }
    val selectedTotal = items.filter { it.product.name in selectedNames }
        .sumOf { it.product.price * it.quantity }
    val remainingAfter = (quotaRemaining - selectedTotal).coerceAtLeast(0.0)

    Column(Modifier.fillMaxSize().background(PlanPage)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, PlanBorder)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = PlanGreen, fontSize = 27.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("My Shopping List", color = Color(0xFF22272C),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Estimate before you visit", color = PlanMuted, fontSize = 7.5.sp)
            }
            Text("▣", color = PlanMuted, fontSize = 15.sp)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlanBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▣ Budget Estimator", color = Color(0xFF22272C),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(color = PlanSoftGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("● Grocery Quota", modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                color = PlanGreen, fontSize = 6.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        BudgetTile("Estimated Total", planMoney(selectedTotal), Modifier.weight(1f), Color(0xFFF5F6F7))
                        BudgetTile("Current Quota", planMoney(quotaRemaining), Modifier.weight(1f), Color(0xFFF5F6F7))
                        BudgetTile("Est. Remaining", planMoney(remainingAfter), Modifier.weight(1f), Color(0xFFF0FAF0))
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Monthly Cap Used: ${planMoney((quotaRemaining - remainingAfter).coerceAtLeast(0.0))} of ${planMoney(quotaRemaining)}",
                            color = PlanMuted, fontSize = 6.5.sp, modifier = Modifier.weight(1f))
                        Text(if (quotaRemaining > 0) "${((selectedTotal / quotaRemaining).coerceIn(0.0, 1.0) * 100).toInt()}% Allocated" else "0% Allocated",
                            color = PlanGreen, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFFE2E5E8), RoundedCornerShape(20.dp))) {
                        Box(Modifier.fillMaxWidth(if (quotaRemaining > 0) (selectedTotal / quotaRemaining).coerceIn(0.0, 1.0).toFloat() else 0f)
                            .height(5.dp).background(PlanGreen, RoundedCornerShape(20.dp)))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Planned Items", color = Color(0xFF20252A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Surface(color = Color(0xFFE9EDF1), shape = RoundedCornerShape(8.dp)) {
                    Text("${items.size} items", color = PlanMuted, fontSize = 6.5.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Uncheck all", color = PlanGreen, fontSize = 7.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        selectedNames.clear()
                        items.forEach { onItemSelectionChanged(it.product.name, false) }
                    })
            }

            if (items.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text("Your shopping list is empty. Add products from Shop.",
                        modifier = Modifier.padding(14.dp), color = PlanMuted, fontSize = 9.sp)
                }
            } else {
                items.forEach { item ->
                    val checked = item.product.name in selectedNames
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PlanBorder),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(15.dp)
                                    .background(if (checked) PlanGreen else Color.White, RoundedCornerShape(3.dp))
                                    .border(1.dp, if (checked) PlanGreen else PlanBorder, RoundedCornerShape(3.dp))
                                    .clickable {
                                        if (checked) selectedNames.remove(item.product.name)
                                        else selectedNames.add(item.product.name)
                                        onItemSelectionChanged(item.product.name, !checked)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(7.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.product.name, color = Color(0xFF20252A), fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Pack: ${item.product.unit} • Brand: Sample brand",
                                    color = PlanMuted, fontSize = 6.3.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(5.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(planMoney(item.product.price * item.quantity), color = Color(0xFF20252A),
                                    fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                Text("₹${(item.product.price * 1.2).toInt()}",
                                    color = PlanMuted, fontSize = 6.sp)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onCheckAvailability,
                modifier = Modifier.fillMaxWidth().height(33.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlanGreen),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("◉  Check Availability at Tambaram Canteen", color = Color.White, fontSize = 8.sp)
            }
            OutlinedButton(
                onClick = onAddMoreProducts,
                modifier = Modifier.fillMaxWidth().height(30.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("+ Add More Products", color = Color(0xFF20252A), fontSize = 8.sp)
            }
        }
        PlanBottomNavigation(selected = "Shop")
    }
}

/**
 * Review screen shown after the shopper checks their plan.
 */
@Composable
fun ShoppingPlanVerificationScreen(
    items: List<ShoppingItem>,
    quotaRemaining: Double = 3000.0,
    onReadyToGo: () -> Unit,
    onEditShoppingList: () -> Unit
) {
    val total = items.sumOf { it.product.price * it.quantity }
    val after = (quotaRemaining - total).coerceAtLeast(0.0)

    Column(Modifier.fillMaxSize().background(PlanPage)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, PlanBorder)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = PlanGreen, fontSize = 27.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Check Before You Go", color = Color(0xFF22272C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("See what you can plan for before visiting the canteen.", color = PlanMuted, fontSize = 7.sp)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlanBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(27.dp).background(Color(0xFFE9EEF0), RoundedCornerShape(5.dp)),
                            contentAlignment = Alignment.Center) {
                            Text("▦", color = PlanGreen, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("DESIGNATED STATION", color = PlanMuted, fontSize = 6.sp, letterSpacing = 0.3.sp)
                            Text("Tambaram Canteen", color = Color(0xFF20252A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = PlanSoftGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("Active Unit", modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                color = PlanGreen, fontSize = 6.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = PlanBorder)
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("➤ Distance: 4.2 km", color = PlanMuted, fontSize = 7.sp, modifier = Modifier.weight(1f))
                        Text("▣ Date: Today", color = PlanMuted, fontSize = 7.sp)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Shopping Plan Verification", color = Color(0xFF20252A), fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text("${items.size} Scheduled Items", color = PlanMuted, fontSize = 6.5.sp)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlanBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    if (items.isEmpty()) {
                        Text("No scheduled items. Edit your shopping list to add products.",
                            modifier = Modifier.padding(10.dp), color = PlanMuted, fontSize = 8.sp)
                    } else items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("${item.product.name} (${item.product.unit})", color = Color(0xFF20252A),
                                    fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(planMoney(item.product.price * item.quantity), color = PlanGreen,
                                    fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(color = if (item.product.name.contains("detergent", true)) PlanOrange else PlanSoftGreen,
                                shape = RoundedCornerShape(4.dp)) {
                                Text(if (item.product.name.contains("detergent", true)) "● Limited Stock" else "● Available",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                    color = if (item.product.name.contains("detergent", true)) Color(0xFF995900) else PlanGreen,
                                    fontSize = 6.3.sp)
                            }
                        }
                        if (index != items.lastIndex) Divider(color = PlanBorder)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlanBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▣ Card Quota Assessment", color = Color(0xFF20252A), fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(color = Color(0xFFE9EDF1), shape = RoundedCornerShape(3.dp)) {
                            Text("Monthly Grocery", color = PlanMuted, fontSize = 6.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    PlanSummaryRow("Remaining entitlement", planMoney(quotaRemaining))
                    Spacer(Modifier.height(5.dp))
                    PlanSummaryRow("Estimated shopping", planMoney(total))
                    Spacer(Modifier.height(5.dp))
                    Divider(color = PlanBorder)
                    Spacer(Modifier.height(5.dp))
                    PlanSummaryRow("Estimated remaining", planMoney(after), true)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFFE2E5E8), RoundedCornerShape(20.dp))) {
                        Box(Modifier.fillMaxWidth(if (quotaRemaining > 0) (total / quotaRemaining).coerceIn(0.0, 1.0).toFloat() else 0f)
                            .height(5.dp).background(PlanGreen, RoundedCornerShape(20.dp)))
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(if (quotaRemaining > 0) "${((total / quotaRemaining).coerceIn(0.0, 1.0) * 100).toInt()}% Quota Planned" else "0% Quota Planned",
                            color = PlanMuted, fontSize = 6.2.sp, modifier = Modifier.weight(1f))
                        Text("Balance: ${planMoney(after)}", color = PlanMuted, fontSize = 6.2.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = PlanSoftGreen,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCDE6CF))
                    ) {
                        Text("✓ Fits within your monthly allowance",
                            modifier = Modifier.padding(7.dp), color = PlanGreen, fontSize = 7.sp)
                    }
                }
            }
        }

        Column(Modifier.background(Color.White).padding(horizontal = 10.dp, vertical = 6.dp)) {
            Button(
                onClick = onReadyToGo,
                modifier = Modifier.fillMaxWidth().height(33.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlanGreen),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("I'm Ready to Go  →", color = Color.White, fontSize = 8.sp)
            }
            Spacer(Modifier.height(5.dp))
            OutlinedButton(
                onClick = onEditShoppingList,
                modifier = Modifier.fillMaxWidth().height(29.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("☷ Edit Shopping List", color = Color(0xFF20252A), fontSize = 8.sp)
            }
        }
        PlanBottomNavigation(selected = "Shop")
    }
}

@Composable
private fun BudgetTile(title: String, amount: String, modifier: Modifier, bg: Color) {
    Column(
        modifier = modifier.background(bg, RoundedCornerShape(5.dp))
            .border(0.5.dp, PlanBorder, RoundedCornerShape(5.dp))
            .padding(6.dp)
    ) {
        Text(title, color = PlanMuted, fontSize = 6.2.sp, maxLines = 2, minLines = 2, lineHeight = 8.sp)
        Spacer(Modifier.height(3.dp))
        Text(amount, color = if (title.contains("Remaining")) PlanGreen else Color(0xFF20252A),
            fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlanSummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if (emphasize) Color(0xFF20252A) else PlanMuted,
            fontSize = 7.2.sp, fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f))
        Text(value, color = if (emphasize) PlanGreen else Color(0xFF20252A),
            fontSize = if (emphasize) 8.5.sp else 7.2.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun PlanBottomNavigation(selected: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .border(0.5.dp, PlanBorder).padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlanNavItem("⌂", "Home", selected == "Home")
        PlanNavItem("▤", "Canteens", selected == "Canteens")
        PlanNavItem("♧", "Shop", selected == "Shop")
        PlanNavItem("◴", "History", selected == "History")
        PlanNavItem("♟", "Profile", selected == "Profile")
    }
}

@Composable
private fun PlanNavItem(glyph: String, label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(glyph, color = if (selected) PlanGreen else PlanMuted, fontSize = 14.sp,
            fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) PlanGreen else PlanMuted, fontSize = 6.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
