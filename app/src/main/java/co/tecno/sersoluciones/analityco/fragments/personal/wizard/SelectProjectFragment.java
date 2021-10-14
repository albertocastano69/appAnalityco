package co.tecno.sersoluciones.analityco.fragments.personal.wizard;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.project.ProjectRecyclerViewAdapter;
import co.tecno.sersoluciones.analityco.callback.OnListProjectInteractionListener;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.Utils;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListProjectInteractionListener}
 * interface.
 * Created by Ser Soluciones SAS on 01/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class SelectProjectFragment extends Fragment {

    private Unbinder unbinder;
    private OnListProjectInteractionListener mListener;
    private String projectId;
    private ProjectRecyclerViewAdapter adapter;
    private String companyId;
    private boolean first;
    @BindView(R.id.layout_info)
    CardView cardViewInfo;
    private ArrayList<ProjectList> mValues;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SelectProjectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectId = "";
        companyId = "";
        first = true;
        mValues = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        RecyclerView recyclerView = view.findViewById(R.id.list);
        adapter = new ProjectRecyclerViewAdapter(getActivity(), mListener, true, mValues);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_IS_SELECTED, false);
        getActivity().getContentResolver().update(Constantes.CONTENT_PROJECT_URI, contentValues, null, null);

        updateList();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.info_button)
    public void hideInfo() {
        cardViewInfo.setVisibility(View.GONE);
    }

    public boolean submit() {
        return !projectId.isEmpty();
    }

    public void updateFragment(String companyId) {
        this.companyId = companyId;
        projectId = processGeoJsonOffline();
        if (!projectId.isEmpty()) {
            //((TabsProjectFragment) fragment).syncData(mPosition, projectId, companyId);
            /*Bundle bundle = new Bundle();
            String selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID + " = ? )";
            String[] selectionArgs = new String[]{projectId};
            bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
            bundle.putString(Constantes.KEY_SELECTION, selection);
            getActivity().getSupportLoaderManager().restartLoader(0, bundle, this);*/
            if (first) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_IS_SELECTED, true);
                String selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID + " = ? )";
                String[] selectionArgs = new String[]{projectId};
                getActivity().getContentResolver().update(Constantes.CONTENT_PROJECT_URI,
                        contentValues, selection, selectionArgs);
                first = false;
                Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_PROJECT_URI, null,
                        selection, selectionArgs, null);
                cursor.moveToFirst();
                JSONObject jsonObject = Utils.cursorToJObject(cursor);
                if (null != mListener) {
                    mListener.onProjectInteraction(new Gson().fromJson(jsonObject.toString(), ProjectList.class), null);
                }
            }
        } else {
            //((TabsProjectFragment) fragment).updateList(mPosition, companyId);
            logW("No hay proyectos segun la ubicacion del dispositivo");
        }
        updateList();
    }

    private String processGeoJsonOffline() {

        String _id = "";
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver()
                .query(Constantes.CONTENT_PROJECT_URI, null, null, null, DBHelper.PROJECT_TABLE_COLUMN_NAME);

        if (cursor == null || cursor.getCount() == 0) {
            logE("No projects");
            return "";
        }
        // Obtenemos los índices de las columnas
        int geofenceColumn = cursor.getColumnIndex(DBHelper.PROJECT_TABLE_COLUMN_GEOFENCE);
        int projectId = cursor.getColumnIndex(DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID);
        int companyColumn = cursor.getColumnIndex(DBHelper.PROJECT_TABLE_COLUMN_COMPANY_INFO_ID);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String geofenceJson = cursor.getString(geofenceColumn);
            logW("companyColumn: " + cursor.getString(companyColumn) + ", is active: " +
                    cursor.getInt(cursor.getColumnIndex(DBHelper.PROJECT_TABLE_COLUMN_ACTIVE)));
            try {
                boolean isInGeo = isIngeo(geofenceJson);
                if (isInGeo) {
                    _id = cursor.getString(projectId);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return _id;
    }

    private boolean isIngeo(String geofenceJson) throws JSONException {

        JSONObject jsonObjectGeo = new JSONObject(geofenceJson);
        JSONArray jsonArray = jsonObjectGeo.getJSONArray("coordinates");
        final List<LatLng> latLngsGoogle = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            for (int j = 0; j < jsonArray.getJSONArray(i).length(); j++) {
                JSONArray jsonArrayLatLng = jsonArray.getJSONArray(i).getJSONArray(j);
                latLngsGoogle.add(new LatLng(jsonArrayLatLng.getDouble(1), jsonArrayLatLng.getDouble(0)));
            }
        }

        return false;
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

    private void updateList() {
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
                    mValues.add(mItem);
                } while (cursor.moveToNext());
            }
            adapter.notifyDataSetChanged();
            cursor.close();
        }
    }

    private Bundle getBundleList() {
        Bundle bundle = new Bundle();
        String[] selectionArgs;
        String selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_COMPANY_INFO_ID + " = ? )";
        selection += " AND (" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )";
        selectionArgs = new String[]{companyId, "1"};
        //log("selection: " + selection);
        bundle.putStringArray(Constantes.KEY_SELECTION_ARGS, selectionArgs);
        bundle.putString(Constantes.KEY_SELECTION, selection);

        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_PROJECT_URI, null,
                selection, selectionArgs, null);
        if (first && cursor != null && cursor.getCount() == 1) {
            first = false;
            cursor.moveToFirst();
            JSONObject jsonObject = Utils.cursorToJObject(cursor);
            if (null != mListener) {
                mListener.onProjectInteraction(new Gson().fromJson(jsonObject.toString(), ProjectList.class), null);
            }
        }

        return bundle;
    }

}