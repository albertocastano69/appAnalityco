
package co.tecno.sersoluciones.analityco.individualContract;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.IndivudualContractAdapter;
import co.tecno.sersoluciones.analityco.models.InfoIndividualContract;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContractSingleListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContractSingleListFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener{
    List<InfoIndividualContract>infoIndividualContracts;
    IndivudualContractAdapter adapter;
    RecyclerView recyclerView;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListSingleContractInteractionListener mListener;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    public static ContractSingleListFragment newInstance(int mColumnCount) {
        ContractSingleListFragment fragment = new ContractSingleListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, mColumnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        infoIndividualContracts = new ArrayList<>();
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_contract_single_list, container, false);

        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_INDIVIDUALCONTRACTS_URL,
                Request.Method.GET, "", "", true, false);
        recyclerView= view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onStringResult(@Nullable String action, int option, @Nullable String response, @Nullable String url) {
        switch (option) {
            case Constantes.SEND_REQUEST:
                getBundleList(response);
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
                requireActivity().finish();
                break;

        }

    }
    private void LoadList(String response) {

        try {
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonPersonalEmployerInfo,jsonPersonal,jsonIndividualContractStage, jsonEmployer;
            infoIndividualContracts.removeAll(infoIndividualContracts);
            for (int i=0; i < jsonArray.length();i++){
                InfoIndividualContract infoIndividualContract = new InfoIndividualContract();
                JSONObject object = jsonArray.getJSONObject(i);
                jsonPersonalEmployerInfo = object.getJSONObject("personalemployerinfo");
                jsonPersonal = jsonPersonalEmployerInfo.getJSONObject("Personal");
                jsonEmployer = jsonPersonalEmployerInfo.getJSONObject("Employer");
                jsonIndividualContractStage = object.getJSONObject("IndividualContractStage");
                infoIndividualContract.setIndividualContractTypeId(object.getInt("IndividualContractTypeId"));
                if(!(object.get("PositionId").toString().equals("null"))){
                    infoIndividualContract.setPositionId(object.getInt("PositionId"));
                }
                if(!(object.get("Salary").toString().equals("null"))){
                    infoIndividualContract.setSalary(object.getInt("Salary"));
                }
                if(!(object.get("PayPeriodId").toString().equals("null"))){
                    infoIndividualContract.setPayPeriodId(object.getInt("PayPeriodId"));
                }
                if(!(object.get("ContractCityId").toString().equals("null"))){
                    infoIndividualContract.setContractCityId(object.getString("ContractCityId"));
                }
                infoIndividualContract.setContractNumber(object.getString("ContractNumber"));
                infoIndividualContract.setStartDate(object.getString("StartDate"));
                infoIndividualContract.setProjectId(object.getString("ProjectId"));
                infoIndividualContract.setContractId(object.getString("ContractId"));
                infoIndividualContract.setId(object.getString("Id"));
                infoIndividualContract.setName(jsonPersonal.getString("Name"));
                infoIndividualContract.setLastName(jsonPersonal.getString("LastName"));
                infoIndividualContract.setDocumentType(jsonPersonal.getString("DocumentType"));
                infoIndividualContract.setDocumentNumber(jsonPersonal.getString("DocumentNumber"));
                infoIndividualContract.setDescripction(jsonIndividualContractStage.getString("Description"));
                infoIndividualContract.setComment(jsonIndividualContractStage.getString("Comment"));
                infoIndividualContract.setPhoto(jsonPersonalEmployerInfo.getString("Photo"));
                infoIndividualContract.setPersonalId(jsonPersonalEmployerInfo.getString("PersonalId"));
                infoIndividualContract.setPersonalEmployerInfo(object.getString("PersonalEmployerInfoId"));
                infoIndividualContract.setEmployerId(jsonEmployer.getString("Id"));
                infoIndividualContract.setNameEmployer(jsonEmployer.getString("Name"));
                infoIndividualContract.setLogoEmployer(jsonEmployer.getString("Logo"));
                infoIndividualContract.setRolEmployer(jsonEmployer.getString("Rol"));
                infoIndividualContract.setDocumentTypeEmployer(jsonEmployer.getString("DocumentType"));
                infoIndividualContract.setDocumentNumberEmployer(jsonEmployer.getString("DocumentNumber"));
                infoIndividualContract.setCreateDate(jsonIndividualContractStage.getString("CreateDate"));
                infoIndividualContracts.add(infoIndividualContract);
                Collections.sort(infoIndividualContracts, new Comparator<InfoIndividualContract>() {
                    @Override
                    public int compare(InfoIndividualContract o1, InfoIndividualContract o2) {
                        return o2.CreateDate.compareTo(o1.CreateDate);
                    }
                });
            }
            adapter = new IndivudualContractAdapter(infoIndividualContracts,getActivity(),mListener);
            recyclerView.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void LoadListContract(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonPersonalEmployerInfo,jsonPersonal,jsonIndividualContractStage, jsonEmployer;
            infoIndividualContracts.removeAll(infoIndividualContracts);
            String ValidateColum;
            for (int i=0; i < jsonArray.length();i++){
                InfoIndividualContract infoIndividualContract = new InfoIndividualContract();
                JSONObject object = jsonArray.getJSONObject(i);
                jsonPersonalEmployerInfo = object.getJSONObject("personalemployerinfo");
                jsonIndividualContractStage = object.getJSONObject("IndividualContractStage");
                jsonEmployer = jsonPersonalEmployerInfo.getJSONObject("Employer");
                ValidateColum = jsonIndividualContractStage.getString("Description");
                if(ValidateColum.equals("CONTRATADO") || ValidateColum.equals("LIQUIDADO")){
                    jsonPersonal = jsonPersonalEmployerInfo.getJSONObject("Personal");
                    jsonIndividualContractStage = object.getJSONObject("IndividualContractStage");
                    infoIndividualContract.setIndividualContractTypeId(object.getInt("IndividualContractTypeId"));
                    infoIndividualContract.setContractNumber(object.getString("ContractNumber"));

                    if(!(object.get("PositionId").toString().equals("null"))){
                        infoIndividualContract.setPositionId(object.getInt("PositionId"));
                    }
                    if(!(object.get("Salary").toString().equals("null"))){
                        infoIndividualContract.setSalary(object.getInt("Salary"));
                    }
                    if(!(object.get("PayPeriodId").toString().equals("null"))){
                        infoIndividualContract.setPayPeriodId(object.getInt("PayPeriodId"));
                    }
                    infoIndividualContract.setStartDate(object.getString("StartDate"));
                    infoIndividualContract.setProjectId(object.getString("ProjectId"));
                    infoIndividualContract.setContractId(object.getString("ContractId"));
                    infoIndividualContract.setId(object.getString("Id"));
                    infoIndividualContract.setName(jsonPersonal.getString("Name"));
                    infoIndividualContract.setLastName(jsonPersonal.getString("LastName"));
                    infoIndividualContract.setDocumentType(jsonPersonal.getString("DocumentType"));
                    infoIndividualContract.setDocumentNumber(jsonPersonal.getString("DocumentNumber"));
                    infoIndividualContract.setDescripction(jsonIndividualContractStage.getString("Description"));
                    infoIndividualContract.setComment(jsonIndividualContractStage.getString("Comment"));
                    infoIndividualContract.setPhoto(jsonPersonalEmployerInfo.getString("Photo"));
                    infoIndividualContract.setPersonalId(jsonPersonalEmployerInfo.getString("PersonalId"));
                    infoIndividualContract.setPersonalEmployerInfo(object.getString("PersonalEmployerInfoId"));
                    infoIndividualContract.setEmployerId(jsonEmployer.getString("Id"));
                    infoIndividualContract.setNameEmployer(jsonEmployer.getString("Name"));
                    infoIndividualContract.setLogoEmployer(jsonEmployer.getString("Logo"));
                    infoIndividualContract.setRolEmployer(jsonEmployer.getString("Rol"));
                    infoIndividualContract.setDocumentTypeEmployer(jsonEmployer.getString("DocumentType"));
                    infoIndividualContract.setDocumentNumberEmployer(jsonEmployer.getString("DocumentNumber"));
                    infoIndividualContract.setCreateDate(jsonIndividualContractStage.getString("CreateDate"));
                    infoIndividualContracts.add(infoIndividualContract);
                    Collections.sort(infoIndividualContracts, new Comparator<InfoIndividualContract>() {
                        @Override
                        public int compare(InfoIndividualContract o1, InfoIndividualContract o2) {
                            return o2.CreateDate.compareTo(o1.CreateDate);
                        }
                    });
                }
            }
            adapter = new IndivudualContractAdapter(infoIndividualContracts,getActivity(),mListener);
            recyclerView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void LoadListOrders(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonPersonalEmployerInfo,jsonPersonal,jsonIndividualContractStage,jsonEmployer;
            infoIndividualContracts.removeAll(infoIndividualContracts);
            String ValidateColum;
            for (int i=0; i < jsonArray.length();i++){
                InfoIndividualContract infoIndividualContract = new InfoIndividualContract();
                JSONObject object = jsonArray.getJSONObject(i);
                jsonPersonalEmployerInfo = object.getJSONObject("personalemployerinfo");
                jsonIndividualContractStage = object.getJSONObject("IndividualContractStage");
                ValidateColum = jsonIndividualContractStage.getString("Description");
                jsonEmployer = jsonPersonalEmployerInfo.getJSONObject("Employer");
                if(ValidateColum.equals("INICIADO") || ValidateColum.equals("SOLICITADO") || ValidateColum.equals("APROBADO") || ValidateColum.equals("ANULADO") || ValidateColum.equals("RECHAZADO PARA CORREGIR")){
                    jsonPersonal = jsonPersonalEmployerInfo.getJSONObject("Personal");
                    jsonIndividualContractStage = object.getJSONObject("IndividualContractStage");
                    infoIndividualContract.setIndividualContractTypeId(object.getInt("IndividualContractTypeId"));
                    infoIndividualContract.setContractNumber(object.getString("ContractNumber"));
                    if(!(object.get("PositionId").toString().equals("null"))){
                        infoIndividualContract.setPositionId(object.getInt("PositionId"));
                    }
                    if(!(object.get("Salary").toString().equals("null"))){
                        infoIndividualContract.setSalary(object.getInt("Salary"));
                    }
                    if(!(object.get("PayPeriodId").toString().equals("null"))){
                        infoIndividualContract.setPayPeriodId(object.getInt("PayPeriodId"));
                    }
                    infoIndividualContract.setStartDate(object.getString("StartDate"));
                    infoIndividualContract.setProjectId(object.getString("ProjectId"));
                    infoIndividualContract.setContractId(object.getString("ContractId"));
                    infoIndividualContract.setId(object.getString("Id"));
                    infoIndividualContract.setName(jsonPersonal.getString("Name"));
                    infoIndividualContract.setLastName(jsonPersonal.getString("LastName"));
                    infoIndividualContract.setDocumentType(jsonPersonal.getString("DocumentType"));
                    infoIndividualContract.setDocumentNumber(jsonPersonal.getString("DocumentNumber"));
                    infoIndividualContract.setDescripction(jsonIndividualContractStage.getString("Description"));
                    infoIndividualContract.setComment(jsonIndividualContractStage.getString("Comment"));
                    infoIndividualContract.setPhoto(jsonPersonalEmployerInfo.getString("Photo"));
                    infoIndividualContract.setPersonalId(jsonPersonalEmployerInfo.getString("PersonalId"));
                    infoIndividualContract.setPersonalEmployerInfo(object.getString("PersonalEmployerInfoId"));
                    infoIndividualContract.setEmployerId(jsonEmployer.getString("Id"));
                    infoIndividualContract.setNameEmployer(jsonEmployer.getString("Name"));
                    infoIndividualContract.setLogoEmployer(jsonEmployer.getString("Logo"));
                    infoIndividualContract.setRolEmployer(jsonEmployer.getString("Rol"));
                    infoIndividualContract.setDocumentTypeEmployer(jsonEmployer.getString("DocumentType"));
                    infoIndividualContract.setDocumentNumberEmployer(jsonEmployer.getString("DocumentNumber"));
                    infoIndividualContract.setCreateDate(jsonIndividualContractStage.getString("CreateDate"));
                    infoIndividualContracts.add(infoIndividualContract);
                    Collections.sort(infoIndividualContracts, new Comparator<InfoIndividualContract>() {
                        @Override
                        public int compare(InfoIndividualContract o1, InfoIndividualContract o2) {
                            return o2.CreateDate.compareTo(o1.CreateDate);
                        }
                    });
                }

            }
            adapter = new IndivudualContractAdapter(infoIndividualContracts,getActivity(),mListener);
            recyclerView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private Bundle getBundleList(String response) {
        Bundle bundle = new Bundle();
        switch (mColumnCount) {
            case 1:
                LoadList(response);
                break;
            case 2:
                LoadListContract(response);
                break;
            case 3:
                LoadListOrders(response);
                break;
        }
        return bundle;
    }

    public interface OnListSingleContractInteractionListener {
        void onListFragmentInteraction(String imageView,InfoIndividualContract item);
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
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListSingleContractInteractionListener) {
            mListener = (OnListSingleContractInteractionListener) context;
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
    public void filtrar(String SearchText){
        ArrayList<InfoIndividualContract> filtrarList= new ArrayList<>();
        filtrarList.removeAll(filtrarList);
        for(InfoIndividualContract info: infoIndividualContracts){
            if(info.getName().toLowerCase().contains(SearchText.toLowerCase())){
                filtrarList.add(info);
            }
            if(info.getDocumentNumber().toLowerCase().contains(SearchText.toLowerCase())){
                filtrarList.add(info);
            }
            if(info.getLastName().toLowerCase().contains(SearchText.toLowerCase())){
                filtrarList.add(info);
            }
        }
        adapter.filtrar(filtrarList);
        adapter.notifyDataSetChanged();
    }
    public void filtrarlastName(String SearchText){
        ArrayList<InfoIndividualContract> filtrarListLastName= new ArrayList<>();
        filtrarListLastName.removeAll(filtrarListLastName);
        for(InfoIndividualContract info: infoIndividualContracts){
            if(info.getLastName().toLowerCase().contains(SearchText.toLowerCase())){
                filtrarListLastName.add(info);
            }
        }
        adapter.filtrar(filtrarListLastName);
        adapter.notifyDataSetChanged();
    }
    public void filtrarDni(String SearchText){
        ArrayList<InfoIndividualContract> filtrarListDni= new ArrayList<>();
        filtrarListDni.removeAll(filtrarListDni);
        for(InfoIndividualContract info: infoIndividualContracts){
            if(info.getDocumentNumber().toLowerCase().contains(SearchText.toLowerCase())){
                filtrarListDni.add(info);
            }
        }
        adapter.filtrar(filtrarListDni);
        adapter.notifyDataSetChanged();
    }
    public void ReloadList(){
        adapter.notifyDataSetChanged();
    }
}