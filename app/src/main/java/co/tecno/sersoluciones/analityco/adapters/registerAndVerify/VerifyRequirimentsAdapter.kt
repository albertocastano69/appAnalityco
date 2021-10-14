package co.tecno.sersoluciones.analityco.adapters.registerAndVerify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.SurveyDetailsActivity
import co.tecno.sersoluciones.analityco.models.RequirementsList
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import kotlinx.android.synthetic.main.requerimets_list_adapter.view.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import net.steamcrafted.materialiconlib.MaterialIconView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class VerifyRequirimentsAdapter(
    private val mContext: Context,
    private val mItems: ArrayList<RequirementsList>?,
    private val mListener: OnVerifyReqListener?,
    private val personInfoId: Int?,
    private val namePerson : String?
) : RecyclerView.Adapter<VerifyRequirimentsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.requerimets_list_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItems?.size ?: 0
    }

    fun swap(data: ArrayList<RequirementsList>) {
        if (mItems != null) {
            mItems.clear()
            mItems.addAll(data)
            notifyDataSetChanged()
        }
    }

    fun update() {
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        @JvmField
        var btnEdit: ImageView = mView.btn_edit

        @JvmField
        var typeDocument: TextView = mView.typeDocument

        @JvmField
        var description: TextView = mView.description

        @JvmField
        var iconFile: LinearLayout = mView.iconFile

        @JvmField
        var icon_calendar: MaterialIconView = mView.icon_calendar

        @JvmField
        var imgArl: ImageView = mView.imgArl

        @JvmField
        var textArl: TextView = mView.textArl

        @JvmField
        var arlContent: LinearLayout = mView.arlContent

        @JvmField
        var date: TextView = mView.date

        var mItem: RequirementsList? = null

        init {
            val drawablePen = MaterialDrawableBuilder.with(mContext)
                .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                .setColor(Color.rgb(106, 202, 37))
                .setSizeDp(20)
                .build()
            btnEdit.setImageDrawable(drawablePen)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preferences = MyPreferences(mContext)
        holder.mItem = mItems!![position]
        if (holder.mItem!!.IsBiometric) {
            holder.mItem!!.Type = "BIO"
        }
        holder.typeDocument.text = holder.mItem!!.Type.toUpperCase()
        holder.btnEdit.visibility = View.GONE
        if (!holder.mItem!!.IsValided && !holder.mItem?.IsSurvey!!) {
            holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
            holder.icon_calendar.setColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.bar_undecoded
                )
            )
            holder.date.setTextColor(ContextCompat.getColor(mContext, R.color.bar_undecoded))
            if (holder.mItem!!.Model != null && !holder.mItem!!.Model.isEmpty()) holder.btnEdit.visibility =
                View.VISIBLE
            if (holder.mItem?.IsSurvey!!) holder.btnEdit.visibility = View.VISIBLE
        } else if (!holder.mItem?.IsSurvey!!) {
            if (holder.mItem!!.DocsJSON.isNullOrEmpty() || JSONObject(holder.mItem!!.DocsJSON).length() == 0) {
                holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
                holder.icon_calendar.setColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.bar_undecoded
                    )
                )
                holder.date.setTextColor(ContextCompat.getColor(mContext, R.color.bar_undecoded))
                if (holder.mItem!!.Model != null && !holder.mItem!!.Model.isEmpty()) holder.btnEdit.visibility =
                    View.VISIBLE

            } else {
                holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_green)
                holder.btnEdit.visibility = View.GONE
            }
        }
        if (holder.mItem?.IsSurvey!!) {
            if (holder.mItem?.IsValided!!) {
                holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_green)
                holder.date.setTextColor(ContextCompat.getColor(mContext, R.color.black_alpha))
                holder.icon_calendar.setColor(ContextCompat.getColor(mContext, R.color.black_alpha))
                holder.btnEdit.visibility = View.VISIBLE
                if (holder.mItem?.State != null && holder.mItem?.State!! == 2L) {
                    holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
                    holder.icon_calendar.setColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.bar_undecoded
                        )
                    )
                    holder.date.setTextColor(
                        ContextCompat.getColor(
                            mContext,
                            R.color.bar_undecoded
                        )
                    )
                }
                if (holder.mItem?.State != null && holder.mItem?.State!! == 1L) {
                    holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_orange)
                    holder.icon_calendar.setColor(ContextCompat.getColor(mContext, R.color.expiry))
                    holder.date.setTextColor(ContextCompat.getColor(mContext, R.color.expiry))
                }
            } else {
                holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
                holder.btnEdit.visibility = View.VISIBLE
                holder.icon_calendar.setColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.bar_undecoded
                    )
                )
                holder.date.setTextColor(ContextCompat.getColor(mContext, R.color.bar_undecoded))
            }
        }

        if (!holder.mItem!!.withFile && !holder.mItem!!.IsValided && !holder.mItem?.IsSurvey!!) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnEdit.setImageResource(R.drawable.ic_tick)
            //holder.btnEdit.setBackgroundResource(R.drawable.ic_check_circle_white_24dp);
        }
        var mDate = ""
        if (holder.mItem!!.ValidityDate != null) mDate =
            holder.mItem!!.ValidityDate else if (holder.mItem!!.Date != null) mDate =
            holder.mItem!!.Date
        try {
            mDate = if (!mDate.isEmpty()) {
                @SuppressLint("SimpleDateFormat") val format =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val date = format.parse(mDate)
                @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("yyyy MMM dd")
                dateFormat.format(date)
            } else {
                "--/--/--"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        holder.icon_calendar.visibility = View.GONE
        holder.date.visibility = View.GONE
        if (holder.mItem!!.RequiredDate) {
            holder.icon_calendar.visibility = View.VISIBLE
            holder.date.visibility = View.VISIBLE
            holder.date.text = mDate
        }
        holder.description.text = holder.mItem!!.Attr
        if (holder.mItem!!.IsCertification) {
            holder.description.text = holder.mItem!!.Attr
            holder.arlContent.visibility = View.GONE
        }
        if (holder.mItem!!.EPSName != null) {
            holder.description.text = holder.mItem!!.EPSName
            holder.arlContent.visibility = View.GONE
        } else if (holder.mItem!!.AFPName != null) {
            holder.description.text = holder.mItem!!.AFPName
            holder.arlContent.visibility = View.GONE
        } else if (holder.mItem!!.ARLName != null) {
            holder.description.text = holder.mItem!!.ARLName
            if (holder.mItem!!.typeARL != null) {
                if (holder.mItem!!.typeARL.IsValided != null && !holder.mItem!!.typeARL.IsValided) {
                    holder.arlContent.visibility = View.VISIBLE
                    holder.textArl.text = String.format(" %s", holder.mItem!!.typeARL.Desc)
                    holder.imgArl.setImageResource(R.drawable.ic_close_arl)
                    mListener!!.arlRiskAlert()
                } else {
                    holder.imgArl.setImageResource(R.drawable.ic_checkmark)
                    holder.arlContent.visibility = View.GONE
                }
            }
        }
        holder.iconFile.setOnClickListener {
            if (holder.mItem!!.File != null && holder.mItem!!.IsValided) {

//                val rute: String = holder.mItem!!.File
//                val intent = Intent(Intent.ACTION_VIEW)
//                val extension = rute.split("\\.").toTypedArray()
//                val index = extension.size - 1
//                when (extension[index]) {
//                    "pdf", "jpg", "png" -> {
//                        intent.setDataAndType(Uri.parse(Constantes.URL_IMAGES + holder.mItem!!.File), "text/html")
//                        DebugLog.logW("URL: " + Constantes.URL_IMAGES + holder.mItem!!.File)
//                        mContext.startActivity(intent)
//                    }
//                }
                val url = Constantes.URL_IMAGES + holder.mItem!!.File
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                mContext.startActivity(i)
            } else if (holder.mItem?.IsSurvey!! && holder.mItem?.SurveyId != null && holder.mItem?.SurveyId!! > 0L) {
                val intent = Intent(mContext, SurveyDetailsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("createDate",holder.mItem?.Date)
                intent.putExtra("PersonalInfoId", personInfoId.toString())
                intent.putExtra("mode", "Details")
                mListener?.openSurveyActivity(intent,position)
            }

        }

        holder.btnEdit.setOnClickListener {
            if (holder.mItem!!.withFile) {
                mListener?.editRequerimentItem(holder.mItem, position)
            } else if (holder.mItem?.IsSurvey!!) {
                val intent = Intent(mContext, SurveyDetailsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("surveyId", holder.mItem?.SurveyId.toString())
                intent.putExtra("DocumentNumber", holder.mItem?.documentNumber)
                intent.putExtra("BirthDate", holder.mItem?.birthday)
                intent.putExtra("ProjectId", holder.mItem?.projectId)
                intent.putExtra("PersonalInfoId", personInfoId.toString())
                intent.putExtra("namePerson",namePerson)
                intent.putExtra("mode", "Edit")
                mListener?.openSurveyActivity(intent,position)
            }
        }

        if (!preferences.editReqEnabled) holder.btnEdit.visibility = View.VISIBLE
    }

    interface OnVerifyReqListener {
        fun editRequerimentItem(mItem: RequirementsList?, pos: Int)
        fun arlRiskAlert()
        fun openSurveyActivity(intent: Intent, pos: Int)
    }

}