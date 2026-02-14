package com.example.my_fitness.repository

import android.util.Log
import com.example.my_fitness.data.Resource
import com.example.my_fitness.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireUserRepository@Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    suspend fun saveUser(user: User): Resource<Unit> {
        return try {
            usersCollection.document(user.id)
                .set(user, SetOptions.merge()) // Merges data instead of overwriting
                .await()
            Log.d("FireUserRepository", "User saved successfully")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.d("FireUserRepository", "error : ${e.message}")
            Resource.Error(e)
        }
    }
    suspend fun getUser(userId: String): Resource<User> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Log.d("FireUserRepository", "User fetched successfully : ${user.name}")
                Resource.Success(user)
            } else {
                Resource.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.d("FireUserRepository", e.message.toString())
            Resource.Error(e)
        }
    }
}