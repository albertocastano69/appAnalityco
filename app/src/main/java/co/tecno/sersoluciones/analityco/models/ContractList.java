package co.tecno.sersoluciones.analityco.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class ContractList implements Serializable {

    public String Id;
    public boolean IsActive;
    public String FinishDate;
    public String CreateDate;
    public String ContractNumber;
    public String ContractReview;
    public String ContractorName;
    public String CompanyName;
    public String FormImageLogo;
    private String DescriptionPersonalType;
    public boolean Expiry;
    public String StartDate;
    public String Position;
    private String ContractId;
    public boolean IsRegister;
    public int ContractTypeId;
    public String ProjectIds;
    public int CountPersonal;
    public String CompanyInfoId;
    public String EmployerId;
    public String ProjectId;
    public String CompanyInfoParentId;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public boolean isActive() {
        return IsActive;
    }

    public String getFinishDate() {
        return FinishDate;
    }

    public void setFinishDate(String finishDate) {
        FinishDate = finishDate;
    }

    public boolean isExpiry() {
        return Expiry;
    }

    public void setExpiry(boolean expiry) {
        Expiry = expiry;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getPosition() {
        return Position;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public String getContractId() {
        return ContractId;
    }

    public void setContractId(String contractId) {
        ContractId = contractId;
    }

    @NonNull
    @Override
    public String toString() {
        return DescriptionPersonalType + " " + ContractNumber;
    }
}
