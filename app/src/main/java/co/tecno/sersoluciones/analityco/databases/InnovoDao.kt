package co.tecno.sersoluciones.analityco.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.tecno.sersoluciones.analityco.models.PersonalContractOfflineNetwork
import co.tecno.sersoluciones.analityco.models.PersonalList
import co.tecno.sersoluciones.analityco.models.ProjectList
import co.tecno.sersoluciones.analityco.utilities.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnovoDao @Inject constructor(private val mContext: Context) {

    private var mSqLiteDatabase: SQLiteDatabase? = null

    private var mDbHelper: DBHelper? = null

    init {
        mDbHelper = DBHelper(mContext)
        open()
    }

    private fun open() {
        mSqLiteDatabase = mDbHelper!!.writableDatabase
    }

    fun selectAllProjects(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        orderBy: String? = null
    )
            : List<ProjectList> {
        val list = mutableListOf<ProjectList>()
        val cursor =
            mSqLiteDatabase!!.query(
                DBHelper.TABLE_NAME_PROJECT,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            )
        cursor?.use {
            val jArray = Utils.cursorToJArray(cursor)
            list.addAll(
                Gson().fromJson<List<ProjectList>>(
                    jArray.toString(),
                    object : TypeToken<List<ProjectList>?>() {}.type
                )
            )
        }
        return list
    }

    fun selectPersonal(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>?,
        orderBy: String? = null
    )
            : List<PersonalContractOfflineNetwork> {
        val list = mutableListOf<PersonalContractOfflineNetwork>()
        val cursor =
            mSqLiteDatabase!!.query(
                DBHelper.TABLE_NAME_PERSONAL,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            )
        cursor?.use {
            val jArray = Utils.cursorToJArray(cursor)
            list.addAll(
                Gson().fromJson<List<PersonalContractOfflineNetwork>>(
                    jArray.toString(),
                    object : TypeToken<List<PersonalContractOfflineNetwork>?>() {}.type
                )
            )
        }
        return list
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun loadProjectSyn(id: String?, Name: String?, Date: String?) {
        val formato = "yyyy-MM-dd HH:mm:ss"
        val formateador = DateTimeFormatter.ofPattern(formato)
        val ahora = LocalDateTime.now()
        if (mSqLiteDatabase != null) {
            val args = arrayOf(id)
            val campos = arrayOf(DBHelper.PROJECT_TABLE_COLUMN_ID_Sync)
            val cursor = mSqLiteDatabase!!.query(
                DBHelper.TABLE_NAME_PROJECT_SYNC,
                campos,
                DBHelper.PROJECT_TABLE_COLUMN_ID_Sync + "=?",
                args,
                null,
                null,
                null
            )
            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val idSqlite = cursor.getString(0)
                if (idSqlite == id) {
                    val cv = ContentValues()
                    cv.put(DBHelper.PROJECT_TABLE_COLUMN_DATE_SYNC, formateador.format(ahora))
                    mSqLiteDatabase!!.update(DBHelper.TABLE_NAME_PROJECT_SYNC, cv, "_id=?", args)
                }
            } else {
                val contentValues = ContentValues()
                contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_ID_Sync, id)
                contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_NAME_SYNC, Name)
                contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_DATE_SYNC, Date)
                mSqLiteDatabase!!.insert(DBHelper.TABLE_NAME_PROJECT_SYNC, null, contentValues)
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    fun loadpersonaltSyn(personalCompanyInfoId: String?) {
        val formato = "yyyy-MM-dd HH:mm:ss"
        val formateador = DateTimeFormatter.ofPattern(formato)
        val ahora = LocalDateTime.now()
        if (mSqLiteDatabase != null) {
            val args = arrayOf(personalCompanyInfoId)
            val campos = arrayOf(DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID)
            val cursor = mSqLiteDatabase!!.query(
                DBHelper.TABLE_NAME_PERSONAL,
                campos,
                DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID + "=?",
                args,
                null,
                null,
                null
            )
            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val idSqlite = cursor.getString(0)
                if (idSqlite == personalCompanyInfoId.toString()) {
                    val cv = ContentValues()
                    cv.put(DBHelper.PROJECT_TABLE_COLUMN_DATE_SYNC, formateador.format(ahora))
                    mSqLiteDatabase!!.update(DBHelper.TABLE_NAME_PERSONAL, cv, "_id=?", args)
                }
            } else {
                val contentValues = ContentValues()
                contentValues.put(DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID, personalCompanyInfoId)
                mSqLiteDatabase!!.insert(DBHelper.TABLE_NAME_PERSONAL, null, contentValues)
            }
        }
    }
    fun DeleteTableProjectSyn() {
        mSqLiteDatabase!!.delete(DBHelper.TABLE_NAME_PROJECT_SYNC, null, null)
    }
    fun insertPersonal(columnValues: ContentValues) {
        val id = mSqLiteDatabase!!.insert(DBHelper.TABLE_NAME_PERSONAL, null, columnValues)
        log("id personal insertado $id")
    }
    fun updatePersonal(columnValues: ContentValues, where: String, whereArgs: Array<String>) {
        val count = mSqLiteDatabase!!.update(
            DBHelper.TABLE_NAME_PERSONAL,
            columnValues,
            where,
            whereArgs
        )
        log("rows affected $count")
    }

    fun deletePersonalOfflineFromProject(selection: String, selectionArgs: Array<String?>) {
        mSqLiteDatabase?.delete(DBHelper.TABLE_NAME_CONTRACT_PERSON, selection, selectionArgs)
    }
    fun updagradePersonalOffilineFromProject(values: ContentValues, whereArgs: Array<String?>){
        mSqLiteDatabase = mDbHelper!!.writableDatabase
        mSqLiteDatabase?.update(
            DBHelper.TABLE_NAME_CONTRACT_PERSON,
            values,
            "ProjectId=? AND PersonalCompanyInfoId=?",
            whereArgs
        )
        //mSqLiteDatabase?.execSQL("UPDATE ContractPerson Set FinishDate= '2021-04-31T23:59:00' WHERE ProjectId= 'bedcacd3-a436-44b6-ad2f-2a40a20d31e8' and PersonalCompanyInfoId='15460'")
    }


    fun getPersonal(selection: String, selectionArgs: Array<String>): PersonalList? {
        mSqLiteDatabase!!.query(
            DBHelper.TABLE_NAME_PERSONAL, null, selection, selectionArgs,
            null, null, null
        ).use {
            it?.let { cursor ->
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    val personalList = PersonalList()
                    personalList.Name = cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_NAME))
                    personalList.LastName = cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_LASTNAME))
                    personalList.DocumentNumber = cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_DOC_NUM))
                    personalList.CompanyInfoId = cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_COMPANY_INFO_ID))
                    personalList.PersonalCompanyInfoId = cursor.getInt(
                        cursor.getColumnIndex(
                            DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID
                        )
                    )
                    return personalList
                }
            }
        }
        return null
    }
}