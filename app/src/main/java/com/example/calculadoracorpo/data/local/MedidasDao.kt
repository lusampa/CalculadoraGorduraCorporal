package com.example.calculadoracorpo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.calculadoracorpo.data.model.Medidas
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
@Dao
interface MedidasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(medidas: Medidas)

    @Update
    suspend fun editarMedida (medidas:Medidas)

    @Delete
    suspend fun excluir(medidas:Medidas)

    @Query("SELECT * FROM medidas WHERE pacienteId = :pacienteId ORDER BY dataAvaliacao ASC")
    fun buscarAvaliacoesdoPaciente(pacienteId: Int) : Flow<List<Medidas>>

    /** A função abaixo busca a avaliação imediatamente anterior à data fornecida para um paciente.
    1. Filtra pelo pacienteId.
    2. Filtra por datas MENORES (<) que a data da avaliação atual.
    3. Ordena pela data em ordem DECRESCENTE (a mais próxima primeiro).
    4. Pega apenas a PRIMEIRA (LIMIT 1).
    */
    @Query("SELECT * FROM medidas WHERE pacienteId = :pacienteId AND dataAvaliacao < :dataDaAvaliacaoAtual " +
            "ORDER BY dataAvaliacao DESC LIMIT 1")
    suspend fun buscarAvaliacaoAnterior(pacienteId: String, dataDaAvaliacaoAtual: LocalDate): Medidas?


    // Função de buscar a primeira e a última caso necessário
    @Query("SELECT * FROM medidas WHERE pacienteId =  :pacienteId ORDER BY dataAvaliacao ASC LIMIT 1")
    suspend fun  buscarPrimeiraAvaliacao(pacienteId: Int): Medidas?

    @Query("SELECT * FROM medidas WHERE pacienteId = :pacienteId ORDER BY dataAvaliacao DESC LIMIT 1")
    suspend fun buscarUltimaAvaliacao(pacienteId: Int):Medidas?
}