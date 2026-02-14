package com.example.my_fitness.data

import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.my_fitness.model.User
import com.example.my_fitness.model.WeightRecord

@Database(entities = [User::class, WeightRecord::class], version = 2 , exportSchema = false)
// @TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase(){
    abstract fun FitnessDao() : FitnessDao
}