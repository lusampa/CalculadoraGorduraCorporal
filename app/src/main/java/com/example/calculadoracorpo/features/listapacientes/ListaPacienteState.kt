package com.example.calculadoracorpo.features.listapacientes

import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente

/**
 * @param isLoading True se estiver buscando no banco, false caso contrario
 * @param pacientes é a lista de pacientes vindo do banco
 * @param erro Mesangem de erro caso tenha algum
 */

data class PacienteComUltimaMedida(
    val paciente: Paciente,
    val ultimaMedida: Medidas?,
    val imc: Double?
)

/**
 * Estado da UI
 */
data class ListaPacienteState(
    val isLoading : Boolean = true,
    val pacientesComMedidas : List<PacienteComUltimaMedida> = emptyList(),
    val erro : String? = null
)