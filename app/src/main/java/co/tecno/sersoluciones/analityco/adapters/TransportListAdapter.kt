package co.tecno.sersoluciones.analityco.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.databinding.SurveyCardAdapterBinding

class TransportListAdapter : RecyclerView.Adapter<TransportListAdapter.ViewHolder>() {

    var data = listOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount() = data.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): String {
        return data[position]
    }

    class ViewHolder private constructor(val binding: SurveyCardAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.name = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SurveyCardAdapterBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}