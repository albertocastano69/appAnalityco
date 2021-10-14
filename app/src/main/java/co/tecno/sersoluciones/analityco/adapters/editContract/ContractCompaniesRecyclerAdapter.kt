package co.tecno.sersoluciones.analityco.adapters.editContract

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.CompanyList
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialIconView
import java.util.*
import java.util.regex.Pattern

class ContractCompaniesRecyclerAdapter(
    private val context: Context,
    private val mItems: ArrayList<CompanyList?>?,
    private val mListener: OnEditContractCompany?,
    private val isEmployer: Boolean
) : RecyclerView.Adapter<ContractCompaniesRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_list_info_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItems?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ContractCompaniesRecyclerAdapter.ViewHolder,
        position: Int
    ) {
        val pos = holder.adapterPosition
        val mItem = mItems!![pos]!!
        holder.textName?.text = mItem.Name + " " + mItem.Name
        holder.textSubName?.text = mItem.DocumentType + ": " + mItem.DocumentNumber
        holder.textValidity?.text = mItem.Address

        holder.logo?.setImageResource(R.drawable.image_not_available)
        //Log.e("logo",mItem.Logo);
        if (mItem.Logo != null) {
            val format = mItem.Logo.split(Pattern.quote(".").toRegex()).toTypedArray()
            if (format[format.size - 1] == "svg") {
                Glide.with(context)
                    .load(mItem.Logo)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.image_not_available)
                    )
                    .into(holder.logo!!)
            } else {
                val url = Constantes.URL_IMAGES + mItem.Logo
                Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(holder.logo)
            }
        }
        holder.cardViewDetail?.setOnClickListener {
            if (isEmployer) mListener?.onEmployerCard(mItem)
            else mListener?.onCompanyCard(mItem)
        }
        holder.btnEdit?.visibility = View.GONE
        holder.labelValidity?.visibility = View.GONE
        holder.textActive?.visibility = View.GONE
        holder.btnEdit?.visibility = View.GONE
    }

    inner class ViewHolder internal constructor(view: View?) : RecyclerView.ViewHolder(view!!) {

        var mView: View? = null
        @JvmField
        @BindView(R.id.logo)
        var logo: ImageView? = null
        @JvmField
        @BindView(R.id.text_name)
        var textName: TextView? = null
        @JvmField
        @BindView(R.id.text_sub_name)
        var textSubName: TextView? = null
        @JvmField
        @BindView(R.id.label_validity)
        var labelValidity: TextView? = null
        @JvmField
        @BindView(R.id.text_validity)
        var textValidity: TextView? = null
        @JvmField
        @BindView(R.id.icon_is_active)
        var iconIsActive: ImageView? = null
        @JvmField
        @BindView(R.id.text_active)
        var textActive: TextView? = null
        @JvmField
        @BindView(R.id.btn_edit)
        var btnEdit: ImageView? = null
        @JvmField
        @BindView(R.id.card_view_detail)
        var cardViewDetail: CardView? = null
        @JvmField
        @BindView(R.id.state_icon)
        var stateIcon: ImageView? = null
        @JvmField
        @BindView(R.id.icon_date)
        var dateIcon: MaterialIconView? = null
        @JvmField
        @BindView(R.id.top_layout)
        var topLayout: ConstraintLayout? = null
        @JvmField
        @BindView(R.id.iconHeader)
        var topIconDate: MaterialIconView? = null
        @JvmField
        @BindView(R.id.text_date)
        var textTopDate: TextView? = null
        @JvmField
        @BindView(R.id.image_dots)
        var imageDots: ImageView? = null
        @JvmField
        @BindView(R.id.job_icon)
        var jobIcon: ImageView? = null
        @JvmField
        @BindView(R.id.job_text)
        var textJob: TextView? = null

        init {
            ButterKnife.bind(this, view!!)
        }
    }

    interface OnEditContractCompany {
        fun onCompanyCard(mItem: CompanyList)
        fun onEmployerCard(mItem: CompanyList)
    }
}