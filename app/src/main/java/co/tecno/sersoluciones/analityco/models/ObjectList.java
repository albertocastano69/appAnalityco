package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ObjectList implements Parcelable {

    public String Id;
    public String Name;
    public String Logo;
    public String DocumentType;
    public String DocumentNumber;
    private String CompanyName;
    public String Address;
    public String ProjectNumber;
    public String CityName;
    public boolean IsActive;
    public boolean Expiry;
    private boolean IsSelected;
    private String StateName;
    public String FinishDate;
    public String StartDate;
    public String FormImageLogo;
    public String ContractorName;
    public String ContractNumber;
    public String ContractReview;
    public boolean IsRegister;
    public String DescriptionPersonalType;
    public String Position;
    public String FinishDatePerson;
    public String EmployerName;
    public ArrayList<ProjectStageContract> ProjectStageContracts;
    public String DaysToExpire;
    public String FinishDateContract;
    public String CompanyInfoParentId;
    public String CompanyInfoId;
    public String ValueContractType;
    public String CompanyId;
    public String Rol;
    public int IsManaged;

    public ObjectList() {
    }

    public ObjectList(String id, String name, String documentNumber, String imageUrl) {
        this.Id = id;
        this.Name = name;
        this.DocumentNumber = documentNumber;
        this.Logo = imageUrl;
    }

    public ObjectList(String id, String name, String documentNumber, String imageUrl, String documentType) {
        this.Id = id;
        this.Name = name;
        this.DocumentNumber = documentNumber;
        this.Logo = imageUrl;
        this.DocumentType = documentType;
    }

    protected ObjectList(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        Logo = in.readString();
        DocumentType = in.readString();
        DocumentNumber = in.readString();
        CompanyName = in.readString();
        Address = in.readString();
        ProjectNumber = in.readString();
        CityName = in.readString();
        IsActive = in.readByte() != 0;
        Expiry = in.readByte() != 0;
        IsSelected = in.readByte() != 0;
        StateName = in.readString();
        FinishDate = in.readString();
        FormImageLogo = in.readString();
        ContractorName = in.readString();
        ContractNumber = in.readString();
        ContractReview = in.readString();
        IsRegister = in.readByte() != 0;
        DescriptionPersonalType = in.readString();
        ValueContractType = in.readString();
        IsManaged = in.readInt();
        CompanyId = in.readString();
        Rol = in.readString();

    }

    public static final Creator<ObjectList> CREATOR = new Creator<ObjectList>() {
        @Override
        public ObjectList createFromParcel(Parcel in) {
            return new ObjectList(in);
        }

        @Override
        public ObjectList[] newArray(int size) {
            return new ObjectList[size];
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
        dest.writeString(Logo);
        dest.writeString(DocumentType);
        dest.writeString(DocumentNumber);
        dest.writeString(CompanyName);
        dest.writeString(Address);
        dest.writeString(ProjectNumber);
        dest.writeString(CityName);
        dest.writeByte((byte) (IsActive ? 1 : 0));
        dest.writeByte((byte) (Expiry ? 1 : 0));
        dest.writeByte((byte) (IsSelected ? 1 : 0));
        dest.writeString(StateName);
        dest.writeString(FinishDate);
        dest.writeString(FormImageLogo);
        dest.writeString(ContractorName);
        dest.writeString(ContractNumber);
        dest.writeString(ContractReview);
        dest.writeByte((byte) (IsRegister ? 1 : 0));
        dest.writeString(DescriptionPersonalType);
        dest.writeInt(IsManaged);
        dest.writeString(CompanyId);
        dest.writeString(Rol);
    }
}
