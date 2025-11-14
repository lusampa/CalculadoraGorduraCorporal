package com.example.calculadoracorpo.features.avaliacoesgeral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calculadoracorpo.data.repository.PacienteRepository

class HistoricoGeralViewModelFactory (
    private val repository: PacienteRepository
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(HistoricoGeralViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return HistoricoGeralViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}