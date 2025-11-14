package com.example.calculadoracorpo.features.medidas

import com.example.calculadoracorpo.data.model.Protocolo
import java.time.LocalDate

data class MedidasState(
    // Dados de Entrada
    val pacienteId: Int = -1, // ID do paciente que está sendo avaliado
    val nomePaciente: String = "", // Nome para exibição na tela
    val dataAvaliacao: LocalDate = LocalDate.now(),
    val protocoloSelecionado: Protocolo = Protocolo.PROTOCOLO_7_DOBRAS,

    // Campos de entrada como String para facilitar no TextField
    val peso: String = "",
    val altura: String = "",

    // --- 1. DOBRAS CUTÂNEAS (mm) - Nomes de campos corrigidos (dobraXxx) ---
    val dobraTriciptal: String = "",
    val dobraSubescapular: String = "",
    val dobraPeitoral: String = "",
    val dobraAxilarMedia: String = "",
    val dobraAbdominal: String = "",
    val dobraSupraIliaca: String = "",
    val dobraCoxa: String = "",

    // --- 2. CIRCUNFERÊNCIAS (cm) ---
    val circunferenciaBiceps: String = "",
    val circunferenciaBicepsContraido: String = "",
    val circunferenciaPeitoral: String = "",
    val circunferenciaCintura: String = "",
    val circunferenciaAbdomen: String = "",
    val circunferenciaQuadril: String = "",
    val circunferenciaCoxa: String = "",
    val circunferenciaPanturrilha: String = "",

    // UI State
    val isLoading: Boolean = false,
    val salvouComSucesso: Boolean = false,
    val erro: String? = null
)