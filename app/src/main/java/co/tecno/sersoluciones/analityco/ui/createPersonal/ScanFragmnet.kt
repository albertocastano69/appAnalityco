package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.adapters.ViewBindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databinding.FragmentScanNavPersonBinding
import co.tecno.sersoluciones.analityco.individualContract.IinitiateOrderFragmentDirections
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.InfoUser
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.android.volley.Request
import com.google.android.gms.vision.barcode.Barcode
import com.google.gson.Gson
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class ScanFragmnet : Fragment(), RequestBroadcastReceiver.BroadcastListener {

    private var enableQR = false
    private var docNumber: Long? = null
    private lateinit var binding: FragmentScanNavPersonBinding
    private var TypeDocument = 0
    private var TypeDoc = "CC"
    var TypeIn: Boolean = true
    private var ProjectID: String? = null
    private var ContractID: String? = null
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    var PersonalEmployerInfoId: String? = null
    var PersoinBlackList = ""
    var personInContract: Boolean? = false
     var suspender: Boolean = true

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Enrolar Personal"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = ""

        (binding.typeDocumentAutoCompleteTextView as? TextView)?.text = "Cedula de Ciudadania"
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
            setOnClickListener {
                (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.width = 600

            }

            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as String
                TypeDocument = position
                TypeDoc = item
                MaxSizeDocumentNumber(item)
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
                        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.width =
                            255
                        TypeDoc = "CC"
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
                        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.width =
                            255
                        TypeDoc = "CE"
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
                        (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.width =
                            255
                        TypeDoc = "PEP"
                    }
                }
            }
        }
        binding.InputaLayoutTypeDocument.setEndIconOnClickListener {
            (binding.typeDocumentAutoCompleteTextView as? AutoCompleteTextView)?.width = 600
            binding.typeDocumentAutoCompleteTextView.showDropDown()

        }

        if (!viewModel.documentNumber.isNullOrEmpty()) {
            docNumber = viewModel.documentNumber!!.toLong()
            viewModel.fetchPersonal(viewModel.documentNumber!!)
        }

        binding.writeCc.visibility = View.GONE
        binding.write.visibility = View.VISIBLE
        binding.readBarcode.setOnClickListener {
            TypeIn = true
            val values = ContentValues()
            values.put("documentType", viewModel.TypeDocument)
            values.put("documentNumber", viewModel.documentNumber)
            val paramsQuery = HttpRequest.makeParamsInUrl(values)
            CRUDService.startRequest(
                activity, Constantes.PERSONAL_BLACK_LIST_INFO_URL,
                Request.Method.GET, paramsQuery, false
            )
        }
        binding.sendDocument.setOnClickListener {sendDocument() }
        viewModel.navigateToSelectContractFragment.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    if (it) {
                        findNavController()
                            .navigate(
                                ScanFragmnetDirections.actionScanFragmnetToDetailsPersonalFragment(
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
                            ScanFragmnetDirections.actionScanFragmnetToPersonalFragment(
                                null,
                                viewModel.infoUser,
                                docNumber!!,
                                TypeDoc
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

    fun MaxSizeDocumentNumber(item: String) {
        when (item) {
            "Cedula de Ciudadania" -> {
                binding.document.filters = arrayOf(InputFilter.LengthFilter(11))
            }
            "Cedula de Extrangeria" -> {
                binding.document.filters = arrayOf(InputFilter.LengthFilter(20))
            }
            "Permiso Especial de Permanencia" -> {
                binding.document.filters = arrayOf(InputFilter.LengthFilter(16))
            }
        }

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
            viewModel.scan = false

            TypeIn = false
            val values = ContentValues()
            values.put("documentType",TypeDoc)
            values.put("documentNumber", docNumber)
            logW("documentNumber: "+ viewModel.documentNumber)
            val paramsQuery = HttpRequest.makeParamsInUrl(values)
            CRUDService.startRequest(
                activity, Constantes.PERSONAL_BLACK_LIST_INFO_URL,
                Request.Method.GET, paramsQuery, false
            )
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
        logW("processCode: $processCode")
        if (processCode.isNotEmpty()) {
            binding.tvScanButtonError.error = null
            viewModel.infoUser = Gson().fromJson(processCode, InfoUser::class.java)
            docNumber = viewModel.infoUser!!.dni
            viewModel.fetchPersonal(docNumber.toString())
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

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {

        if (url == Constantes.PERSONAL_BLACK_LIST_INFO_URL) {
            PersoinBlackList = response!!
            logW("response: " + response)
            if (PersoinBlackList == "false") {
                if (TypeIn) {
                    scanDNI()
                } else {
                    val doc = binding.document.text.toString()
                    docNumber = doc.toLong()
                    viewModel.fetchPersonal(doc)
                    viewModel.scan = false
                }
            }
            if(PersoinBlackList == "true"){


                val alertDialogBuilder = AlertDialog.Builder(requireActivity())
                alertDialogBuilder.setTitle("Lo sentimos,")
                alertDialogBuilder.setMessage("No podemos tramitar su solicitud")
                val alertDialog = alertDialogBuilder.create()
                alertDialogBuilder.setNegativeButton("SALIR"){_,_ ->
                    alertDialog.dismiss()

                }


                alertDialog.show()



            }


        }


    }


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST)
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(requestBroadcastReceiver!!, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(requestBroadcastReceiver!!)
    }

}