package com.example.my_fitness.Screens

import InitAppScaler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_fitness.BackHandler
import com.example.my_fitness.data.Resource
import com.example.my_fitness.navigation.FitnessScreens
import com.example.my_fitness.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import pDp
import pSp

@Composable
fun VerificationScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    BackHandler(navController)
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    var isVerified by remember { mutableStateOf(currentUser?.isEmailVerified ?: false) }
    val load = remember { mutableStateOf(false) }
    val verficaState by authViewModel.verificationState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(verficaState) {
        when (verficaState) {
            is Resource.Loading -> load.value = true
            is Resource.Success -> load.value = false
            is Resource.Error -> load.value = false
            else -> load.value = false
        }
    }

    InitAppScaler()

    // Polling for verification status
    LaunchedEffect(Unit) {
        while (!isVerified) {
            currentUser?.reload()?.addOnCompleteListener {
                isVerified = auth.currentUser?.isEmailVerified ?: false
            }
            delay(1500)
        }
    }

    if (!isVerified) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ✅ Changed: Use Theme background
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.pDp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ✅ Changed: Use Theme onBackground color
                Text(
                    text = "Verify Your Email",
                    fontSize = 24.pSp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.pDp))

                // ✅ Changed: Use Theme onSurface for main text
                Text(
                    text = "Sent to ${currentUser?.email}",
                    fontSize = 14.pSp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // ✅ Changed: Use Theme onSurfaceVariant for secondary text
                Text(
                    text = "(Check your Spam folder!)",
                    fontSize = 14.pSp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.pDp))

                // PRIMARY ACTION: Resend
                Button(
                    onClick = { authViewModel.triggerEmailVerification() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.pDp),
                    shape = RoundedCornerShape(12.pDp),
                    // ✅ Changed: Use Theme Primary Colors
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Resend Email", fontSize = 16.pSp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.pDp))

                // SECONDARY ACTION: Logout / Exit
                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate(FitnessScreens.LoginScreen.name) {
                            popUpTo(FitnessScreens.VerificationScreen.name) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.pDp),
                    shape = RoundedCornerShape(12.pDp),
                    // ✅ Changed: Use Theme Outline and onSurface colors
                    border = BorderStroke(1.pDp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Logout & Use Different Email", fontSize = 14.pSp)
                }
            }

            // LOADING OVERLAY
            if (load.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // ✅ Changed: Added semi-transparent black background
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            enabled = true,
                            onClick = {}, // Consumes clicks
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Changed: Ensure spinner uses primary color
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

    } else {
        navController.navigate(FitnessScreens.OnboardingScreen.name) {
            popUpTo(FitnessScreens.VerificationScreen.name) { inclusive = true }
        }
    }
}