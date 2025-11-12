package com.example.calculadoracorpo.data.repository
/** É o mediador entre o Room e a ViewModel e o banco de dados*/

import com.example.calculadoracorpo.data.local.MedidasDao
import com.example.calculadoracorpo.data.local.PacienteDao
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PacienteRepository(
private val pacienteDao: PacienteDao,
private val  medidasDao: MedidasDao
) {
    // CRUD Do paciente
    suspend fun inserirPaciente(paciente: Paciente) = pacienteDao.inserir(paciente)
    suspend fun editarPaciente(paciente: Paciente) = pacienteDao.atualizar(paciente)
    suspend fun excluirPaciente(paciente: Paciente) = pacienteDao.deletar(paciente)
    suspend fun buscarPaciente(id: Int): Paciente? = pacienteDao.buscarPorId(id)
    fun listarTodosPacientes(): Flow<List<Paciente>> = pacienteDao.buscarTodos()

    // CRUD Das Medidas
    suspend fun inserirAvaliacao(medidas: Medidas) = medidasDao.inserir(medidas)
    fun buscarAvaliacoes(pacienteId: Int): Flow<List<Medidas>> {
        return medidasDao.buscarAvaliacoesdoPaciente(pacienteId)
    }

    suspend fun buscarPrimeiraAvaliacao(pacienteId: Int): Medidas? {
        return medidasDao.buscarPrimeiraAvaliacao(pacienteId)
    }

    suspend fun buscarUltimaAvaliacao(pacienteId: Int): Medidas? {
        return medidasDao.buscarUltimaAvaliacao(pacienteId)
    }

    suspend fun buscarAvaliacaoAnterior(pacienteId: String, dataAtual: LocalDate): Medidas? {
        return medidasDao.buscarAvaliacaoAnterior(pacienteId, dataAtual)
    }
}
