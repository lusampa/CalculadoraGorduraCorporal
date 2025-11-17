package com.example.calculadoracorpo.features.medidas

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.navigation.AppRoutes

class MedidasViewModelFactory(
    private val repository: PacienteRepository,
    owner: SavedStateRegistryOwner,
    private val pacienteId: Int, // Argumento de Paciente
    private val avaliacaoId: Int // Argumento de Avaliação (usado para Edição)
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(MedidasViewModel::class.java)) {
            // Define o PacienteId no SavedStateHandle
            if (!handle.contains(AppRoutes.ARG_PACIENTE_ID)) {
                handle[AppRoutes.ARG_PACIENTE_ID] = pacienteId
            }
            // Define o AvaliacaoId no SavedStateHandle
            if (!handle.contains(AppRoutes.ARG_AVALIACAO_ID)) {
                handle[AppRoutes.ARG_AVALIACAO_ID] = avaliacaoId
            }
            @Suppress("UNCHECKED_CAST")
            return MedidasViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}