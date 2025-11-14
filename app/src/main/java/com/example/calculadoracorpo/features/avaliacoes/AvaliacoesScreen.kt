package com.example.calculadoracorpo.features.avaliacoes

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.clickable // IMPORTADO
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// CORRIGIDO: Icone não depreciado
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.MainApplication
import com.example.calculadoracorpo.navigation.AppRoutes
import com.example.calculadoracorpo.data.repository.PdfGenerator
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacoesScreen(
    pacienteId: Int,
    onNavigateBack: () -> Unit,
    onAddMedidaClick: (pacienteId: Int) -> Unit
) {
    // --- 1. Injeção de Dependência e ViewModel ---
    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val repository = application.repository

    // O PdfGenerator precisa ser instanciado aqui (ou na Factory)
    val pdfGenerator = remember { PdfGenerator(context) }

    // Obtém o SavedStateRegistryOwner (Activity) para a Factory
    val owner = LocalContext.current as SavedStateRegistryOwner

    // Cria a Factory com o novo argumento (pdfGenerator)
    val factory = AvaliacoesViewModelFactory(repository, owner, pacienteId, pdfGenerator)
    val viewModel: AvaliacoesViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() } // Para mostrar mensagens de erro

    // --- Efeito para Compartilhamento do PDF ---
    LaunchedEffect(Unit) {
        viewModel.compartilhamentoEvent.collect { event ->
            when (event) {
                is CompartilhamentoEvent.Sucesso -> {
                    compartilharArquivo(context, event.arquivo)
                }
                is CompartilhamentoEvent.Erro -> {
                    snackbarHostState.showSnackbar(event.mensagem)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.paciente?.nome ?: "Avaliações",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // CORRIGIDO: Uso do ícone AutoMirrored
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.historicoAvaliacoes.isNotEmpty()) {
                        IconButton(onClick = {
                            // Compartilha a avaliação mais recente (que está no topo da lista)
                            viewModel.onCompartilharPdfClick(uiState.historicoAvaliacoes.first())
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddMedidaClick(pacienteId) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Avaliação",
                    tint = Color.White
                )
            }
        },
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) } // Para exibir erros
    ) { paddingValues ->
        FundoPadrao(modifier = Modifier.padding(paddingValues)) {
            when {
                // ... (Lógica de estados inalterada: isLoading, erro, isEmpty)
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }

                uiState.erro != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Erro: ${uiState.erro}", color = Color.Red) }

                uiState.historicoAvaliacoes.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhuma avaliação registrada.\nUse o botão '+' para adicionar.",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.historicoAvaliacoes) { resultado ->
                        ResultadoAvaliacaoCard(resultado)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// --- FUNÇÃO AUXILIAR PARA COMPARTILHAMENTO INALTERADA ---
private fun compartilharArquivo(context: Context, arquivo: File) {
    try {
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Deve corresponder ao manifest
            arquivo
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar avaliação via..."))

    } catch (e: Exception) {
        // ... (Lógica de erro)
    }
}


// --- COMPOSABLE PRINCIPAL DE EXIBIÇÃO ---
@Composable
fun ResultadoAvaliacaoCard(resultado: AvaliacaoResultado) {
    val corFundo = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val corTexto = Color.White
    val corLabel = corTexto.copy(alpha = 0.7f)
    // CORRIGIDO: Depreciação Locale
    val formatoData = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("pt-BR")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ... (Cabeçalho, Gordura, Massa, IMC - Seção de Resultados)

            // --- 1. Seção de Circunferências ---
            Spacer(modifier = Modifier.height(24.dp))
            Text("Circunferências (cm)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = corTexto)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MedidaDupla("Bíceps", resultado.medidas.circunferenciaBiceps, "cm", corLabel, corTexto)
                    MedidaDupla("Bíceps Cont.", resultado.medidas.circunferenciaBicepsContraido, "cm", corLabel, corTexto)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MedidaDupla("Peitoral", resultado.medidas.circunferenciaPeitoral, "cm", corLabel, corTexto)
                    MedidaDupla("Cintura", resultado.medidas.circunferenciaCintura, "cm", corLabel, corTexto)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MedidaDupla("Abdômen", resultado.medidas.circunferenciaAbdomen, "cm", corLabel, corTexto)
                    MedidaDupla("Quadril", resultado.medidas.circunferenciaQuadril, "cm", corLabel, corTexto)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    MedidaDupla("Coxa", resultado.medidas.circunferenciaCoxa, "cm", corLabel, corTexto)
                    Spacer(Modifier.width(32.dp)) // Espaçamento de 2 itens
                    MedidaDupla("Panturrilha", resultado.medidas.circunferenciaPanturrilha, "cm", corLabel, corTexto)
                }
            }


            // --- 2. Seção de Dobras Cutâneas ---
            Spacer(modifier = Modifier.height(24.dp))
            Text("Dobras Cutâneas (mm)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = corTexto)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MedidaDupla("Triciptal", resultado.medidas.dobraTriciptal, "mm", corLabel, corTexto)
                    MedidaDupla("Subescapular", resultado.medidas.dobraSubescapular, "mm", corLabel, corTexto)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MedidaDupla("Peitoral", resultado.medidas.dobraPeitoral, "mm", corLabel, corTexto)
                    MedidaDupla("Axilar Média", resultado.medidas.dobraAxilarMedia, "mm", corLabel, corTexto)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MedidaDupla("Abdominal", resultado.medidas.dobraAbdominal, "mm", corLabel, corTexto)
                    MedidaDupla("Supra Ilíaca", resultado.medidas.dobraSupraIliaca, "mm", corLabel, corTexto)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    MedidaDupla("Coxa", resultado.medidas.dobraCoxa, "mm", corLabel, corTexto)
                }
            }
        }
    }
}

// NOVO COMPOSABLE: MedidaDupla (para organizar em duas colunas)
@Composable
fun RowScope.MedidaDupla(label: String, valor: Double?, unidade: String, corLabel: Color, corTexto: Color) {
    Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp), horizontalAlignment = Alignment.Start) {
        Text(text = "$label:", fontSize = 12.sp, color = corLabel)
        Text(
            text = if (valor != null) String.format("%.1f %s", valor, unidade) else "N/D",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = corTexto
        )
    }
}

// ... (DetalheItem inalterado)
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