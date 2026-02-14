package com.example.my_fitness.Screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_fitness.BackHandler
import com.example.my_fitness.components.HeightSelectionScreen
import com.example.my_fitness.components.WeightSelectionScreen
import com.example.my_fitness.components.cmToFootString
import com.example.my_fitness.navigation.FitnessScreens
import com.example.my_fitness.viewmodel.AuthViewModel
import com.example.my_fitness.viewmodel.FireUserViewModel
import com.example.my_fitness.viewmodel.RecordViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    recordViewModel: RecordViewModel = hiltViewModel(),
    fireUserViewModel: FireUserViewModel = hiltViewModel()
) {
    BackHandler(navController)
    val context = LocalContext.current
    val auth = Firebase.auth
    val currentUserUid = auth.currentUser?.uid
    val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
    val savedName = prefs.getString("saved_name", "User") ?: "User"

    val userState by recordViewModel.userdata.collectAsState()

    // --- OPTIMISTIC STATE ---
    var optimisticTarget by remember { mutableStateOf<Float?>(null) }
    var optimisticHeight by remember { mutableStateOf<Int?>(null) }
    var optimisticUnit by remember { mutableStateOf<String?>(null) }

    var showTargetDialog by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserUid) {
        if (currentUserUid != null) recordViewModel.getUser(currentUserUid)
    }

    LaunchedEffect(userState) {
        userState?.let {
            if (it.targetWeight == optimisticTarget) optimisticTarget = null
            if (it.heightCm == optimisticHeight) optimisticHeight = null
            if (it.heightUnit == optimisticUnit) optimisticUnit = null
        }
    }

    val displayTarget = optimisticTarget ?: userState?.targetWeight ?: 0f
    val displayHeight = optimisticHeight ?: userState?.heightCm ?: 0
    val displayUnit = optimisticUnit ?: userState?.heightUnit ?: "cm"

    val heightString = if (displayUnit == "ft") {
        cmToFootString(displayHeight)
    } else {
        "$displayHeight cm"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- USER AVATAR ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userState?.name?.take(1)?.uppercase() ?: "U",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- USER NAME ---
            Text(
                text = savedName,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- MENU ITEMS (Now with distinct colors) ---

            // 1. Target Weight (GREEN TINT)
            SettingsItem(
                title = "Change Target Weight",
                value = "${displayTarget.toInt()} kg",
                // ✅ Green tint background
                cardColor = MaterialTheme.colorScheme.primaryContainer,
                // ✅ Green tint text
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { showTargetDialog = true }
            )

            // 2. Height (BLUE/TEAL TINT)
            SettingsItem(
                title = "Update Height",
                value = heightString,
                // ✅ Secondary tint background
                cardColor = MaterialTheme.colorScheme.secondaryContainer,
                // ✅ Secondary tint text
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { showHeightDialog = true }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- LOGOUT BUTTON (RED TINT) ---
            Button(
                onClick = {
                    authViewModel.signOut()
                    with(prefs.edit()) {
                        putString("LogInFinal", "not" )
                        apply()
                    }
                    navController.navigate(FitnessScreens.LoginScreen.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- DIALOGS ---
    if (showTargetDialog) {
        WeightSelectionScreen(
            title = "Set Target Weight",
            initialWeight = if (displayTarget > 0) displayTarget else 70f,
            onNextClick = { newTarget ->
                optimisticTarget = newTarget
                userState?.let {
                    val updated = it.copy(targetWeight = newTarget)
                    recordViewModel.saveUser(updated)
                    fireUserViewModel.saveUser(updated)
                }
                Toast.makeText(context, "Target updated!", Toast.LENGTH_SHORT).show()
                showTargetDialog = false
            }
        )
    }

    if (showHeightDialog && userState != null) {
        HeightSelectionScreen(
            currentHeightCm = if(displayHeight > 0) displayHeight else 170,
            isCmInitially = displayUnit == "cm",
            onSaveClick = { newCm, newUnit ->
                optimisticHeight = newCm
                optimisticUnit = newUnit
                val updated = userState!!.copy(heightCm = newCm, heightUnit = newUnit)
                recordViewModel.saveUser(updated)
                fireUserViewModel.saveUser(updated)
                Toast.makeText(context, "Height updated!", Toast.LENGTH_SHORT).show()
                showHeightDialog = false
            }
        )
    }
}

// --- HELPER COMPONENT ---
@Composable
fun SettingsItem(
    title: String,
    value: String,
    cardColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor) // ✅ Uses specific color
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor // ✅ Text matches background contrast
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = contentColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f)
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}