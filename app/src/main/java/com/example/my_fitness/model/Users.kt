package com.example.my_fitness.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val name: String = "",
    val gender: String = "",
    val heightCm: Int = 0,
    val heightUnit: String = "",
    val targetWeight: Float = 0f,
    val age: Int = 0
)