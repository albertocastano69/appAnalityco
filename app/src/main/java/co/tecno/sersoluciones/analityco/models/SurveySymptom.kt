package co.tecno.sersoluciones.analityco.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "surveySymptom")
data class SurveySymptom(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0L,
        val data: String
)
