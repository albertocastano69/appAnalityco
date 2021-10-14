package co.tecno.sersoluciones.analityco.fragments.enrollment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;

/**
 * Created by Ser Soluciones SAS on 05/10/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class CheckReqsPersonalFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragmnet_info_user_enrollment_update, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

//
//    @OnClick(R.id.negative_button2)
//    public void cancelButton2() {
//        boolean isValided = true;
//        for (RequirementsList requirementsList : requirementReport.RemainRequirement) {
//            if (!requirementsList.IsValided) {
//                isValided = false;
//                break;
//            }
//        }
//        Gson gsonReq = new Gson();
//        String jsonReq = gsonReq.toJson(requirementReport);
//
//        ReportPersonal newReport;
//        try {
//            newReport = new ReportPersonal(serviveGps.getLong("report_date"), serviveGps.getInt("battery"),
//                    serviveGps.getDouble("latitude"), serviveGps.getDouble("longitude"), serviveGps.getInt("altitude"),
//                    serviveGps.getInt("velocity"), sendEvent(), serviveGps.getString("provider"), serviveGps.getInt("precision"),
//                    serviveGps.getInt("bearing"), serviveGps.getString("is_gps"), personalInfoId, userLogin.CompanyId, idProject,
//                    idSteps, contractId, jsonReq, checkReq, inspectReq, false, isValided);
//            //modificar -> firstJsonRequirement  por -> requirementsReport(requirements)
//            Gson gson = new Gson();
//            String json = gson.toJson(newReport);
//            ContentValues values = new ContentValues();
//            values.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_DATA, json);
//            logW(json);
////            getActivity().getContentResolver().insert(Constantes.CONTENT_PERSON_REPORT_URI, values);
////            UpdateDBService.startRequest(getActivity(), false);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        if (mListener != null) {
//            mListener.onCancelUserInfo();
//        }
//    }
}


