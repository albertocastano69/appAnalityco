package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.employer.EmployerRecyclerViewAdapter
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentSelectNavEmployerBinding
import co.tecno.sersoluciones.analityco.models.ContractEnrollment
import co.tecno.sersoluciones.analityco.models.Employer
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.models.PersonalContract
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.nav.ResponseResult
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter.TextWatcherListener
import co.tecno.sersoluciones.analityco.utilities.Utils.cursorToJObject
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject


class SelectNavEmployerFragment : Fragment(), TextWatcherListener {

    private var mValues: ArrayList<ObjectList>? = null
    private var adapter: EmployerRecyclerViewAdapter? = null
    private var contract: ContractEnrollment? = null
    private var moveToPersonal = false
    private var personalContract: PersonalContract? = null
    private var employerId: String? = null
    private var originalEmployerId: String? = null

    @Inject
    lateinit var viewModel: CreatePersonalViewModel
    private lateinit var binding: FragmentSelectNavEmployerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        employerId = ""
        mValues = ArrayList()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectNavEmployerBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.searchEditText.addTextChangedListener(TextWatcherAdapter(binding.searchEditText, this))
        updateListEmployers()

        binding.enableSearchBtn.setOnClickListener { enableSearch() }
        binding.cancelAction.setOnClickListener { cleanSearch() }

        val arguments = SelectNavEmployerFragmentArgs.fromBundle(requireArguments())

        val contract = arguments.contract
        val personalContract = arguments.personalContract
        moveToPersonal = arguments.moveToPersonal

        binding.controlButtons.nextButton.text = "FINALIZAR"
        binding.controlButtons.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.controlButtons.nextButton.setOnClickListener {
            submit()
        }
        viewModel.responseJoinContract.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                when (it) {
                    is ResponseResult.Success -> {
                        Toast.makeText(activity, "Empleado asociado exitosamente", Toast.LENGTH_SHORT).show()
                        val i = Intent()
                        i.putExtra("personalInfoId", personalContract.PersonalCompanyInfoId)
                        activity?.setResult(Activity.RESULT_OK, i)
                        viewModel.navigateToRequirements()
                        /*viewModel.clearForm()
                        activity?.finish()*/
                    }
                    is ResponseResult.Error -> {
                        if (it.exception.code() == 403) {
                            Toast.makeText(requireContext(), "Este usuario no tiene permisos para esta accion", Toast.LENGTH_SHORT).show()
                            viewModel.clearForm()
                            requireActivity().finish()
                        } else {

                        }
                    }
                }
            }
        })
        viewModel.navigateToRequirementFragment.observe(viewLifecycleOwner,androidx.lifecycle.Observer {
            it?.let {
                if (it) {
                    if (contract.Id != null) {
                        findNavController()
                                .navigate(
                                        SelectNavEmployerFragmentDirections.actionSelectNavEmployerFragmentToRequerimentsFragment(contract.Id!!)
                                )
                        viewModel.doneNavigatoRequeriments()
                    }else{
                        DebugLog.logW("Id del contract ${contract.Id}")
                    }
                }
            }
        })
        fillData(contract, moveToPersonal, personalContract)
    }

    private fun fillData(contract: ContractEnrollment, moveToPersonal: Boolean, personalContract: PersonalContract) {
        val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
        val selectionArgs = arrayOf("1")
        val bundle = Bundle()
        bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, null)
        bundle.putString(Constantes.KEY_SELECTION, null)
        updateList(bundle)
        this.contract = contract
        this.personalContract = personalContract
        this.moveToPersonal = moveToPersonal
        originalEmployerId = contract.EmployerId
        fillData(contract.EmployerId)
    }

    private fun fillData(employerId: String?) {
        this.employerId = employerId
        val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_SERVER_ID + " = ? )"
        val selectionArgs = arrayOf(employerId)
        val cursor = requireActivity().contentResolver
                .query(Constantes.CONTENT_EMPLOYER_URI, null, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val mItem =
                    Gson().fromJson(cursorToJObject(cursor).toString(), Employer::class.java)
            binding.textName.text = mItem.Name
            binding.textAddress.text = String.format("%s %s", mItem.DocumentType, mItem.DocumentNumber)
            binding.textValidity.text = mItem.Address
            binding.labelValidity.visibility = View.GONE
            binding.labelActive.visibility = View.GONE
            /*if (mItem.IsActive) {
                if (mItem.Expiry) binding.stateIcon.setImageResource(R.drawable.state_icon) else binding.stateIcon.visibility = View.GONE
            } else {
                binding.stateIcon.setImageResource(R.drawable.state_icon_red)
            }*/
            val url = Constantes.URL_IMAGES + mItem.Logo
            val format = url.split(Pattern.quote(".").toRegex()).toTypedArray()
            if (format[format.size - 1] == "svg") {
                GlideApp.with(requireActivity())
                        .`as`(PictureDrawable::class.java)
                        .apply(
                                RequestOptions()
                                        .placeholder(R.drawable.loading_animation)
                                        .error(R.drawable.image_not_available)
                        )
                        .fitCenter()
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                        .load(url)
                        .into(binding.logo)
            } else {
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(binding.logo)
            }
        }
    }

    private fun updateListEmployers() {
        adapter = EmployerRecyclerViewAdapter(requireActivity(), OnListEmployerInteractionListener { item, _ ->
            item?.let {
                fillData(item.Id)
            }
        }, mValues!!)
        binding.employersList.adapter = adapter
        binding.employersList.visibility = View.GONE
        val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
        val selectionArgs = arrayOf("1")
        val bundle = Bundle()
        bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, null)
        bundle.putString(Constantes.KEY_SELECTION, null)
        updateList(bundle)
    }

    private fun updateList(args: Bundle?) {
        var orderBy: String? = null
        var selection: String? = null
        var selectionArgs: Array<String?>? = null
        if (args != null) {
            if (args.containsKey(Constantes.KEY_ORDER_BY)) orderBy = args.getString(Constantes.KEY_ORDER_BY)
            if (args.containsKey(Constantes.KEY_SELECTION_ARGS)) selectionArgs = args.getStringArray(Constantes.KEY_SELECTION_ARGS)
            if (args.containsKey(Constantes.KEY_SELECTION)) selection = args.getString(Constantes.KEY_SELECTION)
            val cursor =
                    requireActivity().contentResolver.query(Constantes.CONTENT_EMPLOYER_URI, null, selection, selectionArgs, orderBy)
            mValues!!.clear()
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val mItem =
                                Gson().fromJson(
                                        cursorToJObject(cursor).toString(),
                                        ObjectList::class.java
                                )
                        mValues!!.add(mItem)
                    } while (cursor.moveToNext())
                }
                adapter!!.swap()
                cursor.close()
            }
        }else{
            val cursor =
                    requireActivity().contentResolver.query(Constantes.CONTENT_EMPLOYER_URI, null, null, null, orderBy)
            mValues!!.clear()
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val mItem =
                                Gson().fromJson(
                                        cursorToJObject(cursor).toString(),
                                        ObjectList::class.java
                                )
                        mValues!!.add(mItem)
                    } while (cursor.moveToNext())
                }
                adapter!!.swap()
                cursor.close()
            }
        }

    }

    private fun enableSearch() {
        binding.enableSearchBtn.visibility = View.GONE
        binding.searchEditText.visibility = View.VISIBLE
        binding.employersList.visibility = View.VISIBLE
    }

    override fun onTextChanged(view: EditText, text: String) {
        syncData(text)
    }

    private fun cleanSearch() {
        syncData(null)
        binding.searchEditText.setText("")
    }

    private fun submit() {
        personalContract!!.EmployerId = employerId
        if (originalEmployerId == employerId) personalContract!!.EmployerId = null

        var path = "PersonalInfo"
        if (moveToPersonal) {
            path = "MovePersonalInfo"
        }
        showProgress(true)
        viewModel.createPersonalContract(personalContract!!, contract!!.Id!!, path)
    }

    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
        binding.mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                if (show) 1.toFloat() else 0.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    private fun syncData(item: String?) {
        if (item != null) {
            var selection = ("((" + DBHelper.EMPLOYER_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_DOC_NUM + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_ADDRESS + " like ?))")
            //selection += " AND (" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
            val itemSelection = String.format("%%%s%%", item)
            val selectionArgs = arrayOf(itemSelection, itemSelection, itemSelection)
            val bundle = Bundle()
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs)
            bundle.putString(Constantes.KEY_SELECTION, selection)
            updateList(bundle)
        } else {
            val selection = "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )"
            val selectionArgs = arrayOf("1")
            val bundle = Bundle()
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, null)
            bundle.putString(Constantes.KEY_SELECTION, null)
            updateList(bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}