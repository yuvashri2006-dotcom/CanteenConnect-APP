package com.example.canteenconnect

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canteenconnect.ui.theme.CanteenConnectTheme
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.delay

private val Green = Color(0xFF176B45)
private val PageBackground = Color(0xFFF3F6F3)
private val DarkText = Color(0xFF24372C)
private val MutedText = Color(0xFF647067)
private val LightGreen = Color(0xFFE1EEE5)
private val Red = Color(0xFFB3261E)

private const val PREFS_NAME = "canteen_connect"
private const val SHOPPING_KEY = "shopping_list"
private const val HISTORY_KEY = "purchase_history"
private const val ENTITLEMENT = 7000.0

private const val ACCOUNT_NAME = "account_name"
private const val ACCOUNT_EMAIL = "account_email"
private const val ACCOUNT_SALT = "account_salt"
private const val ACCOUNT_HASH = "account_hash"
private const val SESSION_ACTIVE = "session_active"

private const val CONFIRMED = "Confirmed"
private const val PREPARING = "Preparing"
private const val READY = "Ready for collection"
private const val CANCELLED = "Cancelled"

// --------------------------------------------------
// DATA MODELS
// --------------------------------------------------

data class GroceryProduct(
    val name: String,
    val price: Double,
    val unit: String,
    val emoji: String
)

data class ShoppingItem(
    val product: GroceryProduct,
    val quantity: Int
)

data class Canteen(
    val name: String,
    val address: String,
    val hours: String,
    val description: String,
    val emoji: String
)

data class PurchaseLine(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val unit: String,
    val emoji: String
) {
    val lineTotal: Double
        get() = quantity * unitPrice
}

data class PurchaseOrder(
    val orderId: String,
    val date: String,
    val total: Double,
    val items: List<PurchaseLine>,
    val status: String = CONFIRMED,
    val legacySummary: String = "",
    val paymentMethod: String = "Cash at collection",
    val paymentStatus: String = "Demo only — not processed"
)

// --------------------------------------------------
// SAMPLE CATALOGUE
// --------------------------------------------------

private val products = listOf(
    GroceryProduct("Rice", 60.0, "1 kg", "🍚"),
    GroceryProduct("Milk", 30.0, "500 ml", "🥛"),
    GroceryProduct("Cooking Oil", 150.0, "1 litre", "🫗"),
    GroceryProduct("Sugar", 45.0, "1 kg", "🍬"),
    GroceryProduct("Wheat Flour", 50.0, "1 kg", "🌾"),
    GroceryProduct("Tea", 120.0, "250 g", "🍵"),
    GroceryProduct("Dal", 100.0, "1 kg", "🫘"),
    GroceryProduct("Salt", 20.0, "1 kg", "🧂")
)

private val canteens = listOf(
    Canteen(
        "Tambaram Canteen",
        "Air Force Station, Chennai",
        "8:00 AM - 8:00 PM",
        "Unit Run Canteen (URC) with grocery essentials and monthly ration items.",
        "🏪"
    ),
    Canteen(
        "Avadi Canteen",
        "Avadi, Chennai",
        "8:00 AM - 8:00 PM",
        "Authorized canteen with grocery essentials and household items.",
        "🏬"
    ),
    Canteen(
        "St. Thomas Mount Canteen",
        "St. Thomas Mount, Chennai",
        "8:00 AM - 8:00 PM",
        "Authorized canteen serving nearby defence personnel.",
        "🛒"
    )
)

private fun money(value: Double): String =
    "₹" + String.format(Locale.US, "%,.2f", value)

private fun currentDateTime(): String =
    SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date())

// --------------------------------------------------
// LOCAL DEMONSTRATION ACCOUNT
// --------------------------------------------------

// This is for learning on one device, not production
// authentication. Real apps should use a trusted auth service.

private fun hashPassword(
    password: CharArray,
    salt: ByteArray
): String {
    val spec = PBEKeySpec(password, salt, 60000, 256)

    return try {
        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded

        Base64.encodeToString(hash, Base64.NO_WRAP)
    } finally {
        spec.clearPassword()
        password.fill('\u0000')
    }
}

private fun createAccount(
    prefs: SharedPreferences,
    name: String,
    email: String,
    password: String
): Boolean {
    if (prefs.contains(ACCOUNT_EMAIL)) return false

    val salt = ByteArray(16)
    SecureRandom().nextBytes(salt)

    val hash = hashPassword(password.toCharArray(), salt)

    prefs.edit()
        .putString(ACCOUNT_NAME, name.trim())
        .putString(ACCOUNT_EMAIL, email.trim().lowercase(Locale.ROOT))
        .putString(
            ACCOUNT_SALT,
            Base64.encodeToString(salt, Base64.NO_WRAP)
        )
        .putString(ACCOUNT_HASH, hash)
        .putBoolean(SESSION_ACTIVE, true)
        .apply()

    return true
}

private fun loginAccount(
    prefs: SharedPreferences,
    email: String,
    password: String
): Boolean {
    val savedEmail = prefs.getString(ACCOUNT_EMAIL, null)
        ?: return false

    val savedSalt = prefs.getString(ACCOUNT_SALT, null)
        ?: return false

    val savedHash = prefs.getString(ACCOUNT_HASH, null)
        ?: return false

    if (email.trim().lowercase(Locale.ROOT) != savedEmail) {
        return false
    }

    return try {
        val salt = Base64.decode(savedSalt, Base64.NO_WRAP)
        val candidateHash = hashPassword(password.toCharArray(), salt)

        val matches = MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            savedHash.toByteArray(Charsets.UTF_8)
        )

        if (matches) {
            prefs.edit()
                .putBoolean(SESSION_ACTIVE, true)
                .apply()
        }

        matches
    } catch (_: Exception) {
        false
    }
}

private fun logoutAccount(prefs: SharedPreferences) {
    prefs.edit()
        .putBoolean(SESSION_ACTIVE, false)
        .apply()
}

// --------------------------------------------------
// SHOPPING LIST STORAGE
// --------------------------------------------------

private fun saveShoppingList(
    items: List<ShoppingItem>,
    prefs: SharedPreferences
) {
    val array = JSONArray()

    items.forEach { item ->
        array.put(
            JSONObject().apply {
                put("name", item.product.name)
                put("quantity", item.quantity)
            }
        )
    }

    prefs.edit()
        .putString(SHOPPING_KEY, array.toString())
        .apply()
}

private fun loadShoppingList(
    prefs: SharedPreferences
): List<ShoppingItem> {
    val saved = prefs.getString(SHOPPING_KEY, null)
        ?: return emptyList()

    return try {
        val array = JSONArray(saved)
        val result = mutableListOf<ShoppingItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.getString("name")
            val quantity = obj.getInt("quantity")
            val product = products.find { it.name == name }

            if (product != null && quantity > 0) {
                result.add(ShoppingItem(product, quantity))
            }
        }

        result
    } catch (_: Exception) {
        emptyList()
    }
}

// --------------------------------------------------
// PURCHASE HISTORY STORAGE
// --------------------------------------------------

private fun savePurchaseHistory(
    orders: List<PurchaseOrder>,
    prefs: SharedPreferences
) {
    val array = JSONArray()

    orders.forEach { order ->
        val lines = JSONArray()

        order.items.forEach { line ->
            lines.put(
                JSONObject().apply {
                    put("name", line.name)
                    put("quantity", line.quantity)
                    put("unitPrice", line.unitPrice)
                    put("unit", line.unit)
                    put("emoji", line.emoji)
                }
            )
        }

        array.put(
            JSONObject().apply {
                put("orderId", order.orderId)
                put("date", order.date)
                put("total", order.total)
                put("status", order.status)
                put("items", lines)
                put("legacySummary", order.legacySummary)
                put("paymentMethod", order.paymentMethod)
                put("paymentStatus", order.paymentStatus)
            }
        )
    }

    prefs.edit()
        .putString(HISTORY_KEY, array.toString())
        .apply()
}

private fun loadPurchaseHistory(
    prefs: SharedPreferences
): List<PurchaseOrder> {
    val saved = prefs.getString(HISTORY_KEY, null)
        ?: return emptyList()

    return try {
        val array = JSONArray(saved)
        val result = mutableListOf<PurchaseOrder>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val lines = mutableListOf<PurchaseLine>()

            if (obj.has("items")) {
                val itemArray = obj.getJSONArray("items")

                for (j in 0 until itemArray.length()) {
                    val line = itemArray.getJSONObject(j)

                    lines.add(
                        PurchaseLine(
                            name = line.getString("name"),
                            quantity = line.getInt("quantity"),
                            unitPrice = line.getDouble("unitPrice"),
                            unit = line.optString("unit", ""),
                            emoji = line.optString("emoji", "🛒")
                        )
                    )
                }
            }

            // Preserve order summaries from Lesson 7 and Lesson 8.
            val oldSummary = obj.optString("itemsSummary", "")

            result.add(
                PurchaseOrder(
                    orderId = obj.getString("orderId"),
                    date = obj.getString("date"),
                    total = obj.getDouble("total"),
                    items = lines,
                    status = obj.optString("status", CONFIRMED),
                    legacySummary = obj.optString(
                        "legacySummary",
                        oldSummary
                    ),
                    paymentMethod = obj.optString(
                        "paymentMethod",
                        "Cash at collection"
                    ),
                    paymentStatus = obj.optString(
                        "paymentStatus",
                        "Demo only — not processed"
                    )
                )
            )
        }

        result
    } catch (_: Exception) {
        emptyList()
    }
}

// --------------------------------------------------
// MAIN ACTIVITY
// --------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CanteenConnectTheme {
                // Use the CanteenConnect forest-green palette across every screen.
                // This replaces Material's default blue focus, cursor, and selection accents.
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = Green,
                        onPrimary = Color.White,
                        primaryContainer = LightGreen,
                        onPrimaryContainer = DarkText,
                        secondary = Green,
                        onSecondary = Color.White,
                        tertiary = Green,
                        onTertiary = Color.White,
                        background = PageBackground,
                        onBackground = Color(0xFF111111),
                        surface = Color.White,
                        onSurface = Color(0xFF111111),
                        surfaceVariant = Color(0xFFF1F4F1),
                        onSurfaceVariant = Color(0xFF303830),
                        outline = Color(0xFFCBD4CC),
                        outlineVariant = Color(0xFFDCE3DD),
                        error = Red,
                        onError = Color.White
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = PageBackground
                    ) {
                        CanteenConnectApp()
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// LOGIN / REGISTRATION ROUTER
// --------------------------------------------------

@Composable
fun CanteenConnectApp() {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    var signedIn by remember {
        mutableStateOf(
            prefs.getBoolean(SESSION_ACTIVE, false) &&
                    prefs.contains(ACCOUNT_EMAIL)
        )
    }
    var splashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2200)
        splashVisible = false
    }

    if (splashVisible) {
        SplashScreen()
    } else if (signedIn) {
        var showAssistantPopup by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            CanteenHomeScreen(
                prefs = prefs,
                onLogout = {
                    logoutAccount(prefs)
                    signedIn = false
                }
            )

            // Floating assistant stays above the current app screen.
            FloatingActionButton(
                onClick = { showAssistantPopup = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 92.dp)
                    .size(58.dp),
                containerColor = Color(0xFF153F1B),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text("AI", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }

            if (showAssistantPopup) {
                CanteenAssistantPopup(
                    onClose = { showAssistantPopup = false }
                )
            }
        }
    } else {
        AuthScreen(
            prefs = prefs,
            onLoginSuccess = {
                signedIn = true
            }
        )
    }
}

// --------------------------------------------------
// --------------------------------------------------
// SPLASH SCREEN
// --------------------------------------------------

@Composable
private fun SplashScreen() {
    val forest = Color(0xFF0D2D0D)
    val lime = Color(0xFF72C936)
    val track = Color(0xFFE7EFE4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(forest)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Uses the exact logo image stored in res/drawable.
            Image(
                painter = painterResource(id = R.drawable.canteenconnect_logo),
                contentDescription = "CanteenConnect Logo",
                modifier = Modifier.size(130.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "CanteenConnect",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp
            )

            Spacer(modifier = Modifier.height(110.dp))

            SplashLoadingBar(
                trackColor = track,
                progressColor = lime
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SplashLoadingBar(
    trackColor: Color,
    progressColor: Color
) {
    val transition = rememberInfiniteTransition(label = "splashLoading")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "splashLoadingProgress"
    )

    Box(
        modifier = Modifier
            .width(186.dp)
            .height(7.dp)
            .background(trackColor, RoundedCornerShape(50.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(progressColor, RoundedCornerShape(50.dp))
        )
    }
}

// AUTHENTICATION SCREEN
// --------------------------------------------------

@Composable
fun AuthScreen(
    prefs: SharedPreferences,
    onLoginSuccess: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("DEF-88291-K") }
    var otp by remember { mutableStateOf("") }
    var showOtp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var otpMessage by remember { mutableStateOf("") }
    var loginMode by remember { mutableStateOf("OTP") }
    var loginEmail by remember { mutableStateOf(prefs.getString(ACCOUNT_EMAIL, "demo@canteenconnect.local") ?: "demo@canteenconnect.local") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val forestGreen = Color(0xFF173F1B)
    val borderGrey = Color(0xFFD7DCE0)
    val infoBackground = Color(0xFFF1F3F4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.login_canteenconnect_logo),
                    contentDescription = "CanteenConnect Logo",
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "CanteenConnect",
                            color = forestGreen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Plan before you go.", color = MutedText, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Welcome back",
                color = Color(0xFF202522),
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign in to manage your canteen planning.",
                color = Color(0xFF657080),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE6E8EA)))
            Spacer(Modifier.height(14.dp))

            Text("Choose sign-in method", color = Color(0xFF222722), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth().background(Color(0xFFF0F3F0), RoundedCornerShape(9.dp)).padding(4.dp)) {
                listOf("OTP", "Password").forEach { mode ->
                    Surface(
                        modifier = Modifier.weight(1f).clickable { loginMode = mode; errorMessage = "" },
                        color = if (loginMode == mode) forestGreen else Color.Transparent,
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text(mode, modifier = Modifier.padding(vertical = 9.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (loginMode == mode) Color.White else Color(0xFF526078), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Defence / Canteen Card Number", color = Color(0xFF222722), fontSize = 10.sp,
                    modifier = Modifier.weight(1f))
                Text("Format: DEF-XXXXX-X", color = Color(0xFF526078), fontSize = 9.sp)
            }
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it.uppercase(Locale.ROOT).take(20); errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Text("▣", color = Color(0xFF596779), fontSize = 17.sp) },
                placeholder = { Text("DEF-12345-K", fontSize = 12.sp) },
                shape = RoundedCornerShape(8.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = forestGreen,
                    unfocusedBorderColor = borderGrey,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (loginMode == "OTP") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure Verification / OTP", color = Color(0xFF222722), fontSize = 10.sp,
                        modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { otpMessage = "Demo mode: use OTP 123456." },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("Resend OTP", color = forestGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it.filter(Char::isDigit).take(6); errorMessage = "" },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("♙", color = Color(0xFF596779), fontSize = 17.sp) },
                    placeholder = { Text("Enter 6-digit OTP", letterSpacing = 1.4.sp, fontSize = 12.sp) },
                    trailingIcon = {
                        Text(
                            if (showOtp) "◉" else "◎",
                            modifier = Modifier.clickable { showOtp = !showOtp }.padding(horizontal = 8.dp),
                            color = Color(0xFF596779),
                            fontSize = 17.sp
                        )
                    },
                    visualTransformation = if (showOtp) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = forestGreen,
                        unfocusedBorderColor = borderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

            } else {
                Text("Account email", color = Color(0xFF222722), fontSize = 10.sp)
                Spacer(Modifier.height(5.dp))
                OutlinedTextField(
                    value = loginEmail,
                    onValueChange = { loginEmail = it; errorMessage = "" },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("name@example.com", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("Password", color = Color(0xFF222722), fontSize = 10.sp)
                Spacer(Modifier.height(5.dp))
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it; errorMessage = "" },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Enter your password", fontSize = 12.sp) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { Text(if (showPassword) "Hide" else "Show", modifier = Modifier.clickable { showPassword = !showPassword }.padding(8.dp), color = forestGreen, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(5.dp))
                Text("If you have not set a password yet, sign in with OTP first, then set it in Profile → Privacy & Security.",
                    color = MutedText, fontSize = 10.sp, lineHeight = 14.sp)
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(infoBackground, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("ⓘ", color = forestGreen, fontSize = 13.sp)
                Spacer(Modifier.width(7.dp))
                Text(
                    "Registered smart chip cards automatically sync monthly ration quotas and current inventory records.",
                    color = Color(0xFF4F5751),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            if (loginMode == "OTP" && otpMessage.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(otpMessage, color = forestGreen, fontSize = 11.sp)
            }
            if (errorMessage.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(errorMessage, color = Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    errorMessage = ""
                    val validCard = Regex("DEF-[A-Z0-9]{5}-[A-Z0-9]").matches(cardNumber.trim())
                    if (!validCard) {
                        errorMessage = "Enter a card number in DEF-XXXXX-X format."
                    } else if (loginMode == "OTP") {
                        if (otp.length != 6) errorMessage = "Enter the 6-digit demo OTP. Tap Resend OTP for the demo code."
                        else if (otp != "123456") errorMessage = "Invalid demo OTP. Use 123456 or choose Demo Login."
                        else {
                            prefs.edit()
                                .putString("card_number", cardNumber.trim())
                                .putString(ACCOUNT_NAME, prefs.getString(ACCOUNT_NAME, "Canteen User") ?: "Canteen User")
                                .putString(ACCOUNT_EMAIL, prefs.getString(ACCOUNT_EMAIL, "demo@canteenconnect.local") ?: "demo@canteenconnect.local")
                                .putBoolean(SESSION_ACTIVE, true).apply()
                            onLoginSuccess()
                        }
                    } else {
                        if (loginEmail.isBlank() || loginPassword.isBlank()) errorMessage = "Enter your account email and password."
                        else if (loginAccount(prefs, loginEmail, loginPassword)) {
                            prefs.edit().putString("card_number", cardNumber.trim()).apply()
                            onLoginSuccess()
                        } else errorMessage = "Email or password is incorrect. Sign in with OTP and set a password in Privacy & Security if needed."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = forestGreen)
            ) {
                Text("Continue  →", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(7.dp))
            OutlinedButton(
                onClick = {
                    prefs.edit()
                        .putString(ACCOUNT_NAME, prefs.getString(ACCOUNT_NAME, "Demo User") ?: "Demo User")
                        .putString(ACCOUNT_EMAIL, prefs.getString(ACCOUNT_EMAIL, "demo@canteenconnect.local") ?: "demo@canteenconnect.local")
                        .putString("card_number", cardNumber.trim())
                        .putBoolean(SESSION_ACTIVE, true)
                        .apply()
                    onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF47764A))
            ) {
                Text("ϟ  Demo Login (Quick Access)", color = forestGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F8FA))
                .padding(vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("♙", color = forestGreen, fontSize = 11.sp)
                Spacer(Modifier.width(5.dp))
                Text("Your information is protected.", color = Color(0xFF333A35), fontSize = 10.sp)
            }
        }
    }
}

// --------------------------------------------------
// FLOATING CANTEEN ASSISTANT POPUP
// --------------------------------------------------

@Composable
private fun CanteenAssistantPopup(
    onClose: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            "assistant" to "Hi! I'm your CanteenConnect assistant. Ask me about grocery quota, canteens, products, or your shopping list."
        )
    }
    var question by remember { mutableStateOf("") }

    fun answerFor(input: String): String {
        val q = input.lowercase(Locale.ROOT)
        return when {
            "quota" in q || "balance" in q || "remaining" in q ->
                "Your current remaining monthly grocery quota is shown on Home, My Shopping List, Profile, and My Card / Eligibility. These screens use the same purchase history and monthly limit."
            "canteen" in q || "nearby" in q || "location" in q || "distance" in q ->
                "You can open Canteens from the bottom navigation to view the available demo canteens, including Tambaram, Avadi, and St. Thomas Mount."
            "rice" in q || "oil" in q || "sugar" in q || "dal" in q || "product" in q || "stock" in q ->
                "Open Shop to browse the demo grocery catalogue, check displayed prices, and add products to My Shopping List."
            "shopping list" in q || "list" in q || "add" in q ->
                "Open Shop to add grocery items. Your planned items appear in My Shopping List, where you can review quantities and the estimated total."
            "history" in q || "purchase" in q || "order" in q || "bill" in q ->
                "Open History in the bottom navigation to review orders saved in this demo app."
            "help" in q || "support" in q ->
                "Open Profile, then Help & Support, for the app's support information and guidance."
            else ->
                "I can help you navigate CanteenConnect. Try asking about your quota, canteen locations, grocery products, shopping list, or purchase history."
        }
    }

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.76f),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF153F1B))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AI", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Canteen Assistant",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your canteen shopping helper",
                            color = Color(0xFFDCEBDD),
                            fontSize = 11.sp
                        )
                    }
                    TextButton(onClick = onClose) {
                        Text("✕", color = Color.White, fontSize = 20.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7F5))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    messages.forEach { (speaker, message) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (speaker == "user") Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.88f),
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (speaker == "user") 14.dp else 4.dp,
                                    bottomEnd = if (speaker == "user") 4.dp else 14.dp
                                ),
                                color = if (speaker == "user") Color(0xFF153F1B) else Color.White,
                                border = if (speaker == "user") null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E5E0))
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(12.dp),
                                    color = if (speaker == "user") Color.White else Color(0xFF26342A),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    Text(
                        "SUGGESTED QUESTIONS",
                        color = Color(0xFF647067),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    listOf("Check my quota", "Find a canteen", "Plan my groceries").forEach { prompt ->
                        OutlinedButton(
                            onClick = {
                                messages.add("user" to prompt)
                                messages.add("assistant" to answerFor(prompt))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(prompt, color = Color(0xFF24552B), fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about quota or canteens…", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp)
                    )
                    IconButton(
                        onClick = {
                            val outgoing = question.trim()
                            if (outgoing.isNotEmpty()) {
                                messages.add("user" to outgoing)
                                messages.add("assistant" to answerFor(outgoing))
                                question = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF153F1B), RoundedCornerShape(12.dp))
                    ) {
                        Text("➤", color = Color.White, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// MAIN CANTEEN SCREEN
// --------------------------------------------------

@Composable
fun CanteenHomeScreen(
    prefs: SharedPreferences,
    onLogout: () -> Unit
) {
    var showProfile by remember { mutableStateOf(false) }
    var showPaymentScreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Home") }
    var productSearch by remember { mutableStateOf("") }
    var canteenSearch by remember { mutableStateOf("Chennai Garrison Zone") }
    var selectedCanteen by remember { mutableStateOf<Canteen?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("Cash at collection") }
    var showNotifications by remember { mutableStateOf(false) }
    // Persist the read state so the notification dot stays hidden after reopening the app.
    var notificationsRead by remember {
        mutableStateOf(prefs.getBoolean("notifications_read", false))
    }
    var showShoppingPreferences by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showPrivacySecurity by remember { mutableStateOf(false) }
    var showSmartCardPin by remember { mutableStateOf(false) }
    var showPersonalDetails by remember { mutableStateOf(false) }
    var showEligibility by remember { mutableStateOf(false) }

    val displayName = prefs.getString(ACCOUNT_NAME, "Arun") ?: "Arun"
    val shoppingList = remember {
        mutableStateListOf<ShoppingItem>().apply { addAll(loadShoppingList(prefs)) }
    }
    val purchaseHistory = remember {
        mutableStateListOf<PurchaseOrder>().apply { addAll(loadPurchaseHistory(prefs)) }
    }

    LaunchedEffect(shoppingList.toList()) { saveShoppingList(shoppingList, prefs) }
    LaunchedEffect(purchaseHistory.toList()) { savePurchaseHistory(purchaseHistory, prefs) }

    val filteredProducts = products.filter { it.name.contains(productSearch.trim(), ignoreCase = true) }
    val filteredCanteens = canteens.filter {
        val query = canteenSearch.trim()
        query.isBlank() || query.equals("Chennai Garrison Zone", ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
    }
    val listTotal = shoppingList.sumOf { it.product.price * it.quantity }
    val confirmedSpending = purchaseHistory.filter { it.status != CANCELLED }.sumOf { it.total }
    val remaining = (ENTITLEMENT - confirmedSpending).coerceAtLeast(0.0)
    val progress = (confirmedSpending / ENTITLEMENT).coerceIn(0.0, 1.0).toFloat()

    if (showPersonalDetails) {
        PersonalDetailsScreen(
            prefs = prefs,
            onBack = { showPersonalDetails = false; showProfile = true },
            onNavigate = { tab ->
                showPersonalDetails = false
                if (tab == "Profile") showProfile = true else selectedTab = tab
            }
        )
        return
    }

    if (showEligibility) {
        EligibilityScreen(
            prefs = prefs,
            spentAmount = confirmedSpending,
            remainingAmount = remaining,
            onBack = { showEligibility = false; showProfile = true },
            onNavigate = { tab ->
                showEligibility = false
                if (tab == "Profile") showProfile = true else selectedTab = tab
            }
        )
        return
    }

    if (showPrivacySecurity) {
        PrivacySecurityScreen(
            prefs = prefs,
            onBack = { showPrivacySecurity = false; showProfile = true }
        )
        return
    }

    if (showSmartCardPin) {
        SmartCardPinScreen(
            prefs = prefs,
            onBack = { showSmartCardPin = false; showProfile = true }
        )
        return
    }

    if (showHelpSupport) {
        HelpSupportScreen(
            onBack = {
                showHelpSupport = false
                showProfile = true
            },
            onNavigate = { tab ->
                showHelpSupport = false
                if (tab == "Profile") {
                    showProfile = true
                } else {
                    showProfile = false
                    selectedTab = tab
                }
            }
        )
        return
    }

    if (showShoppingPreferences) {
        ShoppingPreferencesScreen(
            prefs = prefs,
            onBack = {
                showShoppingPreferences = false
                showProfile = true
            },
            onNavigate = { tab ->
                showShoppingPreferences = false
                if (tab == "Profile") {
                    showProfile = true
                } else {
                    showProfile = false
                    selectedTab = tab
                }
            }
        )
        return
    }

    if (showAbout) {
        AboutCanteenConnectScreen(
            onBack = {
                showAbout = false
                showProfile = true
            }
        )
        return
    }

    if (showProfile) {
        CanteenConnectProfilePage(
            prefs = prefs,
            spentAmount = confirmedSpending,
            remainingAmount = remaining,
            onBack = { showProfile = false },
            onLogout = onLogout,
            onNavigate = { destination ->
                showProfile = false
                when (destination) {
                    "Home" -> selectedTab = "Home"
                    "Canteens" -> selectedTab = "Canteens"
                    "Shop" -> selectedTab = "Shop"
                    "History" -> selectedTab = "History"
                    "Profile" -> showProfile = true
                    "PersonalDetails" -> showPersonalDetails = true
                    "Eligibility" -> showEligibility = true
                    "Notifications" -> showNotifications = true
                    "Help" -> showHelpSupport = true
                    "About" -> showAbout = true
                    "Privacy" -> showPrivacySecurity = true
                    "CardPin" -> showSmartCardPin = true
                }
            },
            onNotifications = { showProfile = false; showNotifications = true },
            onCanteens = { showProfile = false; selectedTab = "Canteens" },
            onShoppingPreferences = { showProfile = false; showShoppingPreferences = true }
        )
        return
    }

    if (showNotifications) {
        Column(
            Modifier
                .fillMaxSize()
                .background(PageBackground)
                .statusBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .heightIn(min = 54.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹",
                    color = Green,
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    modifier = Modifier.clickable {
                        // Notifications are opened from the profile or home; the back arrow returns Home.
                        showNotifications = false
                        showProfile = false
                        selectedTab = "Home"
                    }
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Notifications",
                    color = DarkText,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (notificationsRead) "All read ✓" else "Mark all as read",
                    color = Green,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            notificationsRead = true
                            prefs.edit().putBoolean("notifications_read", true).apply()
                        }
                        .padding(start = 8.dp, top = 6.dp, bottom = 6.dp)
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "Your saved item is available" to "Rice (1 kg) is currently marked available at your canteen.",
                    "Monthly entitlement updated" to "Your remaining grocery balance is ${money(remaining)}.",
                    "Shopping list ready" to "Your saved shopping list is ready to check before your next visit.",
                    "Visit reminder" to "Check canteen information before visiting."
                ).forEach { (title, detail) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFFDCE3DD)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    color = DarkText,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (notificationsRead) {
                                    Text(
                                        text = "Read ✓",
                                        color = MutedText,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(Modifier.height(7.dp))
                            Text(
                                text = detail,
                                color = MutedText,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
            HomeBottomNavigation(selectedTab) { tab ->
                showNotifications = false
                if (tab == "Profile") {
                    showProfile = true
                } else {
                    showProfile = false
                    selectedTab = tab
                }
            }
        }
        return
    }

    if (showPaymentScreen) {
        PaymentScreen(
            total = listTotal,
            selectedMethod = selectedPaymentMethod,
            onMethodChange = { selectedPaymentMethod = it },
            onBack = { showPaymentScreen = false },
            onPayNow = {
                if (shoppingList.isNotEmpty()) {
                    val lines = shoppingList.map { item ->
                        PurchaseLine(item.product.name, item.quantity, item.product.price, item.product.unit, item.product.emoji)
                    }
                    purchaseHistory.add(
                        0,
                        PurchaseOrder(
                            orderId = "CC-${System.currentTimeMillis()}",
                            date = currentDateTime(),
                            total = lines.sumOf { it.lineTotal },
                            items = lines,
                            paymentMethod = selectedPaymentMethod,
                            paymentStatus = if (selectedPaymentMethod == "Cash at collection") "Pay at collection" else "Payment successful"
                        )
                    )
                    shoppingList.clear()
                }
                showPaymentScreen = false
                selectedTab = "History"
            }
        )
        return
    }

    if (selectedTab == "Shop") {
        ShopCatalogueScreen(
            shoppingList = shoppingList,
            remainingQuota = remaining,
            searchText = productSearch,
            onSearchChange = { productSearch = it },
            onAddProduct = { product ->
                val index = shoppingList.indexOfFirst { it.product.name == product.name }
                if (index >= 0) shoppingList[index] = shoppingList[index].copy(quantity = shoppingList[index].quantity + 1)
                else shoppingList.add(ShoppingItem(product, 1))
            },
            onViewList = { selectedTab = "List" },
            onNavigate = { tab ->
                if (tab == "Profile") showProfile = true else selectedTab = tab
            }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F8FA)).statusBarsPadding()) {
        when (selectedTab) {
            "Canteens" -> {
                Row(
                    Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("‹", color = Green, fontSize = 28.sp, modifier = Modifier.clickable { selectedTab = "Home" })
                    Spacer(Modifier.width(10.dp))
                    Text("Find a Canteen", color = DarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("LIVE DB", color = MutedText, fontSize = 10.sp)
                }
                OutlinedTextField(
                    value = canteenSearch,
                    onValueChange = { canteenSearch = it; selectedCanteen = null },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    singleLine = true,
                    placeholder = { Text("Search canteen or station", fontSize = 13.sp) },
                    leadingIcon = { Text("⌕", fontSize = 18.sp) },
                    trailingIcon = {
                        Text("⊗", fontSize = 14.sp, modifier = Modifier.clickable { canteenSearch = "" })
                    }
                )
                Row(Modifier.padding(start = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = Green, shape = RoundedCornerShape(15.dp)) {
                        Text("➤ Nearby (<10km)", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                    }
                    Surface(color = Color(0xFFE9EDF1), shape = RoundedCornerShape(15.dp)) {
                        Text("Open Now", color = MutedText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                    }
                }
                // Illustrative map panel matching the supplied design; it does not use live GPS.
                Box(Modifier.fillMaxWidth().height(145.dp).background(Color(0xFFE0E9E3))) {
                    Canvas(Modifier.fillMaxSize()) {
                        val road = Path().apply {
                            moveTo(0f, size.height * .78f)
                            cubicTo(size.width * .3f, size.height * .55f, size.width * .62f, size.height * .8f, size.width, size.height * .18f)
                        }
                        drawPath(road, Color(0xFFB4D0DF), style = Stroke(width = 18f))
                        drawPath(road, Color(0xFFF8FAF8), style = Stroke(width = 12f))
                        val route = Path().apply {
                            moveTo(0f, size.height * .2f)
                            cubicTo(size.width * .3f, size.height * .3f, size.width * .58f, size.height * .48f, size.width, size.height)
                        }
                        drawPath(route, Color(0xFFFFC83D), style = Stroke(width = 5f))
                        drawPath(route, Color(0xFFE49A00), style = Stroke(width = 1.5f))
                    }
                    Text("AIR FORCE STATION", color = MutedText, fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
                    Surface(color = Green, shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.align(Alignment.Center).padding(start = 12.dp)) {
                        Text("▦ Tambaram URC", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
                    }
                    Surface(color = Color.White, shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                        Text("Avadi (8.7 km)", color = DarkText, fontSize = 10.sp, modifier = Modifier.padding(7.dp))
                    }
                    Text("● GPS Accurate to 15m", color = MutedText, fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                    Column(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
                        listOf("+", "−", "⌾").forEach { mark ->
                            Surface(color = Color.White, shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(mark, color = Green, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("AUTHORIZED CANTEENS NEARBY", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                        Text("(${filteredCanteens.size})", color = MutedText, fontSize = 10.sp)
                    }
                    Text("Sort by Distance", color = Green, fontSize = 10.sp)
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredCanteens.forEachIndexed { index, canteen ->
                        Card(
                            Modifier.fillMaxWidth().clickable { selectedCanteen = canteen },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(if (index == 0) 1.5.dp else 1.dp, if (index == 0) Green else Color(0xFFDCE2DD)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(canteen.name, color = DarkText, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (index == 0) Text("✓", color = Green, fontSize = 18.sp)
                                    else Text("›", color = MutedText, fontSize = 18.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("⌖ ${listOf("4.2 km", "8.7 km", "11.5 km").getOrElse(index) { "4.2 km" }} away  •  ● ${if (index == 0) "Open (8 AM - 8 PM)" else "Open"}", color = MutedText, fontSize = 11.sp)
                                Spacer(Modifier.height(7.dp))
                                Row(Modifier.fillMaxWidth().background(Color(0xFFF4F6F7), RoundedCornerShape(6.dp)).padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("▣  ${18 - index * 4} saved items available", color = MutedText, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    Text("${100 - index * 17}% Match", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if (index == 0 || selectedCanteen?.name == canteen.name) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Button(onClick = { selectedCanteen = canteen; selectedTab = "Details" },
                                            modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
                                            Text("ⓘ View Details", fontSize = 10.sp)
                                        }
                                        OutlinedButton(onClick = { selectedCanteen = canteen },
                                            modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
                                            Text("◇ Directions", color = Green, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "List" -> {
                // Shopping-list header
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "‹",
                        color = Green,
                        fontSize = 28.sp,
                        modifier = Modifier.clickable { selectedTab = "Home" }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "My Shopping List",
                        color = DarkText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text("☻", color = MutedText, fontSize = 19.sp)
                }

                if (shoppingList.isEmpty()) {
                    // Empty state matching the supplied design
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F3F5))
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▤  Station Canteen: Delhi Canteen", color = MutedText, fontSize = 13.sp,
                                modifier = Modifier.weight(1f))
                            Surface(color = Color(0xFFE1E5E9), shape = RoundedCornerShape(5.dp)) {
                                Text("0 Items", color = MutedText, fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(36.dp))

                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .border(1.5.dp, Color(0xFFBFCDBA), CircleShape)
                                    .background(Color(0xFFF5F7F4), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color(0xFFE0E5DF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("♧", color = Color(0xFF285E2A), fontSize = 30.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-3).dp)
                                        .size(23.dp)
                                        .background(Color(0xFFFFF4DE), CircleShape)
                                        .border(1.dp, Color(0xFFF4A62A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("?", color = Color(0xFFB65B00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(26.dp))
                            Text(
                                "You haven't added anything yet",
                                color = Color(0xFF202522),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(7.dp))
                            Text(
                                "Search groceries or explore canteen inventory to plan your monthly quota allowance before visiting.",
                                color = Color(0xFF647084),
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4E1)),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(Modifier.padding(13.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("▤  Grocery Quota Status", color = DarkText, fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                        Surface(color = Color(0xFFE4F2E5), shape = RoundedCornerShape(7.dp)) {
                                            Text("Active Smart Card", color = Green, fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Available Balance", color = MutedText, fontSize = 10.sp,
                                            modifier = Modifier.weight(1f))
                                        Text(money(remaining), color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(" / ${money(ENTITLEMENT)}", color = MutedText, fontSize = 10.sp)
                                    }
                                    Spacer(Modifier.height(7.dp))
                                    Box(Modifier.fillMaxWidth().height(6.dp)
                                        .background(Color(0xFFE9ECEB), RoundedCornerShape(50.dp))) {
                                        Box(Modifier.fillMaxWidth((remaining / ENTITLEMENT).coerceIn(0.0, 1.0).toFloat()).fillMaxHeight()
                                            .background(Color(0xFF285E2A), RoundedCornerShape(50.dp)))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("ⓘ ${money(remaining)} grocery quota remaining for this month", color = MutedText, fontSize = 10.sp, lineHeight = 15.sp)
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { selectedTab = "Shop" },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF123B16))
                            ) {
                                Text("◉  Browse Products", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { selectedTab = "Canteens" },
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4E1))
                            ) {
                                Text("▦  View Saved Canteens", color = Color(0xFF536078), fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(18.dp))
                        }
                    }
                } else {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD))) {
                            Column(Modifier.padding(12.dp)) {
                                Text("▣ Budget Estimator", color = DarkText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Estimated total     ${money(listTotal)}", color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Current quota ${money(remaining)}  •  Est. remaining ${money((remaining - listTotal).coerceAtLeast(0.0))}",
                                    color = MutedText, fontSize = 10.sp)
                            }
                        }
                        Text("Planned Items (${shoppingList.sumOf { it.quantity }} items)", color = DarkText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        shoppingList.toList().forEach { item ->
                            ShoppingListCard(item,
                                onIncrease = {
                                    val i = shoppingList.indexOfFirst { it.product.name == item.product.name }
                                    if (i >= 0) shoppingList[i] = shoppingList[i].copy(quantity = shoppingList[i].quantity + 1)
                                },
                                onDecrease = {
                                    val i = shoppingList.indexOfFirst { it.product.name == item.product.name }
                                    if (i >= 0) {
                                        if (shoppingList[i].quantity > 1) shoppingList[i] = shoppingList[i].copy(quantity = shoppingList[i].quantity - 1)
                                        else shoppingList.removeAt(i)
                                    }
                                },
                                onRemove = { shoppingList.removeAll { it.product.name == item.product.name } }
                            )
                        }
                        Button(onClick = { selectedTab = "Canteens" }, modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("Check Availability at Canteen") }
                        OutlinedButton(onClick = { selectedTab = "Shop" }, modifier = Modifier.fillMaxWidth()) { Text("+ Add More Products") }
                        Button(onClick = { showPaymentScreen = true }, enabled = shoppingList.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                            Text("Proceed to payment")
                        }
                    }
                }
            }
            "History" -> {
                Row(Modifier.fillMaxWidth().background(Color.White).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Purchase History", color = DarkText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (purchaseHistory.isEmpty()) {
                        Text("No purchases yet", color = MutedText, fontSize = 14.sp)
                    } else purchaseHistory.toList().forEach { order ->
                        PurchaseHistoryCard(
                            order = order,
                            onAdvanceStatus = {
                                val i = purchaseHistory.indexOfFirst { it.orderId == order.orderId }
                                if (i >= 0) {
                                    val next = when (order.status) { CONFIRMED -> PREPARING; PREPARING -> READY; else -> order.status }
                                    purchaseHistory[i] = order.copy(status = next)
                                }
                            },
                            onCancel = {
                                val i = purchaseHistory.indexOfFirst { it.orderId == order.orderId }
                                if (i >= 0 && (order.status == CONFIRMED || order.status == PREPARING)) purchaseHistory[i] = order.copy(status = CANCELLED)
                            }
                        )
                    }
                }
            }
            "Details" -> {
                val detailProducts = listOf(
                    Triple(GroceryProduct("Rice (5kg Bag)", 850.0, "5 kg", "🍚"), "Sona Masoori & Basmati", "Available"),
                    Triple(GroceryProduct("Cooking Oil (1L Pouch)", 600.0, "1 L", "🫗"), "Sunflower Refined", "Available"),
                    Triple(GroceryProduct("Toor Dal (1kg)", 250.0, "1 kg", "🫘"), "Premium Unpolished", "Available"),
                    Triple(GroceryProduct("Refined Sugar (1kg)", 200.0, "1 kg", "🍚"), "Sulphur Free", "Available"),
                    Triple(GroceryProduct("Detergent Powder (2kg)", 400.0, "2 kg", "🧼"), "12 packets left", "Limited Stock"),
                    Triple(GroceryProduct("Sample Grocery Item (Atta 10kg)", 380.0, "10 kg", "🌾"), "Currently unavailable", "Unavailable")
                )
                val availableProducts = detailProducts.filter { it.third != "Unavailable" }
                val selected = selectedCanteen ?: canteens.first()

                // Canteen details header
                Row(
                    Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("‹", color = Green, fontSize = 28.sp,
                        modifier = Modifier.clickable { selectedTab = "Canteens" })
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(selected.name, color = DarkText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Unit Run Canteen (URC)", color = MutedText, fontSize = 10.sp)
                    }
                    Text("♧", color = DarkText, fontSize = 18.sp,
                        modifier = Modifier.clickable { /* saved canteen demo action */ })
                }

                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Canteen overview card
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(11.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selected.name, color = DarkText, fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Surface(color = LightGreen, shape = RoundedCornerShape(5.dp)) {
                                    Text("● Open Today", color = Green, fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("⌖ ${selected.address}", color = MutedText, fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.material3.HorizontalDivider(color = Color(0xFFE3E7E4))
                            Spacer(Modifier.height(7.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("◷  ${selected.hours}", color = MutedText, fontSize = 10.sp,
                                    modifier = Modifier.weight(1f))
                                Text("➤  ${if (selected.name == "Tambaram Canteen") "4.2" else if (selected.name == "Avadi Canteen") "8.7" else "11.5"} km away",
                                    color = DarkText, fontSize = 10.sp)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Column(
                                    Modifier.weight(1f).background(Color(0xFFF5F7F8), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFFE1E6E3), RoundedCornerShape(6.dp)).padding(7.dp)
                                ) {
                                    Text("♧  Grocery Counter", color = MutedText, fontSize = 9.sp)
                                    Text("Operational", color = DarkText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(
                                    Modifier.weight(1f).background(Color(0xFFF5F7F8), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFFE1E6E3), RoundedCornerShape(6.dp)).padding(7.dp)
                                ) {
                                    Text("▣  Ration Tokens", color = MutedText, fontSize = 9.sp)
                                    Text("Active (Slots Open)", color = DarkText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                Modifier.fillMaxWidth().background(Color(0xFFF5F7F8), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFE1E6E3), RoundedCornerShape(6.dp)).padding(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("▣  Billing Checkouts", color = MutedText, fontSize = 9.sp)
                                    Text("4 POS Counters Active", color = DarkText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text("~6 min wait", color = MutedText, fontSize = 9.sp)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Button(
                                    onClick = { selectedTab = "Canteens" },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
                                ) {
                                    Text("◉ Check Availability", color = Color.White, fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = { /* demo directions action */ },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
                                ) {
                                    Text("◇ Directions", color = Green, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Text("Available Today", color = DarkText, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    detailProducts.forEach { (product, description, status) ->
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(9.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E5E2)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(product.name, color = DarkText, fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Spacer(Modifier.width(4.dp))
                                        val statusColor = when (status) {
                                            "Available" -> LightGreen
                                            "Limited Stock" -> Color(0xFFFFEFCB)
                                            else -> Color(0xFFFBE5E5)
                                        }
                                        Surface(color = statusColor, shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                when (status) {
                                                    "Available" -> "● Available"
                                                    "Limited Stock" -> "● Limited Stock"
                                                    else -> "● Currently Unavailable"
                                                },
                                                color = when (status) {
                                                    "Available" -> Green
                                                    "Limited Stock" -> Color(0xFF9B5B00)
                                                    else -> Red
                                                },
                                                fontSize = 6.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(description, color = MutedText, fontSize = 10.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(money(product.price), color = if (status == "Unavailable") MutedText else DarkText,
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(7.dp))
                                OutlinedButton(
                                    onClick = {
                                        if (status != "Unavailable") {
                                            val i = shoppingList.indexOfFirst { it.product.name == product.name }
                                            if (i >= 0) shoppingList[i] = shoppingList[i].copy(quantity = shoppingList[i].quantity + 1)
                                            else shoppingList.add(ShoppingItem(product, 1))
                                        }
                                    },
                                    enabled = status != "Unavailable",
                                    modifier = Modifier.size(width = 34.dp, height = 34.dp),
                                    shape = RoundedCornerShape(7.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(if (status == "Unavailable") "⊘" else "+",
                                        color = if (status == "Unavailable") MutedText else Green, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    // Monthly entitlement meter
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(9.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("▣  Smart Card Monthly Grocery Limit", color = DarkText,
                                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text("${money(remaining)} rem.", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFFE1E5E3), RoundedCornerShape(20.dp))) {
                                Box(Modifier.fillMaxWidth(progress).height(5.dp).background(Green, RoundedCornerShape(20.dp)))
                            }
                            Spacer(Modifier.height(5.dp))
                            Text("${money(confirmedSpending)} of ${money(ENTITLEMENT)} monthly ration entitlement used",
                                color = MutedText, fontSize = 9.sp)
                        }
                    }

                    Button(
                        onClick = {
                            availableProducts.forEach { (product, _, _) ->
                                val i = shoppingList.indexOfFirst { it.product.name == product.name }
                                if (i >= 0) shoppingList[i] = shoppingList[i].copy(quantity = shoppingList[i].quantity + 1)
                                else shoppingList.add(ShoppingItem(product, 1))
                            }
                            selectedTab = "List"
                        },
                        modifier = Modifier.fillMaxWidth().height(35.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
                    ) {
                        Text("☷  Add All Available to Shopping List", color = Color.White,
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("${availableProducts.size} items ready to plan", color = MutedText,
                        fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
            else -> {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Good morning, ${displayName.substringBefore(" ")} 👋", color = Color(0xFF20252A), fontSize = 19.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text("Plan your next canteen visit.", color = MutedText, fontSize = 14.sp, lineHeight = 17.sp)
                        }
                        Surface(Modifier.size(40.dp).clickable { showNotifications = true }, color = Color.White,
                            shape = RoundedCornerShape(9.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD))) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("♟", color = Green, fontSize = 20.sp)
                                if (!notificationsRead) {
                                    Box(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 4.dp, end = 4.dp)
                                            .size(7.dp)
                                            .background(Red, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD)), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("▣  September Entitlement", color = DarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Surface(color = Color(0xFFE2EAF8), shape = RoundedCornerShape(12.dp)) {
                                    Text("Quota Tracker", color = Color(0xFF56657D), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Monthly Grocery Limit", color = MutedText, fontSize = 14.sp)
                            Text("₹7,000", color = Green, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(7.dp))
                            androidx.compose.material3.HorizontalDivider(color = Color(0xFFE3E7E4))
                            Spacer(Modifier.height(7.dp))
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text("Spent", color = MutedText, fontSize = 13.sp)
                                    Text(money(confirmedSpending), color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Box(Modifier.width(1.dp).height(30.dp).background(Color(0xFFE3E7E4)))
                                Column(Modifier.weight(1f).padding(start = 9.dp)) {
                                    Text("Remaining", color = MutedText, fontSize = 13.sp)
                                    Text(money(remaining), color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(Modifier.height(9.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${(progress * 100).toInt()}% Quota Utilized", color = MutedText, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text("${money(remaining)} left", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(5.dp))
                            Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFE4F1E4), RoundedCornerShape(20.dp))) {
                                Box(Modifier.fillMaxWidth(progress).height(6.dp).background(Green, RoundedCornerShape(20.dp)))
                            }
                        }
                    }
                    Text("QUICK ACTIONS", color = MutedText, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        HomeQuickAction("▥", "Find Canteen", "Browse nearby", Modifier.weight(1f)) { selectedTab = "Canteens" }
                        HomeQuickAction("☑", "Check Stock", "Live inventory", Modifier.weight(1f)) { selectedTab = "Details"; selectedCanteen = canteens.firstOrNull() }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        HomeQuickAction("☷", "My Shopping List", "${shoppingList.sumOf { it.quantity }} items planned", Modifier.weight(1f)) { selectedTab = "List" }
                        HomeQuickAction("▤", "Purchase History", "Invoices & bills", Modifier.weight(1f)) { selectedTab = "History" }
                    }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD)), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("♧ Nearby Canteen", color = MutedText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Surface(color = Color(0xFFE6F4E7), shape = RoundedCornerShape(8.dp)) {
                                    Text("● Open today", color = Green, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                                }
                            }
                            Spacer(Modifier.height(7.dp))
                            Text("Tambaram Canteen", color = DarkText, fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                            Text("Air Force Station • 4.2 km", color = MutedText, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            Surface(Modifier.fillMaxWidth(), color = Color(0xFFF1F3F4), shape = RoundedCornerShape(6.dp)) {
                                Text("✓  18 saved items available", color = DarkText, fontSize = 13.sp, modifier = Modifier.padding(11.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { selectedTab = "Canteens" }, modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF153F1B)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
                                Text("View Canteen  →", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        HomeBottomNavigation(selectedTab) { tab ->
            if (tab == "Profile") showProfile = true else selectedTab = tab
        }
    }
}

@Composable
private fun HomeQuickAction(
    glyph: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(94.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(32.dp).background(Color(0xFFE7F3E8), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                Text(glyph, color = Green, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(title, color = DarkText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = MutedText, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun HomeBottomNavigation(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShopNavItem("⌂", "Home", selected == "Home") { onSelect("Home") }
        ShopNavItem("▤", "Canteens", selected == "Canteens" || selected == "Details") { onSelect("Canteens") }
        ShopNavItem("♧", "Shop", selected == "Shop") { onSelect("Shop") }
        ShopNavItem("◴", "History", selected == "History") { onSelect("History") }
        ShopNavItem("♙", "Profile", selected == "Profile") { onSelect("Profile") }
    }
}

// --------------------------------------------------
// SHOP CATALOGUE SCREEN
// --------------------------------------------------

@Composable
private fun ShopCatalogueScreen(
    shoppingList: List<ShoppingItem>,
    remainingQuota: Double,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onAddProduct: (GroceryProduct) -> Unit,
    onViewList: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var showFilters by remember { mutableStateOf(false) }

    // Demo catalogue: prices are illustrative and can be connected to live inventory later.
    val shopProducts = listOf(
        Triple(GroceryProduct("Rice (5kg Bag)", 850.0, "5 kg", "🍚"), "Groceries • Unit staple • 5 kg", "Available"),
        Triple(GroceryProduct("Sunflower Cooking Oil", 600.0, "1 L", "🫗"), "Groceries • Refined oil • 1 L", "Available"),
        Triple(GroceryProduct("Toor Dal (1kg)", 250.0, "1 kg", "🫘"), "Groceries • Pulses • 1 kg", "Available"),
        Triple(GroceryProduct("Refined Sugar (1kg)", 200.0, "1 kg", "🍚"), "Groceries • Staples • 1 kg", "Available"),
        Triple(GroceryProduct("Wheat Flour / Atta", 320.0, "5 kg", "🌾"), "Groceries • Whole wheat • 5 kg", "Available"),
        Triple(GroceryProduct("Chana Dal (1kg)", 180.0, "1 kg", "🫘"), "Groceries • Pulses • 1 kg", "Available"),
        Triple(GroceryProduct("Moong Dal (1kg)", 190.0, "1 kg", "🫘"), "Groceries • Pulses • 1 kg", "Available"),
        Triple(GroceryProduct("Iodized Salt", 28.0, "1 kg", "🧂"), "Groceries • Essential • 1 kg", "Available"),
        Triple(GroceryProduct("Tea Powder", 145.0, "500 g", "🍵"), "Groceries • Beverage • 500 g", "Available"),
        Triple(GroceryProduct("Poha (Flattened Rice)", 75.0, "1 kg", "🌾"), "Groceries • Breakfast • 1 kg", "Available"),
        Triple(GroceryProduct("Laundry Detergent", 400.0, "2 kg", "🧼"), "Household • Laundry • 2 kg", "Limited"),
        Triple(GroceryProduct("Dishwashing Liquid", 180.0, "500 ml", "🧴"), "Household • Cleaning • 500 ml", "Available"),
        Triple(GroceryProduct("Floor Cleaner", 165.0, "1 L", "🧹"), "Household • Cleaning • 1 L", "Available"),
        Triple(GroceryProduct("Bathing Soap (4 pack)", 120.0, "4 bars", "🧼"), "Personal Care • Bath • 4 bars", "Available"),
        Triple(GroceryProduct("Toothpaste", 95.0, "150 g", "🪥"), "Personal Care • Oral care • 150 g", "Available"),
        Triple(GroceryProduct("Shampoo", 175.0, "340 ml", "🧴"), "Personal Care • Hair care • 340 ml", "Available")
    )
    val categories = listOf("All", "Groceries", "Household", "Personal Care")
    val visibleProducts = shopProducts.filter { (product, subtitle, _) ->
        val query = searchText.trim()
        val matchesSearch = query.isBlank() || product.name.contains(query, ignoreCase = true) ||
                subtitle.contains(query, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "Groceries" -> subtitle.startsWith("Groceries")
            "Household" -> subtitle.startsWith("Household")
            "Personal Care" -> subtitle.startsWith("Personal Care")
            else -> true
        }
        matchesSearch && matchesCategory
    }
    val total = shoppingList.sumOf { it.product.price * it.quantity }
    val quantity = shoppingList.sumOf { it.quantity }

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F8F9))) {
        // Shop toolbar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = Color(0xFF153D19), fontSize = 27.sp,
                modifier = Modifier.clickable { onNavigate("Home") })
            Spacer(Modifier.width(12.dp))
            Text("Shop", color = Color(0xFF153D19), fontSize = 18.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box {
                Text("🛍️", color = Color(0xFF153D19), fontSize = 18.sp,
                    modifier = Modifier.clickable { onViewList() })
                if (quantity > 0) Surface(
                    modifier = Modifier.align(Alignment.TopEnd), color = Color(0xFF315F35), shape = CircleShape
                ) { Text("$quantity", color = Color.White, fontSize = 7.sp, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Text("☻", color = Color(0xFF536174), fontSize = 19.sp,
                modifier = Modifier.clickable { onNavigate("Profile") })
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f).height(56.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF171D18)
                    ),
                    placeholder = {
                        Text(
                            text = "Search grocery products",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF667168),
                            maxLines = 1
                        )
                    },
                    leadingIcon = { Text("⌕", fontSize = 18.sp, color = Color(0xFF536174)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF315F35), unfocusedBorderColor = Color(0xFFD4DCD3),
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    )
                )
                Surface(
                    modifier = Modifier.size(42.dp).clickable { showFilters = !showFilters },
                    color = Color.White, shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4DCD3))
                ) { Box(contentAlignment = Alignment.Center) { Text("☷", fontSize = 18.sp, color = Color(0xFF536174)) } }
            }
            if (showFilters) {
                Text("Sort: Recommended  •  Availability first", color = Color(0xFF536174), fontSize = 11.sp,
                    modifier = Modifier.padding(top = 5.dp, start = 3.dp))
            }
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { category ->
                    val active = selectedCategory == category
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = category },
                        color = if (active) Color(0xFF153D19) else Color.White,
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Color(0xFF153D19) else Color(0xFFD5DDD4))
                    ) {
                        Text(category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = if (active) Color.White else Color(0xFF566158), fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Showing ${visibleProducts.size} products", color = Color(0xFF566158), fontSize = 12.sp,
                    modifier = Modifier.weight(1f))
                Surface(color = Color(0xFFE4F1E4), shape = RoundedCornerShape(4.dp)) {
                    Text("QUOTA VERIFIED", color = Color(0xFF245A2B), fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))

            visibleProducts.chunked(2).forEach { productRow ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    productRow.forEach { (product, subtitle, stock) ->
                        val added = shoppingList.any { it.product.name == product.name }
                        Card(
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DED5)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                                Box(Modifier.fillMaxWidth().height(88.dp).background(Color(0xFFF0F1ED), RoundedCornerShape(7.dp)),
                                    contentAlignment = Alignment.Center) {
                                    Text(product.emoji, fontSize = 42.sp)
                                    Surface(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp),
                                        color = if (stock == "Limited") Color(0xFFFFE9B8) else Color(0xFFE6F4E7),
                                        shape = RoundedCornerShape(8.dp)) {
                                        Text(if (stock == "Limited") "● Limited" else "● Available",
                                            color = if (stock == "Limited") Color(0xFF9A5B00) else Color(0xFF286331),
                                            fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp))
                                    }
                                }
                                Spacer(Modifier.height(7.dp))
                                Text(product.name, color = Color(0xFF202820), fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(subtitle.substringAfter(" • ", subtitle), color = Color(0xFF667168), fontSize = 10.sp,
                                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(Modifier.height(8.dp))
                                androidx.compose.material3.HorizontalDivider(color = Color(0xFFE4E8E3))
                                Spacer(Modifier.height(7.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(money(product.price), color = Color(0xFF252D26), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(money(product.price * 1.2), color = Color(0xFF7B8490), fontSize = 10.sp,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                    }
                                    if (added) {
                                        Button(onClick = { onAddProduct(product) }, modifier = Modifier.height(29.dp),
                                            shape = RoundedCornerShape(7.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF153D19)),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp)) {
                                            Text("✓ Added", fontSize = 10.sp, color = Color.White)
                                        }
                                    } else {
                                        OutlinedButton(onClick = { if (stock != "Limited") onAddProduct(product) },
                                            enabled = stock != "Limited", modifier = Modifier.height(29.dp),
                                            shape = RoundedCornerShape(7.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp)) {
                                            Text(if (stock == "Limited") "Limited" else "+ Add", fontSize = 10.sp, color = Color(0xFF245A2B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (productRow.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
            shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF153D19)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(Modifier.fillMaxWidth().clickable { onViewList() }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("▣", color = Color.White, fontSize = 19.sp)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("Items in list: $quantity (${money(total)})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Monthly Balance: ${money((remainingQuota - total).coerceAtLeast(0.0))}", color = Color(0xFFC8E0C9), fontSize = 10.sp)
                }
                Button(onClick = onViewList, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5E8B6)),
                    shape = RoundedCornerShape(6.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 9.dp, vertical = 5.dp)) {
                    Text("View List →", color = Color(0xFF153D19), fontSize = 11.sp)
                }
            }
        }

        // Use the same bottom navigation component and sizing as the Home screen.
        HomeBottomNavigation(selected = "Shop", onSelect = onNavigate)
    }
}

@Composable
private fun ShopNavItem(
    glyph: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = if (selected) Color(0xFF153D19) else Color(0xFF65716A),
            fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (selected) Color(0xFF153D19) else Color(0xFF65716A),
            fontSize = 9.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

// --------------------------------------------------
// CANTEEN CARD
// --------------------------------------------------

@Composable
fun CanteenCard(
    canteen: Canteen,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LightGreen else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(canteen.emoji, fontSize = 30.sp)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    canteen.name,
                    color = DarkText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(canteen.address, color = MutedText, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                Text("🕒 ${canteen.hours}", color = Green, fontSize = 12.sp)
            }

            Text(if (selected) "▲" else "▼", color = Green)
        }
    }
}

// --------------------------------------------------
// PRODUCT CARD
// --------------------------------------------------

@Composable
fun ProductCard(
    product: GroceryProduct,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(product.emoji, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    color = DarkText,
                    fontWeight = FontWeight.Bold
                )
                Text(product.unit, color = MutedText, fontSize = 12.sp)
                Text(
                    money(product.price),
                    color = Green,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(onClick = onAdd) {
                Text("Add +")
            }
        }
    }
}

// --------------------------------------------------
// SHOPPING LIST ITEM
// --------------------------------------------------

@Composable
fun ShoppingListCard(
    item: ShoppingItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.product.emoji, fontSize = 26.sp)
                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        item.product.name,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        "${money(item.product.price)} each",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }

                Text(
                    money(item.product.price * item.quantity),
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDecrease) {
                    Text("−")
                }

                Text(
                    "${item.quantity}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(onClick = onIncrease) {
                    Text("+")
                }

                Spacer(Modifier.weight(1f))

                Text(
                    "Remove",
                    color = Red,
                    modifier = Modifier.clickable { onRemove() }
                )
            }
        }
    }
}

// --------------------------------------------------
// ORDER HISTORY, RECEIPT AND STATUS
// --------------------------------------------------

@Composable
fun PurchaseHistoryCard(
    order: PurchaseOrder,
    onAdvanceStatus: () -> Unit,
    onCancel: () -> Unit
) {
    var expanded by remember(order.orderId) {
        mutableStateOf(false)
    }

    val canCancel =
        order.status == CONFIRMED || order.status == PREPARING

    val nextStatus = when (order.status) {
        CONFIRMED -> PREPARING
        PREPARING -> READY
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (order.status == CANCELLED) {
                Color(0xFFFFF0EE)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            LightGreen,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧾", fontSize = 22.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        order.orderId,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(order.date, fontSize = 12.sp, color = MutedText)
                }

                Text(
                    money(order.total),
                    fontWeight = FontWeight.Bold,
                    color = if (order.status == CANCELLED) Red else Green
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Status: ${order.status}",
                color = when (order.status) {
                    CANCELLED -> Red
                    READY -> Green
                    else -> DarkText
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                if (expanded) "Tap to hide details" else "Tap to view receipt",
                color = MutedText,
                fontSize = 11.sp
            )

            if (expanded) {
                Spacer(Modifier.height(14.dp))

                Text(
                    "ORDER RECEIPT",
                    color = Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(10.dp))

                if (order.items.isNotEmpty()) {
                    order.items.forEach { line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(line.emoji, fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    line.name,
                                    color = DarkText,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    "${line.quantity} × ${money(line.unitPrice)}" +
                                            if (line.unit.isNotBlank()) {
                                                " (${line.unit})"
                                            } else {
                                                ""
                                            },
                                    color = MutedText,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                money(line.lineTotal),
                                color = DarkText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (order.legacySummary.isNotBlank()) {
                    Text(
                        order.legacySummary,
                        color = DarkText,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        "No item details available for this older order.",
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Order total",
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        money(order.total),
                        fontWeight = FontWeight.Bold,
                        color = Green
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Payment method: ${order.paymentMethod}",
                    color = DarkText,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Payment status: ${order.paymentStatus}",
                    color = MutedText,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    if (order.status == CANCELLED) {
                        "This order has been cancelled."
                    } else {
                        "Your order."
                    },
                    color = if (order.status == CANCELLED) Red else MutedText,
                    fontSize = 11.sp
                )

                if (nextStatus != null) {
                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = onAdvanceStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update status to $nextStatus")
                    }
                }

                if (canCancel) {
                    Spacer(Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel order", color = Red)
                    }
                }
            }
        }
    }
}



// --------------------------------------------------
// DEMO PAYMENT SCREEN
// --------------------------------------------------

@Composable
private fun PaymentScreen(
    total: Double,
    selectedMethod: String,
    onMethodChange: (String) -> Unit,
    onBack: () -> Unit,
    onPayNow: () -> Unit
) {
    var upiId by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Payment",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )
        Spacer(Modifier.height(6.dp))
        Text("Choose how you want to pay", color = MutedText)
        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Green),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Amount to pay", color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    money(total),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("CanteenConnect checkout", color = Color(0xFFE0F2E7), fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        listOf("UPI", "Card", "Cash at collection").forEach { method ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable {
                        onMethodChange(method)
                        message = ""
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == method) LightGreen else Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (selectedMethod == method) "🔘" else "⚪", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(method, color = DarkText, fontWeight = FontWeight.Bold)
                        Text(
                            when (method) {
                                "UPI" -> "Enter a your UPI ID below"
                                "Card" -> "Enter your card details below"
                                else -> "Pay when you collect your order"
                            },
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        when (selectedMethod) {
            "UPI" -> {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it; message = "" },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("UPI ID") },
                    placeholder = { Text("example@upi") }
                )
            }
            "Card" -> {
                Spacer(Modifier.height(8.dp))
                Text("Enter your card details correctly.", color = Red, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it; message = "" },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cardholder name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter(Char::isDigit).take(16); message = "" },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Test card number (16 digits)") }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it.take(5); message = "" },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Expiry (MM/YY)") }
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it.filter(Char::isDigit).take(4); message = "" },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Test CVV") }
                    )
                }
            }
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(message, color = Red, fontSize = 13.sp)
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                val valid = when (selectedMethod) {
                    "UPI (demo)" -> upiId.contains("@") && upiId.substringAfter("@").isNotBlank()
                    "Card (demo)" -> cardName.isNotBlank() && cardNumber.length == 16 && expiry.matches(Regex("\\d{2}/\\d{2}")) && cvv.length in 3..4
                    else -> true
                }
                if (valid) {
                    onPayNow()
                } else {
                    message = when (selectedMethod) {
                        "UPI (demo)" -> "Enter a sample UPI ID such as demo@upi."
                        else -> "Enter complete fake test details, not real card details."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedMethod == "Cash at collection") "Confirm order" else "Pay now (demo)")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to shopping list")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Important: this is only a UI demonstration. It does not open a UPI app, contact a payment gateway, or charge money.",
            color = MutedText,
            fontSize = 12.sp
        )
    }
}



// --------------------------------------------------
// --------------------------------------------------
// PRIVACY & SECURITY
// --------------------------------------------------

private const val BIOMETRIC_LOCK_KEY = "biometric_lock_enabled"
private const val PIN_SALT_KEY = "smart_card_pin_salt"
private const val PIN_HASH_KEY = "smart_card_pin_hash"

@Composable
private fun PrivacySecurityScreen(prefs: SharedPreferences, onBack: () -> Unit) {
    var biometricEnabled by remember { mutableStateOf(prefs.getBoolean(BIOMETRIC_LOCK_KEY, false)) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showChangePassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val green = Color(0xFF17451F)

    Column(Modifier.fillMaxSize().background(PageBackground).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = green, fontSize = 25.sp, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(12.dp))
            Text("Privacy & Security", color = green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("App protection", color = DarkText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Choose how you protect access to CanteenConnect.", color = MutedText, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Biometric lock", color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Use fingerprint or face unlock when supported", color = MutedText, fontSize = 12.sp)
                        }
                        Switch(checked = biometricEnabled, onCheckedChange = {
                            biometricEnabled = it
                            prefs.edit().putBoolean(BIOMETRIC_LOCK_KEY, it).apply()
                            message = if (it) "Biometric preference saved. Device biometric prompt still needs to be integrated for actual locking." else "Biometric preference disabled."
                        })
                    }
                    Text("Note: this switch saves your preference only; it does not activate Android biometric authentication by itself.", color = MutedText, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Account password", color = DarkText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Set or change the password used by Password sign-in.", color = MutedText, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { showChangePassword = !showChangePassword; message = "" }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (showChangePassword) "Cancel password change" else "Change password", color = green)
                    }
                    if (showChangePassword) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; message = "" }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            label = { Text("New password") }, visualTransformation = PasswordVisualTransformation())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; message = "" }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            label = { Text("Confirm new password") }, visualTransformation = PasswordVisualTransformation())
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = {
                            message = when {
                                newPassword.length < 8 -> "Use at least 8 characters for your password."
                                newPassword.none(Char::isDigit) || newPassword.none(Char::isLetter) -> "Include at least one letter and one number."
                                newPassword != confirmPassword -> "Passwords do not match."
                                else -> {
                                    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                                    val hash = hashPassword(newPassword.toCharArray(), salt)
                                    prefs.edit().putString(ACCOUNT_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                                        .putString(ACCOUNT_HASH, hash).apply()
                                    newPassword = ""; confirmPassword = ""; showChangePassword = false
                                    "Password saved. You can now sign in with your account email and password."
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = green)) { Text("Save password") }
                    }
                    if (message.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(message, color = if (message.startsWith("Password saved")) green else Red, fontSize = 12.sp) }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Privacy tips", color = DarkText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Use a unique password and do not share your Smart Card PIN.\n\n• Log out on shared devices.\n• This capstone demo stores credentials locally on this device; production apps should use a trusted authentication service and Android Keystore.", color = MutedText, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun SmartCardPinScreen(prefs: SharedPreferences, onBack: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    val green = Color(0xFF17451F)
    val pinExists = prefs.contains(PIN_HASH_KEY)

    Column(Modifier.fillMaxSize().background(PageBackground).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = green, fontSize = 25.sp, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(12.dp))
            Text("Smart Card PIN", color = green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (pinExists) "Change your PIN" else "Create your PIN", color = DarkText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Set a 4–6 digit PIN for this app's demo counter-verification flow. This does not change a real issued smart card PIN.", color = MutedText, fontSize = 11.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(6); message = "" }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("New 4–6 digit PIN") }, visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { Text(if (showPin) "Hide" else "Show", modifier = Modifier.clickable { showPin = !showPin }.padding(8.dp), color = green, fontSize = 12.sp) })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = confirmPin, onValueChange = { confirmPin = it.filter(Char::isDigit).take(6); message = "" }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("Confirm PIN") }, visualTransformation = PasswordVisualTransformation())
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        message = when {
                            pin.length !in 4..6 -> "PIN must contain 4 to 6 digits."
                            pin != confirmPin -> "PINs do not match."
                            else -> {
                                val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                                val hash = hashPassword(pin.toCharArray(), salt)
                                prefs.edit().putString(PIN_SALT_KEY, Base64.encodeToString(salt, Base64.NO_WRAP)).putString(PIN_HASH_KEY, hash).apply()
                                pin = ""; confirmPin = ""
                                "Smart Card PIN saved securely for this local demo."
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = green)) { Text("Save PIN") }
                    if (message.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(message, color = if (message.startsWith("Smart Card PIN saved")) green else Red, fontSize = 12.sp) }
                }
            }
        }
    }
}

// PROFILE PAGE — mobile typography and spacing refresh.
// Other screens and their navigation are intentionally unchanged.
// --------------------------------------------------

@Composable
private fun CanteenConnectProfilePage(
    prefs: SharedPreferences,
    spentAmount: Double,
    remainingAmount: Double,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onNotifications: () -> Unit,
    onCanteens: () -> Unit,
    onShoppingPreferences: () -> Unit
) {
    val name = prefs.getString(ACCOUNT_NAME, "Arun Kumar") ?: "Arun Kumar"
    val email = prefs.getString(ACCOUNT_EMAIL, "") ?: ""
    val cardNumber = prefs.getString("card_number", "DEF-88291-K") ?: "DEF-88291-K"
    val profileGreen = Color(0xFF17451F)
    val profileMuted = Color(0xFF687383)
    val profileBorder = Color(0xFFE0E5E8)
    val profileBg = Color(0xFFF4F6F5)
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(profileBg)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(32.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("‹", color = profileGreen, fontSize = 30.sp, lineHeight = 32.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Profile",
                color = profileGreen,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(32.dp), contentAlignment = Alignment.CenterEnd) {
                Text("⊙", color = profileMuted, fontSize = 20.sp)
            }
        }
        HorizontalDivider(color = profileBorder)

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, profileBorder)
            ) {
                Column(Modifier.padding(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).background(profileGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name.split(" ").mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2).joinToString(""),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                name,
                                color = DarkText,
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Defence Card Holder",
                                color = profileMuted,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            color = Color(0xFFE7F3E8),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "● Active",
                                color = profileGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = profileBorder)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Smart Card",
                            color = profileMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            cardNumber,
                            color = DarkText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Base / Station",
                            color = profileMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Tambaram AFS", color = DarkText, fontSize = 11.sp)
                    }
                    if (email.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            email,
                            color = profileMuted,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFFF3F6F3),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Monthly Grocery Quota",
                                    color = profileMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${money(spentAmount)} / ${money(ENTITLEMENT)}",
                                    color = profileGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(9.dp))
                            Box(
                                Modifier.fillMaxWidth().height(7.dp)
                                    .background(Color(0xFFDCE6DD), RoundedCornerShape(20.dp))
                            ) {
                                Box(
                                    Modifier.fillMaxWidth((spentAmount / ENTITLEMENT).coerceIn(0.0, 1.0).toFloat()).fillMaxHeight()
                                        .background(profileGreen, RoundedCornerShape(20.dp))
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Remaining: ${money(remainingAmount)}",
                                    color = profileMuted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("Valid till 31 Oct", color = profileGreen, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            ProfileSectionTitle("ACCOUNT")
            ProfileGroupCard {
                ProfileOptionRow(
                    "♙", "My Profile", "Personal details & service record",
                    onClick = { onNavigate("PersonalDetails") }
                )
                ProfileOptionRow(
                    "▤", "My Card / Eligibility", "Grocery & household quota limits",
                    trailing = "● Active",
                    onClick = { onNavigate("Eligibility") }
                )
                ProfileOptionRow(
                    "▦", "My Canteens", "Tambaram URC, Pallavaram Depot",
                    trailing = "2 saved",
                    onClick = onCanteens,
                    last = true
                )
            }

            ProfileSectionTitle("PREFERENCES")
            ProfileGroupCard {
                ProfileOptionRow(
                    "♧", "Notifications", "Stock arrival & quota renewal alerts",
                    onClick = onNotifications
                )
                ProfileOptionRow(
                    "☷", "Shopping Preferences", "Default unit: Tambaram URC",
                    onClick = onShoppingPreferences,
                    last = true
                )
            }

            ProfileSectionTitle("SECURITY")
            ProfileGroupCard {
                ProfileOptionRow(
                    "♙", "Privacy & Security", "Biometric lock & app permissions",
                    onClick = { onNavigate("Privacy") }
                )
                ProfileOptionRow(
                    "▣", "Smart Card PIN", "Verification & counter authentication",
                    onClick = { onNavigate("CardPin") },
                    last = true
                )
            }

            ProfileSectionTitle("SUPPORT & ABOUT")
            ProfileGroupCard {
                ProfileOptionRow(
                    "ⓘ", "Help & Support", "URC helpline & list dispute guide",
                    onClick = { onNavigate("Help") }
                )
                ProfileOptionRow(
                    "ⓘ", "About CanteenConnect", "B.Tech Capstone Project • V1.0.4",
                    onClick = { onNavigate("About") },
                    last = true
                )
            }

            ProfileSectionTitle("SESSION")
            OutlinedButton(
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC9C9)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
            ) {
                Text("⇥  Logout", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(2.dp))
        }

        HomeBottomNavigation(selected = "Profile", onSelect = onNavigate)
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Log out?", fontWeight = FontWeight.Bold, color = Color(0xFF173F1B)) },
            text = { Text("Are you sure you want to log out of CanteenConnect?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    onLogout()
                }) {
                    Text("Yes, log out", color = Color(0xFFB3261E), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = Color(0xFF176B45))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}


@Composable
private fun PersonalDetailsScreen(
    prefs: SharedPreferences,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var name by remember { mutableStateOf(prefs.getString(ACCOUNT_NAME, "") ?: "") }
    var email by remember { mutableStateOf(prefs.getString(ACCOUNT_EMAIL, "") ?: "") }
    var message by remember { mutableStateOf("") }
    val green = Color(0xFF17451F)

    Column(Modifier.fillMaxSize().background(PageBackground).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = green, fontSize = 28.sp, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(14.dp))
            Text("My Profile", color = green, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Personal details", color = DarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Update the name and email saved on this device.", color = MutedText, fontSize = 12.sp)
                    Text("Full name", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = name, onValueChange = { name = it; message = "" }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Enter your full name") })
                    Text("Email address", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = email, onValueChange = { email = it; message = "" }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("name@example.com") })
                    if (message.isNotBlank()) Text(message, color = if (message.startsWith("Saved")) green else Red, fontSize = 12.sp)
                    Button(
                        onClick = {
                            when {
                                name.isBlank() -> message = "Please enter your name."
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> message = "Enter a valid email address."
                                else -> {
                                    prefs.edit().putString(ACCOUNT_NAME, name.trim()).putString(ACCOUNT_EMAIL, email.trim().lowercase(Locale.ROOT)).apply()
                                    message = "Saved successfully."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Save changes", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        HomeBottomNavigation(selected = "Profile", onSelect = onNavigate)
    }
}

@Composable
private fun EligibilityScreen(
    prefs: SharedPreferences,
    spentAmount: Double,
    remainingAmount: Double,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val green = Color(0xFF17451F)
    val cardNumber = prefs.getString("card_number", "DEF-88291-K") ?: "DEF-88291-K"
    Column(Modifier.fillMaxSize().background(PageBackground).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = green, fontSize = 28.sp, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(14.dp))
            Column {
                Text("My Card / Eligibility", color = green, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Smart card and monthly quota", color = MutedText, fontSize = 11.sp)
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E5E8))) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Defence / Canteen Card", color = MutedText, fontSize = 11.sp)
                            Text(cardNumber, color = DarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = Color(0xFFE7F3E8), shape = RoundedCornerShape(20.dp)) {
                            Text("● Active", color = green, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE0E5E8))
                    EligibilityInfoRow("Card holder", prefs.getString(ACCOUNT_NAME, "Canteen User") ?: "Canteen User")
                    EligibilityInfoRow("Base / station", "Tambaram AFS")
                    EligibilityInfoRow("Grocery quota", "${money(ENTITLEMENT)} / month")
                    EligibilityInfoRow("Household items", "Eligible under monthly limits")
                    EligibilityInfoRow("Verification status", "Verified for demo")
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monthly entitlement", color = DarkText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${money(spentAmount)} used", color = MutedText, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFDCE6DD), RoundedCornerShape(20.dp))) {
                        Box(Modifier.fillMaxWidth((spentAmount / ENTITLEMENT).coerceIn(0.0, 1.0).toFloat()).fillMaxHeight().background(green, RoundedCornerShape(20.dp)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Remaining balance", color = MutedText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(money(remainingAmount), color = green, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Demo information only. Confirm eligibility and entitlement with your canteen administrator.", color = MutedText, fontSize = 11.sp)
                }
            }
        }
        HomeBottomNavigation(selected = "Profile", onSelect = onNavigate)
    }
}

@Composable
private fun EligibilityInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MutedText, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        title,
        color = Color(0xFF687383),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 1.dp)
    )
}

@Composable
private fun ProfileGroupCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE0E5E8), RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

@Composable
private fun ProfileOptionRow(
    icon: String,
    title: String,
    subtitle: String,
    trailing: String? = null,
    onClick: () -> Unit,
    last: Boolean = false
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 13.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                color = Color(0xFF687383),
                fontSize = 18.sp,
                modifier = Modifier.width(27.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color(0xFF252B31),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = Color(0xFF687383),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
            if (trailing != null) {
                Spacer(Modifier.width(5.dp))
                Text(
                    trailing,
                    color = if (trailing.contains("Active")) Color(0xFF245B2A) else Color(0xFF687383),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(5.dp))
            Text("›", color = Color(0xFF687383), fontSize = 21.sp)
        }
        if (!last) HorizontalDivider(
            color = Color(0xFFE8EBED),
            thickness = 0.7.dp
        )
    }
}

@Composable
private fun ProfileBottomItem(
    icon: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier.clickable(onClick = onClick).padding(horizontal = 7.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            icon,
            color = if (selected) Color(0xFF17451F) else Color(0xFF687383),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            title,
            color = if (selected) Color(0xFF17451F) else Color(0xFF687383),
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


// --------------------------------------------------
// // SHOPPING PREFERENCES — dedicated settings screen
// --------------------------------------------------

@Composable
private fun ShoppingPreferencesScreen(
    prefs: SharedPreferences,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var defaultUrc by remember {
        mutableStateOf(prefs.getString("shopping_default_urc", "Tambaram URC") ?: "Tambaram URC")
    }
    var defaultMode by remember {
        mutableStateOf(prefs.getString("shopping_default_mode", "Self-Pickup") ?: "Self-Pickup")
    }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val urcOptions = listOf("Tambaram URC", "Pallavaram Depot", "Avadi Canteen", "St. Thomas Mount Canteen")
    val prefGreen = Color(0xFF17451F)
    val prefMuted = Color(0xFF5F646B)
    val prefBorder = Color(0xFFD5D9DC)

    Column(
        modifier = Modifier.fillMaxSize().background(PageBackground).statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).background(Color.White)
                .border(0.5.dp, Color(0xFFE6E8EA)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = prefMuted, fontSize = 28.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.weight(1f))
            Text("Shopping Preferences", color = prefGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("☷", color = prefMuted, fontSize = 14.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text("Default URC", color = prefMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            Box {
                Surface(
                    modifier = Modifier .fillMaxWidth().height(48.dp).clickable { dropdownExpanded = true },
                    color = Color.White,
                    shape = RoundedCornerShape(5.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, prefBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(defaultUrc, color = Color(0xFF25282B), fontSize = 9.sp,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(if (dropdownExpanded) "⌃" else "⌄", color = prefGreen, fontSize = 20.sp)
                    }
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    urcOptions.forEach { option ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(option, fontSize = 14.sp, color = DarkText) },
                            onClick = {
                                defaultUrc = option
                                prefs.edit().putString("shopping_default_urc", option).apply()
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Text("Default Mode", color = prefMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            listOf("Self-Pickup", "Counter Slot Booking").forEach { mode ->
                val selected = defaultMode == mode
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        defaultMode = mode
                        prefs.edit().putString("shopping_default_mode", mode).apply()
                    }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(18.dp)
                            .border(2.dp, if (selected) Color(0xFF168BFF) else Color(0xFF111111), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(Modifier.size(8.dp).background(Color(0xFF168BFF), CircleShape))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(mode, color = Color(0xFF111111), fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        HomeBottomNavigation(selected = "Home", onSelect = onNavigate)
    }
}


// --------------------------------------------------
// HELP & SUPPORT SCREEN
// --------------------------------------------------

@Composable
private fun HelpSupportScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedFaq by remember { mutableStateOf<String?>(null) }

    val helpGreen = Color(0xFF17451F)
    val helpMuted = Color(0xFF667181)
    val helpBorder = Color(0xFFDDE2E6)
    val faqItems = listOf(
        "How is my monthly quota calculated?" to
                "Your monthly quota is based on the entitlement assigned to your smart card. Eligible purchases are counted against the monthly allowance, and the remaining balance updates as purchases are recorded.",
        "Can I cancel my reserved slot?" to
                "If your counter slot has not passed, open your reservation or shopping plan and cancel it if the cancellation option is available. For help with a slot that cannot be cancelled in the app, contact the URC helpline.",
        "I’m unable to visit today ?" to
                "If you cannot visit today, check your reservation details and cancel or change the slot when those options are available. Your shopping list remains available for your next visit.",
        "Wallet related enquiry ?" to
                "For wallet, payment, or refund questions, keep your order ID and payment details ready and contact the URC support team. Never share your smart-card PIN or OTP with anyone."
    )
    val visibleFaqs = faqItems.filter { (question, answer) ->
        question.contains(searchQuery, ignoreCase = true) || answer.contains(searchQuery, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().background(PageBackground).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).background(Color.White)
                .border(0.5.dp, helpBorder).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = Color(0xFF27352B), fontSize = 24.sp,
                modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.weight(1f))
            Text("Help & Support", color = DarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(18.dp))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 12.dp)
                // Keep the full Material text-field height so the input and hint are not clipped.
                .heightIn(min = 56.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = DarkText
            ),
            placeholder = {
                Text(
                    text = "Search FAQs, bills, guidelines...",
                    fontSize = 13.sp,
                    color = helpMuted,
                    maxLines = 1
                )
            },
            leadingIcon = {
                Text("⌕", color = helpMuted, fontSize = 21.sp)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "×",
                        color = helpMuted,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable { searchQuery = "" }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green,
                unfocusedBorderColor = Color(0xFFD4DCD5),
                cursorColor = Green,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                focusedPlaceholderColor = helpMuted,
                unfocusedPlaceholderColor = helpMuted,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { expandedFaq = "URC Helpline" },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("♧", color = Color.Black, fontSize = 22.sp)
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("URC Helpline", color = Color(0xFF111820), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Call 1800-AXXX-XXXX(8AM-8PM)", color = Color.Black, fontSize = 12.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().clickable { expandedFaq = "Bill Dispute Guide" },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("▤", color = Color.Black, fontSize = 22.sp)
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("Bill Dispute Guide", color = Color(0xFF111820), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Step-by-Step dispute process", color = Color.Black, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, helpBorder)
            ) {
                Column {
                    if (visibleFaqs.isEmpty()) {
                        Text("No FAQs found. Try another search.", color = helpMuted, fontSize = 10.sp,
                            modifier = Modifier.padding(14.dp))
                    }
                    visibleFaqs.forEachIndexed { index, (question, answer) ->
                        val isExpanded = expandedFaq == question
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    expandedFaq = if (isExpanded) null else question
                                }.padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(question, color = Color(0xFF20252A), fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text(if (isExpanded) "⌃" else "⌄", color = Color(0xFF171B3B), fontSize = 19.sp)
                            }
                            if (isExpanded) {
                                Text(answer, color = helpMuted, fontSize = 12.sp, lineHeight = 18.sp,
                                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 14.dp, bottom = 12.dp))
                            }
                            if (index != visibleFaqs.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(color = Color(0xFFE5E8EB), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        HomeBottomNavigation(selected = "Profile", onSelect = onNavigate)
    }
}


@Composable
fun AboutCanteenConnectScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        // Shared app-style header: consistent height, typography, and green accent.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                modifier = Modifier
                    .clickable { onBack() }
                    .size(width = 48.dp, height = 48.dp)
                    .wrapContentSize(Alignment.Center),
                fontSize = 30.sp,
                lineHeight = 30.sp,
                color = Color(0xFF17451F)
            )
            Text(
                text = "About CanteenConnect",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF173F20),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        HorizontalDivider(color = Color(0xFFDCE3DD), thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Use the exact logo asset supplied in res/drawable.
            Image(
                painter = painterResource(id = R.drawable.canteenconnect_logo),
                contentDescription = "CanteenConnect Logo",
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(104.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "A digital ecosystem designed to streamline inventory, quota management, and canteen access for Canteen Stores Department (CSD) beneficiaries. This platform optimizes operations for both users and canteen staff.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "B.Tech capstone project",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF53665A)
                )
            }
        }
    }
}
