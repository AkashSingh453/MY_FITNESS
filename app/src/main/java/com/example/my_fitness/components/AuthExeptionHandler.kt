package com.example.my_fitness.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.net.SocketTimeoutException

object AuthExceptionHandler {

    fun handle(context: Context, e: Throwable, tag: String = "AuthRepository") {
        val userMessage = when (e) {
            // 1. Account Issues
            is FirebaseAuthInvalidUserException -> "Account not found. Please Sign Up."
            is FirebaseAuthUserCollisionException -> "This email is already registered."
            is FirebaseAuthInvalidCredentialsException -> {
                if (e.message?.contains("badly formatted") == true) "Invalid email format."
                else "Incorrect email or password."
            }

            // 2. Network / Server Issues (Handling the logs you sent)
            is FirebaseNetworkException, is SocketTimeoutException -> "Network error. Check your connection."

            // 3. Google Sign In Issues
            is GetCredentialCancellationException -> "Sign in cancelled."
            is NoCredentialException -> "No credentials found."
            is ApiException -> {
                when (e.statusCode) {
                    7 -> "Network error. Check internet."
                    12500 -> "Google Sign-In failed. Try again."
                    10 -> "Configuration error (SHA-1 missing)."
                    else -> "Google Error: ${e.statusCode}"
                }
            }

            // 4. Specific Internal Errors (from your logs)
            else -> {
                val msg = e.message?.lowercase() ?: ""
                when {
                    msg.contains("unexpected end of stream") -> "Server busy. Please try again."
                    msg.contains("failed to connect") -> "Connection failed. Check Wi-Fi/Data."
                    msg.contains("internal error") -> "Internal error. Please retry."
                    msg.contains("too many requests") -> "Too many attempts. Wait a moment."
                    else -> "Error: ${e.localizedMessage}" // Fallback
                }
            }
        }

        // Log the full technical error for you (Developer)
        Log.e(tag, "Failure: ${e.message}")

        // Show the simple message to the user (User)
        Toast.makeText(context, userMessage, Toast.LENGTH_SHORT).show()
    }
}