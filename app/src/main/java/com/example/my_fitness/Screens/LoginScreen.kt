package com.example.my_fitness.Screens

import InitAppScaler
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_fitness.BackHandler
import com.example.my_fitness.data.Resource
import com.example.my_fitness.model.User
import com.example.my_fitness.navigation.FitnessScreens
import com.example.my_fitness.viewmodel.AuthViewModel
import com.example.my_fitness.viewmodel.FireUserViewModel
import com.example.my_fitness.viewmodel.RecordViewModel
import com.google.firebase.auth.FirebaseUser
import pDp
import pSp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    fireUserViewModel: FireUserViewModel = hiltViewModel(),
    recordViewModel: RecordViewModel = hiltViewModel(),
    navController: NavController
) {
    InitAppScaler()
    val context = LocalContext.current
    var isLoginMode by remember { mutableStateOf(true) }
    BackHandler(navController)
    // This state captures the name
    var inputName by remember { mutableStateOf("") }

    val isLoading = false
    val loginState by viewModel.loginState.collectAsState()
    val signUpState by viewModel.signUpState.collectAsState()
    val googleState by viewModel.googleSignInState.collectAsState()
    val verificationstate by viewModel.verificationState.collectAsState()
    val resetpasswordState by viewModel.resetPasswordState.collectAsState()
    val user by fireUserViewModel.getUserState.collectAsState()
    val load = remember { mutableStateOf(false) }

    // 1. Handle Auth Events (Login / SignUp / Google)
    LaunchedEffect(googleState, loginState, signUpState) {
        if (googleState is Resource.Loading || loginState is Resource.Loading || signUpState is Resource.Loading) {
            load.value = true
        }
        if (googleState is Resource.Success) {
            val firebaseUser = (googleState as Resource.Success<FirebaseUser>).data
            val uid = firebaseUser.uid
            val googleName = firebaseUser.displayName
            if (!googleName.isNullOrBlank()) {
                inputName = googleName
                val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("saved_name", googleName).apply()
            }
            fireUserViewModel.getUser(uid)
        }
        if (loginState is Resource.Success) {
            val uid = (loginState as Resource.Success<FirebaseUser>).data.uid
            fireUserViewModel.getUser(uid)
        }
        if (signUpState is Resource.Success) {
            load.value = false
            val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putString(
                    "saved_name",
                    (signUpState as Resource.Success<FirebaseUser>).data.displayName
                )
                apply()
            }
            viewModel.triggerEmailVerification()
            navController.navigate(FitnessScreens.VerificationScreen.name) {
                popUpTo(FitnessScreens.LoginScreen.name) { inclusive = true }
            }
        }
    }
    LaunchedEffect(loginState) {
        if (loginState is Resource.Error) {
            load.value = false
        }
    }
    LaunchedEffect(resetpasswordState) {
        if (resetpasswordState is Resource.Error) {
            load.value = false
        }
        if (resetpasswordState is Resource.Success) {
            load.value = false
        }
        if (resetpasswordState is Resource.Loading) {
            load.value = true
        }
    }
    LaunchedEffect(signUpState) {
        if (signUpState is Resource.Error) {
            load.value = false
        }
    }
    LaunchedEffect(googleState) {
        if (googleState is Resource.Error) {
            load.value = false
        }
    }
    // 2. Handle User Data Fetch & Saving (Same as before)
    LaunchedEffect(user) {
        when (val result = user) {
            is Resource.Success -> {
                val fetchedUser = result.data

                // Now inputName will hold the Google Name if we used OAuth
                val finalName =
                    if (inputName.isNotBlank()) inputName else fetchedUser.name.ifBlank { "User" }

                val userToSave = User(
                    id = fetchedUser.id,
                    name = finalName,
                    gender = fetchedUser.gender,
                    heightCm = fetchedUser.heightCm,
                    heightUnit = fetchedUser.heightUnit,
                    targetWeight = fetchedUser.targetWeight,
                    age = fetchedUser.age
                )
                recordViewModel.saveUser(userToSave)

                val prefs = context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
                with(prefs.edit()) {
                    putString("saved_name", finalName)
                    apply()
                }

                if (signUpState !is Resource.Success) {
                    navController.navigate(FitnessScreens.HomeScreen.name) {
                        popUpTo(FitnessScreens.LoginScreen.name) { inclusive = true }
                    }
                } else {
                    navController.navigate(FitnessScreens.VerificationScreen.name) {
                        popUpTo(FitnessScreens.LoginScreen.name) { inclusive = true }
                    }
                }
                load.value = false
            }

            is Resource.Error -> {
                // Even if we go to Onboarding, we already saved the Google Name to Prefs above!
                navController.navigate(FitnessScreens.VerificationScreen.name) {
                    popUpTo(FitnessScreens.LoginScreen.name) { inclusive = true }
                }
                load.value = false
            }

            else -> {
                load.value = false
            }
        }
    }

    // --- UI START ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // ✅ Changed: Use Theme background instead of hardcoded White
                .background(MaterialTheme.colorScheme.background)
                .padding(24.pDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLoginMode) "Welcome Back" else "Create Account",
                fontSize = 28.pSp,
                fontWeight = FontWeight.Bold,
                // ✅ Changed: Use onBackground (Black in Light mode, White in Dark mode)
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.pDp))

            if (isLoginMode) {
                LoginSection(
                    viewModel = viewModel,
                    isLoading = isLoading,
                    onToggleMode = { isLoginMode = false },
                    onGoogleSignIn = { viewModel.onGoogleSignInClick(context) }
                )
            } else {
                SignUpSection(
                    viewModel = viewModel,
                    isLoading = isLoading,
                    onToggleMode = { isLoginMode = true },
                    onGoogleSignIn = { viewModel.onGoogleSignInClick(context) },
                    onSignup = { name -> inputName = name }
                )
            }
        }

        // --- LOADING OVERLAY ---
        if (load.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ✅ Added: Semi-transparent black background for dimming effect
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        enabled = true,
                        onClick = {}, // Consumes clicks
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Removes the ripple effect when clicking the overlay
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ Changed: Ensure spinner is visible on the dark overlay
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun LoginSection(
    viewModel: AuthViewModel,
    isLoading: Boolean,
    onToggleMode: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // ✅ Helper for reusable text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            // ✅ Changed: Use onSurfaceVariant for label
            label = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.pSp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(16.pDp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            // ✅ Changed: Use onSurfaceVariant for label
            label = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.pSp) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image =
                    if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = "Toggle Password",
                        modifier = Modifier.size(24.pDp),
                        // ✅ Changed: Use Theme tint
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            singleLine = true,
            colors = textFieldColors
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = {
                if (email.isNotEmpty()) viewModel.resetPassword(email)
                else Toast.makeText(context, "Enter email to reset", Toast.LENGTH_SHORT).show()
            }) {
                Text("Forgot Password?", color = Color(0xFF4DB6AC), fontSize = 14.pSp)
            }
        }

        Spacer(modifier = Modifier.height(24.pDp))

        Button(
            onClick = { viewModel.onLoginClick(email, password) },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.pDp),
            shape = RoundedCornerShape(8.pDp),
            // ✅ Added: Explicit button colors for consistency
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.pDp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Sign In", fontSize = 16.pSp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.pDp))

        Row {
            // ✅ Changed: Use Theme color
            Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.pSp)
            Text(
                text = "Sign Up.",
                color = Color(0xFF4DB6AC),
                fontWeight = FontWeight.Bold,
                fontSize = 14.pSp,
                modifier = Modifier.clickable { onToggleMode() }
            )
        }

        Spacer(modifier = Modifier.height(24.pDp))
        GoogleSignInButton(onClick = onGoogleSignIn)
    }
}

@Composable
fun SignUpSection(
    viewModel: AuthViewModel,
    isLoading: Boolean,
    onToggleMode: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignup: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // ✅ Helper for reusable text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            // ✅ Changed: Use onSurfaceVariant
            label = { Text("Full Name", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.pSp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(16.pDp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            // ✅ Changed: Use onSurfaceVariant
            label = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.pSp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(16.pDp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            // ✅ Changed: Use onSurfaceVariant
            label = { Text("Create Password", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.pSp) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image =
                    if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = "Toggle Password",
                        modifier = Modifier.size(24.pDp),
                        // ✅ Changed: Use Theme tint
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            singleLine = true,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(24.pDp))

        Button(
            onClick = {
                onSignup(name)
                viewModel.onSignUpClick(name, email, password)
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.pDp),
            shape = RoundedCornerShape(8.pDp),
            // ✅ Added: Explicit button colors
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.pDp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Sign Up", fontSize = 16.pSp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.pDp))

        Row {
            // ✅ Changed: Use Theme color
            Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.pSp)
            Text(
                text = "Sign In.",
                color = Color(0xFF4DB6AC),
                fontWeight = FontWeight.Bold,
                fontSize = 14.pSp,
                modifier = Modifier.clickable { onToggleMode() }
            )
        }

        Spacer(modifier = Modifier.height(24.pDp))
        GoogleSignInButton(onClick = onGoogleSignIn)
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.pDp),
        shadowElevation = 4.pDp,
        color = Color.White, // Keeping white as standard for Google Auth buttons
        modifier = Modifier
            .fillMaxWidth()
            .height(50.pDp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("G ", fontWeight = FontWeight.Bold, fontSize = 20.pSp, color = Color.Red)
            Spacer(modifier = Modifier.width(8.pDp))
            // ✅ Changed: Explicit Black to ensure visibility on the White button in Dark Mode
            Text("Sign in with Google", fontSize = 16.pSp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}