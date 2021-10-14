package co.tecno.sersoluciones.analityco.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.tecno.sersoluciones.analityco.BuildConfig
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "real_time_person")
class PersonalRealTime() : Parcelable {
    @PrimaryKey
    var personalId: Long = 0L
    var getTime: Long = 0L
    var lastTime: Long = 0L
    var message: String? = null
    var projectId: String? = null
    var name: String? = null
    var lastName: String? = null
    var documentNumber: String? = null
    var companyInfoId: String? = null
    var avatar: String? = null
    var timestamp: Long = System.currentTimeMillis()
    var inputStatus : Long = 0L
    @Ignore
    var versionApp: String? = "${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}"

    constructor(
        personalId: Long,
        name: String,
        lastName: String,
        avatar: String,
        projectId: String,
        companyInfoId: String,
        documentNumber: String
    ) : this() {
        this.personalId = personalId
        this.name = name
        this.lastName = lastName
        this.avatar = avatar
        this.projectId = projectId
        this.companyInfoId = companyInfoId
        this.documentNumber = documentNumber
        this.timestamp = System.currentTimeMillis()
    }

    constructor(
        personalId: Long,
        projectId: String
    ) : this() {
        this.personalId = personalId
        this.projectId = projectId
    }

    constructor(parcel: Parcel) : this() {
        personalId = parcel.readLong()
        getTime = parcel.readLong()
        lastTime = parcel.readLong()
        message = parcel.readString()
        projectId = parcel.readString()
        name = parcel.readString()
        lastName = parcel.readString()
        documentNumber = parcel.readString()
        companyInfoId = parcel.readString()
        avatar = parcel.readString()
        timestamp = parcel.readLong()
        versionApp = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(personalId)
        parcel.writeLong(getTime)
        parcel.writeLong(lastTime)
        parcel.writeString(message)
        parcel.writeString(projectId)
        parcel.writeString(name)
        parcel.writeString(lastName)
        parcel.writeString(documentNumber)
        parcel.writeString(companyInfoId)
        parcel.writeString(avatar)
        parcel.writeLong(timestamp)
        parcel.writeString(versionApp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PersonalRealTime> {
        override fun createFromParcel(parcel: Parcel): PersonalRealTime {
            return PersonalRealTime(parcel)
        }

        override fun newArray(size: Int): Array<PersonalRealTime?> {
            return arrayOfNulls(size)
        }
    }

}