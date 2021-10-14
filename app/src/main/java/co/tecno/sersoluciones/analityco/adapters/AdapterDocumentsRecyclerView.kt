package co.tecno.sersoluciones.analityco.adapters

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
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.RequerimentIndividualContract
import kotlinx.android.synthetic.main.requerimets_list_layout.view.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import net.steamcrafted.materialiconlib.MaterialIconView

class AdapterDocumentsRecyclerView(
        private val mContext: Context,
        private val mValues: List<RequerimentIndividualContract>,
        private val mListener: OnDocumentIndivudualContractListener?,
        private val showPencil : Boolean?
):RecyclerView.Adapter<AdapterDocumentsRecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterDocumentsRecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.requerimets_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterDocumentsRecyclerView.ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.description.text = mValues[position].Name
        holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
        if(holder.mItem!!.File != null){
            holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_green)
        }else{
            holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
        }
        //holder.mDateView.setText(mValues.get(position).Type);
        //holder.btnEdit.visibility = View.GONE
        if (showPencil!!){
            holder.btnEdit.visibility = View.VISIBLE
        }else {
            holder.btnEdit.visibility = View.VISIBLE
        }
        holder.typeDocument.text = String.format("%s", mValues[position].Abrv)
        holder.btnEdit.setOnClickListener {
                mListener?.editRequerimentItem(holder!!.mItem, position)
        }
        holder.iconFile.setOnClickListener {
            val intent1 = Intent(Intent.ACTION_VIEW).setData(Uri.parse(holder.mItem!!.File))
            mContext.startActivity(intent1)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
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

        var mItem: RequerimentIndividualContract? = null

        override fun toString(): String {
            return super.toString() + " '" + description.text + "'"
        }

        init {
            val drawablePen = MaterialDrawableBuilder.with(mContext)
                    .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                    .setColor(Color.rgb(106, 202, 37))
                    .setSizeDp(20)
                    .build()
            btnEdit.setImageDrawable(drawablePen)
        }
    }
    interface OnDocumentIndivudualContractListener {
        fun editRequerimentItem(mItem: RequerimentIndividualContract?, pos: Int)
    }
}