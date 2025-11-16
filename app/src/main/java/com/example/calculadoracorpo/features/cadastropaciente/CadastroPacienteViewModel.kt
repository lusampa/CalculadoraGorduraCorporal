package com.example.calculadoracorpo.features.cadastropaciente
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.data.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class CadastroPacienteViewModel (
    private val repository: PacienteRepository) : ViewModel() {
        private val _uiState = MutableStateFlow(CadastroPacienteState())
        val uiState: StateFlow<CadastroPacienteState> = _uiState.asStateFlow()

    // funções chamadas pela ui

    fun onNomeChanged(novoNome: String){
        _uiState.update { it.copy(nome = novoNome, erro = null) }
    }
    fun onDataNascimentoChanged(novaData: LocalDate){
        _uiState.update { it.copy(dataNascimento = novaData, erro = null) }
    }
    fun onSexoChanged(novoSexo: Sexo){
        _uiState.update { it.copy(sexo = novoSexo) }
    }

    fun onAlturaChanged(novaAltura: String) {
        // Permite apenas números e um ponto decimal
        if (novaAltura.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _uiState.update { it.copy(altura = novaAltura, erro = null) }
        }
    }

    fun onSalvarClicked() {
        val estado = _uiState.value

        if (estado.nome.isBlank() || estado.dataNascimento == null) {
            _uiState.update {
                it.copy(erro = "Nome e Data de nascimento são obrigatórios")
            }
            return
        }
        val alturaDouble = estado.altura.toDoubleOrNull()
        if (alturaDouble == null) {
            _uiState.update { it.copy(erro = "Altura deve ser um número válido.") }
            return
        }
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val novoPaciente = Paciente(
                    id = UUID.randomUUID().hashCode(),
                    nome = estado.nome.trim(),
                    dataDeNascimento = estado.dataNascimento,
                    sexo = estado.sexo,
                    altura = alturaDouble
                )
                try {
                    repository.inserirPaciente(novoPaciente)
                    _uiState.update { it.copy(isLoading = false, salvouComSucesso = true) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, erro = e.message) }
                }
            }
        }
    }


