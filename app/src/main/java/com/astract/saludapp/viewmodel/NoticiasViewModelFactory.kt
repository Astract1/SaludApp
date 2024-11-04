package com.astract.saludapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class NoticiasViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoticiasViewModel::class.java)) {
            return NoticiasViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
