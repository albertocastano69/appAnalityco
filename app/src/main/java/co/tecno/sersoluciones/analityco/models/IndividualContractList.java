package co.tecno.sersoluciones.analityco.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.EditText;

public class IndividualContractList implements Parcelable {
    public String Id;
    public int IndividualContractTypeId;
    public String ContractNumber;
    public int PositionId;
    public int Salary;
    public int PayPeriodId;
    public String StartDate;
    public String EndDate;
    public String CreateDate;
    public String UpdateDate;
    public String PersonalEmployerInfoId;
    public String ContractId;
    public String ProjectId;

    public IndividualContractList() {
    }

    protected IndividualContractList(Parcel in) {
        Id = in.readString();
        IndividualContractTypeId = in.readInt();
        ContractNumber = in.readString();
        PositionId = in.readInt();
        Salary = in.readInt();
        PayPeriodId = in.readInt();
        StartDate = in.readString();
        EndDate = in.readString();
        CreateDate = in.readString();
        UpdateDate = in.readString();
        PersonalEmployerInfoId = in.readString();
        ContractId = in.readString();
        ProjectId = in.readString();
    }

    public static final Creator<IndividualContractList> CREATOR = new Creator<IndividualContractList>() {
        @Override
        public IndividualContractList createFromParcel(Parcel in) {
            return new IndividualContractList(in);
        }

        @Override
        public IndividualContractList[] newArray(int size) {
            return new IndividualContractList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeInt(IndividualContractTypeId);
        dest.writeString(ContractNumber);
        dest.writeInt(PositionId);
        dest.writeInt(Salary);
        dest.writeInt(PayPeriodId);
        dest.writeString(StartDate);
        dest.writeString(EndDate);
        dest.writeString(CreateDate);
        dest.writeString(UpdateDate);
        dest.writeString(PersonalEmployerInfoId);
        dest.writeString(ContractId);
        dest.writeString(ProjectId);

    }
}
