package com.example.calculadoracorpo.data.model

/**
* @param peso em quilogramas (ex: 70.3)
* @param altura em cm (ex: 178)
*
*  ## Dobras Cutâneas (em milimetros) --
* Usamos Double? (nullable) para o caso da avalição preencher apenas 3 dobras e não as 7.
* @param coxa (Usada no P3-Masculino,Feminino e P7)
* @param peitoral (Usada no P3-Masculino e P7)
* @param abdominal (Usada no P3-Masculino e P7)
* @param tricipital (Usada no P3-Feminino e P7)
* @param supraIliaca (Usada no P3-Feminino e P7)
* @param subescapular (Usado no P7)
* @param axilarMedia (Usada no P7)
*/

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medidas",
    foreignKeys = [ForeignKey(entity = Paciente::class, parentColumns = ["id"],
        childColumns = ["pacienteId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("pacienteId")]
)

data class Medidas (
    @PrimaryKey val id:Int,
    val pacienteId: Int,
    val dataAvaliacao: LocalDate,
// Medidas antropométricas básicas
    val altura: Double?,
    val peso: Double?,
    val protocoloUsado: Protocolo,
// Dobras Protocolo 3 e 7 (Todos)
    val peitoral: Double?,
    val abdominal: Double?,
    val triceps: Double?,
    val axilarMedia: Double?,
    val subescapular: Double?,
    val supraIliaca: Double?,
    val coxa: Double?,
) {
    // Calculo do IMC de Bonus
    val imc: Double?
        get(){
            if (peso == null || altura == null || altura == 0.0) return null
            val alturaEmMetros = altura/100.0
            return peso/(alturaEmMetros * alturaEmMetros)
        }
}
