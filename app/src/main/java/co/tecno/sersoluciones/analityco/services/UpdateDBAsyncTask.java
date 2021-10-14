package co.tecno.sersoluciones.analityco.services;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.Normalizer;
import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.City;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.ContractList;
import co.tecno.sersoluciones.analityco.models.DaneCity;
import co.tecno.sersoluciones.analityco.models.EconomicActivity;
import co.tecno.sersoluciones.analityco.models.Employer;
import co.tecno.sersoluciones.analityco.models.IndividualContractList;
import co.tecno.sersoluciones.analityco.models.Job;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.RequirementsOfContract;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 11/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
class UpdateDBAsyncTask extends AsyncTask<String, Void, Boolean> {

    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private String url;
    private String responseJSON;
    private final boolean sendBroadcast;

    UpdateDBAsyncTask(Context context) {
        this.context = context;
        sendBroadcast = true;
    }

    UpdateDBAsyncTask(Context context, boolean sendBroadcast) {
        this.context = context;
        this.sendBroadcast = sendBroadcast;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        url = strings[1];
        responseJSON = strings[0];
        return updateDataBase(strings[0]);
    }

    @Override
    protected void onPostExecute(Boolean response) {
        if (response) {
            switch (url) {
                case Constantes.LIST_CITIES_URL:
                    processFinish(url, Constantes.UPDATE_CITIES, null);
                    break;
                case Constantes.LIST_ECONOMIC_URL:
                    processFinish(url, Constantes.UPDATE_ECONOMIC_ACTIVITIES, null);
                    break;
                case Constantes.LIST_JOB_URL:
                    processFinish(url, Constantes.UPDATE_JOBS, null);
                    break;
                default:
                    processFinish(url, Constantes.SEND_REQUEST, responseJSON);
                    break;
            }
        } else {
            processFinish(url, Constantes.BAD_REQUEST, null);
        }
    }

    @SuppressLint("DefaultLocale")
    private boolean updateDataBase(String responseJSON) {

        boolean response = false;

        DBHelper databaseHelper = new DBHelper(context);
        try {
            long partialTime = System.currentTimeMillis();
            int count;
            long seconds;
            //Gson gson = new Gson();
            switch (url) {
                case Constantes.LIST_CITIES_URL:
                    databaseHelper.deleteAllCity();
                    ArrayList<City> cities = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<City>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_CITY_URI_BULK_INSERT,
                            getContentVCities(cities));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_CITY_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_COMPANIES_URL:
                    databaseHelper.deleteAllCompany();
                    Gson gson = new Gson();
                    ArrayList<CompanyList> companyLists = gson.fromJson(responseJSON, new TypeToken<ArrayList<CompanyList>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_COMPANY_URI_BULK_INSERT, getContentVComp(companyLists));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_COMPANY_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_DANE_CITIES_URL:
                    databaseHelper.deleteAllDaneCities();
                    ArrayList<DaneCity> daneCities = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<DaneCity>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_DANE_CITY_URI_BULK_INSERT, getContentVDaneCities(daneCities));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_DANE_CITY_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_ECONOMIC_URL:
                    databaseHelper.deleteAllEconomic();
                    ArrayList<EconomicActivity> economicActivities = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<EconomicActivity>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_ECONOMIC_URI_BULK_INSERT, getContentEconomicActivities(economicActivities));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_ECONOMIC_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_PROJECTS_URL:
                    databaseHelper.deleteAllProject();
                    ArrayList<ProjectList> projectLists = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<ProjectList>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_PROJECT_URI_BULK_INSERT, getContentVProject(projectLists));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_PROJECT_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_EMPLOYERS_URL:
                    databaseHelper.deleteAllEmployer();
                    ArrayList<Employer> employerLists = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<Employer>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_EMPLOYER_URI_BULK_INSERT, getContentVEmployer(employerLists));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_EMPLOYER_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_CONTRACTS_URL:
                    databaseHelper.deleteAllContract();
                    ArrayList<ContractList> contractLists = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<ContractList>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_CONTRACT_URI_BULK_INSERT, getContentVContract(contractLists));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_CONTRACT_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case  Constantes.LIST_INDIVIDUALCONTRACTS_URL:
                    databaseHelper.deleteAllIndiviudalContracts();
                    ArrayList<IndividualContractList> individualContractLists = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<IndividualContractList>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_INDIVIDUAL_CONTRACT_URI_BULK_INSERT,getContentVIndividualContract(individualContractLists));
                    seconds =  (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_INDIVIDUAL_CONTRACT_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_CONTRACT_PER_OFFLINE_URL:
                    databaseHelper.deleteAllContractPerson();
                    ArrayList<RequirementsOfContract> contractPersonOffs = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<RequirementsOfContract>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT, getContentVContractPerson(contractPersonOffs));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_PERSONAL_URL:
                    databaseHelper.deleteAllPersonal();
                    ArrayList<PersonalList> personalLists = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<PersonalList>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_PERSONAL_URI_BULK_INSERT, getContentVPersonal(personalLists));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_PERSONAL_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.REPORTPERSONAL_URL:
                    databaseHelper.deleteAllReport();
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_CONTRACT_PERSON_REPORT_URI_BULK_INSERT, getContentVReport(responseJSON));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_CONTRACT_PERSON_REPORT_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.DASHBOARD_URL:
                    databaseHelper.deleteAllDashBoard();
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_DASHBOARD_URI_BULK_INSERT, getContentVDashBoard(responseJSON));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_DASHBOARD_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                case Constantes.LIST_JOB_URL:
                    databaseHelper.deleteAllJobs();
                    ArrayList<Job> jobsLists = new Gson().fromJson(responseJSON, new TypeToken<ArrayList<Job>>() {
                    }.getType());
                    count = context.getContentResolver().bulkInsert(Constantes.CONTENT_WORK_URI_BULK_INSERT, getContentVWork(jobsLists));
                    seconds = (System.currentTimeMillis() - partialTime) / 1000;
                    logW(String.format("CONTENT_WORK_URI_BULK_INSERT partialTime %02d:%02d, count: %s", seconds / 60, seconds % 60, count));
                    break;
                default:
                    break;
            }
            response = true;
        } catch (Exception e) {
            logE("UpdateDBAsyncTask: " + e.getMessage());
        } finally {
            databaseHelper.close();
        }
        return response;
    }

    private ContentValues[] getContentVComp(ArrayList<CompanyList> companyLists) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (CompanyList companyList : companyLists) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_SERVER_ID, companyList.Id);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_SERVER_IDINFO, companyList.CompanyInfoId);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_NAME, companyList.Name);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_ACTIVE, companyList.IsActive);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_START_DATE, companyList.StartDate);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_FINISH_DATE, companyList.FinishDate);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_PHONE, companyList.Phone);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_ADDRESS, companyList.Address);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_DOC_TYPE, companyList.DocumentType);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_DOC_NUM, companyList.DocumentNumber);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_NAME_CITY, companyList.NameCity);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_NAME_STATE, companyList.StateName);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_EXPIRY, companyList.Expiry);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_LOGO, companyList.Logo);
            cv.put(DBHelper.COMPANY_TABLE_COLUMN_BRANCH_OFFICES, companyList.BranchOffices);
            contentValues.add(cv);
        }

        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVCities(ArrayList<City> cities) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (City city : cities) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.CITY_TABLE_COLUMN_SERVER_ID, city.Id);
            cv.put(DBHelper.CITY_TABLE_COLUMN_NAME, city.Name);
            cv.put(DBHelper.CITY_TABLE_COLUMN_CODE, city.Code);
            cv.put(DBHelper.CITY_TABLE_COLUMN_STATE, city.StateName);
            cv.put(DBHelper.CITY_TABLE_COLUMN_CITY_CODE, city.CityCode);
            cv.put(DBHelper.CITY_TABLE_COLUMN_STATE_CODE, city.StateCode);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }


    private ContentValues[] getContentVDaneCities(ArrayList<DaneCity> daneCities) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (DaneCity daneCity : daneCities) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.DANE_CITY_TABLE_COLUMN_CODE, daneCity.CityCode);
            cv.put(DBHelper.DANE_CITY_TABLE_COLUMN_NAME, daneCity.CityName);
            cv.put(DBHelper.DANE_CITY_TABLE_COLUMN_STATE, daneCity.StateName);
            cv.put(DBHelper.DANE_CITY_TABLE_COLUMN_STATE_CODE, daneCity.StateCode);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentEconomicActivities(ArrayList<EconomicActivity> economicActivities) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (EconomicActivity economicActivity : economicActivities) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.ECONOMIC_TABLE_COLUMN_CODE, economicActivity.Code);
            cv.put(DBHelper.ECONOMIC_TABLE_COLUMN_NAME, economicActivity.Name);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVProject(ArrayList<ProjectList> projectLists) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (ProjectList projectList : projectLists) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID, projectList.Id);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_NAME, projectList.Name);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_ACTIVE, projectList.IsActive);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_START_DATE, projectList.StartDate);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_FINISH_DATE, projectList.FinishDate);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_CREATE_DATE, projectList.CreateDate);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_NUM_PROJECT, projectList.ProjectNumber);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_ADDRESS, projectList.Address);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_NAME_CITY, projectList.CityName);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_NAME_STATE, projectList.StateName);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_STAGES, projectList.ProjectStageArray);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_GEOFENCE, projectList.GeoFenceJson);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_LOGO, projectList.Logo);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_EXPIRY, projectList.Expiry);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_COMPANYNAME, projectList.CompanyName);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_COMPANY_INFO_ID, projectList.CompanyInfoId);
            cv.put(DBHelper.PROJECT_TABLE_COLUMN_JOIN_COUNT, projectList.JoinCompaniesCount);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVEmployer(ArrayList<Employer> employers) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (Employer employer : employers) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_SERVER_ID, employer.Id);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE, true);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_DOC_TYPE, employer.DocumentType);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_DOC_NUM, employer.DocumentNumber);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_NAME, employer.Name);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_ADDRESS, employer.Address);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_CITY_CODE, employer.CityCode);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_CREATE_DATE, employer.CreateDate);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_START_DATE, employer.StartDate);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_FINISH_DATE, employer.FinishDate);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_WEBSITE, employer.Website);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_PHONE, employer.Phone);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_EMAIL, employer.Email);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_LOGO, employer.Logo);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_ROL, employer.Rol);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_NAME_CITY, employer.CityName);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_NAME_STATE, employer.StateName);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_EXPIRY, employer.Expiry);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_COMPANY_ID, employer.CompanyInfoParentId);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_COMPANY, employer.CompanyId);
            cv.put(DBHelper.EMPLOYER_TABLE_COLUMN_IS_MANAGED,employer.IsManaged);
            contentValues.add(cv);
        }


        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }


    private ContentValues[] getContentVContract(ArrayList<ContractList> contractLists) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (ContractList contractList : contractLists) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID, contractList.Id);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_NAME, contractList.CompanyName);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE, contractList.IsActive);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_FINISH_DATE, contractList.FinishDate);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_START_DATE, contractList.StartDate);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_CREATE_DATE, contractList.CreateDate);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER, contractList.ContractNumber);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NAME, contractList.ContractorName);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_LOGO, contractList.FormImageLogo);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_REVIEW, contractList.ContractReview);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_ISREGISTER, contractList.IsRegister);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID, contractList.ContractTypeId);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_EXPIRY, contractList.Expiry);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS, contractList.ProjectIds);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_COUNT_PERSONAL, contractList.CountPersonal);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_COMPANY_ID, contractList.CompanyInfoId);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_EMPLOYER_ID, contractList.EmployerId);
            cv.put(DBHelper.CONTRACT_TABLE_COLUMN_COMPANY_INFO_ID, contractList.CompanyInfoParentId);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    @SuppressLint("DefaultLocale")
    private void processFinish(String url, int option, String jsonObject) {
        if (sendBroadcast) {
            Intent localIntent = new Intent();
            localIntent.setAction(Constantes.BROADCAST_GET_JSON);
            localIntent.putExtra(Constantes.OPTION_JSON_BROADCAST, option);
            localIntent.putExtra(Constantes.VALUE_JSON_BROADCAST, jsonObject);
            localIntent.putExtra(Constantes.URL_REQUEST_BROADCAST, url);
            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(localIntent);
        }
    }

    private ContentValues[] getContentVContractPerson(ArrayList<RequirementsOfContract> contractPersonOffs) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (RequirementsOfContract contractPersonOff : contractPersonOffs) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID, contractPersonOff.ContractId);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID, contractPersonOff.ProjectId);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID, contractPersonOff.PersonalCompanyInfoId);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_NUMBER, contractPersonOff.ContractNumber);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON, contractPersonOff.CarnetCodes);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT, contractPersonOff.EnterProject);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_REVIEW, contractPersonOff.ContractReview);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE, contractPersonOff.StartDate);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE, contractPersonOff.FinishDate);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT, contractPersonOff.StartDateContract);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT, contractPersonOff.FinishDateContract);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACTOR_ID, contractPersonOff.EmployerId);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_MAXHOUR, contractPersonOff.MaxHour);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_MINHOUR, contractPersonOff.MinHour);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_AGEMAX, contractPersonOff.AgeMax);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_AGEMIN, contractPersonOff.AgeMin);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_WEEKDAYS, contractPersonOff.WeekDays);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_ID, contractPersonOff.PersonalGuidId);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM, contractPersonOff.DocumentNumber);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_POSITION, contractPersonOff.Position);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_CONTRACTS, contractPersonOff.ProjectContracts);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_TYPE, contractPersonOff.DescriptionPersonalType);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_REQS, contractPersonOff.Requirements);
            cv.put(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_BIRTHDAY, contractPersonOff.BirthDate);
            contentValues.add(cv);
        }

        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }
    private  ContentValues[] getContentVIndividualContract(ArrayList<IndividualContractList> IndividualContractLists){
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for(IndividualContractList individualContractList : IndividualContractLists){
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_ID, individualContractList.Id);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_TYPEID, individualContractList.IndividualContractTypeId);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_CONTRACTNUMBER, individualContractList.ContractNumber);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_POSITIONID, individualContractList.PositionId);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_SALARY, individualContractList.Salary);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_PAYPERIODID, individualContractList.PayPeriodId);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_STARTDATE, individualContractList.StartDate);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_ENDDATE, individualContractList.EndDate);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_CREATEDATE, individualContractList.CreateDate);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_UPDATEDATE, individualContractList.UpdateDate);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_PERSONALEMPLOYERINFOID, individualContractList.PersonalEmployerInfoId);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_CONTRACTID, individualContractList.ContractId);
            cv.put(DBHelper.INDIVIDUALCONTRACT_TABLE_COLUMN_PROJECTID, individualContractList.ProjectId);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVPersonal(ArrayList<PersonalList> personalLists) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (PersonalList personalList : personalLists) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_SERVER_ID, personalList.Id);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID, personalList.PersonalCompanyInfoId);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_DOC_TYPE, personalList.DocumentType);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE, personalList.IsActive);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_DOC_NUM, personalList.DocumentNumber);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_NAME, personalList.Name);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_LASTNAME, personalList.LastName);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_NAME_CITY, personalList.CityOfBirthName);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_NAME_STATE, personalList.StateOfBirthName);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_PHOTO, personalList.Photo);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_EXPIRY, personalList.Expiry);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_FINISH_DATE, personalList.FinishDate);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_START_DATE, personalList.StartDate);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_CREATE_DATE, personalList.CreateDate);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_PHONE, personalList.PhoneNumber);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_JOB, personalList.JobName);
            cv.put(DBHelper.PERSONAL_TABLE_COLUMN_COMPANY_INFO_ID, personalList.CompanyInfoId);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVReport(String Report) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_DATA, Report);
        contentValues.add(cv);
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVDashBoard(String dashBoard) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.DASHBOARD_TABLE_COLUMN_CONTENT, dashBoard);
        contentValues.add(cv);
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }

    private ContentValues[] getContentVWork(ArrayList<Job> JobsLists) {
        ArrayList<ContentValues> contentValues = new ArrayList<>();
        for (Job joblList : JobsLists) {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.WORK_TABLE_COLUMN_SERVER_ID, joblList.Id);
            cv.put(DBHelper.WORK_TABLE_COLUMN_CODE, joblList.Code);
            cv.put(DBHelper.WORK_TABLE_COLUMN_NAME, joblList.Name);
            String asciiName = Normalizer.normalize(joblList.Name, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");
            cv.put(DBHelper.WORK_TABLE_COLUMN_NAME_ASCII, asciiName);
            contentValues.add(cv);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }
}
