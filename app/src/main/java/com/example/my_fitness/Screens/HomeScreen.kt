package com.example.my_fitness.Screens

import InitAppScaler
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_fitness.BackHandler
import com.example.my_fitness.components.DecimalWeightSelectionScreen
import com.example.my_fitness.components.cmToFootString
import com.example.my_fitness.model.WeightRecord
import com.example.my_fitness.navigation.FitnessScreens
import com.example.my_fitness.viewmodel.AuthViewModel
import com.example.my_fitness.viewmodel.RecordViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pDp
import pSp

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    recordViewModel: RecordViewModel = hiltViewModel(),
    navController: NavController
) {
    InitAppScaler()
    BackHandler(navController)
    val context = LocalContext.current
    val auth = Firebase.auth
    val currentUserUid = auth.currentUser?.uid

    // --- STATE COLLECTORS ---
    val userState by recordViewModel.userdata.collectAsState()
    val weightHistory by recordViewModel.userWeightHistory.collectAsState()

    // --- LOCAL UI STATE (For Instant Updates) ---
    var optimisticHeight by remember { mutableStateOf<Int?>(null) }
    var optimisticUnit by remember { mutableStateOf<String?>(null) }

    // --- UI TOGGLES ---
    var showLogWeightDialog by remember { mutableStateOf(false) }
    var showBmiGraph by remember { mutableStateOf(false) }

    // --- DATA LOADING ---
    LaunchedEffect(currentUserUid) {
        if (currentUserUid != null) {
            recordViewModel.getUser(currentUserUid)
        }
    }

    LaunchedEffect(userState) {
        userState?.let {
            recordViewModel.getUserRecord(it)
            if (it.heightCm == optimisticHeight) {
                optimisticHeight = null
                optimisticUnit = null
            }
        }
    }

    // --- CALCULATIONS ---
    val latestRecord = weightHistory.maxByOrNull { it.timestamp }
    val currentWeight = latestRecord?.weightKg ?: 0f
    val targetWeight = userState?.targetWeight ?: 0f

    val effectiveHeightCm = optimisticHeight ?: userState?.heightCm ?: 0
    val effectiveHeightUnit = optimisticUnit ?: userState?.heightUnit ?: "cm"

    val currentBmi = if (effectiveHeightCm > 0 && currentWeight > 0f) {
        val heightM = effectiveHeightCm / 100f
        currentWeight / (heightM * heightM)
    } else 0f

    val heightDisplayValue = if (effectiveHeightUnit == "ft") {
        cmToFootString(effectiveHeightCm)
    } else {
        "$effectiveHeightCm"
    }
    val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
    val savedName = prefs.getString("saved_name", "User") ?: "User"
    with(prefs.edit()) {
        putString("LogInFinal", "Done")
        apply()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // ✅ Changed: Use Theme background
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.pDp)
                .verticalScroll(scrollState)
        ) {

            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // ✅ Changed: Use Theme colors
                    Text(text = "Hello,", fontSize = 16.pSp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = savedName, fontSize = 24.pSp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = {
                    navController.navigate(FitnessScreens.SettingsScreen.name)
                }) {
                    // ✅ Changed: Use Theme color
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(24.pDp))

            // --- STAT CARDS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                // 1. WEIGHT CARD
                // ✅ Changed: Using Surface Variant for background to look good in dark mode
                StatCard(
                    title = "Weight",
                    value = if (currentWeight > 0f) "$currentWeight" else "--",
                    unit = "kg",
                 //   subtitle = if (targetWeight > 0f) "Goal: ${targetWeight.toInt()}" else "Set Goal",
                    color = MaterialTheme.colorScheme.surfaceVariant, // Adaptive Grey
                    textColor = MaterialTheme.colorScheme.primary,    // Theme Primary Color
                    onClick = { }
                )

                // 2. BMI CARD
                val bmiColor = getBmiColor(currentBmi)
                StatCard(
                    title = "BMI",
                    value = String.format("%.1f", currentBmi),
                    unit = "_",
                    subtitle = null,
                    // Keeping specific color logic but ensuring text is readable
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = bmiColor,
                    onClick = { },
                )

                // 3. HEIGHT CARD
                StatCard(
                    title = "Height",
                    value = heightDisplayValue,
                    unit = effectiveHeightUnit,
                    subtitle = null,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.secondary, // Theme Secondary Color
                    onClick = {
                        Toast.makeText(context, "Long Press To Edit Height", Toast.LENGTH_SHORT).show()
                    },
                    onLongClick = {
                        navController.navigate(FitnessScreens.SettingsScreen.name)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.pDp))

            // --- GRAPH SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showBmiGraph) "BMI Trends" else "Weight Trends",
                    fontSize = 20.pSp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.pDp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.pDp)
                    .clip(RoundedCornerShape(16.pDp))
                    // ✅ Changed: Use Theme Surface color
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(5.pDp),
                contentAlignment = Alignment.Center
            ) {
                if (weightHistory.isEmpty()) {
                    Text("No records yet. Add your weight!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val dataPoints = if (showBmiGraph) {
                        weightHistory.sortedBy { it.timestamp }.map { record ->
                            val hM = (if (effectiveHeightCm > 0) effectiveHeightCm else 100) / 100f
                            record.weightKg / (hM * hM)
                        }
                    } else {
                        weightHistory.sortedBy { it.timestamp }.map { it.weightKg }
                    }

                    // Assuming GraphScreen handles its own internal colors,
                    // or you might need to pass the line color here.
                    GraphScreen(
                        dataPoints = dataPoints.map { GraphData(it, it.toLong()) },
                        targetWeight = targetWeight
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.pDp))

            // --- QUICK ADD BUTTON ---
            Button(
                onClick = { showLogWeightDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.pDp),
                shape = RoundedCornerShape(12.pDp),
                // ✅ Changed: Use Theme Primary colors
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.pDp))
                Text("Log Today's Weight", fontSize = 16.pSp)
            }
        }
    }

    // --- DIALOGS ---
    if (showLogWeightDialog) {
        UpdateWeightDialog(
            title = "Log Today's Weight",
            initialWeight = if (currentWeight > 0f) currentWeight else 70f,
            onDismiss = { showLogWeightDialog = false },
            onSave = { newWeight ->
                currentUserUid?.let { uid ->
                    val record = WeightRecord(
                        userId = uid,
                        weightKg = newWeight,
                        timestamp = System.currentTimeMillis()
                    )
                    recordViewModel.saveRecord(record)
                }
                showLogWeightDialog = false
            }
        )
    }
}

// ---------------------------------------------------------
// COMPONENT: Statistic Card
// ---------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.StatCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String? = null,
    color: Color,
    textColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(120.pDp)
            .padding(4.pDp)
            .clip(RoundedCornerShape(16.pDp))
            .background(color)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ✅ Changed: Slight alpha for title to differentiate from value
            Text(text = title, fontSize = 14.pSp, color = textColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.pDp))
            Text(
                text = value,
                fontSize = 22.pSp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (unit.isNotEmpty()) {
                Text(text = unit, fontSize = 12.pSp, color = textColor.copy(alpha = 0.8f))
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.pDp))
                Text(
                    text = subtitle,
                    fontSize = 11.pSp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// DIALOG WRAPPERS
// ---------------------------------------------------------
@Composable
fun UpdateWeightDialog(
    title: String,
    initialWeight: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    DecimalWeightSelectionScreen(
        title = title,
        initialWeight = initialWeight,
        onNextClick = { weight ->
            onSave(weight)
            onDismiss()
        }
    )
}

// ---------------------------------------------------------
// HELPER FUNCTIONS
// ---------------------------------------------------------
fun getBmiColor(bmi: Float): Color {
    // These colors are generally bright enough to work on dark backgrounds too
    return when {
        bmi < 18.5 -> Color(0xFF29B6F6) // Underweight (Blue)
        bmi < 25.0 -> Color(0xFF66BB6A) // Normal (Green)
        bmi < 30.0 -> Color(0xFFFFA726) // Overweight (Orange)
        else -> Color(0xFFEF5350)       // Obese (Red)
    }
}