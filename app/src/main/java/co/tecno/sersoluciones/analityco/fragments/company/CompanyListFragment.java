package co.tecno.sersoluciones.analityco.fragments.company;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;

import com.android.volley.Request;
import com.google.gson.Gson;


import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.CompanyListActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.company.MyCompanyRecyclerViewAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CompanyListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MyCompanyRecyclerViewAdapter adapter;
    private GetJSONBroadcastReceiver getJSONBroadcastReceiver;
    private ArrayList<CompanyList> mValues;

    public static CompanyListFragment newInstance(int columnCount) {
        CompanyListFragment fragment = new CompanyListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getJSONBroadcastReceiver = new GetJSONBroadcastReceiver();
        mValues = new ArrayList<>();
        ContentValues values = new ContentValues();
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.CREATE_COMPANY_URL,
                Request.Method.GET, "", paramsQuery, true, true);

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

            adapter = new MyCompanyRecyclerViewAdapter(getActivity(), mListener, mValues);
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
            //recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(CompanyList item, ImageView imageView);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(getJSONBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(getJSONBroadcastReceiver);
    }

    public void syncData(String item) {
        if (item != null) {
            String selection = "((" + DBHelper.COMPANY_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.COMPANY_TABLE_COLUMN_DOC_NUM + " like ?) OR ("
                    + DBHelper.COMPANY_TABLE_COLUMN_ADDRESS + " like ? ))";
            item = String.format("%%%s%%", item);
            String[] selectionArgs = new String[]{item, item, item};

            switch (mColumnCount) {
                case 1:
                    break;
                case 2:
                    selection += " AND (" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, "1"};
                    break;
                case 3:
                    selection += " AND (" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, "0"};
                    break;
                case 4:
                    selection += " AND (" + DBHelper.COMPANY_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ?)";
                    selectionArgs = new String[]{item, item, item, "1", "1"};
                    break;
            }
            log("selection: " + selection);
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

        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_COMPANY_URI, null, selection, selectionArgs, orderBy);
        mValues.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    CompanyList mItem = new Gson().fromJson(Utils.cursorToJObject(cursor).toString(), CompanyList.class);
                    mValues.add(mItem);
                } while (cursor.moveToNext());
            }
            adapter.swap();
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
                selection += "(" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 3:
                selection += "(" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"0"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 4:
                selection += "(" + DBHelper.COMPANY_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ?)";
                selectionArgs = new String[]{"1", "1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
        }
        MyPreferences preferences = new MyPreferences(getActivity());
        String orderBy = preferences.getOrderCompanyBy();
        bundle.putString(Constantes.KEY_ORDER_BY, orderBy);
        return bundle;
    }

    /**
     * Clase especializada en recibir la respuesta de las peticiones enviadas al servidor
     */
    public class GetJSONBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int option = intent.getIntExtra(Constantes.OPTION_JSON_BROADCAST, 0);
            switch (option) {
                case Constantes.SEND_REQUEST:
                    log("actualizacion exitosa");
                    updateList();
                    ((CompanyListActivity) getActivity()).setQuantities();
                    break;
                case Constantes.NOT_INTERNET:
                case Constantes.BAD_REQUEST:
                case Constantes.TIME_OUT_REQUEST:

                    // Toast.makeText(getActivity(), "Modo Offline", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }
}
