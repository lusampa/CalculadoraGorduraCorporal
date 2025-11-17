package com.example.calculadoracorpo.data.repository

import com.example.calculadoracorpo.data.local.MedidasDao
import com.example.calculadoracorpo.data.local.PacienteDao
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PacienteRepository(
    private val pacienteDao: PacienteDao,
    private val medidasDao: MedidasDao
) {
    // CRUD Do paciente
    suspend fun inserirPaciente(paciente: Paciente) = pacienteDao.inserir(paciente)
    suspend fun editarPaciente(paciente: Paciente) = pacienteDao.atualizar(paciente)
    suspend fun excluirPaciente(paciente: Paciente) = pacienteDao.deletar(paciente)
    suspend fun buscarPaciente(id: Int): Paciente? = pacienteDao.buscarPorId(id)
    fun listarTodosPacientes(): Flow<List<Paciente>> = pacienteDao.buscarTodos()

    // CRUD de Avaliações (Medidas)
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

    suspend fun buscarAvaliacaoAnterior(pacienteId: Int, dataAtual: LocalDate): Medidas? {
        return medidasDao.buscarAvaliacaoAnterior(pacienteId, dataAtual)
    }

    suspend fun buscarMedidaPorId(id: Int): Medidas? = medidasDao.buscarMedidaPorId(id)
    suspend fun excluirAvaliacao(medidas: Medidas) = medidasDao.excluir(medidas)
    fun listarTodasAvaliacoes(): Flow<List<Medidas>> {
        return medidasDao.buscarTodasAvaliacoes()
    }

    fun listarUltimasAvaliacoesDeCadaPaciente(): Flow<List<Medidas>> {
        return medidasDao.listarUltimasAvaliacoesDeCadaPaciente()
    }
}