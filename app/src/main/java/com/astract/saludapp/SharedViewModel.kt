package com.astract.saludapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SharedViewModel : ViewModel() {
    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    fun setUserId(id: String?) {
        _userId.value = id
    }


    fun getUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }
}