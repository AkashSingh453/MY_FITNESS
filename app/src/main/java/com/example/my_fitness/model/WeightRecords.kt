package com.example.my_fitness.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)