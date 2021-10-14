package co.tecno.sersoluciones.analityco.fragments.personal.wizard

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.JoinPersonalProjectWizardActivity
import co.tecno.sersoluciones.analityco.JoinPersonalWizardActivity
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.models.ContractEnrollment
import co.tecno.sersoluciones.analityco.models.ContractType
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import co.tecno.sersoluciones.analityco.utilities.Utils
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_select_contract.view.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 */
class SelectContractFragment : Fragment() {

    private var contractType: ContractType? = null
    private var mListener: OnSelectContractListener? = null
    private lateinit var viewOfLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_select_contract, container, false)
        return viewOfLayout
    }

    fun updateListContracts(jsonObjStr: String, projectId: String = "", contractId: String = "") {

        val contractList = Gson().fromJson<ArrayList<ContractEnrollment>>(
            jsonObjStr,
            object : TypeToken<ArrayList<ContractEnrollment>>() {

            }.type
        )

        val jsonArrayContracts: JSONArray
        val contractNumbersExcept = ArrayList<String>()
        if (contractList.size > 0) {
            for (contractEnrollment in contractList) {
                contractNumbersExcept.add(contractEnrollment.ContractNumber!!)
            }
            jsonArrayContracts = Utils.cursorToJArray(getCursorContracts(contractNumbersExcept, projectId, contractId)!!)

        } else {
            jsonArrayContracts = Utils.cursorToJArray(getCursorContracts(projectId, contractId)!!)
        }
        logW("Json contratos " + jsonArrayContracts.toString())
        val contractPrincipalList = Gson().fromJson<ArrayList<ContractEnrollment>>(
            jsonArrayContracts.toString(),
            object : TypeToken<ArrayList<ContractEnrollment>>() {
            }.type
        )

        if (contractType!!.Value == "AD" || contractType!!.Value == "AS") {
            val itr = contractPrincipalList.iterator()
            while (itr.hasNext()) {
                val contractEnrollment = itr.next()
                if (!contractEnrollment.IsRegister && contractEnrollment.CountPersonal > 0) {
                    itr.remove()
                }
            }
        }

        val mLayoutManager = LinearLayoutManager(activity)
        viewOfLayout.contracts.layoutManager = mLayoutManager
        val adapterContracts = ContractRecyclerAdapter(requireActivity(), contractPrincipalList)
        viewOfLayout.contracts.itemAnimator = DefaultItemAnimator()
        viewOfLayout.contracts.adapter = adapterContracts

        if (contractPrincipalList.size == 1 && !contractId.isEmpty() && contractPrincipalList.get(0).IsActive) {
            mListener!!.onJoinContract(contractPrincipalList.get(0), contractType!!.Value)
            return
        }

        updateListJoinContract(contractList, projectId, contractId)
    }

    private fun getCursorContracts(projectId: String = "", contractId: String = ""): Cursor? {
        if (activity is JoinPersonalWizardActivity)
            contractType = (activity as JoinPersonalWizardActivity).contractType
        else if (activity is JoinPersonalProjectWizardActivity)
            contractType = (activity as JoinPersonalProjectWizardActivity).contractType

        val item = String.format("%s", contractType!!.Id)

        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add(item)
        var selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID + " = ? "

        if (!contractId.isEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID + " = ? "
            arrayList.add(contractId)
        }

        if (!projectId.isEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " like ? "
            arrayList.add("%$projectId%")
        }

        if (activity is JoinPersonalProjectWizardActivity) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_EMPLOYER_ID + " like ? "
            arrayList.add("%${(activity as JoinPersonalProjectWizardActivity?)!!.employerId}%")
        }

        selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " != \"\" )"
        //        if (contractType.Value.equals("AD") || contractType.Value.equals("AS"))
        //            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_COUNT_PERSONAL + " = 0 ";

        val selectionArgs = arrayList.toTypedArray()

        return requireActivity().contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
    }


    private fun getCursorContracts(contractsExceptArray: ArrayList<String>, projectId: String = "", contractId: String = ""): Cursor? {
        if (activity is JoinPersonalWizardActivity)
            contractType = (activity as JoinPersonalWizardActivity).contractType
        else if (activity is JoinPersonalProjectWizardActivity)
            contractType = (activity as JoinPersonalProjectWizardActivity).contractType
        var selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID + " = ? "
        //+ DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? AND "
        //        if (contractType.Value.equals("AD") || contractType.Value.equals("AS"))
        //            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_COUNT_PERSONAL + " = 0 ";

        val item = String.format("%s", contractType!!.Id)
        //arrayList.add(0, "1");
        val arrayList = ArrayList<String>()
        arrayList.add(0, item)

        if (!contractId.isEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID + " = ? "
            arrayList.add(1, contractId)
        }

        if (!projectId.isEmpty()) {
            selection += (" AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " like ? ")
            arrayList.add(1, "%$projectId%")
        }

        if (activity is JoinPersonalProjectWizardActivity) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_EMPLOYER_ID + " like ? "
            arrayList.add("%${(activity as JoinPersonalProjectWizardActivity?)!!.employerId}%")
        }

        selection += (" AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " != \"\" AND "
                + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " not in(" + Utils.makePlaceholders(contractsExceptArray.size) + ")"
                + ")")
        arrayList.addAll(contractsExceptArray)
        logW("Query $selection")
        val selectionArgs = arrayList.toTypedArray()
        for (select in selectionArgs) {
            log("select: $select")
        }
        return requireActivity().contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
    }

    private fun updateListJoinContract(contractList: ArrayList<ContractEnrollment>, projectId: String, contractId: String) {

        viewOfLayout.layout_contratc_join.visibility = View.GONE
        if (contractList.size > 0) {
            viewOfLayout.layout_contratc_join.visibility = View.VISIBLE
        }

        if (!projectId.isEmpty()) {
            val it = contractList.iterator()
            while (it.hasNext()) {
                if (!it.next().ProjectIds!!.contains(projectId)) {
                    it.remove()
                }
            }
        }

        if (contractList.size == 1 && !contractId.isEmpty() && contractList.get(0).IsActive) {
            mListener!!.onApplyContract(contractList.get(0))
            return
        }

        val mLayoutManager = LinearLayoutManager(activity)
        viewOfLayout.contractsJoin.layoutManager = mLayoutManager
        val adapter = ContractJoinRecyclerAdapter(requireActivity(), contractList)
        viewOfLayout.contractsJoin.itemAnimator = DefaultItemAnimator()
        //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewContractsJoin.getContext(),
        //        mLayoutManager.getOrientation());
        //recyclerViewContractsJoin.addItemDecoration(mDividerItemDecoration);
        viewOfLayout.contractsJoin.adapter = adapter
    }

    private fun getCursorContractsJoin(arrayList: ArrayList<String>, projectId: String): Cursor? {
        if (activity is JoinPersonalWizardActivity)
            contractType = (activity as JoinPersonalWizardActivity).contractType
        else if (activity is JoinPersonalProjectWizardActivity)
            contractType = (activity as JoinPersonalProjectWizardActivity).contractType
        val selection = ("("
                + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " like ? AND "
                //+ DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? AND "
                + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " in (" + Utils.makePlaceholders(arrayList.size) + ")"
                + ")")

        arrayList.add(0, "%$projectId%")
        //arrayList.add(1, "1");

        //String item = String.format("%s", contractType.Id);
        val selectionArgs = arrayList.toTypedArray()
        return requireActivity().contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
    }


    private inner class ContractRecyclerAdapter(context: Context, mItems: ArrayList<ContractEnrollment>) :
        CustomListInfoRecyclerViewUsers<ContractEnrollment>(context, mItems) {

        override fun onBindViewHolder(holder: CustomListInfoRecyclerViewUsers<*>.ViewHolder, position: Int) {
            val pos = holder.adapterPosition
            val mItem = mItems[pos]
            holder.textName.setText(mItem.ContractReview)
            holder.textName.setTypeface(holder.textName.getTypeface(), Typeface.BOLD)
            holder.textSubName.setText(mItem.ContractorName)

            holder.textValidity.setVisibility(View.GONE)
            holder.vigence.setVisibility(View.GONE)
            holder.viewDivider.setVisibility(View.GONE)
            holder.linearLayoutSectionIcon.setVisibility(View.VISIBLE)
            try {
                @SuppressLint("SimpleDateFormat")
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val finishDateContract = format.parse(mItem.FinishDate)
                val myFormat = "dd-MMM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                holder.textViewIcon1.setText(sdf.format(finishDateContract))
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            holder.textViewIcon2.setText(mItem.ContractNumber)

            holder.phone.setVisibility(View.GONE)
            holder.iconIsActive.setVisibility(View.GONE)
            holder.textValidity2.setVisibility(View.GONE)

            holder.logo.setImageResource(R.drawable.image_not_available)
            holder.btnEdit.setVisibility(View.GONE)

            when (contractType!!.Value) {
                "AD" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_02_administrativotrasparente2)
                "FU" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_14_funcionariotrasparente)
                "PR" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_10_proveedor_trasparente)
                "AS" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_04_asociado_trasparente2)
                "VI" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_16_visitante_trasparente)
                "OT" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_18_otros_trasparente)
                "CO" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_06_contratista_trasparente)
            }

            //            if (!isActive) {
            //                holder.stateIcon.setVisibility(View.VISIBLE);
            //                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
            //            }
            if (mItem.FormImageLogo != null) {
                val url = Constantes.URL_IMAGES + mItem.FormImageLogo
                val format = url.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (format[format.size - 1] == "svg") {
//                    val uri = Uri.parse(url)
                    GlideApp.with(context!!)
                        .`as`(PictureDrawable::class.java)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.image_not_available)
                        )
                        .fitCenter()
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                        .load(url)
                        .into(holder.logo)
                } else {
                    Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo)
                }
            }

            holder.cardViewDetail.setOnClickListener { mListener!!.onJoinContract(mItem, contractType!!.Value) }
        }
    }

    private inner class ContractJoinRecyclerAdapter(context: Context, mItems: ArrayList<ContractEnrollment>) :
        CustomListInfoRecyclerViewUsers<ContractEnrollment>(context, mItems) {

        override fun onBindViewHolder(holder: CustomListInfoRecyclerViewUsers<*>.ViewHolder, position: Int) {
            val pos = holder.getAdapterPosition()
            val mItem = mItems[pos]
            holder.textName.setText(mItem.ContractReview)
            holder.textName.setTypeface(holder.textName.getTypeface(), Typeface.BOLD)
            holder.textSubName.setText(mItem.EmployerName)

            val isActive = mItem.IsActive
            holder.phone.setVisibility(View.GONE)
            holder.iconIsActive.setVisibility(View.GONE)
            holder.textValidity2.setVisibility(View.GONE)
            holder.linearLayoutSectionIcon.setVisibility(View.VISIBLE)

            holder.textViewIcon2.setText(mItem.ContractNumber)
            try {
                @SuppressLint("SimpleDateFormat")
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val finishDateContract = format.parse(mItem.FinishDate)
                val myFormat = "dd-MMM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                holder.textViewIcon1.setText(sdf.format(finishDateContract))
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            holder.logo.setImageResource(R.drawable.image_not_available)
            holder.btnEdit.setVisibility(View.GONE)


            when (mItem.ValuePersonalType) {
                "AD" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_02_administrativotrasparente2)
                "FU" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_14_funcionariotrasparente)
                "PR" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_10_proveedor_trasparente)
                "AS" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_04_asociado_trasparente2)
                "VI" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_16_visitante_trasparente)
                "OT" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_18_otros_trasparente)
                "CO" -> holder.imageViewIcon1.setImageResource(R.drawable.ic_06_contratista_trasparente)
            }
            holder.user_icon.setImageResource(R.drawable.ic_job)
            holder.profile.setText(mItem.Position)
            try {
                @SuppressLint("SimpleDateFormat")
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val finishDatePerson = format.parse(mItem.FinishDatePerson)
                val myFormat = "dd-MMM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                holder.textValidity.setText(sdf.format(finishDatePerson))
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (!isActive) {
                val calendarRed = MaterialDrawableBuilder.with(mContext) // provide a context
                    .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR) // provide an icon
                    .setColor(Color.RED) // set the icon color
                    .setSizeDp(20) // set the icon size
                    .build()
                holder.calendar.setImageDrawable(calendarRed)
                holder.textValidity.setTextColor(Color.RED)
            }
            if (mItem.FormImageLogo != null) {
                val url = Constantes.URL_IMAGES + mItem.FormImageLogo
                val format = url.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (format[format.size - 1] == "svg") {
                    val uri = Uri.parse(url)

//                Uri uri = Uri.parse(url);
                    GlideApp.with(context!!)
                        .`as`(PictureDrawable::class.java)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.image_not_available)
                        )
                        .fitCenter()
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                        .load(url)
                        .into(holder.logo)
                } else {
                    Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo)
                }
            }

            holder.cardViewDetail.setOnClickListener(View.OnClickListener {
                if (!isActive) {
                    MetodosPublicos.alertDialog(activity, "Este empleado se encuentra inactivo en ese contrato.")
                } else
                    mListener!!.onApplyContract(mItem)
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectContractListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    interface OnSelectContractListener {
        fun onJoinContract(contract: ContractEnrollment, contractTypeValue: String)

        fun onApplyContract(contract: ContractEnrollment)
    }
}
