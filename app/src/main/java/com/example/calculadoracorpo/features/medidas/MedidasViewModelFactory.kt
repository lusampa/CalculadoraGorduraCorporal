package com.example.calculadoracorpo.features.medidas

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.data.repository.PacienteRepository

/**
 * Factory para criar o MedidasViewModel, injetando o PacienteRepository
 * e o SavedStateHandle para ler o argumento 'pacienteId' da navegação.
 */
class MedidasViewModelFactory(
    private val repository: PacienteRepository,
    owner: SavedStateRegistryOwner, // Necessário para SavedStateHandle
    private val defaultArgs: Int // Usamos o defaultArgs para passar o pacienteId inicial
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(MedidasViewModel::class.java)) {
            // Define o argumento inicial no SavedStateHandle, caso ele não exista
            if (!handle.contains("pacienteId")) {
                handle["pacienteId"] = defaultArgs
            }
            @Suppress("UNCHECKED_CAST")
            return MedidasViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}