package co.tecno.sersoluciones.analityco.fragments.employer;

import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.google.gson.Gson;


import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.employer.EmployerRecyclerViewAdapter;
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListEmployerInteractionListener}
 * interface.
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EmployerListFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListEmployerInteractionListener mListener;
    private EmployerRecyclerViewAdapter adapter;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private ArrayList<ObjectList> mValues;
    private ArrayList<String> authorizedCompanies;
    private User user;


    @SuppressWarnings("unused")
    public static EmployerListFragment newInstance(int columnCount) {
        EmployerListFragment fragment = new EmployerListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mValues = new ArrayList<>();
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_EMPLOYERS_URL,
                Request.Method.GET, "", "", true, true);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {

            MyPreferences preferences = new MyPreferences(getContext());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            authorizedCompanies = new ArrayList<>();
            if (!user.IsAdmin && !user.IsSuperUser) {
                for (CompanyList c : user.Companies) {
                    if (c.Permissions != null && c.Permissions.contains("employers.view"))
                        authorizedCompanies.add(c.Id);
                }
            }
            adapter = new EmployerRecyclerViewAdapter(getActivity(), mListener, mValues);
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
        }
        updateList();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListEmployerInteractionListener) {
            mListener = (OnListEmployerInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(requestBroadcastReceiver);
    }

    public void syncData(String item) {
        if (item != null) {
            String selection = "((" + DBHelper.EMPLOYER_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_DOC_NUM + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_COMPANY_ID + " like ?) OR ("
                    + DBHelper.EMPLOYER_TABLE_COLUMN_ADDRESS + " like ?))";
            item = String.format("%%%s%%", item);
            String[] selectionArgs = new String[]{item, item, item, item};
            switch (mColumnCount) {
                case 1:
                    break;
                case 2:
                    selection += " AND (" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, item, "1"};
                    break;
                case 3:
                    selection += " AND (" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, item, "0"};
                    break;
                case 4:
                    selection += " AND (" + DBHelper.EMPLOYER_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ?)";
                    selectionArgs = new String[]{item, item, item, item, "1", "1"};
                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
            bundle.putString(Constantes.KEY_SELECTION, selection);

            updateList(bundle);
        } else {
            updateList();
        }
    }

    public void updateList() {
        updateList(getBundleList());
    }

    private void updateList(Bundle args) {

        String orderBy = null;
        String selection = null;
        String[] selectionArgs = null;
        if (args != null) {
            if (args.containsKey(Constantes.KEY_ORDER_BY))
                orderBy = args.getString(Constantes.KEY_ORDER_BY);
            if (args.containsKey(Constantes.KEY_SELECTION_ARGS))
                selectionArgs = args.getStringArray(Constantes.KEY_SELECTION_ARGS);
            if (args.containsKey(Constantes.KEY_SELECTION))
                selection = args.getString(Constantes.KEY_SELECTION);
        }

        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_EMPLOYER_URI, null, selection, selectionArgs, orderBy);
        mValues.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    ObjectList mItem = new Gson().fromJson(Utils.cursorToJObject(cursor).toString(), ObjectList.class);
                    if (!authorizedCompanies.isEmpty() && !user.IsSuperUser && !user.IsAdmin) {
                        if (authorizedCompanies.contains(mItem.CompanyInfoParentId))
                            mValues.add(mItem);
                    } else if (user.IsSuperUser || user.IsAdmin) mValues.add(mItem);
                } while (cursor.moveToNext());
            }
            adapter.notifyDataSetChanged();
            cursor.close();
        }
    }

    private Bundle getBundleList() {
        Bundle bundle = new Bundle();
        String selection = "";
        String[] selectionArgs;
        switch (mColumnCount) {
            case 1:
                break;
            case 2:
                selection += "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 3:
                selection += "(" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"0"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 4:
                selection += "(" + DBHelper.EMPLOYER_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.EMPLOYER_TABLE_COLUMN_ACTIVE + " = ?)";
                selectionArgs = new String[]{"1", "1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
        }
        MyPreferences preferences = new MyPreferences(getActivity());
        String orderBy = preferences.getOrderEmployerBy();
        bundle.putString(Constantes.KEY_ORDER_BY, orderBy);
        return bundle;
    }

    /**
     * Metodo especializada en recibir la respuesta de las peticiones enviadas al servidor
     */
    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SEND_REQUEST:
                updateList();
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:

                // Toast.makeText(getActivity(), "Modo Offline", Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
