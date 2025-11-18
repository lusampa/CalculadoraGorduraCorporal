package com.example.calculadoracorpo.features.listapacientes

import com.example.calculadoracorpo.MainApplication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
// Remova LocalDensity e DpOffset se não os estiver usando mais
// import androidx.compose.ui.platform.LocalDensity
// import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.text.font.FontWeight // [NOVO] Importe FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao
// [NOVO] Importe a nova classe de estado que você criou
import com.example.calculadoracorpo.features.listapacientes.PacienteComUltimaMedida

@Composable
fun ListaPacienteScreen(
    onPacienteClick: (pacienteId: Int) -> Unit, // Navega para Histórico/Detalhes
    onAddPacienteClick: () -> Unit, // FAB: Cadastro de Novo Paciente
    onEditarPacienteClick: (pacienteId: Int) -> Unit,
    onAdicionarMedidasClick: (pacienteId: Int) -> Unit
){
    // ... (seu código de repository, factory, viewModel está correto)
    val context = LocalContext.current.applicationContext
    val repository = (context as MainApplication).repository
    val factory = ListaPacienteViewModelFactory(repository)
    val viewModel: ListaPacienteViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()

    Scaffold (

        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPacienteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Paciente",
                    tint = Color.Green // (Você tinha Color.Green, talvez queira Color.White?)
                )
            }
        }
    ) { paddingValues ->
        FundoPadrao (modifier = Modifier.padding(paddingValues)){
            when {

                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                uiState.erro != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(text = "Erro: ${uiState.erro}", color = Color.Yellow)
                    }
                }

                // [MODIFICADO] Verifique a lista 'pacientesComMedidas'
                uiState.pacientesComMedidas.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(text = "Nenhum paciente cadastrado.", color = Color.White)
                    }
                }

                // Lista de pacientes
                else -> {
                    LazyColumn(
                        modifier= Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ){

                        items(uiState.pacientesComMedidas, key = { it.paciente.id }) { item ->
                            ItemPaciente(
                                // Passe o 'item' inteiro
                                item = item,
                                // Passe os IDs e objetos corretos
                                onPacienteClick = { onPacienteClick( item.paciente.id) },
                                onEditarClick = {onEditarPacienteClick(item.paciente.id) },
                                onExcluirClick = { viewModel.onExcluirPaciente(item.paciente)},
                                onAdicionarMedidasClick = { onAdicionarMedidasClick(item.paciente.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemPaciente(

    item: PacienteComUltimaMedida,
    onPacienteClick: () -> Unit,
    onEditarClick: () -> Unit,
    onExcluirClick: () -> Unit,
    onAdicionarMedidasClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- 1. Variáveis de estado (correto) ---
    var menuAberto by remember { mutableStateOf(false) }
    var mostrarDialogoExcluir by remember { mutableStateOf(false) }


    val paciente = item.paciente
    val imc = item.imc

    // --- 2. O Card clicável ---
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPacienteClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paciente.nome, // Use o 'paciente' extraído
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Idade: ${paciente.idade} anos", // Use o 'paciente' extraído
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.width(16.dp))
                    // [NOVO] Texto para exibir o IMC
                    Text(
                        text = "IMC: ${if (imc != null) String.format("%.1f", imc) else "N/D"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            // --- Fim da Modificação ---

            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Abrir menu de opções",
                        tint = Color.White
                    )
                }

                // --- 5. O Menu Dropdown
                DropdownMenu(
                    expanded = menuAberto,
                    onDismissRequest = { menuAberto = false },
                    offset = DpOffset(x = 16.dp, y = 0.dp)
                ) {
                    // Opção 1: Adicionar Medidas
                    DropdownMenuItem(
                        text = { Text("Adicionar Medidas") },
                        onClick = {
                            menuAberto = false
                            onAdicionarMedidasClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Add, "Adicionar Medidas")
                        }
                    )
                    Divider()
                    // Opção 2: Editar Dados
                    DropdownMenuItem(
                        text = { Text("Editar Dados") },
                        onClick = {
                            menuAberto = false
                            onEditarClick() // Chama o callback que passa o ID para a ListaPacienteScreen
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    )
                    // Opção 3: Excluir
                    DropdownMenuItem(
                        text = { Text("Excluir Paciente") },
                        onClick = {
                            menuAberto = false
                            mostrarDialogoExcluir = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, "Excluir")
                        }
                    )
                }
            }
        }
    }

    // --- 6. O Diálogo de Confirmação (correto) ---
    if (mostrarDialogoExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExcluir = false },
            title = { Text("Confirmar Exclusão") },
            // [MODIFICADO] Use 'paciente.nome'
            text = { Text("Você tem certeza que deseja excluir o paciente '${paciente.nome}'? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoExcluir = false
                        onExcluirClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoExcluir = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}