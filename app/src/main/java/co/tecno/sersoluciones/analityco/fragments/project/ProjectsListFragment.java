package co.tecno.sersoluciones.analityco.fragments.project;

import android.content.ContentValues;
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

import co.tecno.sersoluciones.analityco.ProjectsListActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.project.ProjectRecyclerViewAdapter;
import co.tecno.sersoluciones.analityco.callback.OnListProjectInteractionListener;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListProjectInteractionListener}
 * interface.
 * Created by Ser Soluciones SAS on 01/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProjectsListFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListProjectInteractionListener mListener;
    private ProjectRecyclerViewAdapter adapter;
    private RequestBroadcastReceiver getJSONBroadcastReceiver;
    private ArrayList<ProjectList> mValues;
    private ArrayList<String> authorizedCompanies;
    private User user;

    public static ProjectsListFragment newInstance(int columnCount) {
        ProjectsListFragment fragment = new ProjectsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getJSONBroadcastReceiver = new RequestBroadcastReceiver(this);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mValues = new ArrayList<>();
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
                    if (c.Permissions != null && c.Permissions.contains("projects.view"))
                        authorizedCompanies.add(c.Id);
                }
            }

            adapter = new ProjectRecyclerViewAdapter(getActivity(), mListener, mValues);
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
        if (context instanceof OnListProjectInteractionListener) {
            mListener = (OnListProjectInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListProjectInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_PROJECT_URI, null, selection, selectionArgs, orderBy);
        mValues.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    ProjectList mItem = new Gson().fromJson(Utils.cursorToJObject(cursor).toString(), ProjectList.class);
                    if (!authorizedCompanies.isEmpty() && !user.IsSuperUser && !user.IsAdmin) {
                        if (authorizedCompanies.contains(mItem.CompanyInfoId))
                            mValues.add(mItem);
                    } else if (user.IsSuperUser || user.IsAdmin) mValues.add(mItem);
                } while (cursor.moveToNext());
            }
            adapter.notifyDataSetChanged();
            cursor.close();
        }
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
            String selection = "((" + DBHelper.PROJECT_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.PROJECT_TABLE_COLUMN_NUM_PROJECT + " like ?) OR ("
                    + DBHelper.PROJECT_TABLE_COLUMN_NAME_CITY + " like ?))";
            item = String.format("%%%s%%", item);
            String[] selectionArgs = new String[]{item, item, item};
            switch (mColumnCount) {
                case 1:
                    break;
                case 2:
                    selection += " AND (" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, "1"};
                    break;
                case 3:
                    selection += " AND (" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, "0"};
                    break;
                case 4:
                    selection += " AND (" + DBHelper.COMPANY_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ?)";
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

    private Bundle getBundleList() {
        Bundle bundle = new Bundle();
        String selection = "";
        String[] selectionArgs;
        switch (mColumnCount) {
            case 1:
                break;
            case 2:
                selection += "(" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 3:
                selection += "(" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"0"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 4:
                selection += "(" + DBHelper.PROJECT_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ?)";
                selectionArgs = new String[]{"1", "1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
        }
        MyPreferences preferences = new MyPreferences(getActivity());
        String orderBy = preferences.getOrderProjectBy();
        bundle.putString(Constantes.KEY_ORDER_BY, orderBy);
        return bundle;
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SEND_REQUEST:
                log("actualizacion exitosa");
                //logW(res);
                updateList();
                ((ProjectsListActivity) getActivity()).setQuantities();
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:

                // Toast.makeText(getActivity(), "Modo Offline", Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
