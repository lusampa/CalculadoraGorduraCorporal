package com.example.calculadoracorpo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.Plagiarism
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calculadoracorpo.features.TelaHome
import com.example.calculadoracorpo.features.cadastropaciente.CadastroPacienteScreen
import com.example.calculadoracorpo.features.listapacientes.ListaPacienteScreen

// --- 1. Definição das Rotas ---
object AppRoutes {
    const val HOME = "home"
    const val PACIENTES = "pacientes"
    const val AVALIACOES = "avaliacoes"
    const val MEDIDAS = "medidas"
    const val CADASTRO_PACIENTE = "cadastro_paciente"

    const val EDITAR_PACIENTE = "editar_paciente/{pacienteId}"

    fun rotaParaEditar(pacienteId: Int): String {
        return "editar_paciente/$pacienteId"
    }
}

// --- 2. Itens do Menu
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("HOME", Icons.Default.Home, AppRoutes.HOME),
    BottomNavItem("PACIENTES", Icons.Default.Person, AppRoutes.PACIENTES),
    BottomNavItem("AVALIAÇÕES", Icons.Outlined.Plagiarism, AppRoutes.AVALIACOES),
    BottomNavItem("MEDIDAS", Icons.Default.Straighten, AppRoutes.MEDIDAS)
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute != AppRoutes.CADASTRO_PACIENTE && currentRoute != AppRoutes.EDITAR_PACIENTE) {
                AppBottomBar(navController = navController, items = bottomNavItems)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Rota 1: Home
            composable(AppRoutes.HOME) {
                TelaHome()
            }

            // Rota 2: Lista de Pacientes
            composable(AppRoutes.PACIENTES) {
                ListaPacienteScreen(
                    onPacienteClick = { pacienteId ->
                        // TODO: navController.navigate("detalhes/$pacienteId")
                    },
                    onAddPacienteClick = {
                        navController.navigate(AppRoutes.CADASTRO_PACIENTE)
                    },
                    onEditarPacienteClick = { pacienteId ->

                        navController.navigate(AppRoutes.rotaParaEditar(pacienteId))
                    }
                )
            }

            // Rota 3: Cadastro de Paciente
            composable(AppRoutes.CADASTRO_PACIENTE) {
                CadastroPacienteScreen(
                    onPacienteSalvo = {
                        navController.popBackStack() // Volta para a tela anterior
                    },
                    onVoltarClick = {
                        navController.popBackStack() // Volta para a tela anterior
                    }
                )
            }

            //Rota 4: Edição de Paciente
            composable(
                route = AppRoutes.EDITAR_PACIENTE,
                arguments = listOf(navArgument("pacienteId") { type = NavType.IntType })
            ) { backStackEntry ->
                // Pega o ID da rota
                val pacienteId = backStackEntry.arguments?.getInt("pacienteId")

                // TODO: Você precisará criar uma 'EditarPacienteScreen'
                // que recebe o 'pacienteId', busca os dados no VM e preenche

                CadastroPacienteScreen(
                    onPacienteSalvo = { navController.popBackStack() },
                    onVoltarClick = { navController.popBackStack() }
                )
            }


            // Rota 5: Avaliações (Placeholder)
            composable(AppRoutes.AVALIACOES) {
                // TODO: TelaAvaliacoesPlaceHolder()
                TelaHome()
            }

            // Rota 6: Medidas (Placeholder)
            composable(AppRoutes.MEDIDAS) {
                // TODO: TelaMedidasPlaceHolder()

                TelaHome()
            }


        }
    }
}

@Composable
fun AppBottomBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.9f)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                },
                label = { Text(item.label, fontSize = 11.sp) },
                icon = { Icon(item.icon, item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Red,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),

                )
            )
        }
    }
}