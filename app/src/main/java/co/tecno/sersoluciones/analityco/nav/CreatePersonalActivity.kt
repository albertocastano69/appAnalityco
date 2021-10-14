package co.tecno.sersoluciones.analityco.nav

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.BaseActivity
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databinding.ActivityCreatePersonalBinding
import co.tecno.sersoluciones.analityco.fragments.personal.PersonalListFragment
import co.tecno.sersoluciones.analityco.models.ProjectList
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.retrofit.RETROFIT_TIME_OUT_EXCEPTION
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.LocationUpdateService
import co.tecno.sersoluciones.analityco.ui.createPersonal.SelectNavContractFragment
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter
import co.tecno.sersoluciones.analityco.views.ClearebleEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.content_create_personal.*
import javax.inject.Inject


class CreatePersonalActivity : BaseActivity(), AdapterView.OnItemSelectedListener, RequestBroadcastReceiver.BroadcastListener, TextWatcherAdapter.TextWatcherListener {

    private var searchToolbar: LinearLayout? = null
    private var editTextSearch: ClearebleEditText? = null
    @Inject
    lateinit var viewModel: CreatePersonalViewModel
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null

    @Inject
    lateinit var personalViewModel: PersonalViewModel
    private lateinit var binding: ActivityCreatePersonalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationContext.analitycoComponent.inject(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_create_personal
        )
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        searchToolbar = findViewById<LinearLayout?>(R.id.search_container)
        editTextSearch = findViewById(R.id.search_edit_text)
        binding.searchEditText.addTextChangedListener(TextWatcherAdapter(binding.searchEditText, this))
        personalViewModel.getData()
        requestBroadcastReceiver = RequestBroadcastReceiver(this)

        intent.extras?.apply {
            if (containsKey("documentNumber"))
                viewModel.documentNumber = getString("documentNumber")
            if (containsKey("infoUser"))
                viewModel.infoUser = getSerializable("infoUser") as DecodeBarcode.InfoUser
            if (containsKey("projectId"))
                viewModel.projectId = getString("projectId")
            viewModel.externalData = true
        }

        binding.contentMain.spinnerBase.onItemSelectedListener = this
        viewModel.projects.observe(this, androidx.lifecycle.Observer {
            it?.let {
                val adapter = ArrayAdapter(this, R.layout.simple_spinner_item_project, it)
                binding.contentMain.spinnerBase.adapter = adapter
                if (viewModel.selectedProject.value != null)
                    binding.contentMain.spinnerBase.setSelection(it.indexOf(viewModel.selectedProject.value!!))

            }

        })

        binding.contentMain.spinnerBaseWithSearch.onItemSelectedListener = this
        viewModel.projects.observe(this, androidx.lifecycle.Observer {
            it?.let {
                val adapter = ArrayAdapter(this, R.layout.simple_spinner_item_project, it)
                binding.contentMain.spinnerBaseWithSearch.adapter = adapter
                if (viewModel.selectedProject.value != null)
                    binding.contentMain.spinnerBaseWithSearch.setSelection(it.indexOf(viewModel.selectedProject.value!!))
            }

        })

        viewModel.selectedProject.observe(this, androidx.lifecycle.Observer { projectList ->
            projectList?.let {
                var i = 0
                for (project in viewModel.projects.value!!) {
                    if (projectList.Name == project.Name) {
                        logW("project found ${projectList.Name} position $i")
                        binding.contentMain.spinnerBase.setSelection(i)
                        binding.contentMain.spinnerBaseWithSearch.setSelection(i)
                        break
                    }
                    i++
                }
            }
        })

        findNavController(R.id.nav_host_fragment)
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.companyFragment,
                    R.id.projectFragment,
                    R.id.detailsPersonalFragment,
                    R.id.personalFragment,
                    R.id.selectNavEmployerFragment,
                    R.id.joinPersonalNavContractFragment,
                    R.id.requerimentsFragment -> {
                        binding.contentMain.layoutSpinner.visibility = View.GONE
                        binding.contentMain.layoutSpinnerWithSearch.visibility = View.GONE
                        binding.searchContainer.visibility = View.GONE

                    }
                    R.id.selectNavContractFragment -> {
                        binding.contentMain.layoutSpinner.visibility = View.GONE
                        binding.contentMain.layoutSpinnerWithSearch.visibility = View.VISIBLE
                        binding.contentMain.searchEmployer.setOnClickListener {
                            binding.contentMain.layoutSpinnerWithSearch.visibility = View.GONE
                            expand()
                        }
                    }
                    else -> {
                        binding.contentMain.layoutSpinner.visibility = View.VISIBLE
                    }
                }

            }
    }

    private fun expand() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            circleReveal(true)
        }else{
            binding.searchContainer.visibility = View.GONE
            val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
            val translateAnimation: Animation = TranslateAnimation(0.0f, 0.0f, 0.0f, (-binding.searchContainer.height).toFloat())
            val animationSet = AnimationSet(true)
            animationSet.addAnimation(alphaAnimation)
            animationSet.addAnimation(translateAnimation)
            animationSet.duration = 220
            binding.searchContainer.startAnimation(animationSet)
        }
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }
    @SuppressLint("PrivateResource")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun circleReveal(isShow: Boolean) {
        var width = binding.toolbar.width
        width -= resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2
        val cx = width
        val cy = toolbar!!.height / 2
        val anim: Animator = if (isShow) ViewAnimationUtils.createCircularReveal(binding.searchContainer, cx, cy, 0f, width.toFloat()) else ViewAnimationUtils.createCircularReveal(binding.searchContainer, cx, cy, width.toFloat(), 0f)
        anim.duration = 250L

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isShow) {
                    super.onAnimationEnd(animation)
                    binding.searchContainer.visibility = View.GONE
                }
            }
        })

        // make the view visible and start the animation
        if (isShow) {
            binding.searchContainer.visibility = View.VISIBLE
        }

        // start the animation
        anim.start()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isShow) {
                    binding.contentMain.layoutSpinnerWithSearch.visibility = View.VISIBLE
                }
            }
        })
    }
    fun collapse(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circleReveal(false)
        } else {
            binding.searchContainer.visibility = View.GONE
            val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
            val translateAnimation: Animation = TranslateAnimation(0.0f, 0.0f, 0.0f, (-binding.searchContainer.height).toFloat())
            val animationSet = AnimationSet(true)
            animationSet.addAnimation(alphaAnimation)
            animationSet.addAnimation(translateAnimation)
            animationSet.duration = 220
            binding.searchContainer.startAnimation(animationSet)
        }
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
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

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_company_list)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            alertClose()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertClose() {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle("Atencion")
            .setMessage("¿Desea salir sin crear este empleado?")
            .setPositiveButton("Aceptar") { _, _ ->
                viewModel.clearForm()
                personalViewModel.clearData()
                finish()
            }
            .setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent!!.adapter.getItem(position) as ProjectList
        viewModel.setSelectedProject(item)
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

    fun startLocationService() {
        checkLocationSettings {
            if (!it) {
                Toast.makeText(this, "Por favor prenda el GPS y vuelva a ingresar a este modulo", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    fun stopLocationService() {
        stopService(Intent(this, LocationUpdateService::class.java))
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (action) {
            CRUDService.ACTION_REQUEST_SAVE -> {
                when (url) {
                    Constantes.LIST_CONTRACTS_URL -> {
                        DebugLog.log("tabla actualizada LIST_CONTRACTS_URL")
                    }
                    Constantes.LIST_EMPLOYERS_URL -> {
                        DebugLog.log("tabla llena LIST_EMPLOYERS_URL")
                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        DebugLog.log("tabla llena LIST_PROJECTS_URL")
                    }
                }
                viewModel.verifyInitialDataInDB()
            }

        }
    }

    override fun onTextChanged(view: EditText?, text: String?) {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment as NavHostFragment?
        val fragmentManager: FragmentManager = navHostFragment!!.childFragmentManager
        val fragment : Fragment = fragmentManager.primaryNavigationFragment!!
        if(fragment is  SelectNavContractFragment){
            fragment.Filtro(text!!)
        }
    }
}