package com.example.my_fitness.Screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_fitness.BackHandler
import com.example.my_fitness.components.*
import com.example.my_fitness.model.User
import com.example.my_fitness.navigation.FitnessScreens
import com.example.my_fitness.viewmodel.AuthViewModel
import com.example.my_fitness.viewmodel.FireUserViewModel
import com.example.my_fitness.viewmodel.RecordViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    fireUserViewModel: FireUserViewModel = hiltViewModel(),
    recordViewModel: RecordViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val auth = Firebase.auth
    BackHandler(navController)

    // 1. Unified State
    var gender by remember { mutableStateOf("Male") }
    var height by remember { mutableStateOf(0) }
    var heightUnit by remember { mutableStateOf("cm") }
    var weight by remember { mutableFloatStateOf(70f) }
    var age by remember { mutableIntStateOf(24) }

    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // 2. Calculate Progress
    val targetProgress = (pagerState.currentPage + 1) / 4f

    // 3. Smooth Animation
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Changed: Use Theme background
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {

        // --- THE UNIFIED PROGRESS BAR ---
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                // ✅ Changed: Use Theme surfaceVariant for the track
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    // ✅ Changed: Use Theme Primary (Green) for the progress
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current

        // --- The Pager ---
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> GenderSelectionScreen(
                    onSaveClick = { selectedGender ->
                        gender = selectedGender
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> HeightSelectionScreen(
                    onSaveClick = { selectedHeight, selectedUnit ->
                        height = selectedHeight
                        heightUnit = selectedUnit
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                2 -> WeightSelectionScreen(
                    onNextClick = { selectedWeight ->
                        weight = selectedWeight
                        scope.launch { pagerState.animateScrollToPage(3) }
                    }
                )
                3 -> AgeSelectionScreen(
                    onSaveClick = { selectedAge ->
                        age = selectedAge
                        val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
                        val savedName = prefs.getString("saved_name", "User") ?: "User"
                        val user = User(Firebase.auth.uid.toString(), savedName, gender, height, heightUnit, weight, age)

                        fireUserViewModel.saveUser(user)
                        recordViewModel.saveUser(user)

                        with(prefs.edit()) {
                            putString("LogInFinal", "Done" )
                            apply()
                        }
                        navController.navigate(FitnessScreens.HomeScreen.name) {
                            popUpTo(FitnessScreens.OnboardingScreen.name) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}