package co.tecno.sersoluciones.analityco.createCar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.AdapaterCountry
import co.tecno.sersoluciones.analityco.databinding.FragmentScanNavCarBinding
import co.tecno.sersoluciones.analityco.databinding.FragmentScanNavPersonBinding
import co.tecno.sersoluciones.analityco.nav.CreateCarViewModel
import javax.inject.Inject

class scanCarFragment:Fragment() {
    private var enableQR = false
    private var docNumber: Long? = null
    private lateinit var binding: FragmentScanNavCarBinding

    @Inject
    lateinit var viewModel: CreateCarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentScanNavCarBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val itemsCountryCar = arrayOf("COLOMBIA", "VENEZUELA")
        val imagesCountryCar: IntArray = intArrayOf(R.drawable.ic_flag_of_colombia,R.drawable.ic_flag_of_venezuela)
        var adapter = AdapaterCountry(requireContext(), itemsCountryCar,imagesCountryCar)
        (binding.countryCar as? AutoCompleteTextView)?.setAdapter(adapter)
    }
}