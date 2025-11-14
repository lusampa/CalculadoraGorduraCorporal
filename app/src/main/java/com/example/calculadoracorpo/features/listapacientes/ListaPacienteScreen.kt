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
import androidx.compose.runtime.remember // Importação para 'remember'
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadoracorpo.data.model.Paciente
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao

@Composable
fun ListaPacienteScreen(
    onPacienteClick: (pacienteId: Int) -> Unit, // Navega para Histórico/Detalhes
    onAddPacienteClick: () -> Unit, // FAB: Cadastro de Novo Paciente
    onEditarPacienteClick: (pacienteId: Int) -> Unit,
    onAdicionarMedidasClick: (pacienteId: Int) -> Unit // NOVO: Navega para a entrada de medidas
){

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
                    tint = Color.Green
                )
            }
        }
    ) { paddingValues ->
        FundoPadrao (modifier = Modifier.padding(paddingValues)){
            when {
                // Barra de progresso
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                //Se tiver algum erro ele avisa
                uiState.erro != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        // [CORRIGIDO] Parâmetro 'color' nomeado
                        Text(text = "Erro: ${uiState.erro}", color = Color.Yellow)
                    }
                }
                //Se vazio, mostra aviso
                uiState.pacientes.isEmpty() -> {
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
                        items(uiState.pacientes, key = { it.id }) { paciente ->
                            ItemPaciente(
                                paciente = paciente,
                                onPacienteClick = { onPacienteClick( paciente.id) },
                                onEditarClick = {onEditarPacienteClick(paciente.id) },
                                onExcluirClick = { viewModel.onExcluirPaciente(paciente)},
                                onAdicionarMedidasClick = { onAdicionarMedidasClick(paciente.id) } // NOVO CALLBACK
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
    paciente: Paciente,
    onPacienteClick: () -> Unit,
    onEditarClick: () -> Unit,
    onExcluirClick: () -> Unit, // Recebe o evento do ViewModel
    onAdicionarMedidasClick: () -> Unit, // NOVO PARAMETRO
    modifier: Modifier = Modifier
) {
    // --- 1. Variáveis de estado para controlar o menu e o diálogo ---
    var menuAberto by remember { mutableStateOf(false) }
    var mostrarDialogoExcluir by remember { mutableStateOf(false) }

    // --- 2. O Card clicável ---
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPacienteClick() }, // Clique principal vai para Detalhes/Histórico
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
                    text = paciente.nome,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "Idade: ${paciente.idade} anos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // --- 4. O Botão de "Três Pontinhos" e o Menu Dropdown ---
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd) // <--- CHAVE PARA O MENU ABRIR CORRETAMENTE
            ) {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Abrir menu de opções",
                        tint = Color.White
                    )
                }

                // --- 5. O Menu Dropdown ---
                DropdownMenu(
                    expanded = menuAberto,
                    onDismissRequest = { menuAberto = false },
                    offset = DpOffset(x = 16.dp, y = 0.dp)
                ) {
                    // Opção 1: Adicionar Medidas (NOVO)
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

                    Divider() // Divisor para separar ações de dados pessoais

                    // Opção 2: Editar Dados (Mantida)
                    DropdownMenuItem(
                        text = { Text("Editar Dados") },
                        onClick = {
                            menuAberto = false
                            onEditarClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    )

                    // Opção 3: Excluir (Mantida)
                    DropdownMenuItem(
                        text = { Text("Excluir Paciente") },
                        onClick = {
                            menuAberto = false
                            mostrarDialogoExcluir = true // Abre o diálogo de confirmação
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, "Excluir")
                        }
                    )
                }
            }
        }
    }

    // --- 6. O Diálogo de Confirmação ---
    if (mostrarDialogoExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExcluir = false },
            title = { Text("Confirmar Exclusão") },
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