package com.example.calculadoracorpo.features.listapacientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calculadoracorpo.data.repository.PacienteRepository

class ListaPacienteViewModelFactory (
    private val repository: PacienteRepository ): ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaPacienteViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ListaPacienteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown view model")
        }
    }