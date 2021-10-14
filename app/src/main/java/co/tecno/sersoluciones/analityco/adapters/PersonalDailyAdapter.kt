package co.tecno.sersoluciones.analityco.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.databinding.PersonalRealtimeItemBinding
import co.tecno.sersoluciones.analityco.models.PersonalRealTime
import java.text.SimpleDateFormat

class PersonalDailyAdapter : RecyclerView.Adapter<PersonalDailyAdapter.ViewHolder>() {

    var data = listOf<PersonalRealTime>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): PersonalRealTime {
        return data[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: PersonalRealtimeItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PersonalRealTime) {
            binding.item = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PersonalRealtimeItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

class ConversationDiffCallback : DiffUtil.ItemCallback<PersonalRealTime>() {
    override fun areItemsTheSame(oldItem: PersonalRealTime, newItem: PersonalRealTime): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PersonalRealTime, newItem: PersonalRealTime): Boolean {
        return oldItem.personalId == newItem.personalId
    }
}

@BindingAdapter("setFormattedTime")
fun TextView.setFormattedTime(timestamp: Long) {
    if (timestamp == 0L) text = ""
    else
        text = convertLongToTimeString(timestamp)
}

@SuppressLint("SimpleDateFormat")
fun convertLongToTimeString(systemTime: Long): String {
    return SimpleDateFormat("HH:mm")
            .format(systemTime).toString()
}
