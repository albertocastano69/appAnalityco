package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.databinding.FragmentDetailsPersonalBinding
import co.tecno.sersoluciones.analityco.nav.CreatePersonalActivity
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import javax.inject.Inject

class DetailsPersonalFragment : Fragment() {

    private lateinit var binding: FragmentDetailsPersonalBinding

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsPersonalBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val arguments = DetailsPersonalFragmentArgs.fromBundle(requireArguments())

        val personal = arguments.personal
        binding.personal = personal

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "${personal.DocumentType} ${personal.DocumentNumber}"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "${personal.Name} ${personal.LastName}"

        binding.iconEditMainForm.setOnClickListener {
            findNavController()
               .navigate(
                    DetailsPersonalFragmentDirections.actionDetailsPersonalFragmentToPersonalFragment(
                        personal,
                        null,
                        personal.DocumentNumber!!.toLong(),
                        personal.DocumentType!!

                    )
                )
        }
        binding.controlButtons.nextButton.setOnClickListener {
            findNavController()
                .navigate(
                    DetailsPersonalFragmentDirections.actionDetailsPersonalFragmentToSelectNavContractFragment()
                )
        }
        binding.controlButtons.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}