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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DetailsGreen = Color(0xFF173F1B)
private val DetailsPaleGreen = Color(0xFFE7F2E7)
private val DetailsPage = Color(0xFFF5F7F8)
private val DetailsMuted = Color(0xFF667181)
private val DetailsBorder = Color(0xFFE1E5E8)
private val DetailsOrange = Color(0xFFFFF1C9)
private val DetailsRed = Color(0xFFB3261E)

/**
 * Product detail screen styled after the CanteenConnect reference.
 * Pass the existing GroceryProduct and connect onAddToShoppingList to the app's
 * existing shopping-list state.
 */
@Composable
fun ProductDetailsScreen(
    product: GroceryProduct,
    onBack: () -> Unit,
    onAddToShoppingList: () -> Unit,
    itemAvailable: Boolean = true,
    oldPrice: Double? = null,
    quotaRemaining: Double = 3000.0
) {
    val isRice = product.name.contains("rice", ignoreCase = true)
    val category = when {
        isRice -> "GROCERIES / UNIT STAPLES"
        product.name.contains("oil", true) -> "GROCERIES / COOKING OIL"
        product.name.contains("dal", true) -> "GROCERIES / PULSES"
        product.name.contains("sugar", true) -> "GROCERIES / UNIT STAPLES"
        else -> "GROCERIES / HOUSEHOLD"
    }
    val displayName = when {
        isRice -> "Premium Rice (5kg Bag)"
        product.name.contains("oil", true) -> "Sunflower Cooking Oil"
        product.name.contains("dal", true) -> "Toor Dal (1kg)"
        product.name.contains("sugar", true) -> "Refined Sugar (1kg)"
        else -> product.name
    }
    val displayUnit = when {
        isRice -> "Golden Harvest / Sona Masoori"
        product.name.contains("oil", true) -> "Refined cooking oil"
        product.name.contains("dal", true) -> "Premium unpolished"
        product.name.contains("sugar", true) -> "Fine grain"
        else -> product.unit
    }
    val estimatedQuotaAfter = (quotaRemaining - product.price).coerceAtLeast(0.0)
    val amount = "₹" + String.format(java.util.Locale("en", "IN"), "%,.0f", product.price)
    val remaining = "₹" + String.format(java.util.Locale("en", "IN"), "%,.0f", quotaRemaining)
    val after = "₹" + String.format(java.util.Locale("en", "IN"), "%,.0f", estimatedQuotaAfter)

    Column(
        modifier = Modifier.fillMaxSize().background(DetailsPage)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, DetailsBorder)
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = DetailsGreen, fontSize = 27.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(12.dp))
            Text("Product Details", color = Color(0xFF20252A),
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f))
            Text("♡", color = DetailsMuted, fontSize = 21.sp,
                modifier = Modifier.clickable { })
            Spacer(Modifier.width(10.dp))
            Text("•", color = DetailsMuted, fontSize = 18.sp)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 9.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // Product hero card. Replace the emoji with a drawable image when available.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, DetailsBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(116.dp)
                            .background(Color(0xFFE9E7DF), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRice) "🌾\n\nPremium Rice" else "${product.emoji}\n\n${product.name}",
                            color = Color(0xFF665D4E),
                            fontSize = 18.sp,
                            lineHeight = 22.sp
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart).padding(5.dp),
                            color = DetailsPaleGreen,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("● Authorised Canteen Ration",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                color = DetailsGreen, fontSize = 6.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▣  Available at Tambaram Canteen",
                            color = DetailsGreen, fontSize = 7.5.sp,
                            modifier = Modifier.weight(1f))
                        Surface(color = DetailsPaleGreen, shape = RoundedCornerShape(3.dp)) {
                            Text(if (itemAvailable) "In Stock" else "Unavailable",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                color = if (itemAvailable) DetailsGreen else DetailsRed, fontSize = 6.5.sp)
                        }
                    }
                }
            }

            // Price and unit tariff
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, DetailsBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Text(category, color = DetailsMuted, fontSize = 6.5.sp, letterSpacing = 0.3.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(displayName, color = Color(0xFF22272C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(displayUnit, color = DetailsMuted, fontSize = 7.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF4F6F6), RoundedCornerShape(6.dp))
                            .padding(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(amount, color = DetailsGreen, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Subsidised Canteen Unit Tariff", color = DetailsMuted, fontSize = 7.sp)
                        }
                        Surface(color = DetailsPaleGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("Save ₹${oldPrice?.let { (it - product.price).coerceAtLeast(0.0).toInt() } ?: 170}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                color = DetailsGreen, fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DetailsOrange,
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF4C55E))
                    ) {
                        Row(Modifier.padding(7.dp), verticalAlignment = Alignment.Top) {
                            Text("ⓘ", color = Color(0xFF9C4D00), fontSize = 10.sp)
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "Unit Quota Limit: Max 2 bags per smart card / calendar month under non-liquor dry ration guidelines.",
                                color = Color(0xFF7A4A0A), fontSize = 7.2.sp, lineHeight = 10.sp
                            )
                        }
                    }
                }
            }

            // Monthly entitlement check
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, DetailsBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▣ Monthly Entitlement Check", color = Color(0xFF22272C),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(color = DetailsPaleGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("✓ Fits limit", color = DetailsGreen,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp), fontSize = 6.5.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("Current remaining grocery quota", color = DetailsMuted, fontSize = 7.2.sp,
                            modifier = Modifier.weight(1f))
                        Text(remaining, color = Color(0xFF20252A), fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text("Product price deduction", color = DetailsRed, fontSize = 7.2.sp,
                            modifier = Modifier.weight(1f))
                        Text("- $amount", color = DetailsRed, fontSize = 7.5.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Divider(color = DetailsBorder)
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text("Estimated balance after purchase", color = Color(0xFF20252A), fontSize = 7.5.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(after, color = DetailsGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Monthly Quota Utilization", color = DetailsMuted, fontSize = 6.5.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(5.dp)
                            .background(Color(0xFFE2E5E8), RoundedCornerShape(20.dp))
                    ) {
                        Box(Modifier.fillMaxWidth(0.43f).height(5.dp)
                            .background(DetailsGreen, RoundedCornerShape(20.dp)))
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text("● Previous spent: ₹2,500", color = DetailsMuted, fontSize = 6.3.sp,
                            modifier = Modifier.weight(1f))
                        Text("● This item: $amount", color = DetailsGreen, fontSize = 6.3.sp)
                    }
                }
            }

            // Specifications
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, DetailsBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(9.dp)) {
                    Text("Product Specifications", color = Color(0xFF22272C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    SpecRow("Net Weight", if (isRice) "5 kg" else product.unit)
                    SpecRow("Grain Grade", if (isRice) "A-Grade Sortex Cleaned" else "Standard quality")
                    SpecRow("Procurement Quality", "AGMARK Certified")
                    SpecRow("Storage Advice", "Store in cool, dry place")
                }
            }
        }

        Button(
            onClick = onAddToShoppingList,
            enabled = itemAvailable,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 5.dp).height(34.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DetailsGreen),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
        ) {
            Text("🛒  Add to Shopping List", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .border(0.5.dp, DetailsBorder).padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DetailNav("⌂", "Home", false)
            DetailNav("▤", "Canteens", false)
            DetailNav("♧", "Shop", true)
            DetailNav("◴", "History", false)
            DetailNav("♟", "Profile", false)
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = DetailsMuted, fontSize = 7.2.sp, modifier = Modifier.weight(1f))
            Text(value, color = Color(0xFF20252A), fontSize = 7.2.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Divider(color = DetailsBorder)
    }
}

@Composable
private fun DetailNav(glyph: String, label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(glyph, color = if (selected) DetailsGreen else DetailsMuted,
            fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) DetailsGreen else DetailsMuted,
            fontSize = 7.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
