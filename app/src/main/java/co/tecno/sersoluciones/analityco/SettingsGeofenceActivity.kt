package co.tecno.sersoluciones.analityco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import android.view.MenuItem

class SettingsGeofenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = "Ajustes"
        }

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsGeofenceFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                //NavUtils.navigateUpFromSameTask(this)
                onBackPressed()
                return true
            }
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsGeofenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, p1: String?) {

        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_geofence)
        }
    }
}
