package co.tecno.sersoluciones.analityco.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import javax.inject.Inject


/**
 * A fragment representing a list of Items.
 */
class RegisterListFragment : Fragment() {

    @Inject
    lateinit var viewModel: PersonalDailyViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_list_list, container, false)
        val adapter = MyRegisterRecyclerViewAdapter(requireActivity())
        if (view is RecyclerView) {
            with(view) {
                this.adapter = adapter
                addItemDecoration(DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL))
                setHasFixedSize(true)
            }
        }

        viewModel.reports.observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                adapter.data = list
            }
        })

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }

}