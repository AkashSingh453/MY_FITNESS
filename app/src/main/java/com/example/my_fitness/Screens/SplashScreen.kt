package com.example.my_fitness.Screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_fitness.data.Resource
import com.example.my_fitness.model.User
import com.example.my_fitness.navigation.FitnessScreens
import com.example.my_fitness.viewmodel.AuthViewModel
import com.example.my_fitness.viewmodel.FireUserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Contextual
import pDp

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    fireUserViewModel: FireUserViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userState by fireUserViewModel.getUserState.collectAsState()
    val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
    val LogInFinal = prefs.getString("LogInFinal"  , "not" ) ?: "not"
    if(LogInFinal == "Done" ){
        navController.navigate(FitnessScreens.HomeScreen.name){
            popUpTo(FitnessScreens.SplashScreen.name){inclusive = true}
        }
    }
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // 1. No user logged in -> Go to Login
            navController.navigate(FitnessScreens.LoginScreen.name) {
                popUpTo(FitnessScreens.SplashScreen.name) { inclusive = true }
            }
        } else {
            // 2. Force a reload to get the latest 'isEmailVerified' status
            currentUser.reload().await()

            if (currentUser.isEmailVerified) {
                // 3. Logged in & Verified -> Go to Home
                navController.navigate(FitnessScreens.OnboardingScreen.name) {
                    popUpTo(FitnessScreens.SplashScreen.name) { inclusive = true }
                }
            } else {
                navController.navigate(FitnessScreens.VerificationScreen.name) {
                    popUpTo(FitnessScreens.SplashScreen.name) { inclusive = true }
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
     //   CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(40.pDp))
    }
}