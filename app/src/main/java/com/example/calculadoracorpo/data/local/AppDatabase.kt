package com.example.calculadoracorpo.data.local

/**
 * É a sintonia da parada, a função é unir todas as peças e fazer elas se comunicarem entre sim
 * e usada para gerar todo o código necessário para gerenciar o banco de dados */

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.calculadoracorpo.data.model.Medidas
import com.example.calculadoracorpo.data.model.Paciente



@Database(entities = [Paciente::class , Medidas::class] ,version =4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){

    abstract fun PacienteDao(): PacienteDao
    abstract fun MedidasDao(): MedidasDao

    }
