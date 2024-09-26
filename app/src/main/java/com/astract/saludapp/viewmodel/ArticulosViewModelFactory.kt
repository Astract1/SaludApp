package com.astract.saludapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.astract.saludapp.database.MyDatabaseHelper

class ArticulosViewModelFactory(private val dbHelper: MyDatabaseHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticuloViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticuloViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
