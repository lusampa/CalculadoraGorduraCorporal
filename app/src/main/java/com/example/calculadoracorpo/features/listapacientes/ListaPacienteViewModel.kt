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
            val medidasFlow = repository.listarTodasAvaliacoes()


            pacientesFlow.combine(medidasFlow) { listaPacientes, listaMedidas ->

                val ultimasMedidasMap = listaMedidas
                    .groupBy { it.pacienteId }
                    .mapValues { (_, medidas) ->
                        medidas.maxByOrNull { it.dataAvaliacao }
                    }

                listaPacientes.map { paciente ->
                    val ultimaMedida = ultimasMedidasMap[paciente.id]

                    // [NOVO] Calcula o IMC
                    val imc = if (ultimaMedida != null && ultimaMedida.peso != null && paciente.altura != null) {
                        calculadora.calcularIMC(paciente, ultimaMedida)
                    } else {
                        null
                    }

                    PacienteComUltimaMedida(
                        paciente = paciente,
                        ultimaMedida = ultimaMedida,
                        imc = imc // <-- [NOVO] Passa o IMC calculado
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

