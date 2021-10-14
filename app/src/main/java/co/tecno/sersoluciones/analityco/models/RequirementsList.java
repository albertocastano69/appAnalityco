package co.tecno.sersoluciones.analityco.models;


public class RequirementsList {
    public boolean IsActive;
    public String Desc;
    public String title;
    public boolean IsValided;

    public String File;
    public String Type;
    public int Id;
    public boolean IsDate;
    public boolean RequiredDate;
    public boolean IsCertification;
    public boolean IsBiometric;
    public boolean IsEntry;
    public boolean IsSocialSec;
    public String ValidityDate;
    public String Date;

    public String ProjectId;
    public String Attr;
    public String contractId;
    public String DocsJSON;
    public boolean withFile;
    public String Model;
    public String EPSName;
    public String AFPName;
    public String ARLName;
    public TypeARL typeARL;

    public boolean IsSurvey;
    public long SurveyId;
    public String documentNumber;
    public String birthday;
    public String projectId;
    public long State;

    public boolean EnableInputEntry;
    public long ArithmeticOperator;
    public float EntryValue;
    public float Value;
    RequirementsList() {
    }

    public class TypeARL{
        public String Attr;
        public String Desc;
        public Boolean IsValided;
        public String Type;
    }

}
