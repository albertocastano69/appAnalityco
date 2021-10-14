package co.tecno.sersoluciones.analityco.ui.createPersonal.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.databinding.ItemCompanyAdapterBinding
import co.tecno.sersoluciones.analityco.databinding.ItemContractAdapterBinding
import co.tecno.sersoluciones.analityco.databinding.ItemProjectAdapterBinding
import co.tecno.sersoluciones.analityco.models.CompanyList
import co.tecno.sersoluciones.analityco.models.ContractEnrollment
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.models.ProjectList
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

private const val ITEM_VIEW_COMPANY = 0
private const val ITEM_VIEW_PROJECT = 1
private const val ITEM_VIEW_CONTRACT = 2

class DataItemListAdapter(private val onClickListener: OnClickListener<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//  :  ListAdapter<DataItem, RecyclerView.ViewHolder>(DataItemDiffCallback()) {

    val adapterScope = CoroutineScope(Dispatchers.Default)

    var data = mutableListOf<DataItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    fun getItem(position: Int) = data[position]

    inline fun <reified T> addSubmitList(list: List<T>?) {
        adapterScope.launch {
            val items = when (T::class) {
                ProjectList::class -> list?.map {
                    DataItem.ProjectItem(it as ProjectList)
                }
                CompanyList::class -> list?.map {
                    DataItem.CompanyItem(it as CompanyList)
                }
                ContractEnrollment::class -> list?.map {
                    DataItem.ContractItem(it as ContractEnrollment)
                }
                else -> {
                    listOf(DataItem.Header)
                }
            }
            withContext(Dispatchers.Main) {
                items?.let {
                    data = items.toMutableList()
                }
//                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CompanyViewHolder -> {
                val item = getItem(position) as DataItem.CompanyItem
                holder.bind(item.item, onClickListener)
            }
            is ProjectViewHolder -> {
                val item = getItem(position) as DataItem.ProjectItem
                holder.bind(item.item, onClickListener)
            }
            is ContractViewHolder -> {
                val item = getItem(position) as DataItem.ContractItem
                holder.bind(item.item, onClickListener)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
//        logW(Gson().toJson(getItem(position)))
        return when (getItem(position)) {
            is DataItem.CompanyItem -> ITEM_VIEW_COMPANY
            is DataItem.ProjectItem -> ITEM_VIEW_PROJECT
            is DataItem.ContractItem -> ITEM_VIEW_CONTRACT
            else -> ITEM_VIEW_COMPANY // throw ClassCastException("Unknown viewType")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_COMPANY -> CompanyViewHolder.from(parent)
            ITEM_VIEW_PROJECT -> ProjectViewHolder.from(parent)
            ITEM_VIEW_CONTRACT -> ContractViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    class CompanyViewHolder private constructor(val binding: ItemCompanyAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CompanyList, clickListener: OnClickListener<Any>) {
            binding.item = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): CompanyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCompanyAdapterBinding.inflate(layoutInflater, parent, false)
                return CompanyViewHolder(binding)
            }
        }
    }
    fun filtar(filtroEmployer: ArrayList<ObjectList>) {

        notifyDataSetChanged()
    }

    class ProjectViewHolder private constructor(val binding: ItemProjectAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProjectList, clickListener: OnClickListener<Any>) {
            binding.item = item
            binding.cardViewDetail.isChecked = item.IsSelected
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ProjectViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemProjectAdapterBinding.inflate(layoutInflater, parent, false)
                return ProjectViewHolder(binding)
            }
        }
    }

    class ContractViewHolder private constructor(val binding: ItemContractAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContractEnrollment, clickListener: OnClickListener<Any>) {
            binding.item = item
            binding.clickListener = clickListener
            binding.textName.setTypeface(binding.textName.typeface, Typeface.BOLD)
            binding.phone.visibility = View.GONE
            binding.iconIsActive.visibility = View.GONE
            binding.textValidity2.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE

            if (!item.isJoin) {
                binding.vigence.visibility = View.GONE
                binding.branchOfficeLine.visibility = View.GONE
                binding.textValidity.visibility = View.GONE
            }
            binding.sectionIcon.visibility = View.VISIBLE

            if (!item.IsActive) {
                binding.textValidity.setTextColor(Color.RED)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ContractViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemContractAdapterBinding.inflate(layoutInflater, parent, false)
                return ContractViewHolder(binding)
            }
        }
    }

    class OnClickListener<T>(val clickListener: (item: T) -> Unit) {
        fun onClick(item: T) = clickListener(item)
    }

}

class DataItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {

    data class CompanyItem(val item: CompanyList) : DataItem() {
        override val id = item.Id
    }

    data class ProjectItem(val item: ProjectList) : DataItem() {
        override val id = item.Id
    }

    data class ContractItem(val item: ContractEnrollment) : DataItem() {
        override val id = item.Id!!
    }

    object Header : DataItem() {
        override val id = ""
    }

    abstract val id: String
}