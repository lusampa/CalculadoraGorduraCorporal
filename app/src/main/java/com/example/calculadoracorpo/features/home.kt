package com.example.calculadoracorpo.features

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
// IMPORTAÇÃO CORRIGIDA (Outlined)
import androidx.compose.material.icons.outlined.Plagiarism
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadoracorpo.R
import com.example.calculadoracorpo.features.listapacientes.ListaPacienteScreen


data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

// FUNDO VERMELHO
@Composable
fun TelaHome(modifier: Modifier = Modifier) {
    val corBase = Color(0xFFC62828)
    val corTopo = Color(0xff3b0000)

    val gradienteVertical =  Brush.verticalGradient(
        colors = listOf(corTopo,corBase)
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradienteVertical),
        contentAlignment = Alignment.Center
    ) {
       Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "Silhueta Corporal",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// Placeholders das telas (com conteúdo para teste)

@Composable
fun TelaAvaliacoesPlaceHolder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tela de Avaliações", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TelaMedidasPlaceHolder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tela de Medidas", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}