package co.tecno.sersoluciones.analityco.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import java.util.*
import java.util.regex.Pattern

class EmployerIndividualContractRecyclerViewAdapter(
        private val mContext: Context,
        private val mListener: OnListEmployerInteractionListener?,
        private var mValues: ArrayList<ObjectList>
): RecyclerView.Adapter<EmployerIndividualContractRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployerIndividualContractRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_company, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EmployerIndividualContractRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        if (holder.mItem != null) {
            holder.textName!!.text = holder.mItem!!.Name
            holder.textAddress!!.text = holder.mItem!!.DocumentType + " " + holder.mItem!!.DocumentNumber
            holder.textValidity!!.text = holder.mItem!!.Rol
            holder.stateIcon!!.visibility = View.VISIBLE
            holder.stateIcon!!.visibility = View.GONE
            if (holder.mItem!!.Logo != null && holder.mItem!!.Logo != "null") {
                val url = Constantes.URL_IMAGES + holder.mItem!!.Logo
                val format = url.split(Pattern.quote(".").toRegex()).toTypedArray()
                if (format[format.size - 1] == "svg") {
//                    Uri uri = Uri.parse(url);
                    GlideApp.with(mContext)
                        .`as`(PictureDrawable::class.java)
                        .apply(
                                RequestOptions()
                                        .placeholder(R.drawable.loading_animation)
                                        .error(R.drawable.image_not_available)
                        )
                        .fitCenter()
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                        .load(url)
                        .into(holder.logo!!)
                } else {
                    Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo)
                }
            } else {
                holder.logo!!.setImageResource(R.drawable.image_not_available)
            }
        }
        ViewCompat.setTransitionName(holder.logo!!, holder.mItem!!.Id)
        holder.mView.setOnClickListener { mListener?.onListFragmentInteraction(holder.mItem, holder.logo) }
    }


    fun swap() {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }
    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val mView: View

        @JvmField
        @BindView(R.id.logo)
        var logo: ImageView? = null

        @JvmField
        @BindView(R.id.text_name)
        var textName: TextView? = null

        @JvmField
        @BindView(R.id.text_address)
        var textAddress: TextView? = null

        @JvmField
        @BindView(R.id.icon_phone)
        var iconPhone: Button? = null

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
        @BindView(R.id.label_active)
        var labelActive: TextView? = null

        @JvmField
        @BindView(R.id.card_view_detail)
        var cardViewDetail: CardView? = null

        @JvmField
        @BindView(R.id.state_icon)
        var stateIcon: ImageView? = null
        var mItem: ObjectList? = null
        override fun toString(): String {
            return super.toString() + " '" + textName!!.text + "'"
        }

        init {
            ButterKnife.bind(this, view)
            mView = view
            labelValidity!!.visibility = View.GONE
            labelActive!!.visibility = View.GONE
        }
    }

    fun filtar(filtroEmployer: ArrayList<ObjectList>) {
        this.mValues = filtroEmployer
        notifyDataSetChanged()
    }
}