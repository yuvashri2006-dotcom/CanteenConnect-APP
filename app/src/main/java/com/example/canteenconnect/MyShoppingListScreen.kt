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

private val ListGreen = Color(0xFF17421D)
private val ListMuted = Color(0xFF687382)
private val ListBorder = Color(0xFFE1E6E9)
private val ListPage = Color(0xFFF6F7F9)
private val ListPaleGreen = Color(0xFFE7F3E8)

private fun listMoney(value: Double): String =
    "₹" + String.format(Locale("en", "IN"), "%,.0f", value)

/**
 * My Shopping List screen matching the supplied reference.
 * Uses the app's existing GroceryProduct and ShoppingItem models.
 */
@Composable
fun MyShoppingListScreen(
    items: List<ShoppingItem>,
    quotaLimit: Double = 3000.0,
    onBack: () -> Unit,
    onCheckAvailability: () -> Unit,
    onAddMoreProducts: () -> Unit,
    onItemSelectionChanged: (String, Boolean) -> Unit = { _, _ -> },
    onNavigate: (String) -> Unit = {}
) {
    val checkedNames = remember(items) {
        mutableStateListOf<String>().apply { addAll(items.map { it.product.name }) }
    }
    val checkedTotal = items.filter { it.product.name in checkedNames }
        .sumOf { it.product.price * it.quantity }
    val allTotal = items.sumOf { it.product.price * it.quantity }
    val remaining = (quotaLimit - checkedTotal).coerceAtLeast(0.0)
    val allocated = if (quotaLimit > 0) (checkedTotal / quotaLimit).coerceIn(0.0, 1.0).toFloat() else 0f

    Column(Modifier.fillMaxSize().background(ListPage)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = ListGreen, fontSize = 27.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text("My Shopping List", color = Color(0xFF20252A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Estimate before you visit", color = ListMuted, fontSize = 7.sp)
            }
            Text("▣", color = ListMuted, fontSize = 15.sp)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Budget estimator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, ListBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▣ Budget Estimator", color = Color(0xFF252B31), fontSize = 8.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(color = ListPaleGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("● Grocery Quota", color = ListGreen, fontSize = 6.2.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = ListBorder)
                    Spacer(Modifier.height(7.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        ListBudgetTile("Estimated Total", listMoney(checkedTotal), Modifier.weight(1f), Color(0xFFF6F7F8))
                        ListBudgetTile("Current Quota", listMoney(quotaLimit), Modifier.weight(1f), Color(0xFFF6F7F8))
                        ListBudgetTile("Est. Remaining", listMoney(remaining), Modifier.weight(1f), Color(0xFFF0FAF1))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Monthly Cap Used: ${listMoney(checkedTotal)} of ${listMoney(quotaLimit)}",
                            color = ListMuted, fontSize = 6.2.sp, modifier = Modifier.weight(1f))
                        Text("${(allocated * 100).toInt()}% Allocated", color = ListGreen,
                            fontSize = 6.2.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFFE2E6E8), RoundedCornerShape(20.dp))) {
                        Box(Modifier.fillMaxWidth(allocated).height(5.dp).background(ListGreen, RoundedCornerShape(20.dp)))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Planned Items", color = Color(0xFF20252A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Surface(color = Color(0xFFE9EDF1), shape = RoundedCornerShape(8.dp)) {
                    Text("${items.size} Items", color = ListMuted, fontSize = 6.3.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Uncheck all", color = ListGreen, fontSize = 6.5.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        checkedNames.clear()
                        items.forEach { onItemSelectionChanged(it.product.name, false) }
                    })
            }

            if (items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ListBorder)
                ) {
                    Text("No products yet. Browse products to build your list.",
                        color = ListMuted, fontSize = 8.sp, modifier = Modifier.padding(12.dp))
                }
            } else {
                items.forEach { item ->
                    val checked = item.product.name in checkedNames
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ListBorder),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(15.dp)
                                    .background(if (checked) ListGreen else Color.White, RoundedCornerShape(3.dp))
                                    .border(1.dp, if (checked) ListGreen else Color(0xFFD2D9DD), RoundedCornerShape(3.dp))
                                    .clickable {
                                        if (checked) checkedNames.remove(item.product.name)
                                        else checkedNames.add(item.product.name)
                                        onItemSelectionChanged(item.product.name, !checked)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.product.name, color = Color(0xFF20252A), fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Pack: ${item.product.unit} • Brand: Sample brand",
                                    color = ListMuted, fontSize = 6.3.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(4.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(listMoney(item.product.price * item.quantity), color = Color(0xFF20252A),
                                    fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                Text(listMoney(item.product.price * 1.2), color = ListMuted, fontSize = 6.sp)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onCheckAvailability,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ListGreen),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("◉  Check Availability at Tambaram Canteen", color = Color.White,
                    fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onAddMoreProducts,
                modifier = Modifier.fillMaxWidth().height(30.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
            ) {
                Text("+ Add More Products", color = Color(0xFF252B31), fontSize = 7.5.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, ListBorder).padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListNavItem("⌂", "Home", false) { onNavigate("Home") }
            ListNavItem("▤", "Canteens", false) { onNavigate("Canteens") }
            ListNavItem("♧", "Shop", true) { onNavigate("Shop") }
            ListNavItem("◴", "History", false) { onNavigate("History") }
            ListNavItem("♟", "Profile", false) { onNavigate("Profile") }
        }
    }
}

@Composable
private fun ListBudgetTile(title: String, amount: String, modifier: Modifier, bg: Color) {
    Column(
        modifier = modifier.background(bg, RoundedCornerShape(5.dp))
            .border(0.5.dp, ListBorder, RoundedCornerShape(5.dp))
            .padding(6.dp)
    ) {
        Text(title, color = ListMuted, fontSize = 6.sp, minLines = 2, lineHeight = 7.sp)
        Spacer(Modifier.height(3.dp))
        Text(amount, color = if (title.contains("Remaining")) ListGreen else Color(0xFF20252A),
            fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ListNavItem(glyph: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = if (selected) ListGreen else ListMuted, fontSize = 14.sp,
            fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) ListGreen else ListMuted, fontSize = 6.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (selected) Box(Modifier.padding(top = 1.dp).size(3.dp).background(ListGreen, RoundedCornerShape(50)))
    }
}
