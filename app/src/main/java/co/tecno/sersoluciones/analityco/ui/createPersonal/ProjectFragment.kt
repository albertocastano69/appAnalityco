package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentProjectListBinding
import co.tecno.sersoluciones.analityco.models.ProjectList
import co.tecno.sersoluciones.analityco.nav.CreatePersonalActivity
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.services.SER_LOCATION_ACTION
import co.tecno.sersoluciones.analityco.ui.createPersonal.adapters.DataItemListAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

private const val LOCATION_PERMISSION_REQUEST = 1
private const val LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"

class ProjectFragment : Fragment() {

    private lateinit var binding: FragmentProjectListBinding
    private var data = mutableListOf<ProjectList>()

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestLastLocationOrStartLocationUpdates()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProjectListBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Enrolar Personal"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = ""

        binding.layoutInfo.visibility = View.GONE

        val adapter = DataItemListAdapter(DataItemListAdapter.OnClickListener {
            val selectedProject = (it as ProjectList)
            logW("project ${selectedProject.Name}")
            viewModel.updateListProject(selectedProject)
        })
        binding.list.adapter = adapter
        val selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )"
        val selectionArgs = arrayOf("1")
        viewModel.getProjects(selection = selection, selectionArgs = selectionArgs)

        viewModel.projects.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                data = it.toMutableList()
                adapter.addSubmitList(it)
            }
        })

        viewModel.navigateToSelectTypeContractFragment.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController()
                    .navigate(
                        ProjectFragmentDirections.actionProjectFragmentToContractTypeFragment(viewModel.selectedProject.value!!)
                    )
                viewModel.doneNavigating()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(SER_LOCATION_ACTION)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)

    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            requestLastLocationOrStartLocationUpdates()
        ) {
            (activity as CreatePersonalActivity).startLocationService()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            requestLastLocationOrStartLocationUpdates()
        ) {
            (activity as CreatePersonalActivity).stopLocationService()
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra("location")) {
                val location = intent.getParcelableExtra<Location>("location")
                val currentLocation = LatLng(location!!.latitude, location.longitude)

                if (viewModel.projectId != null) {
                    data.firstOrNull { x -> x.Id == viewModel.projectId }?.let { selectedProject ->
                        viewModel.updateListProject(selectedProject)
                    }
                } else if (data.size == 1 && viewModel.currentLocation.value == null)
                    viewModel.updateListProject(data.first(), currentLocation)
                else
                    viewModel.setLocation(currentLocation)
            }

        }
    }

    /**
     * Show the user a dialog asking for permission to use location.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocationPermission() {
        requestPermissions(arrayOf(LOCATION_PERMISSION), LOCATION_PERMISSION_REQUEST)
    }

    /**
     * Request the last location of this device, if known, otherwise start location updates.
     * The last location is cached from the last application to request location.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLastLocationOrStartLocationUpdates(): Boolean {
        // if we don't have permission ask for it and wait until the user grants it
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return false
        }
        return true
    }

    /**
     * This will be called by Android when the user responds to the permission request.
     *
     * If granted, continue with the operation that the user gave us permission to do.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLastLocationOrStartLocationUpdates()
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.permissions_location_msg),
                        Snackbar.LENGTH_LONG // How long to display the message.
                    ).show()
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}