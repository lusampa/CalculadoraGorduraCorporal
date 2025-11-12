package com.example.calculadoracorpo.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun FundoPadrao
    (modifier: Modifier = Modifier,
     //'content' permite por colocar coisaas dentro da box
     content: @Composable ()-> Unit) {
    val corBase = Color(0xFFC62828)
    val corTopo = Color(0xff3b0000)

    val gradienteVertical = Brush.verticalGradient(
        colors = listOf(corTopo, corBase)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradienteVertical)
    ) {
        // Box 2: O Conteúdo (Interno)
        Box(
            modifier = modifier
        ) {
            content()
        }
    }
}