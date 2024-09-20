package com.astract.saludapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.astract.saludapp.database.MyDatabaseHelper

class NoticiasViewModelFactory(private val dbHelper: MyDatabaseHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoticiasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoticiasViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
