package com.astract.saludapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ArticulosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticuloViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticuloViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
