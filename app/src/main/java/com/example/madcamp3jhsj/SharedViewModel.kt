package com.example.madcamp3jhsj

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.madcamp3jhsj.data.User
import com.example.madcamp3jhsj.data.UserRepository
import kotlinx.coroutines.launch

class SharedViewModel(private val repository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // 유저 이름으로 데이터 검색
    fun loadUserByUsername(username: String) {
        viewModelScope.launch {
            val userData = repository.getUserByUsername(username)
            _user.postValue(userData) // 검색된 유저 데이터를 업데이트
        }
    }
    fun logout(username: String) {
        viewModelScope.launch {
            // 유저의 last_login을 0으로 설정
            val user = repository.getUserByUsername(username)
            user?.let {
                val updatedUser = it.copy(last_login = 0)
                repository.insertUser(updatedUser) // 업데이트된 유저 저장
            }

            // LiveData로 유저 상태 초기화
            _user.postValue(null)
        }
    }
    fun deleteAccount(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
}


class SharedViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            return SharedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

