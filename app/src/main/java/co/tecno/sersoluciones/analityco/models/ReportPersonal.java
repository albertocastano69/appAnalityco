package co.tecno.sersoluciones.analityco.models;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ReportPersonal implements Serializable {

    public long TimeMillis;
    private final int Battery;
    private final int Velocity;
    private final String Provider;
    private final int Accuracy;
    private final int Bearing;
    private final String InfoGPS;
    public int PersonalCompanyInfoId;
    public String CompanyInfoId;
    public String ProjectId;
    public int ProjectStageId;
    public String ContractId;
    public String RequirementJson;
    public int Event; //0. verificar o inspeccionar, 1. entrada, 2. salida
    public String OptionsJson;
    public String VersionApp;

    public ReportPersonal(long deviceDate, int battery, int velocity, int event, String provider, int accuracy, int bearing, String infoGPS, int PersonaInfoId,
                          String companyId, String projectId, int projectStageId, String contractId, String requirementJson, boolean verificar, boolean inspeccionar,
                          boolean entry, boolean isValided) {
        TimeMillis = deviceDate;
        Battery = battery;
        Velocity = velocity;
        Event = event;
        Provider = provider;
        Accuracy = accuracy;
        Bearing = bearing;
        InfoGPS = infoGPS;
        this.PersonalCompanyInfoId = PersonaInfoId;
        CompanyInfoId = companyId;
        ProjectId = projectId;
        ProjectStageId = projectStageId;
        ContractId = contractId;
        RequirementJson = requirementJson;
        OptionsJson = new Gson().toJson(new OptionsJson(verificar, inspeccionar, entry, isValided));
    }

    public static class OptionsJson {
        final boolean Verify;
        final boolean Inspect;
        final boolean Entry;
        final boolean IsValided;

        public OptionsJson(boolean verificar, boolean inspeccionar, boolean entry, boolean isValided) {
            Verify = verificar;
            Inspect = inspeccionar;
            Entry = entry;
            IsValided = isValided;
        }
    }
}

