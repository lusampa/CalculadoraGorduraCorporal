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

class   CadastroPacienteViewModel (
    private val repository: PacienteRepository,
    // Recebe o ID do paciente a ser editado (ou -1 para novo)
    private val pacienteId: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(CadastroPacienteState())
    val uiState: StateFlow<CadastroPacienteState> = _uiState.asStateFlow()

    // Campo interno para rastrear o paciente original (para Update)
    private var pacienteOriginal: Paciente? = null

    init {
        if (pacienteId > 0) {
            carregarPacienteParaEdicao(pacienteId)
        }
    }

    private fun carregarPacienteParaEdicao(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, erro = null) }
            try {
                // [AJUSTE] Usando o método existente no PacienteRepository (buscarPaciente)
                val paciente = repository.buscarPaciente(id)

                if (paciente != null) {
                    pacienteOriginal = paciente // Armazena o original
                    _uiState.update {
                        it.copy(
                            nome = paciente.nome,
                            dataNascimento = paciente.dataDeNascimento,
                            sexo = paciente.sexo,
                            // Converte altura para String para o TextField
                            altura = paciente.altura.toString(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, erro = "Paciente não encontrado.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, erro = "Erro ao carregar dados: ${e.message}") }
            }
        }
    }


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
        if (alturaDouble == null || alturaDouble <= 0) {
            _uiState.update { it.copy(erro = "Altura deve ser um número válido e maior que zero.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Lógica para INSERIR ou ATUALIZAR
            val pacienteASalvar = Paciente(
                // Se estiver editando, usa o ID existente, senão, usa 0
                id = pacienteOriginal?.id ?: 0,
                nome = estado.nome.trim(),
                dataDeNascimento = estado.dataNascimento,
                sexo = estado.sexo,
                altura = alturaDouble
            )

            try {
                if (pacienteOriginal != null) {
                    // Modo Edição: Atualiza o paciente existente
                    // [AJUSTE] Usando o método existente no PacienteRepository (editarPaciente)
                    repository.editarPaciente(pacienteASalvar)
                } else {
                    // Modo Cadastro: Insere um novo paciente
                    repository.inserirPaciente(pacienteASalvar)
                }
                _uiState.update { it.copy(isLoading = false, salvouComSucesso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, erro = e.message) }
            }
        }
    }
}