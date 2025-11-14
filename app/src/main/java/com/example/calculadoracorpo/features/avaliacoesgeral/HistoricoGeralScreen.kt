package com.example.calculadoracorpo.features.avaliacoesgeral

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.MainApplication
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoGeralAvaliacoesScreen(
    onAvaliacaoClick: (pacienteId: Int) -> Unit
) {
    // --- 1. Injeção de Dependência e ViewModel ---
    val application = LocalContext.current.applicationContext as MainApplication
    val repository = application.repository

    val factory = HistoricoGeralViewModelFactory(repository)
    val viewModel: HistoricoGeralViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Histórico Geral de Avaliações",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        FundoPadrao(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }

                uiState.erro != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Erro: ${uiState.erro}", color = Color.Red) }

                uiState.historicoGeral.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhuma avaliação registrada no sistema.",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.historicoGeral) { item ->
                        HistoricoGeralCard(item) {
                            onAvaliacaoClick(item.resultado.medidas.pacienteId)
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// Adaptação do Card para incluir o nome do paciente e o clique
@Composable
fun HistoricoGeralCard(item: AvaliacaoComPaciente, onClick: () -> Unit) {
    val resultado = item.resultado
    val corFundo = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val corTexto = Color.White
    val formatoData = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("pt", "BR")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- CABEÇALHO: Paciente e Data ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.nomePaciente, // NOME DO PACIENTE EM DESTAQUE
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow // Destaque para o nome
                )
                Text(
                    text = resultado.medidas.dataAvaliacao.format(formatoData),
                    fontSize = 12.sp,
                    color = corTexto.copy(alpha = 0.7f)
                )
            }
            Divider(color = corTexto.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

            // --- 1. Percentual de Gordura (Destaque) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val corRisco = when (resultado.categoriaRisco) {
                    "Obesidade" -> Color.Red
                    "Aceitável" -> Color(0xFFFF9800)
                    "Fitness", "Atleta" -> Color(0xFF4CAF50)
                    else -> corTexto
                }
                Text(
                    text = if (resultado.percentualGordura != null)
                        String.format("%.2f%%", resultado.percentualGordura)
                    else
                        "N/D",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = corRisco
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Gordura Corporal", color = corTexto)
                    Text("Risco: ${resultado.categoriaRisco ?: "N/D"}", color = corRisco, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Detalhes (Massa, Protocolo) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetalheItem(
                    label = "Massa Magra",
                    valor = resultado.massaMagraKg,
                    unidade = "Kg",
                    cor = corTexto
                )
                DetalheItem(
                    label = "Massa Gorda",
                    valor = resultado.massaGordaKg,
                    unidade = "Kg",
                    cor = corTexto
                )
                // Usando DetalheItem para mostrar o Protocolo
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Protocolo",
                        fontSize = 12.sp,
                        color = corTexto.copy(alpha = 0.7f)
                    )
                    Text(
                        text = resultado.medidas.protocoloUsado.name.replace("_", " "),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = corTexto
                    )
                }
            }
        }
    }
}

// DetalheItem copiado de AvaliacoesScreen.kt
@Composable
fun DetalheItem(label: String, valor: Double?, unidade: String, cor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = cor.copy(alpha = 0.7f)
        )
        Text(
            text = if (valor != null) String.format("%.1f %s", valor, unidade) else "N/D",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = cor
        )
    }
}