package co.tecno.sersoluciones.analityco.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.tecno.sersoluciones.analityco.models.PersonalRealTime
import co.tecno.sersoluciones.analityco.models.Reporte
import co.tecno.sersoluciones.analityco.models.Survey
import co.tecno.sersoluciones.analityco.models.SurveySymptom

@Database(
    entities = [PersonalRealTime::class, Survey::class, Reporte::class,SurveySymptom::class],
    version = 13,
    exportSchema = false
)
abstract class AnalitycoDatabase : RoomDatabase() {
    abstract val analitycoDao: AnalitycoDao

    companion object {
        @Volatile
        private var INSTANCE: AnalitycoDatabase? = null

        fun getInstance(context: Context): AnalitycoDatabase {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AnalitycoDatabase::class.java,
                        "analityco_realtime_db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return instance
            }
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE users")
        }
    }
}