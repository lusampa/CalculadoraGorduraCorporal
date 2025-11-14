package com.example.calculadoracorpo.features.avaliacoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.navigation.AppRoutes
import com.example.calculadoracorpo.data.repository.CalculadoraGorduraCorporal
import com.example.calculadoracorpo.data.repository.PdfGenerator // IMPORTADO
import kotlinx.coroutines.channels.Channel // IMPORTADO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow // IMPORTADO
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

// --- Eventos que a UI pode coletar para compartilhar o PDF ---
sealed interface CompartilhamentoEvent {
    data class Sucesso(val arquivo: File) : CompartilhamentoEvent
    data class Erro(val mensagem: String) : CompartilhamentoEvent
}

class AvaliacoesViewModel(
    private val repository: PacienteRepository,
    private val pdfGenerator: PdfGenerator, // DEPENDÊNCIA INJETADA
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvaliacoesState())
    val uiState: StateFlow<AvaliacoesState> = _uiState.asStateFlow()

    // Canal para enviar eventos de compartilhamento para a UI (fora do StateFlow)
    private val _compartilhamentoEvent = Channel<CompartilhamentoEvent>()
    val compartilhamentoEvent = _compartilhamentoEvent.receiveAsFlow()

    private val pacienteId: Int = savedStateHandle[AppRoutes.ARG_PACIENTE_ID] ?: -1

    init {
        if (pacienteId != -1) {
            carregarDadosDoPaciente()
        } else {
            _uiState.update { it.copy(isLoading = false, erro = "ID do paciente inválido.") }
        }
    }

    private fun carregarDadosDoPaciente() {
        // ... (Lógica de Carregamento inalterada)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, erro = null) }

            val paciente = repository.buscarPaciente(pacienteId)
            if (paciente == null) {
                _uiState.update { it.copy(isLoading = false, erro = "Paciente não encontrado.") }
                return@launch
            }

            repository.buscarAvaliacoes(pacienteId)
                .onStart {
                    _uiState.update { it.copy(paciente = paciente) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, erro = e.message) }
                }
                .collect { listaMedidas ->
                    val historicoCalculado = listaMedidas.map { medida ->
                        calcularResultado(medida, paciente)
                    }.sortedByDescending { it.medidas.dataAvaliacao }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historicoAvaliacoes = historicoCalculado
                        )
                    }
                }
        }
    }

    // --- NOVA FUNÇÃO: Gera o PDF e envia o evento de compartilhamento ---
    fun onCompartilharPdfClick(avaliacaoResultado: AvaliacaoResultado) {
        viewModelScope.launch {
            val paciente = _uiState.value.paciente
            if (paciente == null) {
                _compartilhamentoEvent.send(CompartilhamentoEvent.Erro("Dados do paciente não disponíveis."))
                return@launch
            }
            try {
                // Gera o PDF (assumindo que AvaliacaoResultado é a última avaliação)
                val arquivoPdf = pdfGenerator.generatePdf(paciente, avaliacaoResultado)
                _compartilhamentoEvent.send(CompartilhamentoEvent.Sucesso(arquivoPdf))
            } catch (e: IOException) {
                _compartilhamentoEvent.send(CompartilhamentoEvent.Erro("Erro ao gerar PDF: ${e.message}"))
            } catch (e: Exception) {
                _compartilhamentoEvent.send(CompartilhamentoEvent.Erro("Erro inesperado: ${e.message}"))
            }
        }
    }

    // --- LÓGICA DE CÁLCULO INALTERADA ---
    private fun calcularResultado(medida: Medidas, paciente: Paciente): AvaliacaoResultado {
        // ... (Corpo da função inalterado)
        val calculadora = CalculadoraGorduraCorporal()
        val percentualGordura = calculadora.calcularGordura(medida, paciente)
        val peso = medida.peso ?: 0.0
        val massaGordaKg = if (percentualGordura != null) (peso * percentualGordura / 100) else null
        val massaMagraKg = if (massaGordaKg != null) (peso - massaGordaKg) else null
        val categoriaRisco = percentualGordura?.let {
            when (paciente.sexo) {
                Sexo.MASCULINO -> when {
                    it < 6.0 -> "Essencial"
                    it < 14.0 -> "Atleta"
                    it < 18.0 -> "Fitness"
                    it < 25.0 -> "Aceitável"
                    else -> "Obesidade"
                }
                Sexo.FEMININO -> when {
                    it < 14.0 -> "Essencial"
                    it < 21.0 -> "Atleta"
                    it < 25.0 -> "Fitness"
                    it < 32.0 -> "Aceitável"
                    else -> "Obesidade"
                }
            }
        }
        val imc = medida.imc

        return AvaliacaoResultado(
            medidas = medida,
            percentualGordura = percentualGordura,
            massaMagraKg = massaMagraKg,
            massaGordaKg = massaGordaKg,
            imc = imc,
            categoriaRisco = categoriaRisco
        )
    }
}