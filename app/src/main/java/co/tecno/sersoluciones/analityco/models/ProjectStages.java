package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ser Soluciones SAS on 05/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProjectStages implements Parcelable {
    public String StartDate;
    public String FinishDate;
    public int Id;
    private String ProjectId;
    public String Name;
    public String Review;
    public boolean IsActive;
    public boolean Expiry;
    public boolean HaveContract;

    public ProjectStages() {
    }

    public ProjectStages(String startDate, String finishDate, String projectId, String name, boolean isActive, String review) {
        StartDate = startDate;
        FinishDate = finishDate;
        ProjectId = projectId;
        Name = name;
        IsActive = isActive;
        this.Review = review;
    }

    public ProjectStages(String startDate, String finishDate, int id, String projectId, String name, boolean isActive, String review) {
        StartDate = startDate;
        FinishDate = finishDate;
        Id = id;
        ProjectId = projectId;
        Name = name;
        IsActive = isActive;
        this.Review = review;
    }

    public ProjectStages(int id, String name) {
        Id = id;
        Name = name;
    }

    public ProjectStages(String startDate, String finishDate, String name, String review) {
        StartDate = startDate;
        FinishDate = finishDate;
        Name = name;
        Review = review;
    }

    private ProjectStages(Parcel in) {
        StartDate = in.readString();
        FinishDate = in.readString();
        Id = in.readInt();
        ProjectId = in.readString();
        Name = in.readString();
        Review = in.readString();
        IsActive = in.readByte() != 0;
        Expiry = in.readByte() != 0;
        HaveContract = in.readByte() != 0;
    }

    public static final Creator<ProjectStages> CREATOR = new Creator<ProjectStages>() {
        @Override
        public ProjectStages createFromParcel(Parcel in) {
            return new ProjectStages(in);
        }

        @Override
        public ProjectStages[] newArray(int size) {
            return new ProjectStages[size];
        }
    };

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(StartDate);
        dest.writeString(FinishDate);
        dest.writeInt(Id);
        dest.writeString(ProjectId);
        dest.writeString(Name);
        dest.writeString(Review);
        dest.writeByte((byte) (IsActive ? 1 : 0));
        dest.writeByte((byte) (Expiry ? 1 : 0));
        dest.writeByte((byte) (HaveContract ? 1 : 0));
    }
}
