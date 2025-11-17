package com.example.calculadoracorpo.features.medidas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.navigation.AppRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedidasViewModel(
    private val repository: PacienteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedidasState())
    val uiState: StateFlow<MedidasState> = _uiState.asStateFlow()

    private val pacienteId: Int = savedStateHandle[AppRoutes.ARG_PACIENTE_ID] ?: -1
    private val avaliacaoId: Int = savedStateHandle[AppRoutes.ARG_AVALIACAO_ID] ?: -1

    init {
        if (pacienteId != -1) {
            carregarDadosIniciais()
        } else {
            _uiState.update { it.copy(erro = "ID do paciente inválido para avaliação.") }
        }
    }

    private fun carregarDadosIniciais() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, pacienteId = pacienteId) }

            val paciente = repository.buscarPaciente(pacienteId)
            if (paciente == null) {
                _uiState.update { it.copy(isLoading = false, erro = "Paciente não encontrado.") }
                return@launch
            }

            var medidaExistente: Medidas? = null
            if (avaliacaoId != -1) {
                medidaExistente = repository.buscarMedidaPorId(avaliacaoId)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    nomePaciente = paciente.nome,
                    sexoPaciente = paciente.sexo,
                    altura = paciente.altura?.toString() ?: "",
                    peso = medidaExistente?.peso?.toString() ?: "",
                    dataAvaliacao = medidaExistente?.dataAvaliacao ?: it.dataAvaliacao,
                    protocoloSelecionado = medidaExistente?.protocoloUsado ?: Protocolo.PROTOCOLO_7_DOBRAS,

                    dobraPeitoral = medidaExistente?.dobraPeitoral?.toString() ?: "",
                    dobraAbdominal = medidaExistente?.dobraAbdominal?.toString() ?: "",
                    dobraTriciptal = medidaExistente?.dobraTriciptal?.toString() ?: "",
                    dobraAxilarMedia = medidaExistente?.dobraAxilarMedia?.toString() ?: "",
                    dobraSubescapular = medidaExistente?.dobraSubescapular?.toString() ?: "",
                    dobraSupraIliaca = medidaExistente?.dobraSupraIliaca?.toString() ?: "",
                    dobraCoxa = medidaExistente?.dobraCoxa?.toString() ?: "",

                    circunferenciaBiceps = medidaExistente?.circunferenciaBiceps?.toString() ?: "",
                    circunferenciaBicepsContraido = medidaExistente?.circunferenciaBicepsContraido?.toString() ?: "",
                    circunferenciaPeitoral = medidaExistente?.circunferenciaPeitoral?.toString() ?: "",
                    circunferenciaCintura = medidaExistente?.circunferenciaCintura?.toString() ?: "",
                    circunferenciaAbdomen = medidaExistente?.circunferenciaAbdomen?.toString() ?: "",
                    circunferenciaQuadril = medidaExistente?.circunferenciaQuadril?.toString() ?: "",
                    circunferenciaCoxa = medidaExistente?.circunferenciaCoxa?.toString() ?: "",
                    circunferenciaPanturrilha = medidaExistente?.circunferenciaPanturrilha?.toString() ?: ""
                )
            }
        }
    }

    private fun validateInput(valor: String): Boolean {
        return valor.matches(Regex("^\\d*\\.?\\d*\$")) || valor.isEmpty()
    }

    // --- FUNÇÕES DE ENTRADA CORRIGIDAS (Sintaxe explícita) ---

    fun onProtocoloChange(protocolo: Protocolo) {
        _uiState.update { it.copy(protocoloSelecionado = protocolo) }
    }

    fun onPesoChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(peso = valor) }
        }
    }

    fun onAlturaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(altura = valor) }
        }
    }

    fun onDobraPeitoralChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraPeitoral = valor) }
        }
    }

    fun onDobraAbdominalChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraAbdominal = valor) }
        }
    }

    fun onDobraTriciptalChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraTriciptal = valor) }
        }
    }

    fun onDobraAxilarMediaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraAxilarMedia = valor) }
        }
    }

    fun onDobraSubescapularChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraSubescapular = valor) }
        }
    }

    fun onDobraSupraIliacaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraSupraIliaca = valor) }
        }
    }

    fun onDobraCoxaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(dobraCoxa = valor) }
        }
    }

    fun onCircunferenciaBicepsChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaBiceps = valor) }
        }
    }

    fun onCircunferenciaBicepsContraidoChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaBicepsContraido = valor) }
        }
    }

    fun onCircunferenciaPeitoralChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaPeitoral = valor) }
        }
    }

    fun onCircunferenciaCinturaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaCintura = valor) }
        }
    }

    fun onCircunferenciaAbdomenChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaAbdomen = valor) }
        }
    }

    fun onCircunferenciaQuadrilChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaQuadril = valor) }
        }
    }

    fun onCircunferenciaCoxaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaCoxa = valor) }
        }
    }

    fun onCircunferenciaPanturrilhaChange(valor: String) {
        if (validateInput(valor)) {
            _uiState.update { it.copy(circunferenciaPanturrilha = valor) }
        }
    }


    fun onSalvarMedidasClick() {
        val estado = _uiState.value

        if (estado.peso.toDoubleOrNull() == null || estado.altura.toDoubleOrNull() == null || estado.pacienteId == -1) {
            _uiState.update { it.copy(erro = "Peso e Altura devem ser valores numéricos válidos.") }
            return
        }

        val dobras = mapOf(
            "Dobra Peitoral" to estado.dobraPeitoral.toDoubleOrNull(),
            "Dobra Abdominal" to estado.dobraAbdominal.toDoubleOrNull(),
            "Dobra Triciptal" to estado.dobraTriciptal.toDoubleOrNull(),
            "Dobra Supra-Ilíaca" to estado.dobraSupraIliaca.toDoubleOrNull(),
            "Dobra Coxa" to estado.dobraCoxa.toDoubleOrNull(),
            "Dobra Subescapular" to estado.dobraSubescapular.toDoubleOrNull(),
            "Dobra Axilar Média" to estado.dobraAxilarMedia.toDoubleOrNull()
        )

        val dobrasObrigatoriasKeys: List<String> = when(estado.protocoloSelecionado) {
            Protocolo.PROTOCOLO_7_DOBRAS -> dobras.keys.toList()
            Protocolo.PROTOCOLO_3_DOBRAS -> {
                when (estado.sexoPaciente) {
                    Sexo.MASCULINO -> listOf("Dobra Peitoral", "Dobra Abdominal", "Dobra Coxa")
                    Sexo.FEMININO -> listOf("Dobra Triciptal", "Dobra Supra-Ilíaca", "Dobra Coxa")
                }
            }
            Protocolo.SEM_DEFINICAO -> emptyList()
        }

        val dobrasNulasObrigatorias = dobrasObrigatoriasKeys.filter { dobras[it] == null }

        if (dobrasNulasObrigatorias.isNotEmpty()) {
            _uiState.update { it.copy(erro = "As dobras ${dobrasNulasObrigatorias.joinToString(", ")} são obrigatórias para o protocolo ${estado.protocoloSelecionado.name.replace("_", " ")}.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, erro = null) }

            val novaMedida = Medidas(
                id = avaliacaoId.takeIf { it != -1 } ?: 0,
                pacienteId = estado.pacienteId,
                dataAvaliacao = estado.dataAvaliacao,
                peso = estado.peso.toDoubleOrNull(),
                protocoloUsado = estado.protocoloSelecionado,

                dobraPeitoral = estado.dobraPeitoral.toDoubleOrNull(),
                dobraAbdominal = estado.dobraAbdominal.toDoubleOrNull(),
                dobraTriciptal = estado.dobraTriciptal.toDoubleOrNull(),
                dobraAxilarMedia = estado.dobraAxilarMedia.toDoubleOrNull(),
                dobraSubescapular = estado.dobraSubescapular.toDoubleOrNull(),
                dobraSupraIliaca = estado.dobraSupraIliaca.toDoubleOrNull(),
                dobraCoxa = estado.dobraCoxa.toDoubleOrNull(),

                circunferenciaBiceps = estado.circunferenciaBiceps.toDoubleOrNull(),
                circunferenciaBicepsContraido = estado.circunferenciaBicepsContraido.toDoubleOrNull(),
                circunferenciaPeitoral = estado.circunferenciaPeitoral.toDoubleOrNull(),
                circunferenciaCintura = estado.circunferenciaCintura.toDoubleOrNull(),
                circunferenciaAbdomen = estado.circunferenciaAbdomen.toDoubleOrNull(),
                circunferenciaQuadril = estado.circunferenciaQuadril.toDoubleOrNull(),
                circunferenciaCoxa = estado.circunferenciaCoxa.toDoubleOrNull(),
                circunferenciaPanturrilha = estado.circunferenciaPanturrilha.toDoubleOrNull()
            )

            try {
                repository.inserirAvaliacao(novaMedida)
                _uiState.update { it.copy(isLoading = false, salvouComSucesso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, erro = "Falha ao salvar: ${e.message}") }
            }
        }
    }
}