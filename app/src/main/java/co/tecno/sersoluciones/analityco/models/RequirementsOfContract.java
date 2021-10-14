package co.tecno.sersoluciones.analityco.models;

import java.util.ArrayList;


public class RequirementsOfContract {
    public String Id;
    public int PersonalCompanyInfoId;
    public String ProjectId;
    public String ContractId;
    public String ContractNumber;
    public String ContractReview;
    public String StartDate;
    public String FinishDate;
    public String StartDateContract;
    public String FinishDateContract;
    public String EmployerId;
    public String MaxHour;
    public String MinHour;
    public String WeekDays;
    public String FormImageLogo;
    public int AgeMin;
    public String AgeMax;
    public String PersonalGuidId;
    public String DocumentNumber;
    public String Position;
    public String Sex;
    public String BirthDate;
    public int Height;
    public String ProjectContracts;
    public String DescriptionPersonalType;
    public String Requirements;
    public Reqs ReqsObject;
    public boolean EnterProject;
    public String CarnetCodes;

    RequirementsOfContract() {
    }

    public class Reqs {
        public String PersonalCompanyInfoId;
        public String ContractId;
        public Schedule Schedule;
        public ArrayList<EntryModel> SocialSecurity;
        public  ArrayList<EntryModel> Certification;
        public ArrayList<Biometric> Biometric;
        public ArrayList<EntryModel> Entry;
        public ArrayList<ContractValidity> ContractValidity;
    }

    private class ContractValidity {
        public  boolean Allow;
        public String Attr;
        public String Desc;
    }
    private class EntryModel {
        public  boolean IsValided;
        public String Attr;
        public String Desc;
        public String Type;
        public String Model;
        public int Id;
    }
    private class Biometric {

    }
    public class Schedule {
        public  boolean Allow;
        public String Late;
        public String Desc;
    }
}
