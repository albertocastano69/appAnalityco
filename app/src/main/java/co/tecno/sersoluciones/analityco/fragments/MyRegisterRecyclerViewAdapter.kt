package co.tecno.sersoluciones.analityco.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.Reporte


class MyRegisterRecyclerViewAdapter(private val mContext: Context) : RecyclerView.Adapter<MyRegisterRecyclerViewAdapter.ViewHolder>() {

    private var myClipboard: ClipboardManager? = null
    private var myClip: ClipData? = null

    init {
        myClipboard = mContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    var data = listOf<Reporte>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun getItem(position: Int): Reporte {
        return data[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_register_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.contentView.text = item.data
        holder.cardView.setOnLongClickListener {
            myClip = ClipData.newPlainText("text", item.data)
            myClipboard?.setPrimaryClip(myClip!!)
            Toast.makeText(
                mContext, "Registro copiado",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.findViewById(R.id.content)
        val cardView: CardView = view.findViewById(R.id.card)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}