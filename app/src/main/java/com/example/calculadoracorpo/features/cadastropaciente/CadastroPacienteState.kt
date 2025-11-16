package com.example.calculadoracorpo.features.cadastropaciente
import com.example.calculadoracorpo.data.model.Sexo
import java.time.LocalDate


data class CadastroPacienteState (
    val nome: String = "",
    val dataNascimento: LocalDate? = null,
    val sexo: Sexo = Sexo.MASCULINO,
    val altura: String = "",

    // UI
    val isLoading: Boolean = false,
    val salvouComSucesso: Boolean = false,
    val erro: String? = null
)