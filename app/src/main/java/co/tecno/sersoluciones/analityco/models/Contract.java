package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ser Soluciones SAS on 07/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class Contract implements Serializable {
    public String Address;
    public String ContractNumber;
    public String ContractReview;
    public String ContractFile;
    public int ContractTypeId;
    public String Id;
    public String FinishDate;
    public String StarDate;
    public boolean IsRegister;
    public boolean Expiry;
    public boolean IsActive;
    public int FormImageId;
    public int PersonalCompanyInfoId;
    private String CompanyId;
    public String CompanyInfoId;
    public String CompanyInfoParentId;
    public String EmployerId;
    private String ContractorId;

    public transient ArrayList<ProjectStages> ProjectStageArray;
    public String DescriptionPersonalType;
    public String ContractorName;
    public String ContractorDocumentNumber;
    public String ContractorLogo;
    public String CompanyLogo;
    public String CompanyName;
    private String CreatedBy;
    private String CreateDate;
    public String ValueContractType;
    public String DescriptionContractType;
    private String TypeContractType;

    private String EmployerName;
    private String EmployerLogo;
    public String ProjectId;
    public String FormImageLogo;
    public int PersonalTypeId;
    private String[] Tags;
    public String CompanyDocumentNumber;
    public String MinHour;
    public String MaxHour;

    public ArrayList<Integer> WeekDays;

    public Contract() {
    }


    public Contract(String contractNumber, String contractReview, String contractFile, int contractTypeId, String id, int formImageId, String companyId, String contractorId, String companyLogo, String companyName, String createdBy, String createDate, String valueContractType, String descriptionContractType, String typeContractType, String employerName, String employerLogo, String formImageLogo, String[] tags) {
        ContractNumber = contractNumber;
        ContractReview = contractReview;
        ContractFile = contractFile;
        ContractTypeId = contractTypeId;
        Id = id;
        FormImageId = formImageId;
        CompanyId = companyId;
        CompanyInfoId = companyId;
        ContractorId = contractorId;
        EmployerId = ContractorId;
        CompanyLogo = companyLogo;
        CompanyName = companyName;
        CreatedBy = createdBy;
        CreateDate = createDate;
        ValueContractType = valueContractType;
        DescriptionContractType = descriptionContractType;
        TypeContractType = typeContractType;
        EmployerName = employerName;
        EmployerLogo = employerLogo;
        FormImageLogo = formImageLogo;
        Tags = tags;
    }

    public Contract(String contractNumber, String contractReview, int formImageId, int contractTypeId, int personalTypeId,
                    String companyId, String contractorId, String formImageLogo) {
        ContractNumber = contractNumber;
        ContractReview = contractReview;
        ContractTypeId = contractTypeId;
        FormImageId = formImageId;
        CompanyId = companyId;
        CompanyInfoId = companyId;
        EmployerId = contractorId;
        ContractorId = contractorId;
        PersonalTypeId = personalTypeId;
        FormImageLogo = formImageLogo;
    }

    public Contract(String contractNumber, String contractReview, int formImageId, int contractTypeId, int personalTypeId,
                    String companyId, String contractorId, String formImageLogo, String minhour, String maxhour) {
        ContractNumber = contractNumber;
        ContractReview = contractReview;
        ContractTypeId = contractTypeId;
        FormImageId = formImageId;
        CompanyId = companyId;
        CompanyInfoId = companyId;
        ContractorId = contractorId;
        EmployerId = contractorId;
        PersonalTypeId = personalTypeId;
        FormImageLogo = formImageLogo;
        MinHour = minhour;
        MaxHour = maxhour;
    }
}

