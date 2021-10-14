package co.tecno.sersoluciones.analityco

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem
import androidx.fragment.app.Fragment
import co.tecno.sersoluciones.analityco.fragments.InfoFragment
import co.tecno.sersoluciones.analityco.fragments.RegisterListFragment
import co.tecno.sersoluciones.analityco.fragments.SettingFragment

import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : BaseActivity() {

    private var fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalles"
        replaceFragment(InfoFragment())
    }

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_detail)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                if (fragment is RegisterListFragment) replaceFragment(InfoFragment())
                else onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        this.fragment = fragment
    }

}
