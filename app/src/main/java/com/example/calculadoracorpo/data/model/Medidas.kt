package com.example.calculadoracorpo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "medidas",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pacienteId"])
    ]
)
data class Medidas (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pacienteId: Int,
    val dataAvaliacao: LocalDate,
    val peso: Double?,
    val protocoloUsado: Protocolo,

    val dobraTriciptal: Double?,
    val dobraSubescapular: Double?,
    val dobraPeitoral: Double?,
    val dobraAxilarMedia: Double?,
    val dobraAbdominal: Double?,
    val dobraSupraIliaca: Double?,
    val dobraCoxa: Double?,

    val circunferenciaBiceps: Double?,
    val circunferenciaBicepsContraido: Double?,
    val circunferenciaPeitoral: Double?,
    val circunferenciaCintura: Double?,
    val circunferenciaAbdomen: Double?,
    val circunferenciaQuadril: Double?,
    val circunferenciaCoxa: Double?,
    val circunferenciaPanturrilha: Double?
)