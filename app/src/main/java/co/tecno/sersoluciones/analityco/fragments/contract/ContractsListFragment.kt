package co.tecno.sersoluciones.analityco.fragments.contract

import android.content.ContentValues
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ContractsListActivity
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.contracts.ContractRecyclerViewAdapter
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.models.User
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.utilities.Utils.cursorToJObject
import com.android.volley.Request
import com.google.gson.Gson
import java.util.*

/**
 * Created by Ser Soluciones SAS on 01/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class ContractsListFragment : Fragment(), BroadcastListener {
    private var mColumnCount = 1
    private var mListener: OnListContractInteractionListener? = null
    private var adapter: ContractRecyclerViewAdapter? = null
    private var getJSONBroadcastReceiver: RequestBroadcastReceiver? = null
    private var mValues: ArrayList<ObjectList>? = null
    private var authorizedCompanies: ArrayList<String>? = null
    private var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getJSONBroadcastReceiver = RequestBroadcastReceiver(this)
        if (arguments != null) {
            mColumnCount = requireArguments().getInt(ARG_COLUMN_COUNT)
        }
        mValues = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_company_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val preferences = MyPreferences(requireActivity())
            val profile = preferences.profile
            user = Gson().fromJson(profile, User::class.java)
            authorizedCompanies = ArrayList()
            if (!user!!.IsAdmin && !user!!.IsSuperUser) {
                for (c in user!!.Companies) {
                    if (c.Permissions != null && c.Permissions.contains("contracts.view")) authorizedCompanies!!.add(c.Id)
                }
            }
            adapter = ContractRecyclerViewAdapter(activity, mListener, mValues)
            val context = view.getContext()
            val recyclerView = view
            //if (mColumnCount <= 1) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            //} else {
            //recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            //}
            recyclerView.adapter = adapter
        }
        updateList()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnListContractInteractionListener) {
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
    }

    fun updateList() {
        updateList(bundleList)
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
            requireActivity().contentResolver.query(Constantes.CONTENT_CONTRACT_URI, null, selection, selectionArgs, orderBy)
        mValues!!.clear()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val mItem =
                        Gson().fromJson(
                            cursorToJObject(cursor).toString(),
                            ObjectList::class.java
                        )
                    if (!authorizedCompanies!!.isEmpty() && !user!!.IsSuperUser && !user!!.IsAdmin) {
                        if (authorizedCompanies!!.contains(mItem.CompanyInfoParentId)) mValues!!.add(mItem)
                    } else if (user!!.IsSuperUser || user!!.IsAdmin) mValues!!.add(mItem)
                } while (cursor.moveToNext())
            }
            adapter!!.notifyDataSetChanged()
            cursor.close()
        }
    }

    interface OnListContractInteractionListener {
        fun onContractInteraction(item: ObjectList?, imageView: ImageView?)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST)
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            getJSONBroadcastReceiver!!,
            intentFilter
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(getJSONBroadcastReceiver!!)
    }

    fun syncData(item: String?) {
        var item = item
        if (item != null) {
            var selection = ("((" + DBHelper.CONTRACT_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " like ?) OR ("
                    + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_REVIEW + " like ?))")
            item = String.format("%%%s%%", item)
            var selectionArgs = arrayOf(item, item, item)
            when (mColumnCount) {
                1 -> {
                }
                2 -> {
                    selection += " AND (" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? )"
                    selectionArgs = arrayOf(item, item, item, "1")
                }
                3 -> {
                    selection += " AND (" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? )"
                    selectionArgs = arrayOf(item, item, item, "0")
                }
                4 -> {
                    selection += " AND (" + DBHelper.CONTRACT_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ?)"
                    selectionArgs = arrayOf(item, item, item, "1", "1")
                }
            }
            DebugLog.log("selection: $selection")
            val bundle = Bundle()
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
            bundle.putString(Constantes.KEY_SELECTION, selection)
            updateList(bundle)
        } else {
            updateList()
        }
    }

    private val bundleList: Bundle
        private get() {
            val bundle = Bundle()
            var selection = ""
            val selectionArgs: Array<String>
            when (mColumnCount) {
                1 -> {
                }
                2 -> {
                    selection += "(" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? )"
                    selectionArgs = arrayOf("1")
                    bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
                    bundle.putString(Constantes.KEY_SELECTION, selection)
                }
                3 -> {
                    selection += "(" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? )"
                    selectionArgs = arrayOf("0")
                    bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
                    bundle.putString(Constantes.KEY_SELECTION, selection)
                }
                4 -> {
                    selection += "(" + DBHelper.CONTRACT_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ?)"
                    selectionArgs = arrayOf("1", "1")
                    bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
                    bundle.putString(Constantes.KEY_SELECTION, selection)
                }
            }
            val preferences = MyPreferences(activity!!)
            val orderBy = preferences.orderContractBy
            bundle.putString(Constantes.KEY_ORDER_BY, orderBy)
            return bundle
        }

    override fun onStringResult(action: String?, option: Int, res: String?, url: String?) {
        when (option) {
            Constantes.SEND_REQUEST -> {
                DebugLog.log("actualizacion exitosa")
                //logW(res);
                updateList()
                (activity as ContractsListActivity?)!!.setQuantities()
            }
            Constantes.NOT_INTERNET, Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST -> {
            }
        }
    }

    companion object {
        private const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int): ContractsListFragment {
            val fragment = ContractsListFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}