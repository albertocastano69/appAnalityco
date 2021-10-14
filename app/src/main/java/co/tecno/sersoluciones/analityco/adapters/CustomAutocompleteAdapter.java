package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.CompanyList;

public class CustomAutocompleteAdapter extends ArrayAdapter<CompanyList> {
    private final LayoutInflater mInflater;
    private final ArrayList<CompanyList> mItems;
    private final Context mContext;
    private final int mResourse;

    public CustomAutocompleteAdapter(Context context, int layoutId, ArrayList<CompanyList> mItems) {
        super(context, layoutId, mItems);
        this.mItems = mItems;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mResourse = layoutId;
    }

    @Override
    public View getDropDownView(int position, @NotNull View convertView, @NotNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @NotNull
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }


    @SuppressLint("SetTextI18n")
    private View createItemView(int position, View convertView, ViewGroup parent) {

        final View view = mInflater.inflate(mResourse, parent, false);
        TextView textName = (TextView) view.findViewById(android.R.id.text1);
        TextView textSubName = (TextView) view.findViewById(android.R.id.text2);
        final CompanyList mItem = mItems.get(position);
        if (position == 0) {
            textName.setText("Seleccione");
            textSubName.setText("Empresa");
        } else {
            textName.setText(mItem.Name + " " + mItem.Name);
            textSubName.setText(mItem.DocumentType + ": " + mItem.DocumentNumber);
        }
        return view;
    }
}
