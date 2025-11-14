package com.example.calculadoracorpo.navigation

import com.example.calculadoracorpo.features.avaliacoesgeral.HistoricoGeralAvaliacoesScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
// IMPORTAÇÃO CORRIGIDA PARA O ARROWBACK NÃO DEPRECIADO
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.SavedStateRegistryOwner
import com.example.calculadoracorpo.MainApplication
import com.example.calculadoracorpo.features.TelaHome
import com.example.calculadoracorpo.features.avaliacoes.AvaliacoesScreen
import com.example.calculadoracorpo.features.avaliacoes.AvaliacoesViewModelFactory
import com.example.calculadoracorpo.features.cadastropaciente.CadastroPacienteScreen
import com.example.calculadoracorpo.features.listapacientes.ListaPacienteScreen
import com.example.calculadoracorpo.features.medidas.MedidasScreen
import com.example.calculadoracorpo.features.medidas.MedidasViewModelFactory
// IMPORTAÇÃO NECESSÁRIA PARA O NOVO ARGUMENTO
import com.example.calculadoracorpo.data.repository.PdfGenerator

object AppRoutes {
    // Rotas Principais (para o BottomBar)
    const val HOME = "home"
    const val LISTA_PACIENTES = "listaPacientes"
    const val AVALIACOES = "avaliacoes" // Rota para histórico geral

    // Rotas Secundárias (sem BottomBar)
    const val CADASTRO_PACIENTE = "cadastroPaciente"

    // Rotas com Argumentos
    // Detalhes da Avaliação (Histórico)
    const val ARG_PACIENTE_ID = "pacienteId"
    const val DETALHE_AVALIACOES = "detalheAvaliacoes/{$ARG_PACIENTE_ID}"
    fun detalheAvaliacoes(pacienteId: Int) = "detalheAvaliacoes/$pacienteId"

    // Entrada de Medidas (Formulário)
    const val ENTRADA_MEDIDAS = "entradaMedidas/{$ARG_PACIENTE_ID}"
    fun entradaMedidas(pacienteId: Int) = "entradaMedidas/$pacienteId"
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// --- NOVO: Composable Placeholder para o Histórico Geral ---
@Composable
fun HistoricoGeralAvaliacoesScreen(
    onAvaliacaoClick: (pacienteId: Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // **TODO:** Implementar ViewModel e LazyColumn para listar TODAS as avaliações
        Text(
            text = "Esta é a tela de Histórico Geral de Avaliações (de todos os pacientes).",
            color = Color.White
        )
    }
}
// -----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // Acesso ao Repositório do MainApplication
    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val repository = application.repository

    // INSTANCIA O PDF GENERATOR UMA VEZ
    val pdfGenerator = remember { PdfGenerator(context) }

    val bottomNavItems = listOf(
        BottomNavItem("Início", Icons.Default.Home, AppRoutes.HOME),
        BottomNavItem("Pacientes", Icons.Default.Person, AppRoutes.LISTA_PACIENTES),
        BottomNavItem("Avaliações", Icons.Default.Straighten, AppRoutes.AVALIACOES),
    )

    Scaffold(
        bottomBar = { AppBottomBar(navController, bottomNavItems) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Rota 1: Início
            composable(AppRoutes.HOME) {
                TelaHome()
            }

            // Rota 2: Lista de Pacientes
            composable(AppRoutes.LISTA_PACIENTES) {
                ListaPacienteScreen(
                    onPacienteClick = { pacienteId ->
                        navController.navigate(AppRoutes.detalheAvaliacoes(pacienteId))
                    },
                    onAddPacienteClick = { navController.navigate(AppRoutes.CADASTRO_PACIENTE) },
                    onEditarPacienteClick = { /* TODO: Implementar edição */ },
                    onAdicionarMedidasClick = { pacienteId ->
                        navController.navigate(AppRoutes.entradaMedidas(pacienteId))
                    }
                )
            }

            // Rota 3: Cadastro de Paciente
            composable(AppRoutes.CADASTRO_PACIENTE) {
                CadastroPacienteScreen(
                    onPacienteSalvo = { navController.popBackStack() },
                    onVoltarClick = { navController.popBackStack() }
                )
            }

            // Rota 4: Avaliações Geral (Configurada para Histórico Geral)
            composable(AppRoutes.AVALIACOES) {
                HistoricoGeralAvaliacoesScreen(
                    onAvaliacaoClick = { pacienteId ->
                        // Permite clicar em uma avaliação para ir para o histórico individual
                        navController.navigate(AppRoutes.detalheAvaliacoes(pacienteId))
                    }
                )
            }

            // Rota 5: Detalhes das Avaliações (Histórico e Resultado)
            composable(
                route = AppRoutes.DETALHE_AVALIACOES,
                arguments = listOf(navArgument(AppRoutes.ARG_PACIENTE_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val pacienteId = backStackEntry.arguments?.getInt(AppRoutes.ARG_PACIENTE_ID) ?: -1

                // Obtendo o Owner DENTRO do bloco composable
                val owner = LocalContext.current as SavedStateRegistryOwner

                // AGORA PASSAMOS O pdfGenerator PARA A FACTORY
                val factory = AvaliacoesViewModelFactory(repository, owner, pacienteId, pdfGenerator)

                AvaliacoesScreen(
                    pacienteId = pacienteId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddMedidaClick = { id ->
                        navController.navigate(AppRoutes.entradaMedidas(id))
                    }
                )
            }

            // Rota 6: Entrada de Medidas (Formulário)
            composable(
                route = AppRoutes.ENTRADA_MEDIDAS,
                arguments = listOf(navArgument(AppRoutes.ARG_PACIENTE_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val pacienteId = backStackEntry.arguments?.getInt(AppRoutes.ARG_PACIENTE_ID) ?: -1

                // Obtendo o Owner DENTRO do bloco composable
                val owner = LocalContext.current as SavedStateRegistryOwner

                val factory = MedidasViewModelFactory(repository, owner, pacienteId)

                MedidasScreen(
                    pacienteId = pacienteId,
                    onMedidasSalvas = {
                        // Após salvar, volta para a tela de detalhes do paciente
                        navController.popBackStack()
                    }
                )
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
        // Pega a rota da aba selecionada (sem argumentos)
        val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

        items.forEach { item ->
            val isSelected = currentRoute == item.route.substringBefore("/")
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // Limpa o estado e volta para o topo da rota selecionada
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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