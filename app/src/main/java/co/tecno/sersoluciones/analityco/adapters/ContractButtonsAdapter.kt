package co.tecno.sersoluciones.analityco.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databinding.ContractButtonsItemBinding
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class ContractButtons(
        val name: String,
        val type: String,
        val imageId: Int,
        var enable: Boolean = true
)

class ContractButtonsAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<ContractButtonsAdapter.ViewHolder>() {

    var data = listOf<ContractButtons>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    private fun getItem(position: Int): ContractButtons {
        return data[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ContractButtonsItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContractButtons, clickListener: OnClickListener) {
            binding.item = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContractButtonsItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class OnClickListener(val clickListener: (item: ContractButtons) -> Unit) {
        fun onClick(item: ContractButtons) = clickListener(item)
    }

}

@BindingAdapter("fabImage")
fun fabImage(fab: FloatingActionButton, imgId: Int?) {
    imgId?.let {
        fab.setImageResource(imgId)
    }
}

@BindingAdapter("fabEnable")
fun fabEnable(fab: FloatingActionButton, enable: Boolean) {
    fab.isEnabled = enable
    if (!enable)
        fab.backgroundTintList = ContextCompat.getColorStateList(fab.context, R.color.gray)
    else
        fab.backgroundTintList = ContextCompat.getColorStateList(fab.context, R.color.blue_aqua)
}