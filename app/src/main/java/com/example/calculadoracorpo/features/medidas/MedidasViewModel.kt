package com.example.calculadoracorpo.features.medidas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.navigation.AppRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedidasViewModel(
    private val repository: PacienteRepository,
    savedStateHandle: SavedStateHandle // Recebe argumentos da navegação
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedidasState())
    val uiState: StateFlow<MedidasState> = _uiState.asStateFlow()

    // Usando AppRoutes para obter o argumento da navegação
    private val pacienteId: Int = savedStateHandle[AppRoutes.ARG_PACIENTE_ID] ?: -1

    init {
        if (pacienteId != -1) {
            buscarNomeDoPaciente(pacienteId)
        } else {
            _uiState.update { it.copy(erro = "ID do paciente inválido para avaliação.") }
        }
    }

    private fun buscarNomeDoPaciente(id: Int) {
        viewModelScope.launch {
            val paciente = repository.buscarPaciente(id)
            _uiState.update { it.copy(
                nomePaciente = paciente?.nome ?: "Paciente Desconhecido",
                pacienteId = id
            ) }
        }
    }

    // --- Funções de Mudança de Estado (Inputs) ---
    fun onProtocoloChange(protocolo: Protocolo) {
        _uiState.update { it.copy(protocoloSelecionado = protocolo) }
    }

    // Função genérica para validar e atualizar campos (renomeada de onDobraChange para onValorChange)
    fun onValorChange(valor: String, campo: (MedidasState, String) -> MedidasState) {
        // Permite apenas números, um ponto decimal e é robusto
        if (valor.matches(Regex("^\\d*\\.?\\d*\$")) || valor.isEmpty()) {
            _uiState.update { campo(it, valor) }
        }
    }

    // Antropometria Básica
    fun onPesoChange(valor: String) = onValorChange(valor) { state, v -> state.copy(peso = v) }
    fun onAlturaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(altura = v) }

    // Dobras Cutâneas (7) - FUNÇÕES E CHAMADAS CORRIGIDAS
    fun onDobraPeitoralChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraPeitoral = v) }
    fun onDobraAbdominalChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraAbdominal = v) }
    fun onDobraTriciptalChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraTriciptal = v) }
    fun onDobraAxilarMediaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraAxilarMedia = v) }
    fun onDobraSubescapularChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraSubescapular = v) }
    fun onDobraSupraIliacaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraSupraIliaca = v) }
    fun onDobraCoxaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(dobraCoxa = v) }

    // Circunferências (8) - FUNÇÕES CORRIGIDAS
    fun onCircunferenciaBicepsChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaBiceps = v) }
    fun onCircunferenciaBicepsContraidoChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaBicepsContraido = v) }
    fun onCircunferenciaPeitoralChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaPeitoral = v) }
    fun onCircunferenciaCinturaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaCintura = v) }
    fun onCircunferenciaAbdomenChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaAbdomen = v) }
    fun onCircunferenciaQuadrilChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaQuadril = v) }
    fun onCircunferenciaCoxaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaCoxa = v) }
    fun onCircunferenciaPanturrilhaChange(valor: String) = onValorChange(valor) { state, v -> state.copy(circunferenciaPanturrilha = v) }


    fun onSalvarMedidasClick() {
        val estado = _uiState.value

        // 1. Validação básica de Altura e Peso
        if (estado.peso.toDoubleOrNull() == null || estado.altura.toDoubleOrNull() == null || estado.pacienteId == -1) {
            _uiState.update { it.copy(erro = "Peso e Altura devem ser valores numéricos válidos.") }
            return
        }

        // 2. Mapeamento de todas as dobras para validação (USANDO NOMES CORRIGIDOS)
        val dobras = mapOf(
            "Dobra Peitoral" to estado.dobraPeitoral.toDoubleOrNull(),
            "Dobra Abdominal" to estado.dobraAbdominal.toDoubleOrNull(),
            "Dobra Triciptal" to estado.dobraTriciptal.toDoubleOrNull(),
            "Dobra Supra-Ilíaca" to estado.dobraSupraIliaca.toDoubleOrNull(),
            "Dobra Coxa" to estado.dobraCoxa.toDoubleOrNull(),
            "Dobra Subescapular" to estado.dobraSubescapular.toDoubleOrNull(),
            "Dobra Axilar Média" to estado.dobraAxilarMedia.toDoubleOrNull()
        )

        // 3. Definição e Checagem das Dobras Obrigatórias (Lógica Jackson & Pollock)
        val dobrasObrigatoriasKeys: List<String> = when(estado.protocoloSelecionado) {
            Protocolo.PROTOCOLO_3_DOBRAS -> listOf("Dobra Peitoral", "Dobra Triciptal", "Dobra Supra-Ilíaca", "Dobra Coxa")
            Protocolo.PROTOCOLO_7_DOBRAS -> dobras.keys.toList()
            Protocolo.SEM_DEFINICAO -> emptyList()
        }

        val dobrasNulasObrigatorias = dobrasObrigatoriasKeys.filter { dobras[it] == null }

        if (dobrasNulasObrigatorias.isNotEmpty()) {
            _uiState.update { it.copy(erro = "As dobras ${dobrasNulasObrigatorias.joinToString(", ")} são obrigatórias para o protocolo ${estado.protocoloSelecionado.name.replace("_", " ")}.") }
            return
        }
        // Fim da Validação

        // 4. Criação e Inserção no Banco
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, erro = null) }

            val novaMedida = Medidas(
                id = 0,
                pacienteId = estado.pacienteId,
                dataAvaliacao = estado.dataAvaliacao,
                altura = estado.altura.toDoubleOrNull(),
                peso = estado.peso.toDoubleOrNull(),
                protocoloUsado = estado.protocoloSelecionado,

                // Mapeamento das Dobras Cutâneas (USANDO NOMES CORRIGIDOS)
                dobraPeitoral = estado.dobraPeitoral.toDoubleOrNull(),
                dobraAbdominal = estado.dobraAbdominal.toDoubleOrNull(),
                dobraTriciptal = estado.dobraTriciptal.toDoubleOrNull(),
                dobraAxilarMedia = estado.dobraAxilarMedia.toDoubleOrNull(),
                dobraSubescapular = estado.dobraSubescapular.toDoubleOrNull(),
                dobraSupraIliaca = estado.dobraSupraIliaca.toDoubleOrNull(),
                dobraCoxa = estado.dobraCoxa.toDoubleOrNull(),

                // Mapeamento das Circunferências (USANDO NOVOS NOMES)
                circunferenciaBiceps = estado.circunferenciaBiceps.toDoubleOrNull(),
                circunferenciaBicepsContraido = estado.circunferenciaBicepsContraido.toDoubleOrNull(),
                circunferenciaPeitoral = estado.circunferenciaPeitoral.toDoubleOrNull(),
                circunferenciaCintura = estado.circunferenciaCintura.toDoubleOrNull(),
                circunferenciaAbdomen = estado.circunferenciaAbdomen.toDoubleOrNull(),
                circunferenciaQuadril = estado.circunferenciaQuadril.toDoubleOrNull(),
                circunferenciaCoxa = estado.circunferenciaCoxa.toDoubleOrNull(),
                circunferenciaPanturrilha = estado.circunferenciaPanturilha.toDoubleOrNull()
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