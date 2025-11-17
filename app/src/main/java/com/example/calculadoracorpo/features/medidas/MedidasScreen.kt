package com.example.calculadoracorpo.features.medidas

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.MainApplication
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao
import java.time.format.DateTimeFormatter
import com.example.calculadoracorpo.data.model.Sexo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedidasScreen(
    pacienteId: Int,
    avaliacaoId: Int = -1,
    onMedidasSalvas: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as MainApplication
    val repository = application.repository

    val owner = LocalContext.current as SavedStateRegistryOwner

    val factory = MedidasViewModelFactory(repository, owner, pacienteId, avaliacaoId)
    val viewModel: MedidasViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.salvouComSucesso) {
        if (uiState.salvouComSucesso) {
            onMedidasSalvas()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Avaliação de ${uiState.nomePaciente}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMedidasSalvas) { // Usa onMedidasSalvas para voltar
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        FundoPadrao(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Paciente: ${uiState.nomePaciente}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Data: ${uiState.dataAvaliacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                ProtocoloDropdown(
                    protocoloSelecionado = uiState.protocoloSelecionado,
                    onProtocoloChange = viewModel::onProtocoloChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Medidas Básicas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampoMedida(
                        label = "Peso (Kg)",
                        value = uiState.peso,
                        onValueChange = viewModel::onPesoChange,
                        modifier = Modifier.weight(1f)
                    )
                    CampoMedida(
                        label = "Altura (cm)",
                        value = uiState.altura,
                        onValueChange = viewModel::onAlturaChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Circunferências (em cm)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                CircunferenciasInputs(uiState, viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Dobras Cutâneas (em mm)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                DobrasCutaneasInputs(uiState, viewModel)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = viewModel::onSalvarMedidasClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(50)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853) // Verde
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SALVAR MEDIDAS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (uiState.erro != null) {
                    Text(
                        text = uiState.erro!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProtocoloDropdown(
    protocoloSelecionado: Protocolo,
    onProtocoloChange: (Protocolo) -> Unit
) {
    var dropdownAberto by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text("Protocolo de Avaliação:", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = dropdownAberto,
            onExpandedChange = { dropdownAberto = !dropdownAberto }
        ) {
            TextField(
                value = protocoloSelecionado.name.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAberto) },
                // CORRIGIDO: Depreciação de menuAnchor
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    disabledTextColor = Color.Black.copy(alpha = 0.6f),
                    disabledContainerColor = Color(0xFFD9D9D9),
                    disabledIndicatorColor = Color.Transparent,
                    disabledTrailingIconColor = Color.Black.copy(alpha = 0.6f)
                ),
                enabled = false
            )
            ExposedDropdownMenu(
                expanded = dropdownAberto,
                onDismissRequest = { dropdownAberto = false }
            ) {
                Protocolo.entries.filter { it != Protocolo.SEM_DEFINICAO }.forEach { protocolo ->
                    DropdownMenuItem(
                        text = { Text(protocolo.name.replace("_", " ")) },
                        onClick = {
                            onProtocoloChange(protocolo)
                            dropdownAberto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CampoMedida(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isObrigatorio: Boolean = false
) {
    val corCampoFundo = Color(0xFFD9D9D9)
    val corCampoTexto = Color.Black.copy(alpha = 0.8f)

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, fontSize = 14.sp)
            if (isObrigatorio) {
                Text("*", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 2.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("0") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = corCampoFundo, unfocusedContainerColor = corCampoFundo,
                focusedTextColor = corCampoTexto, unfocusedTextColor = corCampoTexto,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun CircunferenciasInputs(uiState: MedidasState, viewModel: MedidasViewModel) {
    val todasCircunferencias = mapOf(
        "Bíceps (cm)" to Pair(uiState.circunferenciaBiceps, viewModel::onCircunferenciaBicepsChange),
        "Bíceps Cont. (cm)" to Pair(uiState.circunferenciaBicepsContraido, viewModel::onCircunferenciaBicepsContraidoChange),
        "Peitoral (cm)" to Pair(uiState.circunferenciaPeitoral, viewModel::onCircunferenciaPeitoralChange),
        "Cintura (cm)" to Pair(uiState.circunferenciaCintura, viewModel::onCircunferenciaCinturaChange),
        "Abdômen (cm)" to Pair(uiState.circunferenciaAbdomen, viewModel::onCircunferenciaAbdomenChange),
        "Quadril (cm)" to Pair(uiState.circunferenciaQuadril, viewModel::onCircunferenciaQuadrilChange),
        "Coxa (cm)" to Pair(uiState.circunferenciaCoxa, viewModel::onCircunferenciaCoxaChange),
        "Panturrilha (cm)" to Pair(uiState.circunferenciaPanturrilha, viewModel::onCircunferenciaPanturrilhaChange),    )

    Column {
        var index = 0
        val keys = todasCircunferencias.keys.toList()
        while (index < keys.size) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val key1 = keys[index]
                val (value1, onChange1) = todasCircunferencias[key1]!!
                CampoMedida(
                    label = key1,
                    value = value1,
                    onValueChange = onChange1,
                    modifier = Modifier.weight(1f)
                )

                index++
                if (index < keys.size) {
                    val key2 = keys[index]
                    val (value2, onChange2) = todasCircunferencias[key2]!!
                    CampoMedida(
                        label = key2,
                        value = value2,
                        onValueChange = onChange2,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                index++
            }
        }
    }
}


@Composable
fun DobrasCutaneasInputs(uiState: MedidasState, viewModel: MedidasViewModel) {
    val protocolo = uiState.protocoloSelecionado
    val sexo = uiState.sexoPaciente

    val todasDobras = mapOf(
        "Peitoral" to Pair(uiState.dobraPeitoral, viewModel::onDobraPeitoralChange),
        "Abdominal" to Pair(uiState.dobraAbdominal, viewModel::onDobraAbdominalChange),
        "Triciptal" to Pair(uiState.dobraTriciptal, viewModel::onDobraTriciptalChange),
        "Subescapular" to Pair(uiState.dobraSubescapular, viewModel::onDobraSubescapularChange),
        "Supra-Ilíaca" to Pair(uiState.dobraSupraIliaca, viewModel::onDobraSupraIliacaChange),
        "Axilar Média" to Pair(uiState.dobraAxilarMedia, viewModel::onDobraAxilarMediaChange),
        "Coxa" to Pair(uiState.dobraCoxa, viewModel::onDobraCoxaChange)
    )

    val dobrasVisiveis = when (protocolo) {
        Protocolo.PROTOCOLO_7_DOBRAS -> todasDobras.keys.toList()
        Protocolo.PROTOCOLO_3_DOBRAS -> {
            when (sexo) {
                Sexo.MASCULINO -> listOf("Peitoral", "Abdominal", "Coxa")
                Sexo.FEMININO -> listOf("Triciptal", "Supra-Ilíaca", "Coxa")
            }
        }
        Protocolo.SEM_DEFINICAO -> emptyList()
    }

    Column {
        var index = 0
        while (index < dobrasVisiveis.size) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val key1 = dobrasVisiveis[index]
                val (value1, onChange1) = todasDobras[key1]!!
                CampoMedida(
                    label = key1,
                    value = value1,
                    onValueChange = onChange1,
                    modifier = Modifier.weight(1f),
                    isObrigatorio = true
                )

                index++
                if (index < dobrasVisiveis.size) {
                    val key2 = dobrasVisiveis[index]
                    val (value2, onChange2) = todasDobras[key2]!!
                    CampoMedida(
                        label = key2,
                        value = value2,
                        onValueChange = onChange2,
                        modifier = Modifier.weight(1f),
                        isObrigatorio = true
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                index++
            }
        }
    }
}