package com.tecsup.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.avisoscampus.data.AvisosRepository
import com.tecsup.avisoscampus.data.AvisosRepositoryMemoria

class AvisosViewModelFactory(
    private val repo: AvisosRepository = AvisosRepositoryMemoria()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AvisosViewModel::class.java)) {
            return AvisosViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
