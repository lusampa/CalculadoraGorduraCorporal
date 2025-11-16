package com.example.calculadoracorpo.features.cadastropaciente

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.example.calculadoracorpo.MainApplication
import com.example.calculadoracorpo.data.model.Sexo
import com.example.calculadoracorpo.ui.theme.components.FundoPadrao
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroPacienteScreen(
    onPacienteSalvo: () -> Unit, // Callback para navegar de volta
    onVoltarClick: () -> Unit
) {
    // --- 1. Injeção de Dependência Manual ---
    val application = LocalContext.current.applicationContext as MainApplication
    val repository = application.repository
    val factory = CadastroPacienteViewModelFactory(repository)
    val viewModel: CadastroPacienteViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- 2. Lógica para o DatePickerDialog ---
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            viewModel.onDataNascimentoChanged(LocalDate.of(year, month + 1, dayOfMonth))
        },
        LocalDate.now().year,
        LocalDate.now().monthValue - 1,
        LocalDate.now().dayOfMonth
    )

    // --- 3. Efeito para navegar de volta quando salvar ---
    LaunchedEffect(uiState.salvouComSucesso) {
        if (uiState.salvouComSucesso) {
            onPacienteSalvo()
        }
    }

    val corLabel = Color.White
    val corCampoFundo = Color(0xFFD9D9D9) // Cinza claro
    val corCampoTexto = Color.Black.copy(alpha = 0.6f)

    FundoPadrao {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp), // Padding lateral
            horizontalAlignment = Alignment.Start // Alinha tudo à esquerda
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- Nome do Paciente ---
            Text("Nome do paciente:", color = corLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = uiState.nome,
                onValueChange = viewModel::onNomeChanged,
                placeholder = { Text("Insira no nome do paciente") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = corCampoFundo,
                    unfocusedContainerColor = corCampoFundo,
                    focusedTextColor = corCampoTexto,
                    unfocusedTextColor = corCampoTexto,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Data de Nascimento ---
            Text("Data de nascimento:", color = corLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val dataFormatada = remember(uiState.dataNascimento) {
                uiState.dataNascimento?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
            }
            TextField(
                value = dataFormatada,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Insira a data") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }, // Abre o picker
                shape = RoundedCornerShape(8.dp),
                trailingIcon = { Icon(Icons.Default.DateRange, "Selecionar Data") },
                colors = TextFieldDefaults.colors(
                    disabledTextColor = corCampoTexto, // Corrigido para 'disabled'
                    disabledContainerColor = corCampoFundo,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTrailingIconColor = corCampoTexto
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Sexo (Dropdown) ---
            Text("Sexo:", color = corLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            var sexoDropdownAberto by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = sexoDropdownAberto,
                onExpandedChange = { sexoDropdownAberto = !sexoDropdownAberto }
            ) {
                TextField(
                    value = if (uiState.sexo == Sexo.MASCULINO) "Masculino" else "Feminino",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexoDropdownAberto) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = corCampoTexto,
                        disabledContainerColor = corCampoFundo,
                        disabledIndicatorColor = Color.Transparent,
                        disabledTrailingIconColor = corCampoTexto
                    ),
                    enabled = false
                )
                ExposedDropdownMenu(
                    expanded = sexoDropdownAberto,
                    onDismissRequest = { sexoDropdownAberto = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Masculino") },
                        onClick = {
                            viewModel.onSexoChanged(Sexo.MASCULINO)
                            sexoDropdownAberto = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Feminino") },
                        onClick = {
                            viewModel.onSexoChanged(Sexo.FEMININO)
                            sexoDropdownAberto = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Altura e Peso ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Altura em cm:", color = corLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = uiState.altura,
                    onValueChange = viewModel::onAlturaChanged,
                    placeholder = { Text("Informe a altura") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = corCampoFundo, unfocusedContainerColor = corCampoFundo,
                        focusedTextColor = corCampoTexto, unfocusedTextColor = corCampoTexto,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Botão Salvar ---
            Button(
                onClick = viewModel::onSalvarClicked,
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
                    Text("ADICIONAR PACIENTE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // --- Mensagem de Erro ---
            if (uiState.erro != null) {
                Text(
                    text = uiState.erro!!,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}