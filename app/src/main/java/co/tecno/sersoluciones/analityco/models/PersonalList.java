package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonalList implements Parcelable {

    public int Id;
    public int PersonalCompanyInfoId;
    public String DocumentType;
    public String DocumentNumber;
    public String Name;
    public String LastName;
    public String CityOfBirthName;
    public String StateOfBirthName;
    public String Photo;
    public String StartDate;
    public String CreateDate;
    public String FinishDate;
    public String JobName;
    public String PhoneNumber;
    public boolean Expiry;
    public boolean IsActive;
    private boolean checkList;
    public String Position;
    public String DaysToExpire;
    public boolean HaveRegister;
    public String CompanyInfoId;

    public PersonalList() {
    }

    public PersonalList(String documentType, String documentNumber, String name, String lastName) {
        DocumentType = documentType;
        DocumentNumber = documentNumber;
        Name = name;
        LastName = lastName;
    }

    public PersonalList(int personalCompanyInfoId, String name, String lastName,
                        String documentNumber, String companyInfoId,
                        String photo, boolean isActive) {
        PersonalCompanyInfoId = personalCompanyInfoId;
        Photo = photo;
        Name = name;
        LastName = lastName;
        IsActive = isActive;
        DocumentNumber = documentNumber;
        CompanyInfoId = companyInfoId;
    }

    protected PersonalList(Parcel in) {
        Id = in.readInt();
        PersonalCompanyInfoId = in.readInt();
        DocumentType = in.readString();
        DocumentNumber = in.readString();
        Name = in.readString();
        LastName = in.readString();
        CityOfBirthName = in.readString();
        StateOfBirthName = in.readString();
        Photo = in.readString();
        StartDate = in.readString();
        CreateDate = in.readString();
        FinishDate = in.readString();
        JobName = in.readString();
        PhoneNumber = in.readString();
        Expiry = in.readByte() != 0;
        IsActive = in.readByte() != 0;
        checkList = in.readByte() != 0;
        Position = in.readString();
    }

    public static final Creator<PersonalList> CREATOR = new Creator<PersonalList>() {
        @Override
        public PersonalList createFromParcel(Parcel in) {
            return new PersonalList(in);
        }

        @Override
        public PersonalList[] newArray(int size) {
            return new PersonalList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeInt(PersonalCompanyInfoId);
        dest.writeString(DocumentType);
        dest.writeString(DocumentNumber);
        dest.writeString(Name);
        dest.writeString(LastName);
        dest.writeString(CityOfBirthName);
        dest.writeString(StateOfBirthName);
        dest.writeString(Photo);
        dest.writeString(StartDate);
        dest.writeString(CreateDate);
        dest.writeString(FinishDate);
        dest.writeString(JobName);
        dest.writeString(PhoneNumber);
        dest.writeByte((byte) (Expiry ? 1 : 0));
        dest.writeByte((byte) (IsActive ? 1 : 0));
        dest.writeByte((byte) (checkList ? 1 : 0));
        dest.writeString(Position);
    }
}
