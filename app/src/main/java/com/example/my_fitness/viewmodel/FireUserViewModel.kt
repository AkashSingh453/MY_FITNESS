package com.example.my_fitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_fitness.data.Resource
import com.example.my_fitness.model.User
import com.example.my_fitness.repository.FireUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireUserViewModel @Inject constructor(
    private val userRepository: FireUserRepository
) : ViewModel() {

    // State for Saving User
    private val _saveUserState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val saveUserState: StateFlow<Resource<Unit>> = _saveUserState.asStateFlow()

    // State for Fetching User
    private val _getUserState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val getUserState: StateFlow<Resource<User>> = _getUserState.asStateFlow()

    fun saveUser(user: User) {
        viewModelScope.launch {
            _saveUserState.value = Resource.Loading
            val result = userRepository.saveUser(user)
            _saveUserState.value = result
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            _getUserState.value = Resource.Loading
            val result = userRepository.getUser(userId)
            _getUserState.value = result
        }
    }
}