package com.example.calculadoracorpo.features.listapacientes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch




class ListaPacienteViewModel (
    private val repository: PacienteRepository):ViewModel() {
    private val _uiState = MutableStateFlow(ListaPacienteState())
    val uiState: StateFlow<ListaPacienteState> = _uiState.asStateFlow()

    init {
        buscarPacientes()
    }

    private fun buscarPacientes() {
        viewModelScope.launch {
            repository.listarTodosPacientes()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, erro = null) }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, erro = e.message)
                    }
                }
                    .collect { listaDePacientes ->
                        _uiState.update {
                            it.copy(isLoading = false, pacientes = listaDePacientes)
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

