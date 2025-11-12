package com.example.calculadoracorpo

import android.app.Application
import androidx.room.Room
import com.example.calculadoracorpo.data.local.AppDatabase
import com.example.calculadoracorpo.data.repository.PacienteRepository

/**
 * Esta classe é o ponto de entrada de todo o aplicativo.
 * Ela é criada ANTES de qualquer Activity.
 * É o local perfeito para criar "Singletons" (coisas que
 * só devem existir uma vez, como o banco e o repositório).
 */
class MainApplication : Application() {

    // 1. Crie variáveis 'lateinit' para o banco e o repositório.
    //    Elas serão acessíveis de qualquer lugar do app.
    lateinit var database: AppDatabase
    lateinit var repository: PacienteRepository

    /**
     * O 'onCreate' da Application é chamado UMA VEZ
     * quando o aplicativo é iniciado.
     */
    override fun onCreate() {
        super.onCreate()

        // 2. Construa o banco de dados.
        //    (Note que NÃO usamos .allowMainThreadQueries() aqui,
        //     pois o 'onCreate' da Application pode lidar com isso)
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database" // O nome do seu arquivo de banco
        )
            // Adiciona um fallback para evitar crashes ao mudar
            // a versão do banco durante o desenvolvimento.
            .fallbackToDestructiveMigration()
            .build()

        // 3. Crie o repositório UMA VEZ, passando os DAOs do banco.
        //    (Verifique se os nomes dos seus DAOs estão corretos aqui)
        repository = PacienteRepository(
            pacienteDao = database.PacienteDao(),
            medidasDao = database.MedidasDao() // ou o nome do seu DAO de medidas
        )
    }
}