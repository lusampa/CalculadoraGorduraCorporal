package com.example.calculadoracorpo

import android.app.Application
import androidx.room.Room
import com.example.calculadoracorpo.data.local.AppDatabase
import com.example.calculadoracorpo.data.repository.PacienteRepository
import com.example.calculadoracorpo.data.repository.PdfGenerator

class MainApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var repository: PacienteRepository
    lateinit var pdfGenerator: PdfGenerator

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        repository = PacienteRepository(
            pacienteDao = database.PacienteDao(),
            medidasDao = database.MedidasDao()
        )

        pdfGenerator = PdfGenerator(applicationContext)
    }
}