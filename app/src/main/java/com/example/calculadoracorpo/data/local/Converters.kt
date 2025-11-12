package com.example.calculadoracorpo.data.local

/**
 *  É O Tradutor dos Dados de Kotlin para o Banco.
 *  o Room identifica os parametros de retorno e usa sempre q precisa salvar ou ler
 *  DeLocalDate -> manda para o banco com um numero de dias desde 1970
 *  ParaLocalDate-> Converte o long do banco e manda de volta para o Kotlin.
 */
import androidx.room.TypeConverter
import com.example.calculadoracorpo.data.model.Protocolo
import com.example.calculadoracorpo.data.model.Sexo
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun deLocalDate(data: LocalDate?):Long?{
        return data?.toEpochDay()
    }
    @TypeConverter
    fun paraLocalDate(valor: Long?): LocalDate?{
        return valor?.let{ LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun deSexo(sexo: Sexo?): String?{
        return sexo?.name // Salva MASCULINO OU FEMININO
    }

    @TypeConverter
    fun paraSexo(valor: String?): Sexo? {
        return valor?.let{Sexo.valueOf(it) }
    }

    @TypeConverter
    fun deProtocolo(protocolo: Protocolo?): String? {
        return protocolo?.name // Salva como "JACKSON_POLLOCK_3"
    }

    @TypeConverter
    fun paraProtocolo(valor: String?): Protocolo? {
        return valor?.let { Protocolo.valueOf(it) }
    }
}