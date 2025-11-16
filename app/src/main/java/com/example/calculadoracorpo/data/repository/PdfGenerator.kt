package com.example.calculadoracorpo.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import com.example.calculadoracorpo.R
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.features.avaliacoes.AvaliacaoResultado
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.Locale

// Classe responsável por gerar o arquivo PDF
class PdfGenerator(private val context: Context) {

    // Dimensões da página (A4 - em postscript points, 72 dpi)
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842

    @Throws(IOException::class)
    fun generatePdf(paciente: Paciente, resultado: AvaliacaoResultado): File {
        // 1. Configurar o documento PDF
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 2. Configurar as fontes e cores
        val paintTitulo = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = ContextCompat.getColor(context, android.R.color.black)
        }

        val paintSubtitulo = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = ContextCompat.getColor(context, android.R.color.black)
        }

        val paintCorpo = Paint().apply {
            textSize = 14f
            color = ContextCompat.getColor(context, android.R.color.black)
        }

        // 3. Desenhar o conteúdo
        var yPos = 50f
        val xStart = 40f
        val lineHeight = 30f

        val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        // --- Tratamento de Nullidade ---
        val dataNascimentoFormatada = paciente.dataDeNascimento?.format(formatoData) ?: "N/D"
        val dataAvaliacaoFormatada = resultado.medidas.dataAvaliacao?.format(formatoData) ?: "N/D"

        // --- CABEÇALHO ---
        canvas.drawText("Relatório de Avaliação Física", xStart, yPos, paintTitulo)
        yPos += lineHeight

        // --- Dados do Paciente ---
        canvas.drawText("Paciente: ${paciente.nome}", xStart, yPos, paintSubtitulo)
        yPos += lineHeight

        // CORRIGIDO: Uso de dataNascimentoFormatada
        canvas.drawText("Nascimento: $dataNascimentoFormatada", xStart, yPos, paintCorpo)
        yPos += lineHeight / 2

        canvas.drawText("Sexo: ${if (paciente.sexo.name == "MASCULINO") "Masculino" else "Feminino"}", xStart, yPos, paintCorpo)
        yPos += lineHeight * 2 // Espaço antes dos resultados

        // --- Dados da Avaliação ---
        // CORRIGIDO: Uso de dataAvaliacaoFormatada
        canvas.drawText("Avaliação de $dataAvaliacaoFormatada", xStart, yPos, paintSubtitulo)
        yPos += lineHeight

        // Detalhes da Avaliação
        fun drawDetail(label: String, value: String) {
            canvas.drawText("$label:", xStart, yPos, paintCorpo)
            canvas.drawText(value, xStart + 150f, yPos, paintCorpo.apply { typeface = Typeface.DEFAULT_BOLD })
            yPos += lineHeight
        }

        drawDetail("Peso", String.format("%.1f Kg", resultado.medidas.peso ?: 0.0))
        drawDetail("Altura", String.format("%.0f cm", paciente.altura ?: 0.0))
        drawDetail("Protocolo", resultado.medidas.protocoloUsado.name.replace("_", " "))

        yPos += lineHeight / 2

        // --- Resultados Chave ---
        canvas.drawText("Resultados:", xStart, yPos, paintSubtitulo)
        yPos += lineHeight

        drawDetail("Gordura Corporal", String.format("%.2f%%", resultado.percentualGordura ?: 0.0))
        drawDetail("Risco", resultado.categoriaRisco ?: "Não Calculado")
        drawDetail("Massa Magra", String.format("%.1f Kg", resultado.massaMagraKg ?: 0.0))
        drawDetail("Massa Gorda", String.format("%.1f Kg", resultado.massaGordaKg ?: 0.0))
        drawDetail("IMC", String.format("%.2f", resultado.imc ?: 0.0))


        // 4. Finalizar a página
        document.finishPage(page)

        // 5. Salvar o documento em External Cache Dir (Acessível pelo FileProvider)
        // Opcional: Tratar null de dataAvaliacao no nome do arquivo
        val dataParaNome = resultado.medidas.dataAvaliacao?.format(DateTimeFormatter.ofPattern("yyyyMMdd")) ?: "SemData"
        val nomePacienteSeguro = paciente.nome.replace(" ", "_").replace("[^A-Za-z0-9_]".toRegex(), "")

        val fileName = "Avaliacao_${nomePacienteSeguro}_$dataParaNome.pdf"

        // Use getExternalCacheDir() para a pasta de cache externa
        val cacheDir = File(context.externalCacheDir, "pdf")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val file = File(cacheDir, fileName)

        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }
}