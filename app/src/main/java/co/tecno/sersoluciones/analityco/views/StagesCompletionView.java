package co.tecno.sersoluciones.analityco.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenautocomplete.TokenCompleteTextView;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.ProjectStages;

/**
 * Created by Ser Soluciones SAS on 17/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class StagesCompletionView extends TokenCompleteTextView<ProjectStages> {

    public StagesCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(ProjectStages item) {

        /*LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        TextView textView = view.findViewById(R.id.text);
        textView.setText(item.Code);
        MaterialIconView iconDelete = view.findViewById(R.id.delete);*/

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TokenTextView token = (TokenTextView) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        token.setText(String.valueOf(item.Id));
        return token;
        //return view;
    }

    @Override
    protected ProjectStages defaultObject(String completionText) {
        /*int index = completionText.indexOf('@');
        if (index == -1) {
            return new EconomicActivity(completionText, completionText.replace(" ", "") + "@example.com");
        } else {
            return new EconomicActivity(completionText.substring(0, index), completionText);
        }*/
        return new ProjectStages(Integer.valueOf(completionText), completionText);
    }

}