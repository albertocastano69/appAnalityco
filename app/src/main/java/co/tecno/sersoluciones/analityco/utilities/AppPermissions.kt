package co.tecno.sersoluciones.analityco.utilities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.User
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

object AppPermissions {
    private const val RQ_CODE_MULTIPLE_PERMISSIONS = 1
    private val permissions = Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @JvmStatic
    fun checkAppPermissionsAndRequest(activity: Activity?): Boolean {
        val permissionsList: MutableList<String> = ArrayList()
        for (permision in permissions) {
            if (ContextCompat.checkSelfPermission(activity!!, permision) != PackageManager.PERMISSION_GRANTED) permissionsList.add(permision)
        }
        if (permissionsList.size > 0) {
            ActivityCompat.requestPermissions(activity!!, permissionsList.toTypedArray(), RQ_CODE_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    private fun checkPermissionsGranted(results: IntArray): Boolean {
        return if (results.size > 0) {
            var i = 0
            while (i < results.size && results[i] == PackageManager.PERMISSION_GRANTED) {
                i++
            }
            i == results.size
        } else {
            false
        }
    }

    @JvmStatic
    fun requestPermissionsResultHandler(rq_code: Int, results: IntArray, activity: Activity) {
        if (rq_code == RQ_CODE_MULTIPLE_PERMISSIONS) {
            if (checkPermissionsGranted(results)) activity.recreate() else {
                AlertDialog.Builder(activity)
                        .setMessage(R.string.close_app_permissions)
                        .setNegativeButton(R.string.close) { dialogInterface: DialogInterface?, i: Int -> activity.finish() }
                        .setPositiveButton(R.string.restart) { dialogInterface: DialogInterface?, i: Int -> activity.recreate() }
                        .setCancelable(false)
                        .show()
            }
        }
    }

    fun getPermissionsOfUser(context: Context, companyAdminId: String? = null): ArrayList<String> {
        var claims = ArrayList<String>()
        val preferences = MyPreferences(context)
        val profile = preferences.profile
        val user = Gson().fromJson(profile, User::class.java)
        if (!user.IsSuperUser) {
            if (user.Companies.size == 1 || user.IsAdmin) {
                claims = user.claims
            } else {
                for (comp in user.Companies) {
                    if (companyAdminId != null && comp.Id == companyAdminId) {
                        claims = comp.Permissions
                        break
                    }
                }

            }
        }
        return claims
    }
}