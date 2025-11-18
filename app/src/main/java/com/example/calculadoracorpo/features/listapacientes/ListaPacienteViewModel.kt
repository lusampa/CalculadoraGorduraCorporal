package com.example.calculadoracorpo.features.listapacientes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.repository.CalculadoraGorduraCorporal
import com.example.calculadoracorpo.data.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch




class ListaPacienteViewModel (
    private val repository: PacienteRepository):ViewModel() {
    private val _uiState = MutableStateFlow(ListaPacienteState())
    val uiState: StateFlow<ListaPacienteState> = _uiState.asStateFlow()
    private val calculadora = CalculadoraGorduraCorporal()
    init {
        buscarPacientesComMedidas()
    }

    private fun buscarPacientesComMedidas() {
        viewModelScope.launch {

            val pacientesFlow = repository.listarTodosPacientes()
            val ultimasMedidasFlow = repository.listarUltimasAvaliacoesDeCadaPaciente() // <<< NOVO

            // Combina Pacientes com as Ultimas Avaliações
            pacientesFlow.combine(ultimasMedidasFlow) { listaPacientes, listaUltimasMedidas -> // <<< ALTERADO

                // 1. Cria o mapa de acesso rápido (Otimização!)
                val ultimasMedidasMap = listaUltimasMedidas.associateBy { it.pacienteId }

                listaPacientes.map { paciente ->
                    val ultimaMedida = ultimasMedidasMap[paciente.id]

                    // [NOVO] Cálculo do IMC (inalterado)
                    val imc = if (ultimaMedida != null && ultimaMedida.peso != null && paciente.altura != null) {
                        calculadora.calcularIMC(paciente, ultimaMedida)
                    } else {
                        null
                    }

                    PacienteComUltimaMedida(
                        paciente = paciente,
                        ultimaMedida = ultimaMedida,
                        imc = imc
                    )
                }
            }
                .onStart {
                    _uiState.update { it.copy(isLoading = true, erro = null) }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, erro = e.message)
                    }
                }
                .collect { listaCombinada ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pacientesComMedidas = listaCombinada
                        )
                    }
                }
        }
    }

    fun onExcluirPaciente (paciente : Paciente){
        viewModelScope.launch {
            try {
                repository.excluirPaciente(paciente)
            } catch (e: Exception){
                _uiState.update { it.copy(erro = "Falha ao excluir paciente:${e.message}") }
            }
        }
    }
}