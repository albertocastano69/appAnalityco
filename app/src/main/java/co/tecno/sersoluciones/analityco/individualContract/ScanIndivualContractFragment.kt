package co.tecno.sersoluciones.analityco.individualContract

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.RegisterActivity
import co.tecno.sersoluciones.analityco.databinding.FragmentScanNavPersonBinding
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.google.android.gms.vision.barcode.Barcode
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import javax.inject.Inject


class ScanIndivualContractFragment : Fragment() {
    private var enableQR = false
    private var docNumber: Long? = null
    private lateinit var binding: FragmentScanNavPersonBinding
    private var TypeDocument = 0
    private var TypeDoc = "CC"

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableQR = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanNavPersonBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Buscar Empleado"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = ""

        (binding.typeDocumentAutoCompleteTextView as? TextView)?.text = "CC"
        val itemsTypeDocument = listOf(
            "Cedula de Ciudadania",
            "Cedula de Extranjeria",
            "Permiso Especial de Permanecia"
        )

        val adapterTypeDocument =
            ArrayAdapter(requireContext(), R.layout.list_item, itemsTypeDocument)
        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(
            adapterTypeDocument
        )

        binding.typeDocumentAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as String
                TypeDocument = position
                TypeDoc = item

                when (item) {
                    "Cedula de Ciudadania" -> {
                        (binding.typeDocumentAutoCompleteTextView as? TextView)?.text = "CC"
                        val itemsTypeDocument = listOf(
                            "Cedula de Ciudadania",
                            "Cedula de Extranjeria",
                            "Permiso Especial de Permanecia"
                        )
                        val adapterTypeDocument =
                            ArrayAdapter(requireContext(), R.layout.list_item, itemsTypeDocument)
                        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(
                            adapterTypeDocument
                        )
                    }
                    "Cedula de Extranjeria" -> {
                        (binding.typeDocumentAutoCompleteTextView as? TextView)?.text = "CE"
                        val itemsTypeDocument = listOf(
                            "Cedula de Ciudadania",
                            "Cedula de Extranjeria",
                            "Permiso Especial de Permanecia"
                        )
                        val adapterTypeDocument =
                            ArrayAdapter(requireContext(), R.layout.list_item, itemsTypeDocument)
                        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(
                            adapterTypeDocument
                        )
                    }
                    "Permiso Especial de Permanecia" -> {
                        (binding.typeDocumentAutoCompleteTextView as? TextView)?.text = "PEP"
                        val itemsTypeDocument = listOf(
                            "Cedula de Ciudadania",
                            "Cedula de Extranjeria",
                            "Permiso Especial de Permanecia"
                        )
                        val adapterTypeDocument =
                            ArrayAdapter(requireContext(), R.layout.list_item, itemsTypeDocument)
                        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(
                            adapterTypeDocument
                        )
                    }

                }

            }
        }
        if (!viewModel.documentNumber.isNullOrEmpty()) {
            docNumber = viewModel.documentNumber!!.toLong()
            viewModel.fetchPersonal(viewModel.documentNumber!!)
        }
        binding.writeCc.visibility = View.GONE
        binding.write.visibility = View.VISIBLE
        binding.readBarcode.setOnClickListener { scanDNI() }
        binding.sendDocument.setOnClickListener { sendDocument() }

        viewModel.navigateToSelectContractFragment.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    if (it) {

                        findNavController()
                            .navigate(
                                ScanIndivualContractFragmentDirections.actionScanIndividualContractFragmnetToDetailsPersonalFragment2(
                                    viewModel.personal.value!!
                                )
                            )
                        viewModel.doneNavigatingPersonal()
                    }
                }
            })
        viewModel.personalNotFound.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    findNavController()
                        .navigate(
                            ScanIndivualContractFragmentDirections.actionScanIndividualContractFragmnetToPersonalIndividualContractFragment(
                                null,
                                viewModel.infoUser,
                                docNumber!!,
                                ""
                            )
                        )
                    viewModel.clearFlagPersonal()
                }
            }
        })

        binding.fabBack.setOnClickListener { findNavController().popBackStack() }
    }


    fun scanDNI() {
        startActivityForResult(Intent(activity, BarcodeDecodeSerActivity::class.java), 0)
    }

    fun sendDocument() {
        if (binding.document.text.toString().isEmpty()) {
            binding.document.error = "Ingrese documento"
        } else {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.document.windowToken, 0)
            val doc = binding.document.text.toString()
            docNumber = doc.toLong()
            viewModel.findPersonalToIndividualContract(TypeDoc, doc)
            viewModel.TypeDocument = TypeDoc
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            BarcodeDecodeSerActivity.SUCCESS -> if (data!!.hasExtra("barcode")) {
                val barcodeRes = data.getStringExtra("barcode")
                val barcodeType = data.getIntExtra("barcode-type", 0)
                val texto = barcodeRes.trim { it <= ' ' }.replace(
                    String(
                        byteArrayOf(
                            0xEF.toByte(),
                            0xBF.toByte(),
                            0xBD.toByte(),
                            0xEF.toByte(),
                            0xBF.toByte(),
                            0xBD.toByte()
                        )
                    ).toRegex(), String(byteArrayOf(0x00.toByte()))
                )
                //Toast.makeText(activity, texto, Toast.LENGTH_LONG).show()
                val bytes: ByteArray = texto.toByteArray(StandardCharsets.UTF_8)
                for (byte in bytes) {
                    println(byte)
                }
                //val texto1= String(bytes, StandardCharsets.UTF_8)
                when (barcodeType) {
                    Barcode.QR_CODE ->
                        if (!enableQR) {
                            Toast.makeText(
                                activity,
                                "Codigo PDF417 no detectado, por favor escanee el codigo de " +
                                        "barras que se encuentra al respaldo de la cedula",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    Barcode.PDF417 -> {
                        DebugLog.log("CODE PDF417 DETECTED")
                        processBarcodePDF417(barcodeRes!!)
                    }
                }
            }
        }
    }

    private fun processBarcodePDF417(barcodeRes: String) {
        val processCode = DecodeBarcode.processBarcode(barcodeRes)
        DebugLog.logW("processCode: $processCode")
        if (processCode.isNotEmpty()) {
            binding.tvScanButtonError.error = null
            viewModel.infoUser = Gson().fromJson(processCode, DecodeBarcode.InfoUser::class.java)
            docNumber = viewModel.infoUser!!.dni
            val DocType = viewModel.infoUser!!.documentType
            viewModel.findPersonalToIndividualContract(DocType, docNumber.toString())
            viewModel.scan = true
        } else {
            MetodosPublicos.alertDialog(
                activity,
                "No se pudo procesar el codigo de barras, intente de nuevo."
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}