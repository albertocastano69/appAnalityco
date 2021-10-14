package co.tecno.sersoluciones.analityco.fragments.personal.wizard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectCompanyFragment.OnSelectCompanyFragmentListener} interface
 * to handle interaction events.
 */
public class SelectCompanyFragment extends Fragment {


    @BindView(R.id.companies)
    RecyclerView recyclerViewCompanies;
    @BindView(R.id.card_view_detail)
    CardView cardViewDetail;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.text_validity)
    TextView textValidity;
    @BindView(R.id.text_sub_name)
    TextView textName;
    @BindView(R.id.text_name)
    TextView textSubName;
    @BindView(R.id.label_validity)
    TextView labelValidity;
    @BindView(R.id.text_active)
    TextView textActive;

    private OnSelectCompanyFragmentListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_company, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        cardViewDetail.setVisibility(View.GONE);
        updateListCompanies();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectCompanyFragmentListener) {
            mListener = (OnSelectCompanyFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnSelectCompanyFragmentListener {

        void onCompanySelect(CompanyList companyList);
    }

    private void updateListCompanies() {
        MyPreferences preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewCompanies.setLayoutManager(mLayoutManager);
        recyclerViewCompanies.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewCompanies.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewCompanies.addItemDecoration(mDividerItemDecoration);
        CompaniesRecyclerAdapter adapterProjects = new CompaniesRecyclerAdapter(getActivity(), user.Companies);
        recyclerViewCompanies.setAdapter(adapterProjects);
        log(profile);
        if (user.Companies.size() == 1) {
            mListener.onCompanySelect(user.Companies.get(0));
        } else if (user.Companies.isEmpty()) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity())
                    .setTitle("Sin compañia")
                    .setMessage("No puede asociar personal")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            android.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private class CompaniesRecyclerAdapter extends CustomListInfoRecyclerView<CompanyList> {

        CompaniesRecyclerAdapter(Context context, ArrayList<CompanyList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final CompanyList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.DocumentNumber);
            holder.textValidity.setText(mItem.Address);
            boolean isActive = true;
            Date now = new Date();
            String mDate = "" + mItem.FinishDate;
            try {
                if (!mDate.isEmpty() && !mDate.equals("null")) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    mDate = dateFormat.format(date);
                    if (date.before(now)) {
                        isActive = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.iconIsActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            holder.logo.setImageResource(R.drawable.image_not_available);
            holder.btnEdit.setVisibility(View.GONE);

            holder.cardViewDetail.setBackgroundColor(Color.WHITE);
            if (mItem.IsSelected)
                holder.cardViewDetail.setBackgroundColor(Color.GREEN);

            if (!isActive) {
                holder.stateIcon.setVisibility(View.VISIBLE);
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
            }
            //Log.e("logo", mItem.Logo);
            if (mItem.Logo != null) {
                String url = Constantes.URL_IMAGES + mItem.Logo;

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }
            holder.cardViewDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        for (CompanyList companyList : mItems) {
                            companyList.IsSelected = false;
                        }
                        mItem.IsSelected = true;
                        notifyDataSetChanged();
                        mListener.onCompanySelect(mItem);
                    }
                }
            });
            holder.btnEdit.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    public void fillCompany(CompanyList mItem) {

        cardViewDetail.setVisibility(View.VISIBLE);
        cardViewDetail.setBackgroundResource(R.color.accent);
        textValidity.setText(mItem.Address);
        textName.setText(mItem.Name);
        textSubName.setText(mItem.DocumentNumber);

        logo.setImageResource(R.drawable.image_not_available);
        if (mItem.Logo != null) {
            String url = Constantes.URL_IMAGES + mItem.Logo;

            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(logo);
        }

        labelValidity.setVisibility(View.GONE);
        textActive.setVisibility(View.GONE);

        //fragmentTransaction.replace(R.id.container_main, ContractTypeFragment.newInstance(contractTypeList));
    }

}
