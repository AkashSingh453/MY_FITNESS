package com.example.my_fitness.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import com.example.my_fitness.components.AuthExceptionHandler
import com.example.my_fitness.data.Resource
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val applicationContext: Context
) {

    suspend fun signUp(name: String, email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            val profileUpdates = userProfileChangeRequest { displayName = name }
            user?.updateProfile(profileUpdates)?.await()
            user?.reload()?.await()

            Log.d("AuthRepository", "signUp: success $email")
            Toast.makeText(applicationContext, "SignUp Successful", Toast.LENGTH_SHORT).show()
            Resource.Success(auth.currentUser!!)

        } catch (e: Exception) {
            AuthExceptionHandler.handle(applicationContext, e, "AuthRepository")
            Resource.Error(e)
        }
    }

    suspend fun Login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Login: success $email")
            Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            AuthExceptionHandler.handle(applicationContext, e, "AuthRepository")
            Resource.Error(e)
        }
    }

    suspend fun OAuthLogin(activityContext: Context): Resource<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("781468537299-n6fi98f6lnumg2r248gvcskshrcdqdej.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = CredentialManager.create(activityContext).getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()

                Log.d("AuthRepository", "OAuthLogin: success ${authResult.user?.email}")
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                Resource.Success(authResult.user!!)
            } else {
                Resource.Error(Exception("Invalid credential type"))
            }
        } catch (e: Exception) {
            AuthExceptionHandler.handle(applicationContext, e, "AuthRepository")
            Resource.Error(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Resource<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d("AuthRepository", "Reset email sent to $email")
            Toast.makeText(applicationContext, "Reset email sent to $email", Toast.LENGTH_SHORT).show()
            Resource.Success("Password reset email sent to $email")
        } catch (e: Exception) {
            AuthExceptionHandler.handle(applicationContext, e, "AuthRepository")
            Resource.Error(e)
        }
    }

    suspend fun sendEmailVerification(): Resource<Unit> {
        val user = auth.currentUser
        return try {
            user?.sendEmailVerification()?.await()
            Log.d("AuthRepository", "Verification email sent to ${user?.email}")
            Toast.makeText(applicationContext, "Verification email sent", Toast.LENGTH_SHORT).show()
            Resource.Success(Unit)
        } catch (e: Exception) {
            AuthExceptionHandler.handle(applicationContext, e, "AuthRepository")
            Resource.Error(e)
        }
    }

    suspend fun SignOut() {
        try {
            val credentialManager = CredentialManager.create(applicationContext)
            auth.signOut()
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
        } catch (e: Exception) {
            // We usually don't show toasts for silent sign-out failures, but we log it
            Log.e(TAG, "SignOut failed: ${e.localizedMessage}")
        }
    }
}