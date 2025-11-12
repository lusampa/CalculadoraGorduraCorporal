package com.example.calculadoracorpo.features.listapacientes

import com.example.calculadoracorpo.data.model.Paciente

/**
 * @param isLoading True se estiver buscando no banco, false caso contrario
 * @param pacientes é a lista de pacientes vindo do banco
 * @param erro Mesangem de erro caso tenha algum
 */
data class ListaPacienteState(
    val isLoading : Boolean = true,
    val pacientes : List<Paciente> = emptyList(),
    val erro : String? = null
)