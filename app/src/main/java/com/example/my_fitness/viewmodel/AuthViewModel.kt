package com.example.my_fitness.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_fitness.data.Resource
import com.example.my_fitness.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signUpState: StateFlow<Resource<FirebaseUser>?> = _signUpState.asStateFlow()

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginState: StateFlow<Resource<FirebaseUser>?> = _loginState.asStateFlow()

    private val _googleSignInState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val googleSignInState: StateFlow<Resource<FirebaseUser>?> = _googleSignInState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<String>?>(Resource.Loading)
    val resetPasswordState: StateFlow<Resource<String>?> = _resetPasswordState.asStateFlow()

    private val _verificationState = MutableStateFlow<Resource<Unit>?>(null)
    val verificationState: StateFlow<Resource<Unit>?> = _verificationState.asStateFlow()

    fun triggerEmailVerification() {
        viewModelScope.launch {
            _verificationState.value = Resource.Loading
            val result = authRepository.sendEmailVerification()
            _verificationState.value = result
        }
    }

    fun onSignUpClick(name : String ,email: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = Resource.Loading
            val result = authRepository.signUp( name  , email , password)
            _signUpState.value = result
        }
    }

    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = authRepository.Login(email, password)
            _loginState.value = result
        }
    }

    fun onGoogleSignInClick(activityContext: Context) {
        viewModelScope.launch {
            _googleSignInState.value = Resource.Loading
            val result = authRepository.OAuthLogin(activityContext)
            _googleSignInState.value = result
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading
            // Fixed: changed 'repo' to 'authRepository'
            val result = authRepository.sendPasswordResetEmail(email)
            _resetPasswordState.value = result
        }
    }

    fun clearResetPasswordState() {
         _resetPasswordState.value = Resource.Loading
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.SignOut()
//            _loginState.value = null
//            _signUpState.value = null
//            _googleSignInState.value = null
        }
    }
}