package co.tecno.sersoluciones.analityco.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.callback.OnListPlaceJobListener
import co.tecno.sersoluciones.analityco.models.PlaceToJob
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import java.text.Normalizer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class AdapterPlaceToJob(
        private val mContext: Context,
        private val mValues: ArrayList<PlaceToJob>,
        private val mListener: OnListPlaceJobListener?

): RecyclerView.Adapter<AdapterPlaceToJob.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterPlaceToJob.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_placetojob, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: AdapterPlaceToJob.ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        if(holder.mItem != null){
            holder.textName!!.text = holder.mItem!!.Name
            val string = Normalizer.normalize(holder.mItem!!.ContractReview, Normalizer.Form.NFD)
            val string1 = string.replace("Ã\u0093", "O")
            holder.textSub!!.text = string1
            holder.textReview!!.text = holder.mItem!!.ContractNumber
            if(holder.mItem!!.FinishDate!= null){
             var FinishDate = ""
             val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
             val format = SimpleDateFormat("dd/MM/yyyy")
             try {
                val date = simpleDateFormat.parse(holder.mItem!!.FinishDate)
                FinishDate = format.format(date)
                holder.textDate!!.text=FinishDate
             }catch (e: ParseException) {
                 e.printStackTrace()
             }
            }
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
        holder.mView.setOnClickListener { mListener?.onListFragmentInteraction(holder.mItem) }
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
        @BindView(R.id.text_sub)
        var textSub: TextView? = null

        @JvmField
        @BindView(R.id.text_name)
        var textName: TextView? = null

        @JvmField
        @BindView(R.id.text_review)
        var textReview: TextView? = null

        @JvmField
        @BindView(R.id.text_date)
        var textDate: TextView? = null


        var mItem: PlaceToJob? = null
        override fun toString(): String {
            return super.toString() + " '" + textName!!.text + "'"
        }

        init {
            ButterKnife.bind(this, view)
            mView = view
        }
    }

}