package com.example.calculadoracorpo.features.avaliacoes

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.data.repository.PdfGenerator // Importado

class AvaliacoesViewModelFactory(
    private val repository: PacienteRepository,
    owner: SavedStateRegistryOwner, // Necessário para SavedStateHandle
    private val defaultArgs: Int, // Usamos o defaultArgs para passar o pacienteId inicial
    private val pdfGenerator: PdfGenerator // NOVO ARGUMENTO
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(AvaliacoesViewModel::class.java)) {
            // Define o argumento inicial no SavedStateHandle, caso ele não exista
            if (!handle.contains("pacienteId")) {
                handle["pacienteId"] = defaultArgs
            }
            @Suppress("UNCHECKED_CAST")
            // AQUI: Passa a nova dependência (pdfGenerator) para o ViewModel
            return AvaliacoesViewModel(repository, pdfGenerator, handle) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}