package com.example.my_fitness.navigation

enum class FitnessScreens {
    SplashScreen,
    SignUpScreen,
    LoginScreen,
    HomeScreen,
    SettingsScreen,
    OnboardingScreen,
    GraphScreen,
    VerificationScreen;
    companion object{
        fun fromRoute(route:String?): FitnessScreens
                = when(route?.substringBefore("/")){
            SplashScreen.name -> SplashScreen
            SignUpScreen.name -> SignUpScreen
            LoginScreen.name -> LoginScreen
            HomeScreen.name -> HomeScreen
            SettingsScreen.name -> SettingsScreen
            OnboardingScreen.name -> OnboardingScreen
            GraphScreen.name -> GraphScreen
            VerificationScreen.name -> VerificationScreen

            null -> HomeScreen
            else -> throw IllegalArgumentException("Route $route is not recognised")

        }
    }
}