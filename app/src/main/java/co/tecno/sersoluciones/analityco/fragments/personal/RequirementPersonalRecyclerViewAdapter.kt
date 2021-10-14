package co.tecno.sersoluciones.analityco.fragments.personal

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.Requirements
import kotlinx.android.synthetic.main.requerimets_list_layout.view.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import net.steamcrafted.materialiconlib.MaterialIconView
import org.json.JSONObject

/**
 * [RecyclerView.Adapter] that can display a [Requirements] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class RequirementPersonalRecyclerViewAdapter(
    private val mContext: Context,
    private val mValues: List<Requirements>,
    private val mListener: RequirementPersonalFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<RequirementPersonalRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.requerimets_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.description.text = mValues[position].Attr
        //holder.mDateView.setText(mValues.get(position).Type);
        holder.btnEdit.visibility = View.GONE
        holder.typeDocument.text = String.format("%s", mValues[position].Type)
        /* if (mValues.get(position).Attr != null)
            holder.typeDocument.setText(String.format("%s", mValues.get(position).Attr));
        else {
            holder.mItem.Attr = "";
            holder.typeDocument.setText(String.format("%s", mValues.get(position).Type));
        }*/
        if (holder.mItem!!.IsEntry) {
            holder.iconFile.visibility = View.INVISIBLE
        }
        if (holder.mItem!!.DocsJSON.isNullOrEmpty() || JSONObject(holder.mItem!!.DocsJSON).length() == 0) {
            holder.iconFile.visibility = View.INVISIBLE
        }
        if (!holder.mItem!!.IsValided) {
            holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper_red)
        }
        if (holder.mItem!!.IsEntry && holder.mItem?.EnableInputEntry != null) {
            holder.iconFile.setBackgroundResource(R.drawable.ic_blank_paper)
            holder.iconFile.visibility = View.VISIBLE
        }
        holder.mView.setOnClickListener { mListener?.onListFragmentInteraction(holder.mItem) }
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

        var mItem: Requirements? = null

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

}