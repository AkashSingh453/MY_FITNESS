package com.example.my_fitness.di

import android.content.Context
import androidx.room.Room
import com.example.my_fitness.data.FitnessDao
import com.example.my_fitness.data.FitnessDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideTrackerDao(trackerDatabase: FitnessDatabase): FitnessDao
            = trackerDatabase.FitnessDao()


    @Singleton
    @Provides
    fun providesAppDatabase(@ApplicationContext context: Context):FitnessDatabase
            = Room.databaseBuilder(
        context,
        FitnessDatabase::class.java,
        "Bmi_db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}