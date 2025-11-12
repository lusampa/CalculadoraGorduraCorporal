package com.seunome.gorduracorporal.data.repository // ou .domain

import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.model.Sexo


/**
 * Classe de lógica de negócios responsável por
 * calcular a densidade e o percentual de gordura
 * Retorna null se os dados forem insuficientes.
 */
class CalculadoraGorduraCorporal {

    fun calcularGordura(avaliacao: Medidas?, paciente: Paciente?): Double? {
        if (avaliacao == null || paciente == null) return null

        // --- PASSO 1: Obter a Soma das Dobras ---
        // (Como você já tinha feito)
        val somaDobras: Double? = when (avaliacao.protocoloUsado) {
            Protocolo.PROTOCOLO_3_DOBRAS -> {
                when (paciente.sexo) {
                    Sexo.MASCULINO -> somar(avaliacao.peitoral, avaliacao.abdominal, avaliacao.coxa)
                    Sexo.FEMININO -> somar(avaliacao.triceps, avaliacao.supraIliaca, avaliacao.coxa)
                }
            }

            Protocolo.PROTOCOLO_7_DOBRAS -> {
                somar(
                    avaliacao.peitoral, avaliacao.abdominal, avaliacao.triceps,
                    avaliacao.supraIliaca, avaliacao.coxa, avaliacao.subescapular,
                    avaliacao.axilarMedia
                )
            }

            Protocolo.SEM_DEFINICAO -> null
        }
        if (somaDobras == null) return null

        // --- PASSO 2: Aplicar a Fórmula de Densidade Correta ---

        // As fórmulas usam a idade como Double
        val idade = paciente.idade.toDouble()
        // E também usam a soma das dobras ao quadrado
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

        // --- PASSO 3: Converter Densidade em Percentual (Fórmula de Siri) ---
        // Verificação de segurança: densidade não pode ser nula, zero ou negativa.
        if (densidade == null || densidade <= 0) return null

        // Fórmula de Siri
        val percentual = (495 / densidade) - 450

        return percentual
    }
/**
     * Função auxiliarpara somar valores de dobras cutâneas
     * que podem ser nulos (Double?).
     * 'vararg' permite que a função aceite um número variável de argumentos
     * (ex: somar(a), somar(a, b), somar(a, b, c), etc.)
 */
    private fun somar(vararg dobras: Double?): Double? {
    //'any' percorre a lista e 'it == null' é a condição.
        if (dobras.any { it == null }) {
        // 2. Se qualquer dobra for nula, o cálculo é impossível.
            return null
        }
        return dobras.filterNotNull().sum()
    }
}