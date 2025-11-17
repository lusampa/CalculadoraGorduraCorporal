package com.example.calculadoracorpo.features.avaliacoesgeral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.data.repository.CalculadoraGorduraCorporal
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.features.avaliacoes.AvaliacaoResultado
import com.example.calculadoracorpo.ui.theme.CalculadoracorpoTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoricoGeralViewModel(
    private val repository: PacienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoGeralState())
    val uiState: StateFlow<HistoricoGeralState> = _uiState.asStateFlow()

    init {
        carregarHistoricoGeral()
    }

    private fun carregarHistoricoGeral() {
        viewModelScope.launch {

            val todosPacientesFlow = repository.listarTodosPacientes()
            val todasMedidasFlow = repository.listarTodasAvaliacoes()

            todosPacientesFlow.combine(todasMedidasFlow) { listaPacientes, listaMedidas ->

                val pacienteMap = listaPacientes.associateBy { it.id }

                listaMedidas.mapNotNull { medida ->
                    val paciente = pacienteMap[medida.pacienteId]
                    if (paciente != null) {
                        val resultado = calcularResultado(medida, paciente)
                        AvaliacaoComPaciente(resultado, paciente.nome)
                    } else {
                        null
                    }
                }.sortedByDescending { it.resultado.medidas.dataAvaliacao }
            }
                .onStart { _uiState.update { it.copy(isLoading = true, erro = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, erro = e.message) }
                }
                .collect { historicoGeralList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historicoGeral = historicoGeralList
                        )
                    }
                }
        }
    }

    private fun calcularResultado(medida: Medidas, paciente: Paciente): AvaliacaoResultado {
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

        val imc = calculadora.calcularIMC(paciente,medida)

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