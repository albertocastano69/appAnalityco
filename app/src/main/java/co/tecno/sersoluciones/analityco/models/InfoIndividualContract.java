package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoIndividualContract implements Parcelable {
   public String Id;
   public String Name;
   public String LastName;
   public String NameEmployer;
   public String DocumentType;
   public String DocumentNumber;
   public String Descripction;
   public String CreateDate;
   public String Photo;
   public String PersonalId;
   public String PersonalEmployerInfo;
   public String EmployerId;
   public int IndividualContractTypeId;
   public int PositionId;
   public int Salary;
   public int PayPeriodId;
   public String StartDate;
   public String ProjectId;
   public String ContractNumber;
   public String RolEmployer;
   public String DocumentTypeEmployer;
   public String DocumentNumberEmployer;
   public String LogoEmployer;
   public String ContractCityId;
   public  String ContractId;
   public String Comment;

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getContractId() {
        return ContractId;
    }

    public void setContractId(String contractId) {
        ContractId = contractId;
    }

    public String getContractCityId() {
        return ContractCityId;
    }

    public void setContractCityId(String contractCityId) {
        ContractCityId = contractCityId;
    }

    public String getRolEmployer() {
        return RolEmployer;
    }

    public void setRolEmployer(String rolEmployer) {
        RolEmployer = rolEmployer;
    }

    public String getDocumentTypeEmployer() {
        return DocumentTypeEmployer;
    }

    public void setDocumentTypeEmployer(String documentTypeEmployer) {
        DocumentTypeEmployer = documentTypeEmployer;
    }

    public String getDocumentNumberEmployer() {
        return DocumentNumberEmployer;
    }

    public void setDocumentNumberEmployer(String documentNumberEmployer) {
        DocumentNumberEmployer = documentNumberEmployer;
    }

    public String getLogoEmployer() {
        return LogoEmployer;
    }

    public void setLogoEmployer(String logoEmployer) {
        LogoEmployer = logoEmployer;
    }

    public InfoIndividualContract() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getNameEmployer() {
        return NameEmployer;
    }

    public void setNameEmployer(String nameEmployer) {
        NameEmployer = nameEmployer;
    }

    public String getDocumentType() {
        return DocumentType;
    }

    public void setDocumentType(String documentType) {
        DocumentType = documentType;
    }

    public String getDocumentNumber() {
        return DocumentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        DocumentNumber = documentNumber;
    }

    public String getDescripction() {
        return Descripction;
    }

    public void setDescripction(String descripction) {
        Descripction = descripction;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public String getPersonalId() {
        return PersonalId;
    }

    public void setPersonalId(String personalId) {
        PersonalId = personalId;
    }

    public String getPersonalEmployerInfo() {
        return PersonalEmployerInfo;
    }

    public String getContractNumber() {
        return ContractNumber;
    }

    public void setContractNumber(String contractNumber) {
        ContractNumber = contractNumber;
    }

    public static Creator<InfoIndividualContract> getCREATOR() {
        return CREATOR;
    }

    public int getIndividualContractTypeId() {
        return IndividualContractTypeId;
    }

    public void setIndividualContractTypeId(int individualContractTypeId) {
        IndividualContractTypeId = individualContractTypeId;
    }

    public int getPositionId() {
        return PositionId;
    }

    public void setPositionId(int positionId) {
        PositionId = positionId;
    }

    public int getSalary() {
        return Salary;
    }

    public void setSalary(int salary) {
        Salary = salary;
    }

    public int getPayPeriodId() {
        return PayPeriodId;
    }

    public void setPayPeriodId(int payPeriodId) {
        PayPeriodId = payPeriodId;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getProjectId() {
        return ProjectId;
    }

    public void setProjectId(String projectId) {
        ProjectId = projectId;
    }

    public void setPersonalEmployerInfo(String personalEmployerInfo) {
        PersonalEmployerInfo = personalEmployerInfo;
    }

    public String getEmployerId() {
        return EmployerId;
    }

    public void setEmployerId(String employerId) {
        EmployerId = employerId;
    }
    protected InfoIndividualContract(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        LastName = in.readString();
        NameEmployer = in.readString();
        DocumentType = in.readString();
        DocumentNumber = in.readString();
        Descripction = in.readString();
        CreateDate = in.readString();
        Photo = in.readString();
        PersonalId = in.readString();
        PersonalEmployerInfo = in.readString();
        EmployerId = in.readString();
        IndividualContractTypeId = in.readInt();
        PositionId = in.readInt();
        Salary = in.readInt();
        PayPeriodId = in.readInt();
        StartDate = in.readString();
        ProjectId = in.readString();
        ContractNumber = in.readString();
        LogoEmployer = in.readString();
        RolEmployer = in.readString();
        DocumentTypeEmployer = in.readString();
        DocumentNumberEmployer = in.readString();
        ContractCityId = in.readString();
        ContractId = in.readString();
        Comment = in.readString();

    }
    public static final Creator<InfoIndividualContract> CREATOR = new Creator<InfoIndividualContract>() {
        @Override
        public InfoIndividualContract createFromParcel(Parcel in) {
            return new InfoIndividualContract(in);
        }

        @Override
        public InfoIndividualContract[] newArray(int size) {
            return new InfoIndividualContract[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeString(Name);
        dest.writeString(LastName);
        dest.writeString(NameEmployer);
        dest.writeString(DocumentType);
        dest.writeString(DocumentNumber);
        dest.writeString(Descripction);
        dest.writeString(CreateDate);
        dest.writeString(Photo);
        dest.writeString(PersonalId);
        dest.writeString(PersonalEmployerInfo);
        dest.writeString(EmployerId);
        dest.writeInt(IndividualContractTypeId);
        dest.writeInt(PositionId);
        dest.writeInt(Salary);
        dest.writeInt(PayPeriodId);
        dest.writeString(StartDate);
        dest.writeString(ProjectId);
        dest.writeString(ContractNumber);
        dest.writeString(LogoEmployer);
        dest.writeString(RolEmployer);
        dest.writeString(DocumentTypeEmployer);
        dest.writeString(DocumentNumberEmployer);
        dest.writeString(ContractCityId);
        dest.writeString(ContractId);
        dest.writeString(Comment);
    }
}
