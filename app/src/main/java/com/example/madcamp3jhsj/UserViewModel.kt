package com.example.madcamp3jhsj

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madcamp3jhsj.data.User
import com.example.madcamp3jhsj.data.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _lastLoggedInUser = MutableLiveData<User?>()
    val lastLoggedInUser: LiveData<User?> = _lastLoggedInUser
    fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun getUserByUsername(username: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            callback(user)
        }
    }
    // 중복 확인 포함 유저 저장 함수
    fun saveUserIfNotExists(user: User, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val existingUser = repository.getUserByUsername(user.username)
            if (existingUser == null) {
                repository.insertUser(user)
                callback(true) // 새 유저가 저장됨
            } else {
                callback(false) // 유저가 이미 존재함
            }
        }
    }

    fun clearLastLoginExcept(usernameToExclude: String) {
        viewModelScope.launch {
            val allUsers = repository.getAllUsers() // 모든 유저 조회
            allUsers.forEach { user ->
                if (user.username != usernameToExclude) {
                    val updatedUser = user.copy(last_login = 0) // 토큰 값 초기화
                    repository.insertUser(updatedUser) // 업데이트
                }
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun getLastLoginUsers(callback: (List<User>) -> Unit) {
        viewModelScope.launch {
            val users = repository.getUsersWithLastLoginMinusOne()
            callback(users)
        }
    }

    fun fetchLastLoggedInUser() {
        viewModelScope.launch {
            val user = repository.getUsersWithLastLoginMinusOne().firstOrNull()
            _lastLoggedInUser.postValue(user)
        }
    }
}
