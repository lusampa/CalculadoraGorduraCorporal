package com.example.calculadoracorpo.features.avaliacoes

// IMPORTAÇÃO REMOVIDA/AJUSTADA
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Medidas

/**
 * Modelo que agrega a Medida bruta com o resultado do cálculo.
 */
data class AvaliacaoResultado(
    val medidas: Medidas,
    val percentualGordura: Double?,
    val massaMagraKg: Double?,
    val massaGordaKg: Double?,
    val imc: Double?,
    val categoriaRisco: String? // Ex: "Normal", "Obesidade"
)

data class AvaliacoesState(
    val paciente: Paciente? = null,
    val historicoAvaliacoes: List<AvaliacaoResultado> = emptyList(),
    val isLoading: Boolean = true,
    val erro: String? = null
)