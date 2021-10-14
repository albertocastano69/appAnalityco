package co.tecno.sersoluciones.analityco.individualContract

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.adapters.AdapterPlaceToJob
import co.tecno.sersoluciones.analityco.callback.OnListPlaceJobListener
import co.tecno.sersoluciones.analityco.databinding.FragmentPlaceToJobBinding
import co.tecno.sersoluciones.analityco.models.PlaceToJob
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.android.volley.Request
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.json.JSONArray
import javax.inject.Inject

class PlaceToJobFragment: Fragment(), RequestBroadcastReceiver.BroadcastListener {

    private lateinit var binding: FragmentPlaceToJobBinding

    @Inject
    lateinit var viewModel: CreatePersonalViewModel
    @Inject
    lateinit var personalViewModel: PersonalViewModel
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var mValues:ArrayList<PlaceToJob>? = null
    private var Adapter : AdapterPlaceToJob? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                alertClose()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    private fun alertClose() {
        val builder = MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Atencion")
                .setMessage("¿Desea cancelar este proceso?")
                .setPositiveButton("Aceptar") { _, _ ->
                    viewModel.clearForm()
                    personalViewModel.clearData()
                    activity?.finish()
                }
                .setNegativeButton("Cancelar", null)
        builder.create().show()
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaceToJobBinding.inflate(inflater)
        binding.lifecycleOwner = this
        mValues = ArrayList()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.fetchContractsByPersonal()
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Seleccione el lugar de trabajo"

       CRUDService.startRequest(
                activity, Constantes.CONTRACT_SELECT_CONTRACTING_URL+viewModel.EmployerId,
                Request.Method.GET, "", false
       )
        showProgress(true)
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                if (url == Constantes.CONTRACT_SELECT_CONTRACTING_URL + viewModel.EmployerId) {
                    UpdateList(response)
                }

            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun UpdateList(response: String?) {
        var jsonArray = JSONArray(response)
        mValues!!.clear()
        for (i in 0 until jsonArray.length()){
            val mItem =
                    Gson().fromJson(
                            jsonArray.getJSONObject(i).toString(),
                            PlaceToJob::class.java
                    )
            mValues!!.add(mItem)
        }
        Adapter = AdapterPlaceToJob(activity!!,mValues!!, OnListPlaceJobListener {
            findNavController().navigate(
                    PlaceToJobFragmentDirections.actionPlaceToJobFragmentToIinitiateOrderFragment(it)
            )
            viewModel.ItemPlaceJob = it
        })
        binding.contracts.adapter = Adapter
        showProgress(false)
    }
    override fun onResume() {
        super.onResume()
        var intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(requestBroadcastReceiver!!, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver!!)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
        binding.mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                if (show) 1.toFloat() else 0.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }
}