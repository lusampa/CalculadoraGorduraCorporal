package com.example.calculadoracorpo.features.cadastropaciente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calculadoracorpo.data.repository.PacienteRepository

class CadastroPacienteViewModelFactory (
    private val repository: PacienteRepository,
    // [NOVO] Adiciona o ID ao construtor da Factory
    private val pacienteId: Int
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(CadastroPacienteViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            // [MODIFICADO] Passa o ID para o ViewModel
            return CadastroPacienteViewModel(repository, pacienteId) as T
        }
        throw IllegalArgumentException("Unknow view model class")
    }
}