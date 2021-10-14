package co.tecno.sersoluciones.analityco.tasks;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;

import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.PersonalContract;
import co.tecno.sersoluciones.analityco.models.PersonalContractOfflineNetwork;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 27/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class AsyncUpdateDBTask extends AsyncTask<String, Void, Boolean> {

    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private String url;
    private String data;
    private MyPreferences preferences;
    private String RequestByIds;


    public AsyncUpdateDBTask(Context context) {
        mContext = context;
        preferences = new MyPreferences(context);
    }
    @Override
    protected Boolean doInBackground(String... strings) {
        url = strings[0];
        data = strings[1];
        RequestByIds= strings[2];

        if(RequestByIds!=null){
            return updateDataBase(url, data,RequestByIds);
        }
        return updateDataBase(url, data,null);
    }

    private boolean updateDataBase(String url, String data,String requestByIds) {
        boolean flag = true;
        long count = 0;
        try {
            switch (url) {

                case Constantes.LIST_CONTRACT_PER_OFFLINE_URL:
                    String selection = null;
                    String[] selectionArgs = null;
                    if (preferences.getSelectedProjectId() != null && !preferences.getSelectedProjectId().isEmpty()) {
                        selection = "(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ? )";
                        selectionArgs = new String[]{preferences.getSelectedProjectId()};
                    }
                    mContext.getContentResolver().delete(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, selection, selectionArgs);
                    JSONArray jsonArray = new JSONArray(data);
                    ContentValues[] contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"EmployerDocumentType", "EmployerLogo", "Id", "EmployerDocumentNumber",
                            "TypeARL", "EmployerName", "Sex", "PersonalTypeId", "FormImageLogo", "ProjectStages", "Height", "CarnetId", "PersonalContractId", "Reqs",
                            "IsActive", "Expiry", "CountProjects", "ContractIsActive", "ContractExpiry", "AllowUploadReqs", "EmployerCompanyId", "EpsId", "ArlId", "AfpId"
                    });
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT: " + count);
                    preferences.setSynContractOffline(count==jsonArray.length());
                    break;
                case  Constantes.CONTRACT_BY_ARRAYIDS:
                    String [] Contractids = null;
                    if(requestByIds!=null){
                        Contractids = requestByIds.split(",");
                        for(String elemento: Contractids){
                            selection="(" + DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID + " IN(?) )";
                            mContext.getContentResolver().delete(Constantes.CONTENT_CONTRACT_URI,selection, new String[]{elemento});
                        }
                    }
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"ExpiryTemp","CreatedBy","FormImageId","PersonalTypeId","ContractFile","MinHour",
                            "MaxHour","WeekDays","AgeMin","AgeMax","DocsJSON","CompanyDocumentNumber","CompanyLogo","ProjectStageContracts","CountProjects","CountSubContractor","DescriptionContractType","TypeContractType","ValueContractType","ValuePersonalType","DescriptionPersonalType","TypePersonalType",
                            "CompanyArlId","ContractorArlId","ContractorDocumentNumber","ContractorRol","CompanyRol","ContractorDocumentType","CompanyDocumentType","ContractorPhone","ContractorLogo","FormImageLogo","Tags",
                    });
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_CONTRACT_URI_BULK_INSERT, contentValues);
                    //
                    preferences.setSynContract(Contractids.length == count);
                    logW("Count CONTENT_CONTRACT_BY_ARRAYS_URI_BULK_INSERT: " + count);
                    break;
                case  Constantes.PERSONAL_BY_ARRAYIDS:
                    if(requestByIds!=null){
                        selectionArgs=requestByIds.split(",");
                        for (String elemento: selectionArgs){
                            selection="(" + DBHelper.PERSONAL_TABLE_COLUMN_SERVER_ID + " IN(?) )";
                            mContext.getContentResolver().delete(Constantes.CONTENT_PERSONAL_URI,selection, new String[]{elemento});
                        }
                        jsonArray = new JSONArray(data);
                        contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"ExpiryTemp", "BirthDate", "RH", "Address", "CityOfBirthCode",
                                "EmergencyContact", "CountEmployers", "Sex", "EmergencyContactPhone", "CountContracts", "DescEPS", "NameEPS", "DescAFP",
                                "NameAFP", "EpsId", "AfpId", "EpsCode", "EpsType", "AfpCode", "AfpType", "PersonalGuidId",
                                "CityCode", "StateName", "CityName", "JobCode", "CountProjects", "Nationality", "CanEdit","Email","RequestDate","RequestStage",
                                "TownId","MaritalStatus","NumberOfDependants","RelationshipWithContact","NumberOfChilds","SocialStratumId","SocialStratum",
                                "MaritalStatusId","Medicines","Town"});
                        count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_PERSONAL_URI_BULK_INSERT, contentValues);
                        preferences.setSynPersonal(count == selectionArgs.length);
                        logW("Count CONTENT_PERSONAL_BY_ARRAYS_URI_BULK_INSERT: " + count);
                    }
                    break;
                case  Constantes.EMPLOYER_BY_ARRAYIDS:
                    if(requestByIds!=null){
                        selectionArgs=requestByIds.split(",");
                        for (String elemento: selectionArgs){
                            System.out.println(elemento);
                            selection="(" + DBHelper.EMPLOYER_TABLE_COLUMN_SERVER_ID + " IN(?) )";
                            mContext.getContentResolver().delete(Constantes.CONTENT_EMPLOYER_URI,selection, new String[]{elemento});
                        }
                        jsonArray = new JSONArray(data);
                        contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"CompanyInfoId", "ArlId", "ExpiryTemp",
                                "SucursalName", "ServicesProvided", "DescARL", "NameARL", "PersonalIds", "ContractIds", "ProjectIds","EconomicActivities",
                                "BranchOffices","LegalRepresentativeEmail","LegalRepresentativePhone","LegalRepresentativeName"});
                        count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_EMPLOYER_URI_BULK_INSERT, contentValues);
                        preferences.setSynEmployer(count == selectionArgs.length);
                        logW("Count CONTENT_EMPLOYER_BY_ARRAYS_URI_BULK_INSERT: " + count);
                    }
                    break;
                case Constantes.LIST_PERSONAL_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_PERSONAL_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"ExpiryTemp", "BirthDate", "RH", "Address", "CityOfBirthCode",
                            "EmergencyContact", "CountEmployers", "Sex", "EmergencyContactPhone", "CountContracts", "DescEPS", "NameEPS", "DescAFP",
                            "NameAFP", "EpsId", "AfpId", "EpsCode", "EpsType", "AfpCode", "AfpType", "PersonalGuidId",
                            "CityCode", "StateName", "CityName", "JobCode", "CountProjects", "Nationality", "CanEdit","Email","RequestDate",
                            "RequestStage","TownId","RelationshipWithContact", "NumberOfDependants", "SocialStratum", "MaritalStatusId","NumberOfChilds", "MaritalStatus", "Town",
                            "Medicines", "SocialStratumId"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_PERSONAL_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_PERSONAL_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_INDIVIDUALCONTRACTS_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_INDIVIDUAL_CONTRACT_URI,null,null );
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray,new String[]{"personalemployerinfo"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_INDIVIDUAL_CONTRACT_URI_BULK_INSERT , contentValues);
                    logW("Count CONTENT_INDIVUDUAL_CONTRACT_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_PROJECTS_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_PROJECT_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"ExpiryTemp", "JoinCompaniesCount"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_PROJECT_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_PROJECT_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_COMMON_OPTIONS_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_COMMON_OPTIONS_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"File"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_COMMON_OPTIONS_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_COMMON_OPTIONS_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_CONTRACTS_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_CONTRACT_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"ExpiryTemp"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_CONTRACT_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_CONTRACT_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_CITIES_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_CITY_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray);
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_CITY_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_CITY_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_DANE_CITIES_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_DANE_CITY_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"Code", "Id"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_DANE_CITY_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_DANE_CITY_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_ECONOMIC_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_ECONOMIC_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"Id"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_ECONOMIC_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_ECONOMIC_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_JOB_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_WORK_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray);
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_WORK_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_WORK_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_SEC_REF_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_SEC_REFS_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"CreateDate", "OrderNum"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_SEC_REFS_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_SEC_REFS_URI_BULK_INSERT: " + count);
                    break;
                case Constantes.LIST_EMPLOYERS_URL:
                    mContext.getContentResolver().delete(Constantes.CONTENT_EMPLOYER_URI, null, null);
                    jsonArray = new JSONArray(data);
                    contentValues = Utils.reflectToContentValues(jsonArray, new String[]{"CompanyInfoId", "ArlId", "ExpiryTemp",
                            "SucursalName", "ServicesProvided", "DescARL", "NameARL", "PersonalIds", "ContractIds", "ProjectIds","IsExempt","EconomicActivities",
                            "LegalRepresentativeEmail","LegalRepresentativePhone","BranchOffices","LegalRepresentativeName"});
                    count = mContext.getContentResolver().bulkInsert(Constantes.CONTENT_EMPLOYER_URI_BULK_INSERT, contentValues);
                    logW("Count CONTENT_EMPLOYER_URI_BULK_INSERT: " + count);
                    break;
            }
            if (count == -1)
                return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            processFinish(Constantes.SUCCESS_REQUEST, data, url);

        } else {
            processFinish(Constantes.BAD_REQUEST, data, url);
        }
    }

    @SuppressLint("DefaultLocale")
    private void processFinish(int option, String jsonObject, String url) {
        Intent localIntent = new Intent();
        localIntent.setAction(CRUDService.ACTION_REQUEST_SAVE);
        localIntent.putExtra(Constantes.OPTION_JSON_BROADCAST, option);
        localIntent.putExtra(Constantes.VALUE_JSON_BROADCAST, jsonObject);
        localIntent.putExtra(Constantes.URL_REQUEST_BROADCAST, url);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
    }
}
