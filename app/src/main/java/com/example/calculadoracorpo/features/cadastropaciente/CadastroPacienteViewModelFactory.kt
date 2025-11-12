package com.example.calculadoracorpo.features.cadastropaciente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calculadoracorpo.data.repository.PacienteRepository

class CadastroPacienteViewModelFactory (
    private val repository: PacienteRepository
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(CadastroPacienteViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return CadastroPacienteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknow view model class")
    }
}