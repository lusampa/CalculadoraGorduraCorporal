package com.example.calculadoracorpo.features.avaliacoes

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.data.repository.PdfGenerator

class AvaliacoesViewModelFactory(
    private val repository: PacienteRepository,
    owner: SavedStateRegistryOwner,
    private val defaultArgs: Int, // pacienteId
    private val pdfGenerator: PdfGenerator
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(AvaliacoesViewModel::class.java)) {
            if (!handle.contains("pacienteId")) {
                handle["pacienteId"] = defaultArgs
            }
            @Suppress("UNCHECKED_CAST")
            return AvaliacoesViewModel(repository, pdfGenerator, handle) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}