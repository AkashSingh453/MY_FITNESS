package com.example.my_fitness.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.my_fitness.model.User
import com.example.my_fitness.model.WeightRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUser(user: User)

        @Query("SELECT * FROM users WHERE id = :userId")
        suspend fun getUser(userId: String): User?

        // Weight History for Graph
        @Insert
        suspend fun addWeightRecord(record: WeightRecord)

        @Query("SELECT * FROM weight_records WHERE userId = :userId ORDER BY timestamp DESC")
        fun getUserWeightHistory(userId: String): Flow<List<WeightRecord>>

        @Query("SELECT * FROM weight_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
        suspend fun getLatestWeight(userId: String): WeightRecord?

}