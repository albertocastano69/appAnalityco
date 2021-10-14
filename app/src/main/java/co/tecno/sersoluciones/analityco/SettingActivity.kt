package co.tecno.sersoluciones.analityco

import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import co.tecno.sersoluciones.analityco.fragments.SettingFragment

/**
 * Created by Ser Soluciones SAS on 24/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class SettingActivity : BaseActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Explode()
        }
        super.onCreate(savedInstanceState)
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        supportActionBar!!.title = "Configuración Servidor"
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //NavUtils.navigateUpFromSameTask(this)
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_settings)
    }

    override fun onPause() {
        super.onPause()
    }
}