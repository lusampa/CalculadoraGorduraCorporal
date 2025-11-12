package com.example.calculadoracorpo.data.local

/**
 * PacienteDao é reponsável por fazer a comunicação entre o DAO(banco) com o código Kotlin,
 * ele executa os comandos SQL, não é uma classe e sim uma INTERFACE
 * Se quiser adicionar tipos de busca, terá que que incluir aqui, um exemplo seria buscar por nome.
 */

import androidx.room.*
import com.example.calculadoracorpo.data.model.Paciente
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir (paciente: Paciente)

    @Update
    suspend fun atualizar(paciente: Paciente)

    @Delete
    suspend fun deletar(paciente: Paciente)

    @Query("SELECT * FROM pacientes ORDER BY nome ASC")
    fun buscarTodos(): Flow<List<Paciente>>

    @Query("SELECT * FROM pacientes WHERE id= :id LIMIT 1")
    suspend fun buscarPorId(id: Int):Paciente?
}