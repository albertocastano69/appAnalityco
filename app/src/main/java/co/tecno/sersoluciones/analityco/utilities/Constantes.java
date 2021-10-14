package co.tecno.sersoluciones.analityco.utilities;

import android.annotation.SuppressLint;
import android.net.Uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.Personal;

/**
 * Created by Ser SOluciones SAS on 10/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class Constantes {

    public final static String DEFAULT_ROOT_VALUE_SEPARATOR = " ";
    public static final byte ZERO = 48;
    /**
     * US-ASCII SP, space (32)
     */
    public static final char SP = ' ';

    public static final String API_TOKEN_AUTH_SERVER = "connect/token";
    public static final String KEY_IP_SERVER = "pref_key_ip_server";

    public static final String KEY_COMPANY_DAYS = "pref_key_company_days";
    public static final String KEY_EMPLOYER_DAYS = "pref_key_employer_days";
    public static final String KEY_PROJECT_DAYS = "pref_key_projects_days";
    public static final String KEY_CONTRACT_DAYS = "pref_key_contracts_days";
    public static final String KEY_PERSONAL_DAYS = "pref_key_personal_days";
    public static final String KEY_VIBRATION_ENABLED = "pref_key_vibration";
    public static final String KEY_SPEAK_ENABLED = "pref_key_speak";
    public static final String KEY_DEFAULT_ACTIVITY_START = "pref_key_default_start";
    public static final String KEY_DEFAULT_REQUERIMENT_INCOMPLETE = "pref_key_default_requeriment_incomplete";
    public static final String KEY_DEFAULT_ENTRY = "pref_key_default_entry";
    public static final String KEY_CHECK_REQ = "pref_key_check_req";
    public static final String KEY_INSPECT_REQ = "pref_key_inspect_req";
    public static final String KEY_REGISTER_IN = "pref_key_register_in";
    public static final String KEY_REGISTER_OUT = "pref_key_register_out";
    public static final String KEY_ENROLLMENT_ENBABLED_BUTTONS = "pref_key_enrollment_enabled_buttons";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_SELECTED_PROJECT_ID = "selected_project_id";
    public static final String NOTIFICATION = "notification";
    public static final String ITEM_OBJ = "model";
    public static final String ITEM_PHOTO = "photo";
    public static final String ITEM_ID = "PersonalId";
    public static final String ITEM_PERSONAL_INFO = "personalInfo";
    public static final String ITEM_EMPLOYER_ID = "employerId";
    public static final String ITEM_CONTRACT_ID = "contractId";
    public static final String ITEM_TRANSITION_NAME = "transition_name";
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_TIME_EXPIRES_IN = "time_milli_expires_in";
    public static final String KEY_PASS = "password";
    public static final String KEY_SCOPE = "scope";
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_CLIENT_ID_VALUE = "megadatosapi";
    public static final String KEY_CLIENT_SECRET = "client_secret";
    public static final String KEY_CLIENT_SECRET_VALUE = "9BBBEDA7-426E-4350-A3CC-6B8A2680BD57";
    public static final String KEY_GRAN_TYPE = "grant_type";

    static final String KEY_JWT = "jwt";
    static final String KEY_SYNC_DATE = "sync_date";
    public static final int LOGIN_SUCCESS =     0;
    public static final int LOGIN_ERROR = 1;
    static final String IS_USER_LOGIN = "is_user_login";
    static final String KEY_POLICY_DATA = "policy_data";
    static final String KEY_EDIT_REQ_ENABLED = "edit_req_enabled";

    public static final int SUCCESS_REQUEST = 1;
    public static final int SUCCESS_FILE_UPLOAD = 2;
    public static final int SEND_REQUEST = 3;
    public static final int REQUEST_NOT_FOUND = 4;
    public static final int UNAUTHORIZED = 5;
    public static final int FORBIDDEN = 6;
    public static final int NOT_INTERNET = 8;
    public static final int BAD_REQUEST = 9;
    public static final int NOT_CONNECTION = 20;
    public static final int TIME_OUT_REQUEST = 10;
    public static final int UPDATE_CITIES = 11;
    public static final int UPDATE_ECONOMIC_ACTIVITIES = 12;
    public static final int UPDATE_ADMIN_USERS = 13;
    public static final int UPDATE_JOBS = 14;
    public static final int SERVER_ERROR = 15;
    public static final int PRECONDITION_FAILED = 16;

    public static final int CREATE = 11;
    public static final int UPDATE = 12;
    public static final int DETAILS = 13;

    public static final int ContractIdDowload = 1;
    public static final int PersonalIdDowlod = 2;
    public static final int EmployerIdDowlod = 3;
    public static final int SyncSuccessful = 6;
    public static final int DownloadFailed = 6;



    public static final String KEY_TO_DATE = "toDate";
    public static final String KEY_FROM_DATE = "fromDate";

    public static final String KEY_PROFILE = "profile";
    public static final String KEY_JSON_PROFILES = "json_profile";
    public static final String KEY_IMAGE_AVATAR_PATH = "image_path";
    public static final String KEY_ORDER_BY = "order_by";
    public static final String KEY_SELECTION_ARGS = "selectionArgs";
    public static final String KEY_SELECTION = "selection";
    public static final String KEY_SELECT = "select";
    public static final String KEY_ADMIN = "admin";
    public static final String DEVICE_ID = "device_id";

    public static final String BROADCAST_GET_JSON = "BROADCAST_GET_JSON";
    public static final String OPTION_JSON_BROADCAST = "OPTION_JSON_BROADCAST";
    public static final String VALUE_JSON_BROADCAST = "json";
    public static final String URL_REQUEST_BROADCAST = "url_request";

    //urls
    public static final String USER_INFO_URL = "api/userinfo/";
    public static final String PERSON_URL = "api/Person/";
    public static final String POSITION_URL = "api/Position/";
    public static final String EMPLOYER_FOR_INDIVIDUAL_CONTRACT_URL = "api/Company/ManagedEmployers/";
    public static final String INDIVIDUAL_REQUERIMENT_CONTRACT_URL = "api/Document/IndividualContract/";
    public static final String INDIVIDUAL_DOCUMENT_CONTRACT_URL = "api/Document/";
    public static final String PERSONAL_EMPLOYER_INFO_URL = "api/PersonalEmployerInfo/";
    public static final String PERSONAL_EMPLOYER_INFO_PARAMS_URL = "api/PersonalEmployerInfo/EmployerPersonal";
    public static final String PERSONAL_BLACK_LIST_INFO_URL = "api/PersonalBlackList/ByDoc";
    public static final String PERSONAL_IN_CONTRACT_INFO_URL = "api/IndividualContract/GetByPersonalEnployerInfo/";
    public static final String CONTRACT_SELECT_CONTRACTING_URL = "api/Contract/SelectContracting/";
    public static final String REQUEST_HIRING_DOCUMENT_URL = "api/Document/IndividualContract/RequestHiringDocument/";
    public static final String REQUEST_APROVED_HIRING_DOCUMENT_URL = "api/Document/IndividualContract/RequestAprovedHiringDocument/";
    public static final String REQUEST_REJECT_HIRING_DOCUMENT_URL = "api/Document/IndividualContract/RejectHiringRequestDocument/";
    public static final String REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL = "api/IndividualContract/AddStage/";
    public static final String PERSONAL_EMPLOYER_INFO_EMPLOYER_PERSONAL_URL = "api/PersonalEmployerInfo/EmployerPersonal/";
    public static final String USER_CHANGE_PASSWORD_URL = "api/User/ChangePassword/";
    public static final String LIST_USERS_URL = "api/User/";
    public static final String USER_BY_EMAIL_URL = "api/User/ByEmail/";
    public static final String CONTRACT_BY_ARRAYIDS = "api/Contract/ByIds/";
    public static final String PERSONAL_BY_ARRAYIDS = "api/Personal/ByIds/";
    public static final String EMPLOYER_BY_ARRAYIDS = "api/Employer/ByIds";
    public static final String LIST_CITIES_URL = "api/City/";
    public static final String LIST_COMPANIES_URL = "api/Company/";
    public static final String LIST_COMPANIES_BY_DOCNUM = "api/Employer/";
    public static final String LIST_ROLES_URL = "api/Roles/Company/";
    public static final String LIST_EMPLOYERS_URL = "api/Employer/";
    public static final String LIST_PERSONAL_URL = "api/Personal/";
    public static final String IMAGES_CONTRACT_URL = "api/FormImage/";
    public static final String LIST_PROJECTS_URL = "api/Project/";
    public static final String PERSONAL_REPORT_URL = "api/PersonalReport/";
    public static final String LIST_AUTOPOSITION_URL = "api/Contract/Positions/";
    public static final String LIST_PERSONALCONTRACT = "api/Personal/ContractsOffline/";
    public static final String REPORTPERSONAL_URL = "api/PersonalReport/";
    public static final String LIST_CONTRACTS_URL = "api/Contract/";
    public static final String LIST_CONTRACTS_EXIST = "api/Contract/Exist/";
    public static final String LIST_CONTRACT_PER_OFFLINE_URL = "api/Personal/ContractsOffline/";
    public static final String LIST_CONTRACT_PER_OFFLINE_URL_PCII = "api/Personal/ContractsOffline?full=true&personalCompanyInfoId=";
    public static final String LIST_INDIVIDUALCONTRACTS_URL = "api/IndividualContract/";
    public static final String NEW_STAGE_URL = "api/ProjectStage/";
    public static final String STAGEBYPROJECT_URL = "api/ProjectStage/Project/";
    public static final String DASHBOARD_URL = "api/DashBoard/Count/";
    public static final String GETID_URL = "api/Personal/GetId/";
    public static final String PROJECT_PERMISSION = "api/Project/Permissions/";
    public static final String CONTRACT_PERMISSION = "api/Contract/Permissions/";
    public static final String PERSONAL_PERMISSION = "api/Personal/Permissions/";
    public static final String EMPLOYER_PERMISSION = "api/Employer/Permissions/";
    public static final String PERSONALCOMPANY_URL = "api/Personal/GetPersonalInfoId/";
    public static final String CONTRACT_FILE_URL = "api/Contract/UpdateFile/";
    public static final String NEW_PERSONAL_URL = "api/Personal/JoinContract/";
    public static final String NEW_PERSONAL_COMPANY_URL = "api/Personal/InfoCompany";
    public static final String LIST_PROJECTS_BY_COMPANY_URL = "api/Project/";
    public static final String DETAILS_PERSONAL_URL = "api/Personal/GetInfoPersonalCompany/";
    public static final String LIST_JOB_URL = "api/Job/";
    public static final String LIST_USERS_BY_COMPANY_URL = "api/Company/AdminUsers/";
    public static final String CREATE_COMPANY_URL = "api/Company/";
    public static final String CREATE_EMPLOYER_URL = "api/Employer/";
    public static final String UPDATE_PERSONAL_INFO_URL = "api/Personal/UpdatePersonalCompany/";
    public static final String CREATE_PROJECT_URL = "api/Project/";
    public static final String LIST_COMMON_OPTIONS_URL = "api/CommonOptions/";
    public static final String CREATE_COMMONOPTIONS = "api/CommonOptions/";
    public static final String CREATE_CONTRACTTYPE = "api/Contract/GetContractType/";
    public static final String CREATE_USER_URL = "api/User/";
    public static final String UPDATE_PHOTO_PROFILE_URL = "api/User/UpdatePhotoProfile/";
    public static final String UPDATE_PHOTO_NEW_PERSONAL_URL = "api/Personal/CompanyInfo/UpdatePhoto/";
    public static final String UPDATE_PHOTO_PERSONAL_EMPLOYER_INFO_URL = "api/PersonalEmployerInfo/UpdatePhoto/";
    public static final String UPDATE_LOGO_PROJECT_URL = "api/Project/UpdateLogo/";
    public static final String UPDATE_LOGO_COMPANY_URL = "api/Company/UpdateLogo/";
    public static final String UPDATE_LOGO_EMPLOYER_URL = "api/Employer/UpdateLogo/";
    public static final String RUES_URL = "http://www.rues.org.co/RM/ConsultaNIT_json";
    public static final String LIST_DANE_CITIES_URL = "api/DaneCity/";
    public static final String LIST_ECONOMIC_URL = "api/EconomicActivity/";
    public static final String LIST_DASHBOARD_NOTIFICATION_URL = "api/DashBoard/Notification/";
    public static final String LIST_SEC_REF_URL = "api/SecurityReferences/";
    public static final String CONTRAC_REQUEST_NUM = "api/Contract/CountContract/";
    public static final String URL_IMAGES = "https://s3.amazonaws.com/analityco-acceso/";
    public static final String URL_POLICY = "api/References/Policy/";
    public static final String URL_VALIDATED_PERSONAL_CODE = "api/Personal/GetPersonalInfo/";
    public static final String URL_EDIT_SURVEY ="SymptomReport";

    //Uris
    public static final String AUTHORITY = "co.tecno.sersoluciones.DBContentProvider";
    public static final Uri CONTENT_CITY_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_CITY);
    public static final Uri CONTENT_CITY_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_CITY + "/bulk-insert/");
    public static final Uri CONTENT_COMPANY_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_COMPANY);
    public static final Uri CONTENT_COMPANY_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_COMPANY + "/bulk-insert/");
    public static final Uri CONTENT_DANE_CITY_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_DANE_CITY);
    public static final Uri CONTENT_DANE_CITY_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_DANE_CITY + "/bulk-insert/");
    public static final Uri CONTENT_ECONOMIC_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_ECONOMIC);
    public static final Uri CONTENT_ECONOMIC_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_ECONOMIC + "/bulk-insert/");
    public static final Uri CONTENT_PROJECT_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_PROJECT);
    public static final Uri CONTENT_PROJECT_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_PROJECT + "/bulk-insert/");
    public static final Uri CONTENT_EMPLOYER_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_EMPLOYER);
    public static final Uri CONTENT_EMPLOYER_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_EMPLOYER + "/bulk-insert/");
    public static final Uri CONTENT_CONTRACT_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_CONTRACT);
    public static final Uri CONTENT_CONTRACT_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_CONTRACT + "/bulk-insert/");
    public static final Uri CONTENT_CONTRACT_PER_OFFLINE_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_CONTRACT_PERSON);
    public static final Uri CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_CONTRACT_PERSON + "/bulk-insert/");
    public static final Uri CONTENT_INDIVIDUAL_CONTRACT_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_INDIVIDUAL_CONTRACT);
    public static final Uri CONTENT_INDIVIDUAL_CONTRACT_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_INDIVIDUAL_CONTRACT + "/bulk-insert/");
    public static final Uri CONTENT_PERSON_REPORT_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL_REPORT);
    public static final Uri CONTENT_CONTRACT_PERSON_REPORT_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL_REPORT + "/bulk-insert/");
    public static final Uri CONTENT_UPDATE_IMAGE_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_UPDATE_IMAGE);
    public static final Uri CONTENT_UPDATE_IMAGE_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_UPDATE_IMAGE + "/bulk-insert/");
    public static final Uri CONTENT_PERSONAL_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL);
    public static final Uri CONTENT_PERSONAL_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL + "/bulk-insert/");
    public static final Uri CONTENT_DASHBOARD_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_DASHBOARD + "/bulk-insert/");
    public static final Uri CONTENT_WORK_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_WORK);
    public static final Uri CONTENT_WORK_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_WORK + "/bulk-insert/");
    public static final Uri CONTENT_COMMON_OPTIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_COPTIONS);
    public static final Uri CONTENT_COMMON_OPTIONS_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_COPTIONS + "/bulk-insert/");
    public static final Uri CONTENT_SEC_REFS_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_SECURITY_REFERENCE);
    public static final Uri CONTENT_SEC_REFS_URI_BULK_INSERT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_NAME_SECURITY_REFERENCE + "/bulk-insert/");

    /**
     * Constantes utilizados en el envio de reportes
     */

    public static final String IMEI_KEY = "IMEI";
    public static final String BATERIA_KEY = "Battery";
    public static final String SELLER_ID = "UserName";
    public static final String LATITUD_KEY = "Lat";
    public static final String LONGITUD_KEY = "Lon";
    public static final String ALTIUD_KEY = "Altitude";
    public static final String VELOCIDAD_KEY = "Velocity";
    public static final String TIME_KEY = "TimeMillis";
    public static final String PROVIDER_KEY = "Provider";
    public static final String PRECISION_KEY = "Accuracy";
    public static final String IS_GPS_KEY = "InfoGPS";
    public static final String BEARING_KEY = "Bearing";

    public static final String COMPANY_ORDER_BY = "company_order_by";
    public static final String EMPLOYER_ORDER_BY = "employer_order_by";
    public static final String PROJECT_ORDER_BY = "project_order_by";
    public static final String CONTRACT_ORDER_BY = "contract_order_by";
    public static final String PERSONAL_ORDER_BY = "personal_order_by";

    public static final Map<String, Integer> DICT_ASC_ORDER_BY;

    static {
        @SuppressLint("UseSparseArrays")
        Map<String, Integer> tmp = new HashMap<>();
        tmp.put("ASC", 1);
        tmp.put("DESC", 2);
        DICT_ASC_ORDER_BY = Collections.unmodifiableMap(tmp);
    }

}
