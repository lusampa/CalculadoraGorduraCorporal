package com.example.calculadoracorpo.data.repository

import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.model.Sexo

class CalculadoraGorduraCorporal {
    fun calcularGordura(avaliacao: Medidas?, paciente: Paciente?): Double? {
        if (avaliacao == null || paciente == null) return null

        val somaDobras: Double? = when (avaliacao.protocoloUsado) {
            Protocolo.PROTOCOLO_3_DOBRAS -> {
                when (paciente.sexo) {
                    Sexo.MASCULINO -> somar(
                        avaliacao.dobraPeitoral,
                        avaliacao.dobraAbdominal,
                        avaliacao.dobraCoxa
                    )

                    Sexo.FEMININO -> somar(
                        avaliacao.dobraTriciptal,
                        avaliacao.dobraSupraIliaca,
                        avaliacao.dobraCoxa
                    )
                }
            }

            Protocolo.PROTOCOLO_7_DOBRAS -> {
                somar(
                    avaliacao.dobraPeitoral,
                    avaliacao.dobraAbdominal,
                    avaliacao.dobraTriciptal,
                    avaliacao.dobraSupraIliaca,
                    avaliacao.dobraCoxa,
                    avaliacao.dobraSubescapular,
                    avaliacao.dobraAxilarMedia
                )
            }

            Protocolo.SEM_DEFINICAO -> null
        }
        if (somaDobras == null) return null

        val idade = paciente.idade.toDouble()
        val somaDobrasQuadrado = somaDobras * somaDobras

        val densidade: Double? = when (avaliacao.protocoloUsado) {

            Protocolo.PROTOCOLO_3_DOBRAS -> {
                when (paciente.sexo) {
                    Sexo.MASCULINO ->
                        1.10938 - (0.0008267 * somaDobras) + (0.0000016 * somaDobrasQuadrado) - (0.0002574 * idade)

                    Sexo.FEMININO ->
                        1.0994921 - (0.0009929 * somaDobras) + (0.0000023 * somaDobrasQuadrado) - (0.0001392 * idade)
                }
            }

            Protocolo.PROTOCOLO_7_DOBRAS -> {
                when (paciente.sexo) {
                    Sexo.MASCULINO ->
                        1.112 - (0.00043499 * somaDobras) + (0.00000055 * somaDobrasQuadrado) - (0.00028826 * idade)

                    Sexo.FEMININO ->
                        1.097 - (0.00046971 * somaDobras) + (0.00000056 * somaDobrasQuadrado) - (0.00012828 * idade)
                }
            }

            Protocolo.SEM_DEFINICAO -> null
        }

        if (densidade == null || densidade <= 0) return null

        val percentual = (495 / densidade) - 450

        return percentual
    }

    private fun somar(vararg dobras: Double?): Double? {
        if (dobras.any { it == null }) {
            return null
        }
        return dobras.filterNotNull().sum()
    }


    fun calcularIMC(paciente: Paciente, medida: Medidas): Double? {
        val alturaMetros = paciente.altura?.div(100.0)

        val pesoKg = medida.peso

        return if (pesoKg != null && alturaMetros != null && alturaMetros > 0) {
            pesoKg / (alturaMetros * alturaMetros)
        } else {
            null
        }
    }
}
