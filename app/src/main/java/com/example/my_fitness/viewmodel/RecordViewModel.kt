package com.example.my_fitness.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_fitness.data.Resource
import com.example.my_fitness.model.User
import com.example.my_fitness.model.WeightRecord
import com.example.my_fitness.repository.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository
) : ViewModel() {
    val _userWeightHistory = MutableStateFlow<List<WeightRecord>>(emptyList())
    val userWeightHistory = _userWeightHistory.asStateFlow()

    val _user = MutableStateFlow<User?>(null)
    val userdata = _user.asStateFlow()

    fun getUserRecord(user : User) {
        viewModelScope.launch(Dispatchers.IO) {
            recordRepository.getWeightHistory(user.id).distinctUntilChanged()
                .collect { weightlist->
                    if (weightlist.isEmpty()){
                        _userWeightHistory.value = emptyList()
                        Log.d("userWeightHistoryRepository",":Empty List")
                    }else{
                        _userWeightHistory.value = weightlist
                    }
                }
        }
    }
    fun getUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("RecordViewModel",":getting User ${userId}")
           val poop = recordRepository.getUser(userId)
            _user.value = poop
        }
    }
    fun saveUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("RecordViewModel",":Saving User ${user.name}")
            recordRepository.addUser(user)
        }
    }
    fun saveRecord(weightRecord: WeightRecord){
        Log.d("RecordViewModel",":Saving Record")
        viewModelScope.launch(Dispatchers.IO) {
            recordRepository.addWeightRecord(weightRecord)
        }
    }
    private val _latest = MutableStateFlow<WeightRecord?>(null)
    val latest = _latest.asStateFlow()
    fun getLatestWeight(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _latest.value = recordRepository.getLatestWeight(userId)
        }
    }
}