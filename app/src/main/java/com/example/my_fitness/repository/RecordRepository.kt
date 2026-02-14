package com.example.my_fitness.repository

import com.example.my_fitness.data.FitnessDao
import com.example.my_fitness.model.User
import com.example.my_fitness.model.WeightRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecordRepository @Inject constructor(
    private val fitnessDao: FitnessDao
) {
    suspend fun addWeightRecord(record: WeightRecord) {
        fitnessDao.addWeightRecord(record)
    }
    suspend fun addUser( user : User){
        fitnessDao.insertUser(user)
    }
    suspend fun getUser(userId: String): User? {
        return fitnessDao.getUser(userId)
    }
    suspend fun getLatestWeight(userId: String): WeightRecord? {
        return fitnessDao.getLatestWeight(userId)
    }
    suspend fun getWeightHistory(userId: String): Flow<List<WeightRecord>> {
        return fitnessDao.getUserWeightHistory(userId)
    }

}