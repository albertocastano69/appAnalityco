package co.tecno.sersoluciones.analityco.createCar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.CarViewModel
import co.tecno.sersoluciones.analityco.databinding.FragmentNavCompanyBinding
import co.tecno.sersoluciones.analityco.models.CompanyList
import co.tecno.sersoluciones.analityco.nav.CreateCarViewModel
import co.tecno.sersoluciones.analityco.ui.createPersonal.adapters.DataItemListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class CompanyFragmentCar : Fragment() {

    @Inject
    lateinit var viewModel: CreateCarViewModel

    @Inject
    lateinit var personalViewModel: CarViewModel

    private lateinit var binding: FragmentNavCompanyBinding

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
                    //viewModel.clearForm()
                    //personalViewModel.clearData()
                    activity?.finish()
                }
                .setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNavCompanyBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Enrolar Vehiculo"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = ""

        viewModel.verifyInitialDataInDB()
        val adapter = DataItemListAdapter(DataItemListAdapter.OnClickListener {
            val company = it as CompanyList
            viewModel.navigateToJoinProject(company)
        })
        binding.companies.adapter = adapter

        viewModel.companies.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.addSubmitList(it)
            }
        })

        viewModel.navigateToSelectProjectFragment.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    findNavController()
                            .navigate(
                                    CompanyFragmentCarDirections.actionCompanyFragmentToProjectFragment()
                            )
                   viewModel.doneNavigatingCompany()
                }
            }
        })

        viewModel.initialData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) viewModel.getCompanies()
            }
        })
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}