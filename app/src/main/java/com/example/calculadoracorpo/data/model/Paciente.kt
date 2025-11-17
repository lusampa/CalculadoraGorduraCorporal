package com.example.calculadoracorpo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

enum class Sexo {
    MASCULINO,
    FEMININO
}

@Entity(tableName = "pacientes")
data class Paciente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val dataDeNascimento: LocalDate?,
    val sexo: Sexo,
    val altura: Double?,
    ){
    val idade: Int
get() = Period.between(dataDeNascimento, LocalDate.now()).years
}
