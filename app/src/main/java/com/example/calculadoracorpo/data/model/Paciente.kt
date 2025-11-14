package com.example.calculadoracorpo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period
/** Enum para o sexo biológico por que é essencial para a formula */
enum class Sexo {
    MASCULINO,
    FEMININO
}

/**
 * @param id para o banco de dados
 * @param nome para nome completo do paciente
 * @param dataDeNascimento vamos calcular a data dinamicamente, para o usuario não ter ficar alterando manualmente"
*/
@Entity(tableName = "pacientes")
data class Paciente(
    // 1. Dados pessoais do paciente
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val dataDeNascimento: LocalDate?,
    val sexo: Sexo,
    ){
    // Calcula idade a partir da data de nascimento
    val idade: Int
get() = Period.between(dataDeNascimento, LocalDate.now()).years
}
