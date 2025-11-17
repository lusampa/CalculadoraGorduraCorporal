package com.example.calculadoracorpo.features.medidas

import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.model.Sexo
import java.time.LocalDate

data class MedidasState(
    val pacienteId: Int = -1,
    val nomePaciente: String = "",
    val dataAvaliacao: LocalDate = LocalDate.now(),
    val protocoloSelecionado: Protocolo = Protocolo.PROTOCOLO_7_DOBRAS,

    val sexoPaciente: Sexo = Sexo.MASCULINO,
    val peso: String = "",
    val altura: String = "",

    val dobraTriciptal: String = "",
    val dobraSubescapular: String = "",
    val dobraPeitoral: String = "",
    val dobraAxilarMedia: String = "",
    val dobraAbdominal: String = "",
    val dobraSupraIliaca: String = "",
    val dobraCoxa: String = "",

    val circunferenciaBiceps: String = "",
    val circunferenciaBicepsContraido: String = "",
    val circunferenciaPeitoral: String = "",
    val circunferenciaCintura: String = "",
    val circunferenciaAbdomen: String = "",
    val circunferenciaQuadril: String = "",
    val circunferenciaCoxa: String = "",
    val circunferenciaPanturrilha: String = "",

    val isLoading: Boolean = false,
    val salvouComSucesso: Boolean = false,
    val erro: String? = null
)