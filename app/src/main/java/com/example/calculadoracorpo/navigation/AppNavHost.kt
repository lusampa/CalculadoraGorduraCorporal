package com.example.calculadoracorpo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.example.calculadoracorpo.features.avaliacoesgeral.HistoricoGeralAvaliacoesScreen // Importado

// --- 1. ROTAS E ARGUMENTOS ---
object AppRoutes {
    const val HOME = "home"
    const val LISTA_PACIENTES = "listaPacientes"
    const val AVALIACOES = "avaliacoes"

    // [MODIFICADO] Rota de Cadastro/Edição Base
    const val ARG_PACIENTE_ID = "pacienteId"
    const val CADASTRO_PACIENTE_BASE = "cadastroPaciente?$ARG_PACIENTE_ID={$ARG_PACIENTE_ID}"

    // [NOVO] Funções de Navegação para Cadastro e Edição
    fun cadastroPaciente() = "cadastroPaciente" // Novo Cadastro (sem ID)
    fun editarPaciente(pacienteId: Int) = "cadastroPaciente?$ARG_PACIENTE_ID=$pacienteId" // Edição (com ID)

    const val ARG_AVALIACAO_ID = "avaliacaoId" // Usado para Edição

    const val DETALHE_AVALIACOES = "detalheAvaliacoes/{$ARG_PACIENTE_ID}"
    fun detalheAvaliacoes(pacienteId: Int) = "detalheAvaliacoes/$pacienteId"

    // Rota de Entrada/Edição: PacienteId obrigatório, AvaliacaoId opcional (default -1)
    const val ENTRADA_MEDIDAS = "entradaMedidas/{$ARG_PACIENTE_ID}?$ARG_AVALIACAO_ID={$ARG_AVALIACAO_ID}"
    fun entradaMedidas(pacienteId: Int) = "entradaMedidas/$pacienteId"
    fun editarMedidas(pacienteId: Int, avaliacaoId: Int) = "entradaMedidas/$pacienteId?$ARG_AVALIACAO_ID=$avaliacaoId"
}

// --- 2. COMPONENTES AUXILIARES CONSOLIDADOS ---
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppBottomBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    // ... (Lógica do NavigationBar inalterada)
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.9f)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

        items.forEach { item ->
            val isSelected = currentRoute == item.route.substringBefore("/")
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
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

@Composable
fun HistoricoGeralAvaliacoesScreen( // Usando o componente da pasta features.avaliacoesgeral
    onAvaliacaoClick: (pacienteId: Int) -> Unit
) {
    // Implementação real estaria aqui, mas mantemos o placeholder como no seu código anterior
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Esta é a tela de Histórico Geral de Avaliações (de todos os pacientes).",
            color = Color.White
        )
    }
}


// --- 3. NAV HOST PRINCIPAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as MainApplication

    // Injeção de dependências do MainApplication
    val repository = application.repository
    val pdfGenerator = application.pdfGenerator // Assumindo que foi inicializado em MainApplication

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
            composable(AppRoutes.HOME) { TelaHome() }

            // Rota 2: Lista de Pacientes
            composable(AppRoutes.LISTA_PACIENTES) {
                ListaPacienteScreen(
                    onPacienteClick = { pacienteId ->
                        navController.navigate(AppRoutes.detalheAvaliacoes(pacienteId))
                    },
                    // [MODIFICADO] Navega para o Cadastro como "novo"
                    onAddPacienteClick = { navController.navigate(AppRoutes.cadastroPaciente()) },

                    // [MODIFICADO] Navega para o Cadastro como "edição", passando o ID
                    onEditarPacienteClick = { pacienteId ->
                        navController.navigate(AppRoutes.editarPaciente(pacienteId))
                    },
                    onAdicionarMedidasClick = { pacienteId ->
                        navController.navigate(AppRoutes.entradaMedidas(pacienteId))
                    }
                )
            }

            // Rota 3: Cadastro/Edição de Paciente
            composable(
                // [CORRIGIDO] Rota base que aceita o ID opcional.
                route = AppRoutes.CADASTRO_PACIENTE_BASE,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_PACIENTE_ID) {
                        type = NavType.IntType
                        defaultValue = -1 // O -1 serve como indicador de "novo cadastro"
                        // [REMOVIDO] A linha `nullable = true` para evitar o erro.
                    }
                )
            ) { backStackEntry ->
                // O ID será -1 (Novo) ou o ID real (Edição)
                val pacienteId = backStackEntry.arguments?.getInt(AppRoutes.ARG_PACIENTE_ID) ?: -1

                CadastroPacienteScreen(
                    pacienteId = pacienteId,
                    onPacienteSalvo = { navController.popBackStack() },
                    onVoltarClick = { navController.popBackStack() }
                )
            }

            // Rota 4: Avaliações Geral
            composable(AppRoutes.AVALIACOES) {
                HistoricoGeralAvaliacoesScreen(
                    onAvaliacaoClick = { pacienteId ->
                        navController.navigate(AppRoutes.detalheAvaliacoes(pacienteId))
                    }
                )
            }

            // Rota 5: Detalhes das Avaliações (Histórico Individual)
            composable(
                route = AppRoutes.DETALHE_AVALIACOES,
                arguments = listOf(navArgument(AppRoutes.ARG_PACIENTE_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val pacienteId = backStackEntry.arguments?.getInt(AppRoutes.ARG_PACIENTE_ID) ?: -1
                val owner = LocalContext.current as SavedStateRegistryOwner

                val factory = AvaliacoesViewModelFactory(repository, owner, pacienteId, pdfGenerator)

                AvaliacoesScreen(
                    pacienteId = pacienteId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddMedidaClick = { id ->
                        navController.navigate(AppRoutes.entradaMedidas(id))
                    },
                    onEditMedidaClick = { pId, aId ->
                        // Navega para a rota de edição com os dois IDs
                        navController.navigate(AppRoutes.editarMedidas(pId, aId))
                    }
                )
            }

            // Rota 6: Entrada/Edição de Medidas (Formulário)
            composable(
                route = AppRoutes.ENTRADA_MEDIDAS,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_PACIENTE_ID) { type = NavType.IntType },
                    navArgument(AppRoutes.ARG_AVALIACAO_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val pacienteId = backStackEntry.arguments?.getInt(AppRoutes.ARG_PACIENTE_ID) ?: -1
                val avaliacaoId = backStackEntry.arguments?.getInt(AppRoutes.ARG_AVALIACAO_ID) ?: -1

                val owner = LocalContext.current as SavedStateRegistryOwner

                val factory = MedidasViewModelFactory(repository, owner, pacienteId, avaliacaoId)

                MedidasScreen(
                    pacienteId = pacienteId,
                    avaliacaoId = avaliacaoId,
                    onMedidasSalvas = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}