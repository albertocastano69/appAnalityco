package co.tecno.sersoluciones.analityco.fragments.contract

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.facedetectorser.utilities.getBitmapFromUri
import co.com.sersoluciones.facedetectorser.utilities.getRealUriImage
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter
import co.tecno.sersoluciones.analityco.adapters.contracts.SpinnerAdapterImage
import co.tecno.sersoluciones.analityco.adapters.editContract.ContractCompaniesRecyclerAdapter
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.utilities.*
import com.android.volley.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class EditContract : Fragment(), BroadcastListener,
    ContractCompaniesRecyclerAdapter.OnEditContractCompany {
    private var unbinder: Unbinder? = null
    private var getJSONBroadcastReceiver: RequestBroadcastReceiver? = null

    @JvmField
    @BindView(R.id.spinner_employee_type)
    var contractType: Spinner? = null

    @JvmField
    @BindView(R.id.spinner_image)
    var spinnerImages: Spinner? = null

    @JvmField
    @BindView(R.id.edtt_review)
    var review: EditText? = null

    @JvmField
    @BindView(R.id.edtt_numContract)
    var numContract: EditText? = null
    private var mListener: OnEditContract? = null

    @JvmField
    @BindView(R.id.spinner_contract_type)
    var mRoleSpinner: Spinner? = null

    @JvmField
    @BindView(R.id.employerSelected)
    var employerSelected: RelativeLayout? = null

    @JvmField
    @BindView(R.id.list_users_recycler)
    var recyclerViewProjects: RecyclerView? = null

    @JvmField
    @BindView(R.id.list_employer_recycler)
    var recyclerViewEmployer: RecyclerView? = null

    @JvmField
    @BindView(R.id.btn_edit)
    var btnEdit: ImageView? = null

    @JvmField
    @BindView(R.id.label_validity)
    var labelValidity: TextView? = null

    @JvmField
    @BindView(R.id.text_active)
    var text_active: TextView? = null

    @JvmField
    @BindView(R.id.text_name)
    var text_name: TextView? = null

    @JvmField
    @BindView(R.id.text_sub_name)
    var text_sub_name: TextView? = null

    @JvmField
    @BindView(R.id.card_view_detail)
    var card_view_detail: CardView? = null

    @JvmField
    @BindView(R.id.logo)
    var logo_imag: ImageView? = null

    @JvmField
    @BindView(R.id.text_validity)
    var text_validity: TextView? = null
    private var ContractorId: String? = null
    private var companyIdContract: String? = null

    @JvmField
    @BindView(R.id.btn_edit_contract)
    var btnEdit_contract: Button? = null

    @JvmField
    @BindView(R.id.text_name_contract)
    var text_name_contract: TextView? = null

    @JvmField
    @BindView(R.id.text_sub_name_contract)
    var text_sub_name_contract: TextView? = null

    @JvmField
    @BindView(R.id.logo_contract)
    var logo_imag_contract: ImageView? = null

    @JvmField
    @BindView(R.id.see_contract)
    var seeContract: Button? = null

    @JvmField
    @BindView(R.id.send_contract)
    var sendContract: Button? = null

    @JvmField
    @BindView(R.id.file_contract)
    var fileContract: LinearLayout? = null

    @JvmField
    @BindView(R.id.name_contract)
    var nameContract: TextView? = null

    @JvmField
    @BindView(R.id.titleContratista)
    var titleContratista: LinearLayout? = null

    @JvmField
    @BindView(R.id.titleContratante)
    var titleContratante: LinearLayout? = null

    @JvmField
    @BindView(R.id.progress)
    var progress: ProgressBar? = null

    @JvmField
    @BindView(R.id.main_form)
    var mainForm: NestedScrollView? = null

    private var instance: Context? = null
    private var oldContract: Contract? = null
    private var imagesContract: ArrayList<Image>? = null
    private var typeSend = 0
    private var contracts: ArrayList<ContractType>? = null
    private var user: User? = null
    private var personalT: ArrayList<PersonalType>? = null
    private var typeEmployerId = 0
    private var mImageUri: Uri? = null
    private var removeAdminCompany = false
    private var employerData: String? = null

    //Set your layout here
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =
            inflater.inflate(R.layout.edit_contract_fragment, container, false)
        unbinder = ButterKnife.bind(this, v)
        instance = activity
        getJSONBroadcastReceiver = RequestBroadcastReceiver(this)
        if (arguments != null) {
            oldContract =
                arguments?.getSerializable(ARG_PARAM1) as Contract?
            DebugLog.logW(
                Gson().toJson(
                    oldContract
                )
            )
            review?.setText(oldContract?.ContractReview)
            numContract?.setText(oldContract?.ContractNumber)
            typeEmployerId = oldContract!!.PersonalTypeId
            companyIdContract = oldContract?.CompanyInfoId
            ContractorId = oldContract?.EmployerId
            fillForm()
        }
        val preferences = MyPreferences(v.context)
        val profile = preferences.profile
        user = Gson().fromJson(
            profile,
            User::class.java
        )

        mRoleSpinner?.visibility = View.GONE
        mRoleSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                i: Int,
                l: Long
            ) {
                if (mRoleSpinner!!.selectedItemPosition > 0) typeEmployerId =
                    personalT!![mRoleSpinner!!.selectedItemPosition - 1].Id
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        progress?.visibility = View.VISIBLE
        mainForm?.visibility = View.GONE
        sendAllRequest()
        return v
    }


    private fun sendAllRequest() {
        var values = ContentValues()
        GlobalScope.launch {
            CrudIntentService.startRequestCRUD(
                instance, Constantes.CREATE_CONTRACTTYPE,
                Request.Method.GET, "", "", true
            )

            delay(200)
            CrudIntentService.startRequestCRUD(
                instance, Constantes.CREATE_COMPANY_URL + user?.CompanyId + "/JoinCompanies/",
                Request.Method.GET, "", "", true
            )

            delay(200)
            CrudIntentService.startRequestCRUD(
                instance, Constantes.IMAGES_CONTRACT_URL,
                Request.Method.GET, "", "", true
            )

            delay(200)
            values = ContentValues()
            values.put("type", "PersonalType")
            CrudIntentService.startRequestCRUD(
                instance, Constantes.CREATE_COMMONOPTIONS,
                Request.Method.GET, "", HttpRequest.makeParamsInUrl(values), true
            )
            delay(200)
            values = ContentValues()
            values.put("companyId", user!!.CompanyId)
            CrudIntentService.startRequestCRUD(
                instance, Constantes.LIST_EMPLOYERS_URL,
                Request.Method.GET, "", HttpRequest.makeParamsInUrl(values), true
            )

        }
    }

    private fun dispatchPhotoSelectionIntent() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        this.startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR)
    }

    @OnClick(R.id.see_contract)
    fun seeContractButton() {
        if (oldContract!!.ContractFile == null) {
            MetodosPublicos.alertDialog(activity, "No existe contrato escaneado")
        } else {
            val url = Constantes.URL_IMAGES + oldContract!!.ContractFile
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    @OnClick(R.id.send_contract)
    fun sendContractButton() {
        dispatchPhotoSelectionIntent()
    }

    @OnClick(R.id.removeContract)
    fun removeContractButton() {
        fileContract!!.visibility = View.GONE
        mImageUri = null
        nameContract!!.text = ""
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> if (requestCode == REQUEST_IMAGE_SELECTOR) {
                val imageUri = data!!.data ?: return
                val bitmap = getBitmapFromUri(
                    requireActivity().applicationContext,
                    imageUri
                )
                mImageUri = getRealUriImage(requireActivity().applicationContext, bitmap!!, true)
                fileContract?.visibility = View.VISIBLE
                nameContract?.text = mImageUri?.lastPathSegment
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillForm() {
        labelValidity?.visibility = View.GONE
        text_active?.visibility = View.GONE
        //ContractorId= mItem.Id;
        text_name?.text = oldContract?.CompanyName
        text_sub_name?.text = "NIT " + oldContract?.CompanyDocumentNumber
        if (oldContract?.CompanyLogo != null) {
            val url = Constantes.URL_IMAGES + oldContract?.CompanyLogo
            Picasso.get().load(url)
                .resize(0, 250)
                .placeholder(R.drawable.image_not_available)
                .error(R.drawable.image_not_available)
                .into(logo_imag)
        }
        btnEdit?.setBackgroundResource(R.drawable.ic_list_remove)
        btnEdit_contract?.setBackgroundResource(R.drawable.ic_list_remove)
        text_name_contract?.text = oldContract?.ContractorName
        text_sub_name_contract?.text = "NIT " + oldContract?.ContractorDocumentNumber
        if (oldContract?.ContractorLogo != null) {
            val url = Constantes.URL_IMAGES + oldContract?.ContractorLogo
            Picasso.get().load(url)
                .resize(0, 250)
                .placeholder(R.drawable.image_not_available)
                .error(R.drawable.image_not_available)
                .into(logo_imag_contract)
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST)
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON)
        LocalBroadcastManager.getInstance(instance!!).registerReceiver(
            getJSONBroadcastReceiver!!,
            intentFilter
        )
    }

    @OnClick(R.id.btn_edit)
    fun removeCompan() {
        card_view_detail?.visibility = View.GONE
        recyclerViewProjects?.visibility = View.VISIBLE
    }

    @OnClick(R.id.btn_edit_contract)
    fun removeEmploye() {
        employerSelected?.visibility = View.GONE
        recyclerViewEmployer?.visibility = View.VISIBLE
        mRoleSpinner?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(instance!!).unregisterReceiver(getJSONBroadcastReceiver!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->                 //NavUtils.navigateUpFromSameTask(this);
                //onBackPressed();
                return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun submitRequest() {
        review?.error = null
        val contractReview = review?.text.toString()
        val contractNumber = numContract?.text.toString()
        var cancel = false
        var focusView: View? = null
        if (TextUtils.isEmpty(contractReview)) {
            review?.error = getString(R.string.error_field_required)
            focusView = review
            cancel = true
        } else if (contractType!!.selectedItemPosition <= 0) {
            focusView = contractType
            cancel = true
        } else if (contractType!!.selectedItemPosition <= 0) {
            focusView = contractType
            cancel = true
        } /*else if (typeEmployerId == 0) {
            focusView = mRoleSpinner;
            cancel = true;
        }*/
        if (cancel) {
            focusView?.requestFocus()
            //notifyIncomplete();
            return
        }
        val selectImage = spinnerImages!!.selectedItemPosition
        val nwContract =
            Contract(
                contractNumber,
                contractReview,
                imagesContract!![selectImage].Id,
                contracts!![contractType!!.selectedItemPosition - 1].Id,
                typeEmployerId,
                companyIdContract,
                ContractorId,
                imagesContract!![selectImage].Logo,
                oldContract?.MinHour,
                oldContract?.MaxHour
            )
        val gson = Gson()
        val json = gson.toJson(nwContract)
        DebugLog.logW(json)
        CrudIntentService.startRequestCRUD(
            activity,
            Constantes.LIST_CONTRACTS_URL + oldContract!!.Id,
            Request.Method.PUT,
            json,
            "",
            false
        )
    }

    private fun updateList(jsonObjStr: String?) {
//        Log.e("updateList", jsonObjStr)
        try {
            contracts = Gson().fromJson<ArrayList<ContractType>>(
                jsonObjStr,
                object : TypeToken<ArrayList<ContractType?>?>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().finish()
        }
        val contract = arrayOfNulls<String>(contracts!!.size + 1)
        contract[0] = "TIPO DE CONTRATO"
        for (i in 1..contracts!!.size) {
            contract[i] = contracts!![i - 1].Description
        }
        contractType?.adapter = CustomArrayAdapter(instance!!, contract)
        if (oldContract != null) {
            for (i in contracts!!.indices) {
                if (contracts!![i].Id == oldContract?.ContractTypeId) {
                    contractType?.setSelection(i + 1)
                    break
                }
            }
            if (oldContract!!.IsRegister) {
                configureOptionContract(oldContract!!.ValueContractType)
            }
        }
    }

    @OnClick(R.id.cancel_button)
    fun cancel() {
        if (mListener != null) {
            mListener?.onCancelActionEdit()
        }
    }

    @OnClick(R.id.create_button)
    fun send() {
        submitRequest()
    }

    private fun fillListImages(imagesList: String?) {
        imagesContract =
            Gson().fromJson<ArrayList<Image>>(
                imagesList,
                object :
                    TypeToken<ArrayList<Image?>?>() {}.type
            )
        val imageList =
            arrayOfNulls<String>(imagesContract!!.size)
        for (i in imagesContract!!.indices) {
            imageList[i] = imagesContract!![i].Logo
        }
        val adapter = SpinnerAdapterImage(
            activity,
            android.R.layout.simple_spinner_item,
            imageList
        )
        spinnerImages!!.adapter = adapter // Set the custom adapter to the spinner
        // You can create an anonymous listener to handle the event when is selected an spinner item
        val selection = 0
        if (oldContract != null) {
            for (i in imagesContract!!.indices) {
                if (imagesContract!![i].Id == oldContract!!.FormImageId) {
                    spinnerImages!!.setSelection(i)
                    break
                }
            }
        }
        spinnerImages?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View,
                position: Int, id: Long
            ) {
            }

            override fun onNothingSelected(adapter: AdapterView<*>?) {}
        }
    }

    interface OnEditContract {
        fun onCancelActionEdit()
        fun onApplyActionEdit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnEditContract) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {

        Log.e("option", option.toString() + "")
        when (option) {
            Constantes.SUCCESS_REQUEST -> if (mImageUri == null || mImageUri.toString()
                    .isEmpty() || mImageUri.toString() == ""
            ) {
                if (mListener != null) {
                    mListener?.onApplyActionEdit()
                }
            } else {
                if (mImageUri != null && !mImageUri.toString().isEmpty()) {
                    val params =
                        HashMap<String, String>()
                    params["file"] = mImageUri.toString()
                    val urlContract = Constantes.CONTRACT_FILE_URL + oldContract!!.Id
                    CrudIntentService.startActionFormData(
                        activity, urlContract,
                        Request.Method.PUT, params
                    )
                    mImageUri = null
                }
            }
            Constantes.REQUEST_NOT_FOUND -> {
            }
            Constantes.SEND_REQUEST -> {

                when (url) {
                    Constantes.CREATE_CONTRACTTYPE -> {
                        typeSend++
                        updateList(response)
                    }
                    Constantes.CREATE_COMPANY_URL + user?.CompanyId + "/JoinCompanies/" -> {
                        typeSend++
                        fillListCompanies(response)
                    }
                    Constantes.IMAGES_CONTRACT_URL -> {
                        typeSend++
                        fillListImages(response)
                    }

                    Constantes.CREATE_COMMONOPTIONS -> {
                        typeSend++
                        fillListPersonalType(response)
                    }
                    Constantes.LIST_EMPLOYERS_URL -> {
                        typeSend++
                        fillListEmployer(response)
                    }
                }
                if (typeSend >= 5) {
                    progress?.visibility = View.GONE
                    mainForm?.visibility = View.VISIBLE
                }
            }
            Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST -> {
            }
            Constantes.SUCCESS_FILE_UPLOAD -> if (mListener != null) {
                mListener!!.onApplyActionEdit()
            }
            Constantes.NOT_INTERNET -> {
                var dialog: AlertDialog? = null
                val builder = AlertDialog.Builder(requireActivity())
                    .setTitle("Error")
                    .setCancelable(false)
                    .setMessage("Sin conexion a internet")
                    .setPositiveButton("Aceptar") { _, _ ->
                        mListener?.onCancelActionEdit()
                    }
                dialog = builder.create()
                dialog.show()
            }
        }
    }

    /*private void msgSuccess() {
        MetodosPublicos.alertDialog(getContext(), "Proyecto creado satisfactoriamente");
    }*/
    private fun fillListCompanies(companies: String?) {
        val companyContract =
            Gson().fromJson<ArrayList<CompanyList?>>(
                companies,
                object : TypeToken<ArrayList<CompanyList?>?>() {}.type
            )
        if (companyContract.isEmpty()) {
            recyclerViewProjects?.visibility = View.GONE
            return
        }
        DebugLog.logW("flag $removeAdminCompany")
        if (removeAdminCompany) {
            var removeCompany: CompanyList? = CompanyList()
            var remove = false
            for (companyList in companyContract) {
                DebugLog.logW("compare companies " + "admin " + user!!.Name + "list: " + companyList?.Name)
                if (companyList?.DocumentNumber == user!!.DocumentNumber) {
                    removeCompany = companyList
                    remove = true
                }
            }
            if (remove) companyContract.remove(removeCompany)
            removeAdminCompany = false
        }

        //mLayoutManager.setAutoMeasureEnabled(true);
        val mLayoutManager = LinearLayoutManager(activity)
        recyclerViewProjects!!.layoutManager = mLayoutManager
        val adapterProjects =
            activity?.let {
                ContractCompaniesRecyclerAdapter(
                    it,
                    companyContract,
                    this,
                    false
                )
            }
        recyclerViewProjects!!.itemAnimator = DefaultItemAnimator()
        val mDividerItemDecoration = DividerItemDecoration(
            recyclerViewProjects!!.context,
            mLayoutManager.orientation
        )
        recyclerViewProjects!!.addItemDecoration(mDividerItemDecoration)
        recyclerViewProjects!!.adapter = adapterProjects
    }

    private fun fillListPersonalType(jsonObjStr: String?) {
        try {
            personalT = Gson().fromJson<ArrayList<PersonalType>>(
                jsonObjStr,
                object : TypeToken<ArrayList<PersonalType?>?>() {}.type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().finish()
        }
        val contract = arrayOfNulls<String>(personalT!!.size + 1)
        contract[0] = "TIPO "
        for (i in 1..personalT!!.size) {
            contract[i] = personalT!![i - 1].Description
        }
        mRoleSpinner?.adapter = CustomArrayAdapter(instance!!, contract)
        if (oldContract != null) {
            for (i in personalT!!.indices) {
                if (personalT!![i].Id == oldContract!!.PersonalTypeId) {
                    mRoleSpinner?.setSelection(i + 1)
                    typeEmployerId = personalT!![mRoleSpinner!!.selectedItemPosition - 1].Id
                    break
                }
            }
        }
    }

    private fun fillListEmployer(employer: String?) {
        employerData = employer
        val employersContract =
            Gson().fromJson<ArrayList<CompanyList?>>(
                employer,
                object : TypeToken<ArrayList<CompanyList?>?>() {}.type
            )
        if (employersContract.isEmpty()) {
            recyclerViewEmployer!!.visibility = View.GONE
            return
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        val mLayoutManager = LinearLayoutManager(activity)
        recyclerViewEmployer!!.layoutManager = mLayoutManager
        val adapterProjects =
            activity?.let {
                ContractCompaniesRecyclerAdapter(
                    it,
                    employersContract,
                    this,
                    true
                )
            }
        recyclerViewEmployer!!.itemAnimator = DefaultItemAnimator()
        val mDividerItemDecoration = DividerItemDecoration(
            recyclerViewEmployer!!.context,
            mLayoutManager.orientation
        )
        recyclerViewEmployer!!.addItemDecoration(mDividerItemDecoration)
        recyclerViewEmployer!!.adapter = adapterProjects
    }

    private fun removeEmployerCompanyItem(item: CompanyList) {
        DebugLog.logW("entra eliminar compañia de lista")
        var deleteItem: CompanyList? = null
        val employersContract =
            Gson().fromJson<ArrayList<CompanyList?>>(
                employerData,
                object : TypeToken<ArrayList<CompanyList?>?>() {}.type
            )
        if (employersContract != null) {
            for (i in employersContract.indices) {
                if (employersContract[i]?.Id == item.Id) deleteItem = employersContract[i]
            }
            if (deleteItem != null) employersContract.remove(deleteItem)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerViewEmployer!!.layoutManager = mLayoutManager
            val adapterProjects =
                activity?.let {
                    ContractCompaniesRecyclerAdapter(
                        it,
                        employersContract,
                        this,
                        true
                    )
                }
            recyclerViewEmployer!!.itemAnimator = DefaultItemAnimator()
            val mDividerItemDecoration = DividerItemDecoration(
                recyclerViewEmployer!!.context,
                mLayoutManager.orientation
            )
            recyclerViewEmployer!!.addItemDecoration(mDividerItemDecoration)
            recyclerViewEmployer!!.adapter = adapterProjects
            removeEmployer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    @OnClick(R.id.btn_edit)
    fun removeCompany() {
        card_view_detail!!.visibility = View.GONE
        recyclerViewProjects!!.visibility = View.VISIBLE
    }

    @OnClick(R.id.btn_edit_contract)
    fun removeEmployer() {
        employerSelected!!.visibility = View.GONE
        recyclerViewEmployer!!.visibility = View.VISIBLE
        mRoleSpinner!!.visibility = View.GONE
    }

    private fun configureOptionContract(type: String) {
        when (type) {
            "AD" -> {
                disableEditableItems()
                btnEdit!!.visibility = View.GONE
            }
            "CO" -> {
                disableEditableItems()
                card_view_detail!!.visibility = View.GONE
                titleContratante!!.visibility = View.GONE
            }
            "PR" -> {
                disableEditableItems()
                card_view_detail!!.visibility = View.GONE
                titleContratante!!.visibility = View.GONE
            }
            "AS" -> {
                disableEditableItems()
                removeAdminCompany = true
            }
            "FU" -> {
                disableEditableItems()
                card_view_detail!!.visibility = View.GONE
                titleContratante!!.visibility = View.GONE
            }
            "OT" -> {
                disableEditableItems()
                card_view_detail!!.visibility = View.GONE
                titleContratante!!.visibility = View.GONE
            }
            "VI" -> {
                disableEditableItems()
                card_view_detail!!.visibility = View.GONE
                titleContratante!!.visibility = View.GONE
            }
        }
    }

    private fun disableEditableItems() {
        contractType!!.isEnabled = false
        contractType!!.isEnabled = false
        review!!.isFocusable = false
        review!!.isClickable = false
        review!!.setTextColor(resources.getColor(R.color.gray))
        numContract!!.isFocusable = false
        numContract!!.isClickable = false
        numContract!!.setTextColor(resources.getColor(R.color.gray))
        employerSelected!!.visibility = View.GONE
        titleContratista!!.visibility = View.GONE
        mRoleSpinner!!.visibility = View.GONE
    }

    companion object {
        private const val REQUEST_IMAGE_SELECTOR = 199
        private const val ARG_PARAM1 = "param1"

        @JvmStatic
        fun newInstance(contract: Contract?): EditContract {
            val fragment = EditContract()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, contract)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCompanyCard(mItem: CompanyList) {
        btnEdit!!.setBackgroundResource(R.drawable.ic_list_remove)
        card_view_detail!!.visibility = View.VISIBLE
        recyclerViewProjects!!.visibility = View.GONE
        labelValidity!!.visibility = View.GONE
        text_validity!!.text = mItem.Address
        text_active!!.visibility = View.GONE
        companyIdContract = mItem.Id
        text_name!!.text = mItem.Name
        text_sub_name!!.text = mItem.DocumentType + " " + mItem.DocumentNumber
        if (mItem.Logo != null) {
            val url = Constantes.URL_IMAGES + mItem.Logo
            Picasso.get().load(url)
                .resize(0, 250)
                .placeholder(R.drawable.image_not_available)
                .error(R.drawable.image_not_available)
                .into(logo_imag)
        }
        removeEmployerCompanyItem(mItem)
    }

    @SuppressLint("SetTextI18n")
    override fun onEmployerCard(mItem: CompanyList) {
        btnEdit_contract!!.setBackgroundResource(R.drawable.ic_list_remove)
        recyclerViewEmployer!!.visibility = View.GONE
        ContractorId = mItem.Id
        employerSelected!!.visibility = View.VISIBLE
        text_name_contract!!.text = mItem.Name
        text_sub_name_contract!!.text = mItem.DocumentType + " " + mItem.DocumentNumber
        if (mItem.Logo != null) {
            val url = Constantes.URL_IMAGES + mItem.Logo
            Picasso.get().load(url)
                .resize(0, 250)
                .placeholder(R.drawable.image_not_available)
                .error(R.drawable.image_not_available)
                .into(logo_imag_contract)
        }
    }
}