package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserProject implements Parcelable {

    public final String StartDate;
    public final String FinishDate;
    public String RoleId;
    public final String UserId;
    private String ProjectId;
    public String Email;
    public String Photo;
    public String RoleName;
    public String RolName;
    public String Name;
    public String LastName;
    public String UserName;
    public String PhoneNumber;
    private String CompanyId;

    public UserProject(String startDate, String finishDate, String userId, String companyId) {
        StartDate = startDate;
        FinishDate = finishDate;
        UserId = userId;
        CompanyId = companyId;
    }

    public UserProject(String startDate, String finishDate, String userId, String companyId, String rolId) {
        StartDate = startDate;
        FinishDate = finishDate;
        UserId = userId;
        CompanyId = companyId;
        RoleId = rolId;
    }

    public UserProject(String startDate, String finishDate, String userId) {
        StartDate = startDate;
        FinishDate = finishDate;
        UserId = userId;
    }


    private UserProject(Parcel in) {
        StartDate = in.readString();
        FinishDate = in.readString();
        RoleId = in.readString();
        UserId = in.readString();
        ProjectId = in.readString();
        Email = in.readString();
        Photo = in.readString();
        RolName = in.readString();
        Name = in.readString();
        LastName = in.readString();
        UserName = in.readString();
        PhoneNumber = in.readString();
        CompanyId = in.readString();
    }

    public static final Creator<UserProject> CREATOR = new Creator<UserProject>() {
        @Override
        public UserProject createFromParcel(Parcel in) {
            return new UserProject(in);
        }

        @Override
        public UserProject[] newArray(int size) {
            return new UserProject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(StartDate);
        dest.writeString(FinishDate);
        dest.writeString(RoleId);
        dest.writeString(UserId);
        dest.writeString(ProjectId);
        dest.writeString(Email);
        dest.writeString(Photo);
        dest.writeString(RolName);
        dest.writeString(Name);
        dest.writeString(LastName);
        dest.writeString(UserName);
        dest.writeString(PhoneNumber);
        dest.writeString(CompanyId);
    }
}
