package co.tecno.sersoluciones.analityco.individualContract

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.BaseActivity
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databinding.ActivityCreateIndividualContractBinding
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.retrofit.RETROFIT_TIME_OUT_EXCEPTION
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class CreateIndividualContractActivity : BaseActivity(), RequestBroadcastReceiver.BroadcastListener {
    @Inject
    lateinit var viewModel: CreatePersonalViewModel
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null

    @Inject
    lateinit var personalViewModel: PersonalViewModel
    private lateinit var binding: ActivityCreateIndividualContractBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationContext.analitycoComponent.inject(this)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_create_individual_contract
        )
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel



        requestBroadcastReceiver = RequestBroadcastReceiver(this)

        findNavController(R.id.nav_host_fragment_indivual_contract)
                .addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.EmployerContract -> {
                        }
                        else -> {
                        }
                    }

                }
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                RETROFIT_TIME_OUT_EXCEPTION -> alertNotConnection()
            }

        }
    }
    private fun alertNotConnection() {
        val builder = MaterialAlertDialogBuilder(this)
                .setTitle("Atencion")
                .setMessage("Sin conexion con el servidor, revise su conexion a internet.")
                .setCancelable(false)
                .setPositiveButton("Aceptar") { _, _ ->
                    viewModel.clearForm()
                    personalViewModel.clearData()
                    finish()
                }
        builder.create().show()
    }

    private fun alertClose() {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle("Atencion")
            .setMessage("¿Desea salir sin terminar el proceso?")
            .setPositiveButton("Aceptar") { _, _ ->
                viewModel.clearForm()
                personalViewModel.clearData()
                finish()
            }
            .setNegativeButton("Cancelar", null)
        builder.create().show()
    }


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver!!, intentFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(RETROFIT_TIME_OUT_EXCEPTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver!!)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            alertClose()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun attachLayoutResource() {
    }


    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (action) {
            CRUDService.ACTION_REQUEST_SAVE -> {
                when (url) {
                    Constantes.LIST_CONTRACTS_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla actualizada LIST_CONTRACTS_URL")
                    }
                    Constantes.LIST_EMPLOYERS_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_EMPLOYERS_URL")
                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_PROJECTS_URL")
                    }
                }
                //viewModel.verifyInitialDataInDB()
            }
        }
    }
}