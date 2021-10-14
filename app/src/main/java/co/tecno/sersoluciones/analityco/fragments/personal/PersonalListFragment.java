package co.tecno.sersoluciones.analityco.fragments.personal;

import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.google.gson.Gson;


import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.PersonalListActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.personal.PersonalRecyclerViewAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.listeners.EndlessRecyclerOnScrollListener;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListPersonalInteractionListener}
 * interface.
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class PersonalListFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListPersonalInteractionListener mListener;
    private PersonalRecyclerViewAdapter adapter;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private EndlessRecyclerOnScrollListener mScrollListener;
    private ArrayList<PersonalList> mValues;
    private ArrayList<String> authorizedCompanies;
    private User user;

    public static PersonalListFragment newInstance(int columnCount) {
        PersonalListFragment fragment = new PersonalListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        mValues = new ArrayList<>();
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    public void syncPersonal() {
        log("syncPersonal");
        if (mySwipeRefreshLayout != null) mySwipeRefreshLayout.setRefreshing(true);
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_PERSONAL_URL,
                Request.Method.GET, "", "", true, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_personal_list);
        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setEnabled(false);
        MyPreferences preferences = new MyPreferences(getContext());
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);
        authorizedCompanies = new ArrayList<>();
        if (!user.IsAdmin && !user.IsSuperUser) {
            for (CompanyList c : user.Companies) {
                if (c.Permissions != null && c.Permissions.contains("personals.view"))
                    authorizedCompanies.add(c.Id);
            }
        }
        // Seteamos los colores que se usarán a lo largo de la animación
        /*mySwipeRefreshLayout.setColorSchemeResources(
                R.color.s1,
                R.color.s2,
                R.color.s3,
                R.color.s4
        );*/

        mySwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        mySwipeRefreshLayout.setDistanceToTriggerSync(20);
        mySwipeRefreshLayout.setOnRefreshListener(
                this::syncPersonal
        );
        // Set the adapter
        adapter = new PersonalRecyclerViewAdapter(getActivity(), mListener, mValues);
        Context context = view.getContext();
        //RecyclerView recyclerView = (RecyclerView) view;
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // enable pull up for endless loading
        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore() {
                // do something...

                // after loading is done, please call the following method to re-enable onLoadMore
                // usually it should be called in onCompleted() method
                mScrollListener.setLoading(false);
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);
        recyclerView.setAdapter(adapter);
        updateList();
        return view;
    }

    private void refreshClose() {
        if (mySwipeRefreshLayout != null)
            mySwipeRefreshLayout.setRefreshing(false);
        updateList();
    }@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListPersonalInteractionListener) {
            mListener = (OnListPersonalInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListPersonalInteractionListener");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListPersonalInteractionListener {
        void onListFragmentInteraction(PersonalList item, ImageView imageView);

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
            String selection = "((" + DBHelper.PERSONAL_TABLE_COLUMN_LASTNAME + " like ?) OR ("
                    + DBHelper.PERSONAL_TABLE_COLUMN_DOC_NUM + " like ?) OR ("
                    + DBHelper.PERSONAL_TABLE_COLUMN_NAME + " like ?) OR ("
                    + DBHelper.PERSONAL_TABLE_COLUMN_JOB + " like ?))";
            item = String.format("%%%s%%", item);
            String[] selectionArgs = new String[]{item, item, item, item};
            switch (mColumnCount) {
                case 1:
                    break;
                case 2:
                    selection += " AND (" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, item, "1"};
                    break;
                case 3:
                    selection += " AND (" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ? )";
                    selectionArgs = new String[]{item, item, item, item, "0"};
                    break;
                case 4:
                    selection += " AND (" + DBHelper.PERSONAL_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ?)";
                    selectionArgs = new String[]{item, item, item, item, "1", "1"};
                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
            bundle.putString(Constantes.KEY_SELECTION, selection);
            MyPreferences preferences = new MyPreferences(getActivity());
            bundle.putString(Constantes.KEY_ORDER_BY, preferences.getOrderPersonalBy());
            updateList(bundle);
        } else {
            updateList();
        }
    }

    public void updateList(String orderBy) {
        updateList(getBundleList(orderBy));
    }

    public void updateList() {
        updateList(getBundleList(""));
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

        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_PERSONAL_URI, null, selection, selectionArgs, orderBy);
        mValues.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    PersonalList mItem = new Gson().fromJson(Utils.cursorToJObject(cursor).toString(), PersonalList.class);
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

    private Bundle getBundleList(String orderBy) {
        Bundle bundle = new Bundle();
        String selection = "";
        String[] selectionArgs;
        switch (mColumnCount) {
            case 1:
                break;
            case 2:
                selection += "(" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 3:
                selection += "(" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ? )";
                selectionArgs = new String[]{"0"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
            case 4:
                selection += "(" + DBHelper.PERSONAL_TABLE_COLUMN_EXPIRY + " = ? ) and (" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ?)";
                selectionArgs = new String[]{"1", "1"};
                bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
                bundle.putString(Constantes.KEY_SELECTION, selection);
                break;
        }
        MyPreferences preferences = new MyPreferences(getActivity());

        if (!orderBy.isEmpty()) {
            bundle.putString(Constantes.KEY_ORDER_BY, orderBy);
        } else {
            bundle.putString(Constantes.KEY_ORDER_BY, preferences.getOrderPersonalBy());
        }
        return bundle;
    }

    /**
     * Metodo especializada en recibir la respuesta de las peticiones enviadas al servidor
     */
    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SEND_REQUEST:
                log("actualizacion exitosa");
                updateList();
                ((PersonalListActivity) getActivity()).setQuantities();
                refreshClose();
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:

                // Toast.makeText(getActivity(), "Modo Offline", Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
