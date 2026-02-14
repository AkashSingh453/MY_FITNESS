package com.example.my_fitness.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.my_fitness.Screens.GraphScreen
import com.example.my_fitness.Screens.HomeScreen
import com.example.my_fitness.Screens.LoginScreen
import com.example.my_fitness.Screens.OnboardingScreen
import com.example.my_fitness.Screens.SettingsScreen
import com.example.my_fitness.Screens.SplashScreen
import com.example.my_fitness.Screens.VerificationScreen

@Composable
fun FitnessNavigation() {
    val navController= rememberNavController()
    NavHost(navController = navController ,startDestination = FitnessScreens.SplashScreen.name)
    {
        composable(FitnessScreens.SplashScreen.name){
            SplashScreen(navController = navController)
        }
        composable(FitnessScreens.LoginScreen .name){
            LoginScreen(navController = navController)
        }
        composable(route = FitnessScreens.OnboardingScreen.name,) {
            OnboardingScreen(navController = navController)
        }
        composable(FitnessScreens.HomeScreen.name){
            HomeScreen(navController = navController)
        }
        composable(FitnessScreens.SettingsScreen.name){
            SettingsScreen(navController = navController)
        }
        composable(FitnessScreens.VerificationScreen.name){
            VerificationScreen(navController = navController)
        }
    }
}