package co.tecno.sersoluciones.analityco.fragments.personal.wizard

import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.*
import co.tecno.sersoluciones.analityco.adapters.employer.EmployerRecyclerViewAdapter
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.models.ContractEnrollment
import co.tecno.sersoluciones.analityco.models.Employer
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.models.PersonalContract
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter.TextWatcherListener
import co.tecno.sersoluciones.analityco.utilities.Utils.cursorToJObject
import com.android.volley.Request
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.util.*
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
class SelectEmployerFragment : Fragment(), TextWatcherListener, BroadcastListener {
    private var mValues: ArrayList<ObjectList>? = null

    @JvmField
    @BindView(R.id.employers)
    var recyclerViewCompanies: RecyclerView? = null

    @JvmField
    @BindView(R.id.enableSearchBtn)
    var enableSearchBtn: Button? = null

    @JvmField
    @BindView(R.id.search_edit_text)
    var searchEditText: EditText? = null

    @JvmField
    @BindView(R.id.logo)
    var logo: ImageView? = null

    @JvmField
    @BindView(R.id.text_name)
    var mNameView: TextView? = null

    @JvmField
    @BindView(R.id.text_address)
    var mAddressView: TextView? = null

    @JvmField
    @BindView(R.id.text_validity)
    var mValidityView: TextView? = null

    @JvmField
    @BindView(R.id.label_validity)
    var label_validity: TextView? = null

    @JvmField
    @BindView(R.id.label_active)
    var label_active: TextView? = null

    @JvmField
    @BindView(R.id.state_icon)
    var stateIcon: ImageView? = null
    private var adapter: EmployerRecyclerViewAdapter? = null
    private var mListener: OnListEmployerInteractionListener? = null
    private var contract: ContractEnrollment? = null
    private var moveToPersonal = false
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var personalContract: PersonalContract? = null
    private var employerId: String? = null
    private var originalEmployerId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        employerId = ""
        mValues = ArrayList()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_POST)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            requestBroadcastReceiver!!,
            intentFilter
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_select_employer, container, false)
        val unbinder = ButterKnife.bind(this, view)
        searchEditText!!.addTextChangedListener(TextWatcherAdapter(searchEditText, this))
        updateListEmployers()
        moveToPersonal = false
        return view
    }

    fun fillData(contract: ContractEnrollment, moveToPersonal: Boolean, personalContract: PersonalContract?) {
        val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
        val selectionArgs = arrayOf("1")
        val bundle = Bundle()
        bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
        bundle.putString(Constantes.KEY_SELECTION, selection)
        updateList(bundle)
        this.contract = contract
        this.personalContract = personalContract
        this.moveToPersonal = moveToPersonal
        originalEmployerId = contract.EmployerId
        fillData(contract.EmployerId)
    }

    fun fillData(employerId: String?) {
        this.employerId = employerId
        val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_SERVER_ID + " = ? )"
        val selectionArgs = arrayOf(employerId)
        val cursor = requireActivity().contentResolver
            .query(Constantes.CONTENT_EMPLOYER_URI, null, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val mItem =
                Gson().fromJson(cursorToJObject(cursor).toString(), Employer::class.java)
            mNameView!!.text = mItem.Name
            mAddressView!!.text = String.format("%s %s", mItem.DocumentType, mItem.DocumentNumber)
            mValidityView!!.text = mItem.Address
            label_validity!!.visibility = View.GONE
            label_active!!.visibility = View.GONE
            if (mItem.IsActive) {
                if (mItem.Expiry) stateIcon!!.setImageResource(R.drawable.state_icon) else stateIcon!!.visibility = View.GONE
            } else {
                stateIcon!!.setImageResource(R.drawable.state_icon_red)
            }
            val url = Constantes.URL_IMAGES + mItem.Logo
            val format = url.split(Pattern.quote(".").toRegex()).toTypedArray()
            if (format[format.size - 1] == "svg") {
                GlideApp.with(requireActivity())
                    .`as`(PictureDrawable::class.java)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.image_not_available)
                    )
                    .fitCenter()
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .load(url)
                    .into(logo!!)
            } else {
                Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(logo)
            }
        }
    }

    private fun updateListEmployers() {
        val mLayoutManager = LinearLayoutManager(activity)
        recyclerViewCompanies!!.layoutManager = mLayoutManager
        recyclerViewCompanies!!.itemAnimator = DefaultItemAnimator()
        adapter = EmployerRecyclerViewAdapter(requireActivity(), mListener, mValues!!)
        recyclerViewCompanies!!.adapter = adapter
        recyclerViewCompanies!!.visibility = View.GONE
        val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
        val selectionArgs = arrayOf("1")
        val bundle = Bundle()
        bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
        bundle.putString(Constantes.KEY_SELECTION, selection)
        updateList(bundle)
    }

    private fun updateList(args: Bundle?) {
        var orderBy: String? = null
        var selection: String? = null
        var selectionArgs: Array<String?>? = null
        if (args != null) {
            if (args.containsKey(Constantes.KEY_ORDER_BY)) orderBy = args.getString(Constantes.KEY_ORDER_BY)
            if (args.containsKey(Constantes.KEY_SELECTION_ARGS)) selectionArgs = args.getStringArray(Constantes.KEY_SELECTION_ARGS)
            if (args.containsKey(Constantes.KEY_SELECTION)) selection = args.getString(Constantes.KEY_SELECTION)
        }
        val cursor =
            requireActivity().contentResolver.query(Constantes.CONTENT_EMPLOYER_URI, null, selection, selectionArgs, orderBy)
        mValues!!.clear()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val mItem =
                        Gson().fromJson(
                            cursorToJObject(cursor).toString(),
                            ObjectList::class.java
                        )
                    mValues!!.add(mItem)
                } while (cursor.moveToNext())
            }
            adapter!!.swap()
            cursor.close()
        }
    }

    @OnClick(R.id.enableSearchBtn)
    fun enableSearch() {
        enableSearchBtn!!.visibility = View.GONE
        searchEditText!!.visibility = View.VISIBLE
        recyclerViewCompanies!!.visibility = View.VISIBLE
    }

    override fun onTextChanged(view: EditText, text: String) {
        syncData(text)
    }

    @OnClick(R.id.cancel_action)
    fun cleanSearch() {
        syncData(null)
        searchEditText!!.setText("")
    }

    fun submit() {
        if (activity is JoinPersonalWizardActivity) (activity as JoinPersonalWizardActivity?)!!.showProgress(
            true
        ) else if (activity is JoinPersonalProjectWizardActivity) (activity as JoinPersonalProjectWizardActivity?)!!.showProgress(true)
        var url = Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/PersonalInfo/"
        if (moveToPersonal) {
            url = Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/MovePersonalInfo/"
        }
        personalContract!!.EmployerId = employerId
        if (originalEmployerId == employerId) personalContract!!.EmployerId = null
        val json = Gson().toJson(personalContract)
        DebugLog.logW(json)
        CRUDService.startRequest(
            activity, url,
            Request.Method.POST, json
        )
    }

    private fun syncData(item: String?) {
        var item = item
        if (item != null) {
            var selection = ("((" + DBHelper.EMPLOYER_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_DOC_NUM + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_ADDRESS + " like ?))")
            selection += " AND (" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
            item = String.format("%%%s%%", item)
            val selectionArgs = arrayOf(item, item, item, "1")
            val bundle = Bundle()
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
            bundle.putString(Constantes.KEY_SELECTION, selection)
            updateList(bundle)
        } else {
            val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
            val selectionArgs = arrayOf("1")
            val bundle = Bundle()
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
            bundle.putString(Constantes.KEY_SELECTION, selection)
            updateList(bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnListEmployerInteractionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnListFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(requestBroadcastReceiver!!)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (action) {
            CRUDService.ACTION_REQUEST_GET -> {
            }
            CRUDService.ACTION_REQUEST_POST, CRUDService.ACTION_REQUEST_PUT, CRUDService.ACTION_REQUEST_DELETE -> if (contract != null && (url == Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/PersonalInfo/" || url == Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/MovePersonalInfo/")) processRequestAll(
                option
            )
            CRUDService.ACTION_REQUEST_FORM_DATA -> {
            }
            CRUDService.ACTION_REQUEST_SAVE -> {
            }
        }
    }

    private fun processRequestAll(option: Int) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                Toast.makeText(activity, "Empleado asociado exitosamente", Toast.LENGTH_SHORT).show()
                if (activity is JoinPersonalWizardActivity) (activity as JoinPersonalWizardActivity?)!!.submitCreate()
                else if (activity is JoinPersonalProjectWizardActivity) (activity as JoinPersonalProjectWizardActivity?)!!.submitCreate(
                    personalContract!!.PersonalCompanyInfoId
                )
            }
            Constantes.BAD_REQUEST -> {
            }
            Constantes.NOT_INTERNET, Constantes.FORBIDDEN -> {
                Toast.makeText(requireContext(), "Este usuario no tiene permisos para esta accion", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            Constantes.REQUEST_NOT_FOUND -> {
            }
        }
    }
}