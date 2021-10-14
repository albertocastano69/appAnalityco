package co.tecno.sersoluciones.analityco.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by Ser Soluciones SAS on 25/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private final SQLiteDatabase database;

    // database configuration
    // if you want the onUpgrade to run then change the database_version
    private static final int DATABASE_VERSION = 80;
    private static final String DATABASE_NAME = "innovadata.db";

    // table configuration
    public static final String TABLE_NAME_CITY = "City";         // Table name
    public static final String CITY_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String CITY_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String CITY_TABLE_COLUMN_NAME = "Name";
    public static final String CITY_TABLE_COLUMN_STATE = "StateName";
    public static final String CITY_TABLE_COLUMN_STATE_CODE = "StateCode";
    public static final String CITY_TABLE_COLUMN_CITY_CODE = "CityCode";
    public static final String CITY_TABLE_COLUMN_CODE = "Code";
    public static final String CITY_TABLE_COLUMN_COUNTRY_CODE = "CountryCode";

    private final String[] cityCols = {CITY_TABLE_COLUMN_ID, CITY_TABLE_COLUMN_SERVER_ID, CITY_TABLE_COLUMN_NAME,
            CITY_TABLE_COLUMN_STATE, CITY_TABLE_COLUMN_CODE, CITY_TABLE_COLUMN_STATE_CODE, CITY_TABLE_COLUMN_COUNTRY_CODE};


    // table configuration company
    public static final String TABLE_NAME_COMPANY = "Company";         // Table name
    private static final String COMPANY_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String COMPANY_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String COMPANY_TABLE_COLUMN_SERVER_IDINFO = "CompanyInfoId";
    public static final String COMPANY_TABLE_COLUMN_ACTIVE = "IsActive";
    public static final String COMPANY_TABLE_COLUMN_START_DATE = "StartDate";
    public static final String COMPANY_TABLE_COLUMN_FINISH_DATE = "FinishDate";
    public static final String COMPANY_TABLE_COLUMN_NAME = "Name";
    public static final String COMPANY_TABLE_COLUMN_PHONE = "Phone";
    public static final String COMPANY_TABLE_COLUMN_ADDRESS = "Address";
    public static final String COMPANY_TABLE_COLUMN_DOC_TYPE = "DocumentType";
    public static final String COMPANY_TABLE_COLUMN_DOC_NUM = "DocumentNumber";
    public static final String COMPANY_TABLE_COLUMN_NAME_CITY = "NameCity";
    public static final String COMPANY_TABLE_COLUMN_NAME_STATE = "StateName";
    public static final String COMPANY_TABLE_COLUMN_EXPIRY = "Expiry";
    public static final String COMPANY_TABLE_COLUMN_LOGO = "Logo";
    public static final String COMPANY_TABLE_COLUMN_BRANCH_OFFICES = "BranchOffices";

    // table configuration
    public static final String TABLE_NAME_DANE_CITY = "DaneCity";         // Table name
    private static final String DANE_CITY_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String DANE_CITY_TABLE_COLUMN_NAME = "CityName";
    public static final String DANE_CITY_TABLE_COLUMN_STATE = "StateName";
    public static final String DANE_CITY_TABLE_COLUMN_STATE_CODE = "StateCode";
    public static final String DANE_CITY_TABLE_COLUMN_CODE = "CityCode";

    // table configuration
    public static final String TABLE_NAME_ECONOMIC = "EconomicActivity";         // Table name
    private static final String ECONOMIC_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String ECONOMIC_TABLE_COLUMN_CODE = "Code";
    public static final String ECONOMIC_TABLE_COLUMN_NAME = "Name";

    // table DashBoard
    public static final String TABLE_NAME_DASHBOARD = "DashBoard";         // Table name
    private static final String DASHBOARD_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String DASHBOARD_TABLE_COLUMN_CONTENT = "Content";

    // table work
    public static final String TABLE_NAME_WORK = "Work";         // Table name
    private static final String WORK_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String WORK_TABLE_COLUMN_NAME = "Name";
    public static final String WORK_TABLE_COLUMN_CODE = "code";
    public static final String WORK_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String WORK_TABLE_COLUMN_NAME_ASCII = "NameASCII";

    // table CommonOptions
    public static final String TABLE_NAME_COPTIONS = "CommonOptions";
    public static final String COPTIONS_COLUMN_ID = "_id";
    public static final String COPTIONS_COLUMN_SERVER_ID = "Id";
    private static final String COPTIONS_COLUMN_IS_ACTIVE = "IsActive";
    private static final String COPTIONS_COLUMN_ORDER_NUM = "OrderNum";
    public static final String COPTIONS_COLUMN_TYPE = "Type";
    public static final String COPTIONS_COLUMN_VALUE = "Value";
    public static final String COPTIONS_COLUMN_DESC = "Description";

    // table configuration
    public static final String TABLE_NAME_PROJECT = "Project";         // Table name
    public static final String PROJECT_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String PROJECT_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String PROJECT_TABLE_COLUMN_ACTIVE = "IsActive";
    public static final String PROJECT_TABLE_COLUMN_START_DATE = "StartDate";
    public static final String PROJECT_TABLE_COLUMN_FINISH_DATE = "FinishDate";
    public static final String PROJECT_TABLE_COLUMN_CREATE_DATE = "CreateDate";
    public static final String PROJECT_TABLE_COLUMN_NAME = "Name";
    public static final String PROJECT_TABLE_COLUMN_NUM_PROJECT = "ProjectNumber";
    public static final String PROJECT_TABLE_COLUMN_ADDRESS = "Address";
    public static final String PROJECT_TABLE_COLUMN_NAME_CITY = "CityName";
    public static final String PROJECT_TABLE_COLUMN_NAME_STATE = "StateName";
    public static final String PROJECT_TABLE_COLUMN_STAGES = "ProjectStageArray";
    public static final String PROJECT_TABLE_COLUMN_GEOFENCE = "GeoFenceJson";
    public static final String PROJECT_TABLE_COLUMN_LOGO = "Logo";
    public static final String PROJECT_TABLE_COLUMN_EXPIRY = "Expiry";
    public static final String PROJECT_TABLE_COLUMN_COMPANYNAME = "CompanyName";
    public static final String PROJECT_TABLE_COLUMN_COMPANY_INFO_ID = "CompanyInfoId";
    public static final String PROJECT_TABLE_COLUMN_JOIN_COUNT = "JoinCompaniesCount";
    public static final String PROJECT_TABLE_COLUMN_IS_SELECTED = "IsSelected";

    // table configuration employer
    public static final String TABLE_NAME_EMPLOYER = "Employer";         // Table name
    private static final String EMPLOYER_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String EMPLOYER_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String EMPLOYER_TABLE_COLUMN_ACTIVE = "IsActive";
    public static final String EMPLOYER_TABLE_COLUMN_NAME = "Name";
    public static final String EMPLOYER_TABLE_COLUMN_PHONE = "Phone";
    public static final String EMPLOYER_TABLE_COLUMN_ADDRESS = "Address";
    public static final String EMPLOYER_TABLE_COLUMN_CITY_CODE = "CityCode";
    public static final String EMPLOYER_TABLE_COLUMN_CREATE_DATE = "CreateDate";
    public static final String EMPLOYER_TABLE_COLUMN_FINISH_DATE = "FinishDate";
    public static final String EMPLOYER_TABLE_COLUMN_START_DATE = "StartDate";
    public static final String EMPLOYER_TABLE_COLUMN_WEBSITE = "Website";
    public static final String EMPLOYER_TABLE_COLUMN_EMAIL = "Email";
    public static final String EMPLOYER_TABLE_COLUMN_DOC_TYPE = "DocumentType";
    public static final String EMPLOYER_TABLE_COLUMN_DOC_NUM = "DocumentNumber";
    public static final String EMPLOYER_TABLE_COLUMN_NAME_CITY = "CityName";
    public static final String EMPLOYER_TABLE_COLUMN_NAME_STATE = "StateName";
    public static final String EMPLOYER_TABLE_COLUMN_EXPIRY = "Expiry";
    public static final String EMPLOYER_TABLE_COLUMN_LOGO = "Logo";
    public static final String EMPLOYER_TABLE_COLUMN_ROL = "Rol";
    public static final String EMPLOYER_TABLE_COLUMN_COMPANY = "CompanyId";
    public static final String EMPLOYER_TABLE_COLUMN_COMPANY_ID = "CompanyInfoParentId";
    public static final String EMPLOYER_TABLE_COLUMN_IS_MANAGED = "IsManaged";

    // table configuration contract
    public static final String TABLE_NAME_CONTRACT = "Contract";
    private static final String CONTRACT_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String CONTRACT_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String CONTRACT_TABLE_COLUMN_ACTIVE = "IsActive";
    public static final String CONTRACT_TABLE_COLUMN_FINISH_DATE = "FinishDate";
    public static final String CONTRACT_TABLE_COLUMN_CREATE_DATE = "CreateDate";
    public static final String CONTRACT_TABLE_COLUMN_START_DATE = "StartDate";
    public static final String CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER = "ContractNumber";
    public static final String CONTRACT_TABLE_COLUMN_CONTRACT_NAME = "ContractorName";
    public static final String CONTRACT_TABLE_COLUMN_CONTRACT_REVIEW = "ContractReview";
    public static final String CONTRACT_TABLE_COLUMN_NAME = "CompanyName";
    public static final String CONTRACT_TABLE_COLUMN_LOGO = "FormImageLogo";
    public static final String CONTRACT_TABLE_COLUMN_EXPIRY = "Expiry";
    public static final String CONTRACT_TABLE_COLUMN_ISREGISTER = "IsRegister";
    public static final String CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID = "ContractTypeId";
    public static final String CONTRACT_TABLE_COLUMN_PROJECT_IDS = "ProjectIds";
    public static final String CONTRACT_TABLE_COLUMN_COUNT_PERSONAL = "CountPersonal";
    public static final String CONTRACT_TABLE_COLUMN_COMPANY_ID = "CompanyInfoId";
    public static final String CONTRACT_TABLE_COLUMN_EMPLOYER_ID = "EmployerId";
    public static final String CONTRACT_TABLE_COLUMN_COMPANY_INFO_ID = "CompanyInfoParentId";



    // table ContractPerson
    public static final String TABLE_NAME_CONTRACT_PERSON = "ContractPerson";         // Table name
    private static final String CONTRACT_PERSON_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID = "PersonalCompanyInfoId";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID = "ProjectId";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID = "ContractId";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON = "CarnetCodes";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT = "EnterProject";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_NUMBER = "ContractNumber";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_REVIEW = "ContractReview";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT = "StartDateContract";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT = "FinishDateContract";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_START_DATE = "StartDate";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE = "FinishDate";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_CONTRACTOR_ID = "EmployerId";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_MAXHOUR = "MaxHour";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_MINHOUR = "MinHour";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_AGEMIN = "AgeMin";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_AGEMAX = "AgeMax";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_WEEKDAYS = "WeekDays";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_ID = "PersonalGuidId";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM = "DocumentNumber";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_POSITION = "Position";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_PROJECT_CONTRACTS = "ProjectContracts";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_TYPE = "DescriptionPersonalType";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_REQS = "Requirements";
    public static final String CONTRACT_PERSON_TABLE_COLUMN_BIRTHDAY = "BirthDate";

    // table configuration contract
    public static final String TABLE_NAME_PERSONAL_REPORT = "PersonalReport";
    public static final String PERSONAL_REPORT_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String PERSONAL_REPORT_TABLE_COLUMN_DATA = "Data";
    public static final String PERSONAL_REPORT_TABLE_COLUMN_IMAGES = "Images";
    public static final String PERSONAL_REPORT_TABLE_COLUMN_METHOD = "Method";
    public static final String PERSONAL_REPORT_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String PERSONAL_REPORT_TABLE_COLUMN_URL = "Url";

    // table configuration UpdateImage
    public static final String TABLE_NAME_UPDATE_IMAGE = "UpdateImage";
    public static final String UPDATE_IMAGE_TABLE_COLUMN_ID = "_id";     // a column named "_id" is required for cursor
    public static final String UPDATE_IMAGE_TABLE_COLUMN_DATA = "Data";
    public static final String UPDATE_IMAGE_TABLE_COLUMN_IMAGES = "Images";
    public static final String UPDATE_IMAGE_TABLE_COLUMN_METHOD = "Method";
    public static final String UPDATE_IMAGE_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String UPDATE_IMAGE_TABLE_COLUMN_URL = "Url";

    // table configuration PERSONAL
    public static final String TABLE_NAME_PERSONAL = "Personal";         // Table name
    private static final String PERSONAL_TABLE_COLUMN_ID = "_id";
    public static final String PERSONAL_TABLE_COLUMN_SERVER_ID = "Id";
    public static final String PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID = "PersonalCompanyInfoId";
    public static final String PERSONAL_TABLE_COLUMN_COMPANY_INFO_ID = "CompanyInfoId";
    public static final String PERSONAL_TABLE_COLUMN_ACTIVE = "IsActive";
    public static final String PERSONAL_TABLE_COLUMN_NAME = "Name";
    public static final String PERSONAL_TABLE_COLUMN_LASTNAME = "LastName";
    public static final String PERSONAL_TABLE_COLUMN_DOC_TYPE = "DocumentType";
    public static final String PERSONAL_TABLE_COLUMN_DOC_NUM = "DocumentNumber";
    public static final String PERSONAL_TABLE_COLUMN_NAME_CITY = "CityOfBirthName";
    public static final String PERSONAL_TABLE_COLUMN_NAME_STATE = "StateOfBirthName";
    public static final String PERSONAL_TABLE_COLUMN_EXPIRY = "Expiry";
    public static final String PERSONAL_TABLE_COLUMN_PHOTO = "Photo";
    public static final String PERSONAL_TABLE_COLUMN_PHONE = "PhoneNumber";
    public static final String PERSONAL_TABLE_COLUMN_JOB = "JobName";
    public static final String PERSONAL_TABLE_COLUMN_FINISH_DATE = "FinishDate";
    public static final String PERSONAL_TABLE_COLUMN_CREATE_DATE = "CreateDate";
    public static final String PERSONAL_TABLE_COLUMN_START_DATE = "StartDate";

    // table configuration SECURITY_REFERENCE
    public static final String TABLE_NAME_SECURITY_REFERENCE = "SecurityReferences";         // Table name
    private static final String SECURITY_REFERENCE_COLUMN_ID = "_id";
    public static final String SECURITY_REFERENCE_COLUMN_SERVER_ID = "Id";
    public static final String SECURITY_REFERENCE_COLUMN_CODE = "Code";
    public static final String SECURITY_REFERENCE_COLUMN_TYPE = "Type";
    private static final String SECURITY_REFERENCE_COLUMN_ACTIVE = "IsActive";
    public static final String SECURITY_REFERENCE_COLUMN_NAME = "Name";
    private static final String SECURITY_REFERENCE_COLUMN_LOGO = "Logo";
    public static final String SECURITY_REFERENCE_COLUMN_DESCRIPTION = "Description";
    private static final String SECURITY_REFERENCE_COLUMN_DOC_NUM = "DocNumber";

    // table syncProjects
    public static final String TABLE_NAME_PROJECT_SYNC = "ProjectSync";         // Table name
    public static final String PROJECT_TABLE_COLUMN_ID_Sync = "_id";     // a column named "_id" is required for cursor
    public static final String PROJECT_TABLE_COLUMN_NAME_SYNC = "Name";
    public static final String PROJECT_TABLE_COLUMN_DATE_SYNC = "DateSync";

    // table IndivualContract
    public static final String TABLE_NAME_INDIVIDUAL_CONTRACT = "IndividualContract";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_ID = "Id";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_TYPEID = "IndividualContractTypeId";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_CONTRACTNUMBER = "ContractNumber";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_POSITIONID = "PositionId";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_SALARY = "Salary";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_PAYPERIODID = "PayPeriodId";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_STARTDATE = "StartDate";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_ENDDATE = "EndDate";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_CREATEDATE = "CreateDate";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_UPDATEDATE = "UpdateDate";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_PERSONALEMPLOYERINFOID = "EmployerInfoID";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_CONTRACTID = "ContractID";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_PROJECTID = "ProjectID";
    public static final String INDIVIDUALCONTRACT_TABLE_COLUMN_PERSONALEMPLOYERINFO = "PersonalEmployerInfo";


    // table PersonalEmployerInfos
    public static final String TABLE_NAME_PERSONALEMPLOYERINFOS = "PersonalEmployerInfos";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_ID = "Id";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_PERSONALID = "PersonalId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMPLOYERID = "EmployerId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_CITYCODE = "CityCode";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_TOWNCODE = "TownCode";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_ADDRESS = "Address";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_PHOTO = "Photo";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_PHONENUMBER = "PhoneNumber";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMERGENCYCONTACT = "EmergencyContact";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMERGENCYPHONE = "EmergencyPhone";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_CCFID = "CcfId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_EPSID = "EpsId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_AFPID = "AfpId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_BANKACCOUNT = "BankAccount";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_BANKID = "BankId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_BANKACCOUNTTYPEID = "BankAccountTypeId";
    public static final String PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMAIL = "Email";



    public DBHelper(Context aContext) {
        super(aContext, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public void deleteAllCity() {
        database.delete(TABLE_NAME_CITY, null, null);
    }

    public void deleteAllCompany() {
        database.delete(TABLE_NAME_COMPANY, null, null);
    }

    public void deleteAllDaneCities() {
        database.delete(TABLE_NAME_DANE_CITY, null, null);
    }

    public void deleteAllEconomic() {
        database.delete(TABLE_NAME_ECONOMIC, null, null);
    }

    public void deleteAllProject() {
        database.delete(TABLE_NAME_PROJECT, null, null);
    }

    public void deleteAllEmployer() {
        database.delete(TABLE_NAME_EMPLOYER, null, null);
    }

    public void deleteAllContract() {
        database.delete(TABLE_NAME_CONTRACT, null, null);
    }

    public void deleteAllDashBoard() {
        database.delete(TABLE_NAME_DASHBOARD, null, null);
    }

    public void deleteAllJobs() {
        database.delete(TABLE_NAME_WORK, null, null);
    }

    public void deleteAllIndiviudalContracts() {
        database.delete(TABLE_NAME_INDIVIDUAL_CONTRACT,null,null);
    }

    public void deleteAllContractPerson() {
        database.delete(TABLE_NAME_CONTRACT_PERSON, null, null);
    }

    public void deleteAllPersonal() {
        database.delete(TABLE_NAME_PERSONAL, null, null);
    }

    public void deleteAllReport() {
        database.delete(TABLE_NAME_PERSONAL_REPORT, null, null);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //CREATE TABLE PROJECT SYNC
        String buildSqlProjectSync = "CREATE TABLE " + TABLE_NAME_PROJECT_SYNC + "( "
                + PROJECT_TABLE_COLUMN_ID_Sync + " TEXT PRIMARY KEY, "
                + PROJECT_TABLE_COLUMN_NAME_SYNC + " TEXT, "
                + PROJECT_TABLE_COLUMN_DATE_SYNC + " TEXT)";
        sqLiteDatabase.execSQL(buildSqlProjectSync);

        // Create your tables here
        String buildSQL = "CREATE TABLE " + TABLE_NAME_CITY + "( "
                + CITY_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + CITY_TABLE_COLUMN_SERVER_ID + " INTEGER, "
                + CITY_TABLE_COLUMN_NAME + " TEXT, "
                + CITY_TABLE_COLUMN_CITY_CODE + " TEXT, "
                + CITY_TABLE_COLUMN_CODE + " TEXT, "
                + CITY_TABLE_COLUMN_STATE_CODE + " TEXT, "
                + CITY_TABLE_COLUMN_COUNTRY_CODE + " INTEGER, "
                + CITY_TABLE_COLUMN_STATE + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQL);
        sqLiteDatabase.execSQL(buildSQL);


        // Create your tables here
        String buildSQLIndivualContract = "CREATE TABLE " + TABLE_NAME_INDIVIDUAL_CONTRACT + "( "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_ID + " TEXT PRIMARY KEY, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_TYPEID + " INTEGER, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_CONTRACTNUMBER + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_POSITIONID + " INTEGER, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_SALARY + " INTEGER, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_PAYPERIODID + " INTEGER, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_STARTDATE + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_ENDDATE + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_CREATEDATE + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_UPDATEDATE + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_PERSONALEMPLOYERINFOID + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_CONTRACTID + " TEXT, "
                + INDIVIDUALCONTRACT_TABLE_COLUMN_PROJECTID + " TEXT)";
        sqLiteDatabase.execSQL(buildSQLIndivualContract);

        // Create your tables here
        String buildSQLPersonalEmployerInfos = "CREATE TABLE " + TABLE_NAME_PERSONALEMPLOYERINFOS + "( "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_ID + " TEXT PRIMARY KEY, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_PERSONALID + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMPLOYERID + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_CITYCODE + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_TOWNCODE + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_ADDRESS + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_PHOTO + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_PHONENUMBER + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMERGENCYCONTACT + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMERGENCYPHONE + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_CCFID + " INTEGER, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_EPSID + " INTEGER, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_AFPID + " INTEGER, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_BANKACCOUNT + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_BANKID + " TEXT, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_BANKACCOUNTTYPEID + " INTEGER, "
                + PERSONALEMPLOYERINFOS_TABLE_COLUMN_EMAIL + " TEXT)";
        sqLiteDatabase.execSQL(buildSQLPersonalEmployerInfos);


        // Create your tables here
        String buildSQLCompany = "CREATE TABLE " + TABLE_NAME_COMPANY + "( "
                + COMPANY_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COMPANY_TABLE_COLUMN_SERVER_ID + " TEXT, "
                + COMPANY_TABLE_COLUMN_SERVER_IDINFO + " TEXT, "
                + COMPANY_TABLE_COLUMN_ACTIVE + " INTEGER, "
                + COMPANY_TABLE_COLUMN_START_DATE + " TEXT, "
                + COMPANY_TABLE_COLUMN_FINISH_DATE + " TEXT, "
                + COMPANY_TABLE_COLUMN_NAME + " TEXT, "
                + COMPANY_TABLE_COLUMN_PHONE + " TEXT, "
                + COMPANY_TABLE_COLUMN_DOC_TYPE + " TEXT, "
                + COMPANY_TABLE_COLUMN_DOC_NUM + " TEXT, "
                + COMPANY_TABLE_COLUMN_NAME_CITY + " TEXT, "
                + COMPANY_TABLE_COLUMN_NAME_STATE + " TEXT, "
                + COMPANY_TABLE_COLUMN_LOGO + " TEXT, "
                + COMPANY_TABLE_COLUMN_BRANCH_OFFICES + " TEXT, "
                + COMPANY_TABLE_COLUMN_EXPIRY + " INTEGER, "
                + COMPANY_TABLE_COLUMN_ADDRESS + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLCompany);
        sqLiteDatabase.execSQL(buildSQLCompany);

        // Create your tables here
        String buildSQLDaneCity = "CREATE TABLE " + TABLE_NAME_DANE_CITY + "( "
                + DANE_CITY_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + DANE_CITY_TABLE_COLUMN_NAME + " TEXT, "
                + DANE_CITY_TABLE_COLUMN_STATE + " TEXT, "
                + DANE_CITY_TABLE_COLUMN_STATE_CODE + " TEXT, "
                + DANE_CITY_TABLE_COLUMN_CODE + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLDaneCity);
        sqLiteDatabase.execSQL(buildSQLDaneCity);

        // Create your tables here
        String buildSQLEconomic = "CREATE TABLE " + TABLE_NAME_ECONOMIC + "( "
                + ECONOMIC_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + ECONOMIC_TABLE_COLUMN_CODE + " TEXT, "
                + ECONOMIC_TABLE_COLUMN_NAME + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLEconomic);
        sqLiteDatabase.execSQL(buildSQLEconomic);


        // Create your tables here
        String buildSQLProject = "CREATE TABLE " + TABLE_NAME_PROJECT + "( "
                + PROJECT_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + PROJECT_TABLE_COLUMN_SERVER_ID + " TEXT, "
                + PROJECT_TABLE_COLUMN_ACTIVE + " INTEGER, "
                + PROJECT_TABLE_COLUMN_START_DATE + " TEXT, "
                + PROJECT_TABLE_COLUMN_FINISH_DATE + " TEXT, "
                + PROJECT_TABLE_COLUMN_CREATE_DATE + " TEXT, "
                + PROJECT_TABLE_COLUMN_NAME + " TEXT, "
                + PROJECT_TABLE_COLUMN_NUM_PROJECT + " TEXT, "
                + PROJECT_TABLE_COLUMN_NAME_CITY + " TEXT, "
                + PROJECT_TABLE_COLUMN_NAME_STATE + " TEXT, "
                + PROJECT_TABLE_COLUMN_LOGO + " TEXT, "
                + PROJECT_TABLE_COLUMN_STAGES + " TEXT, "
                + PROJECT_TABLE_COLUMN_GEOFENCE + " TEXT, "
                + PROJECT_TABLE_COLUMN_EXPIRY + " INTEGER, "
                + PROJECT_TABLE_COLUMN_COMPANYNAME + " TEXT, "
                + PROJECT_TABLE_COLUMN_COMPANY_INFO_ID + " TEXT, "
                + PROJECT_TABLE_COLUMN_JOIN_COUNT + " INTEGER, "
                + PROJECT_TABLE_COLUMN_IS_SELECTED + " INTEGER, "
                + PROJECT_TABLE_COLUMN_ADDRESS + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLProject);
        sqLiteDatabase.execSQL(buildSQLProject);

        // Create your tables here
        String buildSQLEmployer = "CREATE TABLE " + TABLE_NAME_EMPLOYER + "( "
                + EMPLOYER_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + EMPLOYER_TABLE_COLUMN_SERVER_ID + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_ACTIVE + " INTEGER, "
                + EMPLOYER_TABLE_COLUMN_CITY_CODE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_CREATE_DATE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_START_DATE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_FINISH_DATE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_WEBSITE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_EMAIL + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_NAME + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_PHONE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_DOC_TYPE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_DOC_NUM + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_NAME_CITY + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_NAME_STATE + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_LOGO + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_ROL + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_EXPIRY + " INTEGER, "
                + EMPLOYER_TABLE_COLUMN_COMPANY_ID + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_COMPANY + " TEXT, "
                + EMPLOYER_TABLE_COLUMN_IS_MANAGED + " INTEGER, "
                + EMPLOYER_TABLE_COLUMN_ADDRESS + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLEmployer);
        sqLiteDatabase.execSQL(buildSQLEmployer);

        // Create your tables here
        String buildSQLContract = "CREATE TABLE " + TABLE_NAME_CONTRACT + "( "
                + CONTRACT_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + CONTRACT_TABLE_COLUMN_SERVER_ID + " TEXT, "
                + CONTRACT_TABLE_COLUMN_ACTIVE + " INTEGER, "
                + CONTRACT_TABLE_COLUMN_FINISH_DATE + " TEXT, "
                + CONTRACT_TABLE_COLUMN_START_DATE + " TEXT, "
                + CONTRACT_TABLE_COLUMN_CREATE_DATE + " TEXT, "
                + CONTRACT_TABLE_COLUMN_NAME + " TEXT, "
                + CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " TEXT, "
                + CONTRACT_TABLE_COLUMN_CONTRACT_NAME + " TEXT, "
                + CONTRACT_TABLE_COLUMN_CONTRACT_REVIEW + " TEXT, "
                + CONTRACT_TABLE_COLUMN_ISREGISTER + " INTEGER, "
                + CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID + " INTEGER, "
                + CONTRACT_TABLE_COLUMN_PROJECT_IDS + " TEXT, "
                + CONTRACT_TABLE_COLUMN_COUNT_PERSONAL + " INTEGER, "
                + CONTRACT_TABLE_COLUMN_COMPANY_ID + " TEXT, "
                + CONTRACT_TABLE_COLUMN_EMPLOYER_ID + " TEXT, "
                + CONTRACT_TABLE_COLUMN_EXPIRY + " INTEGER, "
                + CONTRACT_TABLE_COLUMN_COMPANY_INFO_ID + " TEXT, "
                + CONTRACT_TABLE_COLUMN_LOGO + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLContract);
        sqLiteDatabase.execSQL(buildSQLContract);

        // Create your tables here
        String buildSQLContractPerson = "CREATE TABLE " + TABLE_NAME_CONTRACT_PERSON + "( "
                + CONTRACT_PERSON_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID + " INTEGER, "
                + CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT + " INTEGER, "
                + CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_NUMBER + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_REVIEW + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_START_DATE + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_CONTRACTOR_ID + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_MAXHOUR + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_MINHOUR + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_AGEMAX + " INTEGER, "
                + CONTRACT_PERSON_TABLE_COLUMN_AGEMIN + " INTEGER, "
                + CONTRACT_PERSON_TABLE_COLUMN_WEEKDAYS + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_ID + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_POSITION + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_PROJECT_CONTRACTS + " TEXT, "
                + CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_TYPE + " TEXT, "
                + CONTRACT_TABLE_COLUMN_ISREGISTER + " INTEGER, "
                + CONTRACT_PERSON_TABLE_COLUMN_REQS + " TEXT,"
                + CONTRACT_PERSON_TABLE_COLUMN_BIRTHDAY + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLContractPerson);
        sqLiteDatabase.execSQL(buildSQLContractPerson);

        // Create your tables here
        String buildSQLPersonalReport = "CREATE TABLE " + TABLE_NAME_PERSONAL_REPORT + "( "
                + PERSONAL_REPORT_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + PERSONAL_REPORT_TABLE_COLUMN_SERVER_ID + " INTEGER, "
                + PERSONAL_REPORT_TABLE_COLUMN_IMAGES + " TEXT, "
                + PERSONAL_REPORT_TABLE_COLUMN_METHOD + " INTEGER, "
                + PERSONAL_REPORT_TABLE_COLUMN_URL + " TEXT, "
                + PERSONAL_REPORT_TABLE_COLUMN_DATA + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLPersonalReport);
        sqLiteDatabase.execSQL(buildSQLPersonalReport);

        String buildSQLUpdateImage = "CREATE TABLE " + TABLE_NAME_UPDATE_IMAGE + "( "
                + UPDATE_IMAGE_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + UPDATE_IMAGE_TABLE_COLUMN_SERVER_ID + " TEXT, "
                + UPDATE_IMAGE_TABLE_COLUMN_IMAGES + " TEXT, "
                + UPDATE_IMAGE_TABLE_COLUMN_METHOD + " INTEGER, "
                + UPDATE_IMAGE_TABLE_COLUMN_URL + " TEXT, "
                + UPDATE_IMAGE_TABLE_COLUMN_DATA + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLPersonalReport);
        sqLiteDatabase.execSQL(buildSQLUpdateImage);

        // Create your tables here
        String buildSQLPersonal = "CREATE TABLE " + TABLE_NAME_PERSONAL + "( "
                + PERSONAL_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + PERSONAL_TABLE_COLUMN_SERVER_ID + " INTEGER, "
                + PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID + " INTEGER, "
                + PERSONAL_TABLE_COLUMN_COMPANY_INFO_ID + " TEXT, "
                + PERSONAL_TABLE_COLUMN_ACTIVE + " INTEGER, "
                + PERSONAL_TABLE_COLUMN_NAME + " TEXT, "
                + PERSONAL_TABLE_COLUMN_LASTNAME + " TEXT, "
                + PERSONAL_TABLE_COLUMN_DOC_TYPE + " TEXT, "
                + PERSONAL_TABLE_COLUMN_DOC_NUM + " TEXT, "
                + PERSONAL_TABLE_COLUMN_NAME_CITY + " TEXT, "
                + PERSONAL_TABLE_COLUMN_NAME_STATE + " TEXT, "
                + PERSONAL_TABLE_COLUMN_EXPIRY + " INTEGER, "
                + PERSONAL_TABLE_COLUMN_FINISH_DATE + " TEXT, "
                + PERSONAL_TABLE_COLUMN_START_DATE + " TEXT, "
                + PERSONAL_TABLE_COLUMN_CREATE_DATE + " TEXT, "
                + PERSONAL_TABLE_COLUMN_PHONE + " TEXT, "
                + PERSONAL_TABLE_COLUMN_JOB + " TEXT, "
                + PERSONAL_TABLE_COLUMN_PHOTO + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLPersonal);
        sqLiteDatabase.execSQL(buildSQLPersonal);

        // Create your tables here
        String buildSQLDashBoard = "CREATE TABLE " + TABLE_NAME_DASHBOARD + "( "
                + DASHBOARD_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + DASHBOARD_TABLE_COLUMN_CONTENT + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLDashBoard);
        sqLiteDatabase.execSQL(buildSQLDashBoard);

        // Create your tables here
        String buildSQLWork = "CREATE TABLE " + TABLE_NAME_WORK + "( "
                + WORK_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + WORK_TABLE_COLUMN_CODE + " TEXT, "
                + WORK_TABLE_COLUMN_SERVER_ID + " INTEGER, "
                + WORK_TABLE_COLUMN_NAME_ASCII + " TEXT, "
                + WORK_TABLE_COLUMN_NAME + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLWork);
        sqLiteDatabase.execSQL(buildSQLWork);

        String buildSQLCOptions = "CREATE TABLE " + TABLE_NAME_COPTIONS + "( "
                + COPTIONS_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COPTIONS_COLUMN_SERVER_ID + " INTEGER, "
                + COPTIONS_COLUMN_IS_ACTIVE + " INTEGER, "
                + COPTIONS_COLUMN_ORDER_NUM + " INTEGER, "
                + COPTIONS_COLUMN_TYPE + " TEXT, "
                + COPTIONS_COLUMN_DESC + " TEXT, "
                + COPTIONS_COLUMN_VALUE + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLCOptions);
        sqLiteDatabase.execSQL(buildSQLCOptions);

        // Create your tables here
        String buildSQLSecRef = "CREATE TABLE " + TABLE_NAME_SECURITY_REFERENCE + "( "
                + SECURITY_REFERENCE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                + SECURITY_REFERENCE_COLUMN_SERVER_ID + " INTEGER, "
                + SECURITY_REFERENCE_COLUMN_CODE + " TEXT, "
                + SECURITY_REFERENCE_COLUMN_TYPE + " INTEGER, "
                + SECURITY_REFERENCE_COLUMN_ACTIVE + " INTEGER, "
                + SECURITY_REFERENCE_COLUMN_NAME + " TEXT, "
                + SECURITY_REFERENCE_COLUMN_LOGO + " TEXT, "
                + SECURITY_REFERENCE_COLUMN_DESCRIPTION + " TEXT, "
                + SECURITY_REFERENCE_COLUMN_DOC_NUM + " TEXT)";
        Log.d(TAG, "onCreate SQL: " + buildSQLSecRef);
        sqLiteDatabase.execSQL(buildSQLSecRef);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Database schema upgrade code goes here
        String buildSQL = "DROP TABLE IF EXISTS " + TABLE_NAME_CITY;
        Log.d(TAG, "onUpgrade SQL: " + buildSQL);
        sqLiteDatabase.execSQL(buildSQL);       // drop previous table

        String buildSQLCompany = "DROP TABLE IF EXISTS " + TABLE_NAME_COMPANY;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLCompany);
        sqLiteDatabase.execSQL(buildSQLCompany);

        String buildSQLDaneCity = "DROP TABLE IF EXISTS " + TABLE_NAME_DANE_CITY;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLDaneCity);
        sqLiteDatabase.execSQL(buildSQLDaneCity);

        String buildSQLEconomic = "DROP TABLE IF EXISTS " + TABLE_NAME_ECONOMIC;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLEconomic);
        sqLiteDatabase.execSQL(buildSQLEconomic);

        String buildSQLProject = "DROP TABLE IF EXISTS " + TABLE_NAME_PROJECT;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLProject);
        sqLiteDatabase.execSQL(buildSQLProject);

        String buildSQLEmployer = "DROP TABLE IF EXISTS " + TABLE_NAME_EMPLOYER;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLEmployer);
        sqLiteDatabase.execSQL(buildSQLEmployer);

        String buildSQLContract = "DROP TABLE IF EXISTS " + TABLE_NAME_CONTRACT;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLContract);
        sqLiteDatabase.execSQL(buildSQLContract);

        String buildSQLContractPerson = "DROP TABLE IF EXISTS " + TABLE_NAME_CONTRACT_PERSON;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLContractPerson);
        sqLiteDatabase.execSQL(buildSQLContractPerson);

        String buildSQLPersonReport = "DROP TABLE IF EXISTS " + TABLE_NAME_PERSONAL_REPORT;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLPersonReport);
        sqLiteDatabase.execSQL(buildSQLPersonReport);

        String buildSQLUpdateImage = "DROP TABLE IF EXISTS " + TABLE_NAME_UPDATE_IMAGE;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLUpdateImage);
        sqLiteDatabase.execSQL(buildSQLUpdateImage);

        String buildSQLPersonal = "DROP TABLE IF EXISTS " + TABLE_NAME_PERSONAL;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLPersonal);
        sqLiteDatabase.execSQL(buildSQLPersonal);

        String buildSQLDashBoard = "DROP TABLE IF EXISTS " + TABLE_NAME_DASHBOARD;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLDashBoard);
        sqLiteDatabase.execSQL(buildSQLDashBoard);

        String buildSQLWORK = "DROP TABLE IF EXISTS " + TABLE_NAME_WORK;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLWORK);
        sqLiteDatabase.execSQL(buildSQLWORK);

        String buildSQLCOptions = "DROP TABLE IF EXISTS " + TABLE_NAME_COPTIONS;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLCOptions);
        sqLiteDatabase.execSQL(buildSQLCOptions);

        String buildSQLSecRef = "DROP TABLE IF EXISTS " + TABLE_NAME_SECURITY_REFERENCE;
        Log.d(TAG, "onUpgrade SQL: " + buildSQLSecRef);
        sqLiteDatabase.execSQL(buildSQLSecRef);

        String buildSQLProjecSync = "DROP TABLE IF EXISTS " + TABLE_NAME_PROJECT_SYNC;
        sqLiteDatabase.execSQL(buildSQLProjecSync);

        String buildSQLIndivudualContract = "DROP TABLE IF EXISTS " + TABLE_NAME_INDIVIDUAL_CONTRACT;
        sqLiteDatabase.execSQL(buildSQLIndivudualContract);

        String buildSQLPersonalEmployer = "DROP TABLE IF EXISTS " + TABLE_NAME_PERSONALEMPLOYERINFOS;
        sqLiteDatabase.execSQL(buildSQLPersonalEmployer);

        onCreate(sqLiteDatabase);               // create the table from the beginning
    }
}