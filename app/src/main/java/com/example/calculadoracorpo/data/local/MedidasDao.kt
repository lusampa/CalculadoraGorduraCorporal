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

    @Query("SELECT * FROM medidas WHERE pacienteId = :pacienteId ORDER BY dataAvaliacao DESC")
    fun buscarAvaliacoesdoPaciente(pacienteId: Int) : Flow<List<Medidas>>

    @Query("SELECT * FROM medidas WHERE pacienteId = :pacienteId AND dataAvaliacao < :dataDaAvaliacaoAtual " +
            "ORDER BY dataAvaliacao DESC LIMIT 1")
    suspend fun buscarAvaliacaoAnterior(pacienteId: Int, dataDaAvaliacaoAtual: LocalDate): Medidas?

    // NOVO: Busca uma única medida por ID (usada para Edição)
    @Query("SELECT * FROM medidas WHERE id = :id LIMIT 1")
    suspend fun buscarMedidaPorId(id: Int): Medidas?

    @Query("SELECT * FROM medidas WHERE pacienteId =  :pacienteId ORDER BY dataAvaliacao ASC LIMIT 1")
    suspend fun  buscarPrimeiraAvaliacao(pacienteId: Int): Medidas?

    @Query("SELECT * FROM medidas WHERE pacienteId = :pacienteId ORDER BY dataAvaliacao DESC LIMIT 1")
    suspend fun buscarUltimaAvaliacao(pacienteId: Int):Medidas?

    @Query("SELECT * FROM medidas ORDER BY dataAvaliacao DESC")
    fun buscarTodasAvaliacoes(): Flow<List<Medidas>>

    @Query("""
        SELECT m.* FROM medidas m
        INNER JOIN (
            SELECT pacienteId, MAX(dataAvaliacao) AS maxData 
            FROM medidas
            GROUP BY pacienteId
        ) AS sub ON m.pacienteId = sub.pacienteId AND m.dataAvaliacao = sub.maxData
        ORDER BY m.dataAvaliacao DESC
    """)
    fun listarUltimasAvaliacoesDeCadaPaciente(): Flow<List<Medidas>>
}