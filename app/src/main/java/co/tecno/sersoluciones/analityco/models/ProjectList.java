package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by Ser Soluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProjectList implements Parcelable {

    public String Id;
    public boolean IsActive;
    public String Name;
    public String StartDate;
    public String FinishDate;
    public String CreateDate;
    public String Address;
    public String CityName;
    public String StateName;
    public String ProjectNumber;
    public String Logo;
    public String CompanyName;
    public String ProjectStageArray;
    public String GeoFenceJson;
    public String[] ProjectStages;
    public String ProjectId;
    public String CompanyInfoId;
    public int JoinCompaniesCount;
    public boolean Expiry;
    private boolean checkList;
    public boolean AllStages;
    public boolean IsSelected;
    public ArrayList<ProjectStages> Stages;
    public String DaysToExpire;

    public String ProjectFinishDate;
    public boolean ProjectExpiry;
    public boolean ProjectIsActive;
    public ProjectList(){}

    public String getName() {
        return Name;
    }

    protected ProjectList(Parcel in) {
        Id = in.readString();
        IsActive = in.readByte() != 0;
        Name = in.readString();
        StartDate = in.readString();
        FinishDate = in.readString();
        CreateDate = in.readString();
        Address = in.readString();
        CityName = in.readString();
        StateName = in.readString();
        ProjectNumber = in.readString();
        Logo = in.readString();
        CompanyName = in.readString();
        ProjectStageArray = in.readString();
        GeoFenceJson = in.readString();
        ProjectStages = in.createStringArray();
        ProjectId = in.readString();
        CompanyInfoId = in.readString();
        JoinCompaniesCount = in.readInt();
        Expiry = in.readByte() != 0;
        checkList = in.readByte() != 0;
        AllStages = in.readByte() != 0;
        IsSelected = in.readByte() != 0;
        ProjectFinishDate = in.readString();
        ProjectExpiry = in.readByte() != 0;
        ProjectIsActive = in.readByte() != 0;
    }

    public static final Creator<ProjectList> CREATOR = new Creator<ProjectList>() {
        @Override
        public ProjectList createFromParcel(Parcel in) {
            return new ProjectList(in);
        }

        @Override
        public ProjectList[] newArray(int size) {
            return new ProjectList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeByte((byte) (IsActive ? 1 : 0));
        dest.writeString(Name);
        dest.writeString(StartDate);
        dest.writeString(FinishDate);
        dest.writeString(CreateDate);
        dest.writeString(Address);
        dest.writeString(CityName);
        dest.writeString(StateName);
        dest.writeString(ProjectNumber);
        dest.writeString(Logo);
        dest.writeString(CompanyName);
        dest.writeString(ProjectStageArray);
        dest.writeString(GeoFenceJson);
        dest.writeStringArray(ProjectStages);
        dest.writeString(ProjectId);
        dest.writeString(CompanyInfoId);
        dest.writeInt(JoinCompaniesCount);
        dest.writeByte((byte) (Expiry ? 1 : 0));
        dest.writeByte((byte) (checkList ? 1 : 0));
        dest.writeByte((byte) (AllStages ? 1 : 0));
        dest.writeByte((byte) (IsSelected ? 1 : 0));
        dest.writeString(ProjectFinishDate);
        dest.writeByte((byte) (ProjectExpiry ? 1 : 0));
        dest.writeByte((byte) (ProjectIsActive ? 1 : 0));
    }

    @NotNull
    @Override
    public String toString() {
        return Name;
    }
}
