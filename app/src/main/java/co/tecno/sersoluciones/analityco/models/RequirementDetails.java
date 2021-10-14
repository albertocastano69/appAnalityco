package co.tecno.sersoluciones.analityco.models;

import java.util.ArrayList;


public class RequirementDetails {
    public int Id;
    public String ContractId;
    public String ContractNumber;
    public int PersonalTypeId;
    public String PersonalTypeName;
    public ArrayList<Requirements> RequirementContracts;

    public int AgeMin;
    public int AgeMax;
    public String MinHour;
    public String MaxHour;
    public int[] WeekDays;

    public RequirementDetails() {
    }

}
