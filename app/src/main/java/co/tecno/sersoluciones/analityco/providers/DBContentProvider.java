package co.tecno.sersoluciones.analityco.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import co.tecno.sersoluciones.analityco.databases.DBHelper;
import kotlin.reflect.jvm.internal.impl.descriptors.impl.AbstractReceiverParameterDescriptor;

import static co.tecno.sersoluciones.analityco.databases.DBHelper.CITY_TABLE_COLUMN_ID;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.CITY_TABLE_COLUMN_NAME;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.DASHBOARD_TABLE_COLUMN_CONTENT;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.PERSONAL_REPORT_TABLE_COLUMN_ID;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_CITY;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_COMPANY;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_CONTRACT;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_CONTRACT_PERSON;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_COPTIONS;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_DANE_CITY;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_DASHBOARD;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_ECONOMIC;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_EMPLOYER;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_INDIVIDUAL_CONTRACT;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_PERSONAL;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_PERSONAL_REPORT;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_PROJECT;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_SECURITY_REFERENCE;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_UPDATE_IMAGE;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.TABLE_NAME_WORK;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.UPDATE_IMAGE_TABLE_COLUMN_ID;
import static co.tecno.sersoluciones.analityco.databases.DBHelper.WORK_TABLE_COLUMN_SERVER_ID;
import static co.tecno.sersoluciones.analityco.utilities.Constantes.AUTHORITY;
import static co.tecno.sersoluciones.analityco.utilities.Constantes.BAD_REQUEST;

/**
 * Created by Ser Soluciones SAS on 14/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
@SuppressWarnings("ConstantConditions")
public class DBContentProvider extends ContentProvider {

    private SQLiteDatabase database;

    private static final UriMatcher sUriMatcher;
    private static final int DATUM_CITY = 1;
    private static final int DATUM_CITY_ID = 2;
    private static final int DATUM_CITY_BULK_INSERT = 3;
    private static final int DATUM_COMPANY = 4;
    private static final int DATUM_COMPANY_BULK_INSERT = 5;
    private static final int DATUM_DANE_CITY = 6;
    private static final int DATUM_DANE_CITY_BULK_INSERT = 7;
    private static final int DATUM_ECONOMIC = 8;
    private static final int DATUM_ECONOMIC_BULK_INSERT = 9;
    private static final int DATUM_PROJECT = 10;
    private static final int DATUM_PROJECT_ID = 11;
    private static final int DATUM_PROJECT_BULK_INSERT = 12;
    private static final int DATUM_EMPLOYER = 13;
    private static final int DATUM_EMPLOYER_BULK_INSERT = 14;
    private static final int DATUM_CONTRACT = 15;
    private static final int DATUM_CONTRACT_BULK_INSERT = 16;
    private static final int DATUM_CONTRACT_PERSON = 17;
    private static final int DATUM_CONTRACT_PERSON_BULK_INSERT = 18;
    private static final int DATUM_PERSON_REPORT = 19;
    private static final int DATUM_PERSON_REPORT_ID = 20;
    private static final int DATUM_PERSON_REPORT_BULK_INSERT = 21;
    private static final int DATUM_PERSONAL = 22;
    private static final int DATUM_PERSONAL_ID = 23;
    private static final int DATUM_PERSONAL_BULK_INSERT = 24;
    private static final int DATUM_DASHBOARD = 25;
    private static final int DATUM_DASHBOARD_BULK_INSERT = 26;
    private static final int DATUM_WORK_BULK_INSERT = 27;
    private static final int DATUM_WORK = 28;
    private static final int DATUM_COPTIONS = 29;
    private static final int DATUM_COPTIONS_BULK_INSERT = 30;
    private static final int DATUM_SEC_REFS = 31;
    private static final int DATUM_SEC_REFS_BULK_INSERT = 32;
    private static final int DATUM_INDIVUAL_CONTRACT = 33;
    private static final int DATUM_INDIVUAL_CONTRACT_BULK_INSERT = 34;
    private static final int DATUM_UPDATE_IMAGE_REPORT = 35;
    private static final int DATUM_UPDATE_IMAGE_ID = 36;
    private static final int DATUM_UPDATE_IMAGE_BULK_INSERT = 37;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CITY, DATUM_CITY);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CITY + "/#", DATUM_CITY_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CITY + "/bulk-insert/", DATUM_CITY_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_COMPANY, DATUM_COMPANY);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_COMPANY + "/bulk-insert/", DATUM_COMPANY_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_DANE_CITY, DATUM_DANE_CITY);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_DANE_CITY + "/bulk-insert/", DATUM_DANE_CITY_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_ECONOMIC, DATUM_ECONOMIC);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_ECONOMIC + "/bulk-insert/", DATUM_ECONOMIC_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PROJECT, DATUM_PROJECT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PROJECT + "/#", DATUM_PROJECT_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PROJECT + "/bulk-insert/", DATUM_PROJECT_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_EMPLOYER, DATUM_EMPLOYER);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_EMPLOYER + "/bulk-insert/", DATUM_EMPLOYER_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CONTRACT, DATUM_CONTRACT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CONTRACT + "/bulk-insert/", DATUM_CONTRACT_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CONTRACT_PERSON, DATUM_CONTRACT_PERSON);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CONTRACT_PERSON + "/bulk-insert/", DATUM_CONTRACT_PERSON_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PERSONAL_REPORT, DATUM_PERSON_REPORT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PERSONAL_REPORT + "/#", DATUM_PERSON_REPORT_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PERSONAL_REPORT + "/bulk-insert/", DATUM_PERSON_REPORT_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_UPDATE_IMAGE, DATUM_UPDATE_IMAGE_REPORT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_UPDATE_IMAGE + "/#", DATUM_UPDATE_IMAGE_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_UPDATE_IMAGE + "/bulk-insert/", DATUM_UPDATE_IMAGE_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PERSONAL, DATUM_PERSONAL);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PERSONAL + "/#", DATUM_PERSONAL_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PERSONAL + "/bulk-insert/", DATUM_PERSONAL_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_INDIVIDUAL_CONTRACT + "/#", DATUM_INDIVUAL_CONTRACT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_INDIVIDUAL_CONTRACT + "/bulk-insert/", DATUM_INDIVUAL_CONTRACT_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_DASHBOARD, DATUM_DASHBOARD);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_DASHBOARD + "/bulk-insert/", DATUM_DASHBOARD_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_WORK, DATUM_WORK);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_WORK + "/bulk-insert/", DATUM_WORK_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_COPTIONS, DATUM_COPTIONS);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_COPTIONS + "/bulk-insert/", DATUM_COPTIONS_BULK_INSERT);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_SECURITY_REFERENCE, DATUM_SEC_REFS);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_SECURITY_REFERENCE + "/bulk-insert/", DATUM_SEC_REFS_BULK_INSERT);
    }

    private DBHelper dbHelper;

    public DBContentProvider() {
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        // permissions to be writable
        database = dbHelper.getWritableDatabase();
        return database != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // the TABLE_NAME to query on
        queryBuilder.setTables(TABLE_NAME_CITY);

        switch (sUriMatcher.match(uri)) {
            // maps all database column names
            case DATUM_CITY_ID:
                queryBuilder.appendWhere(CITY_TABLE_COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case DATUM_CITY:
                break;
            case DATUM_COMPANY:
                queryBuilder.setTables(TABLE_NAME_COMPANY);
                break;
            case DATUM_DANE_CITY:
                queryBuilder.setTables(TABLE_NAME_DANE_CITY);
                sortOrder = DBHelper.DANE_CITY_TABLE_COLUMN_NAME;
                break;
            case DATUM_ECONOMIC:
                queryBuilder.setTables(TABLE_NAME_ECONOMIC);
                break;
            case DATUM_PROJECT:
                queryBuilder.setTables(TABLE_NAME_PROJECT);
                break;
            case DATUM_EMPLOYER:
                queryBuilder.setTables(TABLE_NAME_EMPLOYER);
                break;
            case DATUM_COPTIONS:
                queryBuilder.setTables(TABLE_NAME_COPTIONS);
                sortOrder = DBHelper.COPTIONS_COLUMN_VALUE;
                break;
            case DATUM_CONTRACT:
                queryBuilder.setTables(TABLE_NAME_CONTRACT);
                if (sortOrder == null)
                    sortOrder = CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER;
                break;
            case DATUM_CONTRACT_PERSON:
                queryBuilder.setTables(TABLE_NAME_CONTRACT_PERSON);
                sortOrder = CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID;
                break;
            case DATUM_PERSON_REPORT:
                queryBuilder.setTables(TABLE_NAME_PERSONAL_REPORT);
                sortOrder = PERSONAL_REPORT_TABLE_COLUMN_ID;
                break;
            case DATUM_PERSON_REPORT_ID:
                queryBuilder.setTables(TABLE_NAME_PERSONAL_REPORT);
                queryBuilder.appendWhere("_id =" + uri.getLastPathSegment());
                sortOrder = PERSONAL_REPORT_TABLE_COLUMN_ID;
                break;
            case DATUM_UPDATE_IMAGE_REPORT:
                queryBuilder.setTables(TABLE_NAME_UPDATE_IMAGE);
                sortOrder = UPDATE_IMAGE_TABLE_COLUMN_ID;
                break;
            case DATUM_UPDATE_IMAGE_ID:
                queryBuilder.setTables(TABLE_NAME_UPDATE_IMAGE);
                queryBuilder.appendWhere("_id =" + uri.getLastPathSegment());
                sortOrder = UPDATE_IMAGE_TABLE_COLUMN_ID;
                break;
            case DATUM_PERSONAL:
                queryBuilder.setTables(TABLE_NAME_PERSONAL);
                break;
            case DATUM_INDIVUAL_CONTRACT:
                queryBuilder.setTables(TABLE_NAME_INDIVIDUAL_CONTRACT);
                break;
            case DATUM_PERSONAL_ID:
                queryBuilder.setTables(TABLE_NAME_PERSONAL);
                queryBuilder.appendWhere(PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID + "=" + uri.getLastPathSegment());
                break;

            case DATUM_DASHBOARD:
                queryBuilder.setTables(TABLE_NAME_DASHBOARD);
                sortOrder = DASHBOARD_TABLE_COLUMN_CONTENT;
                break;
            case DATUM_WORK:
                queryBuilder.setTables(TABLE_NAME_WORK);
                sortOrder = WORK_TABLE_COLUMN_SERVER_ID;
                break;
            case DATUM_SEC_REFS:
                queryBuilder.setTables(TABLE_NAME_SECURITY_REFERENCE);
                sortOrder = DBHelper.SECURITY_REFERENCE_COLUMN_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")) {
            // No sorting-> sort on names by default
            sortOrder = CITY_TABLE_COLUMN_NAME;
        }
        Cursor cursor = queryBuilder.query(database, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            // Get all nicknames records
            case DATUM_CITY:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY;
            // Get a particular name
            case DATUM_CITY_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY;
            case DATUM_CITY_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/bulk-insert";
            case DATUM_COMPANY:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/company";
            case DATUM_COMPANY_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/company/bulk-insert";
            case DATUM_DANE_CITY:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/dane-city";
            case DATUM_DANE_CITY_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/dane-city/bulk-insert";
            case DATUM_ECONOMIC:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/economic";
            case DATUM_ECONOMIC_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/economic/bulk-insert";
            case DATUM_PROJECT:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/project";
            case DATUM_PROJECT_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/project/bulk-insert";
            case DATUM_EMPLOYER:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/employer";
            case DATUM_EMPLOYER_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/employer/bulk-insert";
            case DATUM_CONTRACT:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/contract";
            case DATUM_CONTRACT_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/contract/bulk-insert";
            case DATUM_CONTRACT_PERSON:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/contract-person";
            case DATUM_CONTRACT_PERSON_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/contract-person/bulk-insert";
            case DATUM_PERSON_REPORT:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/person-report";
            case DATUM_PERSON_REPORT_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/person-report/bulk-insert";
            case DATUM_PERSONAL:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/personal";
            case DATUM_PERSONAL_BULK_INSERT:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/personal/bulk-insert";
            case DATUM_INDIVUAL_CONTRACT:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/individual-contract";
            case DATUM_INDIVUAL_CONTRACT_BULK_INSERT:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/individual-contract/bulk-insert";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table = "";

        switch (sUriMatcher.match(uri)) {
            // Get all nicknames records
            case DATUM_CITY:
                table = TABLE_NAME_CITY;
                break;
            case DATUM_PERSON_REPORT:
                table = TABLE_NAME_PERSONAL_REPORT;
                break;
            case  DATUM_UPDATE_IMAGE_REPORT:
                table = TABLE_NAME_UPDATE_IMAGE;
                break;
            case DATUM_CONTRACT_PERSON:
                table = TABLE_NAME_CONTRACT_PERSON;
                break;
            default:
                break;
        }

        long row = database.insert(table, "", values);

        // If record is added successfully
        if (row > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into " + uri);
    }

    /**
     * Perform bulkInsert with use of transaction
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int uriType;
        int insertCount = 0;
        String table = "";
        try {
            uriType = sUriMatcher.match(uri);
            SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
            switch (uriType) {
                case DATUM_CITY_BULK_INSERT:
                    table = TABLE_NAME_CITY;
                    break;
                case DATUM_COMPANY_BULK_INSERT:
                    table = TABLE_NAME_COMPANY;
                    break;
                case DATUM_DANE_CITY_BULK_INSERT:
                    table = TABLE_NAME_DANE_CITY;
                    break;
                case DATUM_ECONOMIC_BULK_INSERT:
                    table = TABLE_NAME_ECONOMIC;
                    break;
                case DATUM_PROJECT_BULK_INSERT:
                    table = TABLE_NAME_PROJECT;
                    break;
                case DATUM_EMPLOYER_BULK_INSERT:
                    table = TABLE_NAME_EMPLOYER;
                    break;
                case DATUM_CONTRACT_BULK_INSERT:
                    table = TABLE_NAME_CONTRACT;
                    break;
                case DATUM_CONTRACT_PERSON_BULK_INSERT:
                    table = TABLE_NAME_CONTRACT_PERSON;
                    break;
                case DATUM_PERSON_REPORT_BULK_INSERT:
                    table = TABLE_NAME_PERSONAL_REPORT;
                    break;
                case DATUM_UPDATE_IMAGE_BULK_INSERT:
                    table = TABLE_NAME_UPDATE_IMAGE;
                    break;
                case DATUM_PERSONAL_BULK_INSERT:
                    table = TABLE_NAME_PERSONAL;
                    break;
                case  DATUM_INDIVUAL_CONTRACT_BULK_INSERT:
                    table = TABLE_NAME_INDIVIDUAL_CONTRACT;
                    break;
                case DATUM_WORK_BULK_INSERT:
                    table = TABLE_NAME_WORK;
                    break;
                case DATUM_COPTIONS_BULK_INSERT:
                    table = TABLE_NAME_COPTIONS;
                    break;
                case DATUM_SEC_REFS_BULK_INSERT:
                    table = TABLE_NAME_SECURITY_REFERENCE;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            try {
                sqlDB.beginTransaction();
                for (ContentValues value : values) {
                    long id = sqlDB.insert(table, null, value);
                    if (id > 0)
                        insertCount++;
                    else if (id == -1) {
                        insertCount = -1;
                        break;
                    }
                }
                sqlDB.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqlDB.endTransaction();
            }

            // getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            // Your error handling
        }

        return insertCount;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count;
        switch (sUriMatcher.match(uri)) {
            case DATUM_CITY:
                // delete all the records of the table
                count = database.delete(TABLE_NAME_CITY, selection, selectionArgs);
                break;
            case DATUM_CITY_ID:
                String id = uri.getLastPathSegment();    //gets the id
                count = database.delete(TABLE_NAME_CITY, CITY_TABLE_COLUMN_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            case DATUM_CONTRACT_PERSON:
                count = database.delete(TABLE_NAME_CONTRACT_PERSON, selection, selectionArgs);
                break;
            case DATUM_PERSONAL:
                count = database.delete(TABLE_NAME_PERSONAL, selection, selectionArgs);
                break;
            case DATUM_INDIVUAL_CONTRACT:
                count = database.delete(TABLE_NAME_INDIVIDUAL_CONTRACT,selection,selectionArgs);
                break;
            case DATUM_CONTRACT:
                count = database.delete(TABLE_NAME_CONTRACT, selection, selectionArgs);
                break;
            case DATUM_PROJECT:
                count = database.delete(TABLE_NAME_PROJECT, selection, selectionArgs);
                break;
            case DATUM_COPTIONS:
                count = database.delete(TABLE_NAME_COPTIONS, selection, selectionArgs);
                break;
            case DATUM_SEC_REFS:
                count = database.delete(TABLE_NAME_SECURITY_REFERENCE, selection, selectionArgs);
                break;
            case DATUM_DANE_CITY:
                count = database.delete(TABLE_NAME_DANE_CITY, selection, selectionArgs);
                break;
            case DATUM_ECONOMIC:
                count = database.delete(TABLE_NAME_ECONOMIC, selection, selectionArgs);
                break;
            case DATUM_WORK:
                count = database.delete(TABLE_NAME_WORK, selection, selectionArgs);
                break;
            case DATUM_EMPLOYER:
                count = database.delete(TABLE_NAME_EMPLOYER, selection, selectionArgs);
                break;
            case DATUM_PERSON_REPORT:
                count = database.delete(TABLE_NAME_PERSONAL_REPORT, selection, selectionArgs);
                break;
            case DATUM_PERSON_REPORT_ID:
                // delete all the records of the table'
                String _id = uri.getLastPathSegment();
                count = database.delete(TABLE_NAME_PERSONAL_REPORT, PERSONAL_REPORT_TABLE_COLUMN_ID + " = " + _id, null);
                break;
            case  DATUM_UPDATE_IMAGE_REPORT:
                count = database.delete(TABLE_NAME_UPDATE_IMAGE,selection,selectionArgs);
                break;
            case DATUM_UPDATE_IMAGE_ID:
                _id = uri.getLastPathSegment();
                count = database.delete(TABLE_NAME_UPDATE_IMAGE,UPDATE_IMAGE_TABLE_COLUMN_ID + " = " + _id,null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count;
        switch (sUriMatcher.match(uri)) {
            case DATUM_CITY:
                count = database.update(TABLE_NAME_CITY, values, selection, selectionArgs);
                break;
            case DATUM_PROJECT:
                count = database.update(TABLE_NAME_PROJECT, values, selection, selectionArgs);
                break;
            case DATUM_PROJECT_ID:
                count = database.update(TABLE_NAME_PROJECT, values, DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID +
                        " = " + uri.getLastPathSegment(), null);
                break;
            case DATUM_CITY_ID:
                count = database.update(TABLE_NAME_CITY, values, CITY_TABLE_COLUMN_ID +
                        " = " + uri.getLastPathSegment() +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            case DATUM_PERSON_REPORT_ID:
                count = database.update(TABLE_NAME_PERSONAL_REPORT, values, PERSONAL_REPORT_TABLE_COLUMN_ID +
                        " = " + uri.getLastPathSegment(), null);
                break;
            case DATUM_UPDATE_IMAGE_ID:
                count = database.update(TABLE_NAME_UPDATE_IMAGE, values, UPDATE_IMAGE_TABLE_COLUMN_ID +
                        " = " + uri.getLastPathSegment(), null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
