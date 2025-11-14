package com.example.calculadoracorpo.features.avaliacoesgeral

import com.example.calculadoracorpo.features.avaliacoes.AvaliacaoResultado

/**
 * Modelo que agrega o resultado da avaliação com o nome do paciente.
 */
data class AvaliacaoComPaciente(
    val resultado: AvaliacaoResultado,
    val nomePaciente: String
)

data class HistoricoGeralState(
    val historicoGeral: List<AvaliacaoComPaciente> = emptyList(),
    val isLoading: Boolean = true,
    val erro: String? = null
)