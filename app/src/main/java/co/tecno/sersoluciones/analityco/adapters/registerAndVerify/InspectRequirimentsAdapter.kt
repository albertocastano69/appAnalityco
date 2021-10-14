package co.tecno.sersoluciones.analityco.adapters.registerAndVerify

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.RequirementsList
import co.tecno.sersoluciones.analityco.utilities.Utils.afterTextChanged
import java.util.*

class InspectRequirimentsAdapter(
    context: Context?,
    private val mItems: ArrayList<RequirementsList>?,
    private val mListener: OnInspectReqListener?
) : RecyclerView.Adapter<InspectRequirimentsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inspect_list_adapter, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return mItems?.size ?: 0
    }

    fun update() {
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(view: View?) :
        RecyclerView.ViewHolder(view!!) {
        @JvmField
        @BindView(R.id.description)
        var description: TextView? = null

        @JvmField
        @BindView(R.id.checkBox)
        var checkBox: CheckBox? = null

        @JvmField
        @BindView(R.id.input_text)
        var inputValue: EditText? = null

        @JvmField
        @BindView(R.id.checkedText)
        var checkedText: TextView? = null
        var mItem: RequirementsList? = null

        init {
            ButterKnife.bind(this, view!!)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mItems!![position]
        holder.description?.text = holder.mItem!!.Attr
        holder.inputValue?.visibility = View.GONE

        if (holder.mItem?.EnableInputEntry!!) {
            holder.description?.typeface = Typeface.DEFAULT_BOLD
            when (holder.mItem?.ArithmeticOperator) {
                0L -> holder.description?.text =
                    "Tiene ${holder.mItem?.Attr} menor a: < ${holder.mItem?.EntryValue}"
                1L -> holder.description?.text =
                    "Tiene ${holder.mItem?.Attr} menor o igual a: <= ${holder.mItem?.EntryValue}"
                2L -> holder.description?.text =
                    "Tiene ${holder.mItem?.Attr} mayor a: > ${holder.mItem?.EntryValue}"
                3L -> holder.description?.text =
                    "Tiene ${holder.mItem?.Attr} mayor o igual a: >= ${holder.mItem?.EntryValue}"
                4L -> holder.description?.text =
                    "Tiene ${holder.mItem?.Attr} igual a: = ${holder.mItem?.EntryValue}"
            }
            holder.checkBox?.visibility = View.GONE
            holder.checkedText?.visibility = View.GONE
            holder.inputValue?.visibility = View.VISIBLE
            holder.inputValue?.afterTextChanged {
                it.let {
                    mListener?.updateValidityInputItem(holder.mItem!!, position, it)
                    val pattern = "[1-9]\\d*(\\.\\d+)?".toRegex()
                    if (it.isBlank()) {
                        //holder.inputValue?.error = "Este campo es obligatorio"
                        holder.mItem?.Value = 0f
                    } else if (!it.matches(pattern)) {
                        holder.inputValue?.error = "Numero no valido"
                        holder.mItem?.Value = 0f
                    } else holder.mItem?.Value = it.toFloat()
                }
            }
        }else{
            holder.checkBox?.visibility = View.VISIBLE
            holder.checkedText?.visibility = View.VISIBLE
            holder.inputValue?.visibility = View.GONE
            holder.description?.typeface = Typeface.DEFAULT_BOLD
            holder.description?.text =
                " ${holder.mItem?.Attr} "
        }
        if (holder.mItem?.IsValided!!) {
            holder.mItem?.IsValided = true
            holder.checkBox?.isChecked = true
            holder.checkedText!!.text = "Cumple"
        } else {
            holder.mItem?.IsValided = false
            holder.checkBox?.isChecked = false
            holder.checkedText?.text = "No Cumple"
        }
        holder.checkBox?.setOnClickListener {
            if (holder.checkBox?.isChecked!!) {
                holder.mItem?.IsValided = true
                holder.checkBox?.isChecked = true
                holder.checkedText?.text = "Cumple"
                mListener?.updateValidityItem(holder.mItem!!, position)
            } else {
                holder.mItem?.IsValided = false
                holder.checkBox?.isChecked = false
                holder.checkedText?.text = "No Cumple"
                mListener?.updateValidityItem(holder.mItem!!, position)
            }
        }
    }

    interface OnInspectReqListener {
        fun updateValidityItem(mItem: RequirementsList, pos: Int)
        fun updateValidityInputItem(mItem: RequirementsList, pos: Int, input: String)
    }

}