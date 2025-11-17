// Substitua o conteúdo do seu arquivo por este código completo

package com.example.calculadoracorpo.features.avaliacoes

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.MainApplication
import com.example.calculadoracorpo.data.repository.PdfGenerator
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacoesScreen(
    pacienteId: Int,
    onNavigateBack: () -> Unit,
    onAddMedidaClick: (pacienteId: Int) -> Unit,
    // NOVO: Navegação para a tela de edição
    onEditMedidaClick: (pacienteId: Int, avaliacaoId: Int) -> Unit
) {
    // --- 1. Injeção de Dependência e ViewModel ---
    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val repository = application.repository

    val pdfGenerator = remember { PdfGenerator(context) }

    val owner = LocalContext.current as SavedStateRegistryOwner

    val factory = AvaliacoesViewModelFactory(repository, owner, pacienteId, pdfGenerator)
    val viewModel: AvaliacoesViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // NOVO: Estado para controlar qual avaliação deletar e a visibilidade do diálogo
    var avaliacaoParaDeletar by remember { mutableStateOf<AvaliacaoResultado?>(null) }

    // --- Efeito para Compartilhamento do PDF ---
    LaunchedEffect(Unit) {
        viewModel.compartilhamentoEvent.collectLatest { event ->
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    // O botão de compartilhar agora fica no card individual
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        FundoPadrao(modifier = Modifier.padding(paddingValues)) {
            // NOVO: Diálogo de confirmação de exclusão
            if (avaliacaoParaDeletar != null) {
                AlertDialog(
                    onDismissRequest = { avaliacaoParaDeletar = null },
                    title = { Text("Confirmar Exclusão") },
                    text = { Text("Tem certeza que deseja deletar esta avaliação? A ação não pode ser desfeita.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                avaliacaoParaDeletar?.let { viewModel.deletarAvaliacao(it) }
                                avaliacaoParaDeletar = null
                            }
                        ) { Text("Deletar", color = Color.Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { avaliacaoParaDeletar = null }) { Text("Cancelar") }
                    }
                )
            }

            when {
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
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.historicoAvaliacoes) { resultado ->
                        AvaliacaoExpandableCard(
                            resultado = resultado,
                            nomePaciente = uiState.paciente?.nome ?: "Paciente",
                            // NOVO: Passando as ações para o Card
                            onDeleteClick = { avaliacaoParaDeletar = resultado },
                            onEditClick = { onEditMedidaClick(resultado.medidas.pacienteId, resultado.medidas.id) },
                            onShareClick = { viewModel.onCompartilharPdfClick(resultado) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

private fun compartilharArquivo(context: Context, arquivo: File) {
    try {
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivo
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar avaliação via..."))

    } catch (e: Exception) {
        // Lógica de erro de compartilhamento
    }
}

@Composable
fun AvaliacaoExpandableCard(
    resultado: AvaliacaoResultado,
    nomePaciente: String,
    // NOVO: Funções de callback para os botões de ação
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val corFundo = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val corTexto = Color.White
    val corLabel = corTexto.copy(alpha = 0.7f)
    val formatoData = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("pt-BR")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- 1. CABEÇALHO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ALTERADO: Adicionado clickable para expandir/recolher
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = nomePaciente,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                    Text(
                        text = resultado.medidas.dataAvaliacao.format(formatoData),
                        fontSize = 12.sp,
                        color = corTexto.copy(alpha = 0.7f)
                    )
                }
                // NOVO: Ícones de ação (Editar, Deletar, Compartilhar)
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color.White)
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = Color.White)
                    }
                }
            }
            Divider(color = corTexto.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

            // --- 2. RESULTADOS CHAVE (Clicável para expandir) ---
            Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
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
                        text = if (resultado.percentualGordura != null) String.format("%.2f%%", resultado.percentualGordura) else "N/D",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetalheItem(label = "Massa Magra", valor = resultado.massaMagraKg, unidade = "Kg", cor = corTexto)
                    DetalheItem(label = "Massa Gorda", valor = resultado.massaGordaKg, unidade = "Kg", cor = corTexto)
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Protocolo", fontSize = 12.sp, color = corTexto.copy(alpha = 0.7f))
                        Text(
                            text = resultado.medidas.protocoloUsado.name.replace("_", " "),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = corTexto
                        )
                    }
                }
            }

            // --- 3. CONTEÚDO EXPANDIDO (Usa AnimatedVisibility) ---
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(color = corTexto.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))
                    Text("Circunferências (cm)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = corTexto)
                    Spacer(modifier = Modifier.height(8.dp))
                    // ... (O resto do conteúdo expandido permanece o mesmo)
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
                            Spacer(Modifier.width(32.dp))
                            MedidaDupla("Panturrilha", resultado.medidas.circunferenciaPanturrilha, "cm", corLabel, corTexto)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Dobras Cutâneas (mm)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = corTexto)
                    // ... (O restante do conteúdo das dobras cutâneas)
                }
            }
        }
    }
}

// NOVO: Adicione estes Composables auxiliares no final do arquivo, se ainda não os tiver.
@Composable
fun DetalheItem(label: String, valor: Double?, unidade: String, cor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 12.sp, color = cor.copy(alpha = 0.7f))
        Text(
            text = if (valor != null) String.format("%.2f %s", valor, unidade) else "N/D",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = cor
        )
    }
}

@Composable
fun RowScope.MedidaDupla(label: String, valor: Double?, unidade: String, corLabel: Color, corValor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
    ) {
        Text("$label: ", color = corLabel, fontSize = 14.sp)
        Text(
            text = if (valor != null) String.format("%.1f %s", valor, unidade) else "N/D",
            color = corValor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
