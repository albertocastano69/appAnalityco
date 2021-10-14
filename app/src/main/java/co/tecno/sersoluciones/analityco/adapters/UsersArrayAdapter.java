package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.UserList;

/**
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */

public class UsersArrayAdapter extends ArrayAdapter{

    private final ArrayList<UserList> items;
    private final ArrayList<UserList> itemsSuggestion;
    private final ArrayList<UserList> itemsAll;

    public UsersArrayAdapter(@NonNull Context context, @NonNull ArrayList<UserList> objects) {
        super(context, R.layout.simple_spinner_item_2, objects);
        items = objects;
        this.itemsSuggestion = new ArrayList<>();
        this.itemsAll = new ArrayList<>(objects);
    }

    @Override
    public int getCount() {
        if (items != null)
            return items.size();
        return 0;
    }

    public UserList getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.simple_spinner_item_2, parent, false);
        }
        UserList user = items.get(position);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(user.Name + " " + user.LastName);
        TextView textView2 = convertView.findViewById(android.R.id.text2);
        textView2.setText(user.UserName);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((UserList) resultValue).UserName;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    itemsSuggestion.clear();
                    for (UserList userList : itemsAll) {
                        if (userList.Name.toLowerCase().startsWith(constraint.toString().toLowerCase()) ||
                                userList.UserName.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            itemsSuggestion.add(userList);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = itemsSuggestion;
                    filterResults.count = itemsSuggestion.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items.clear();
                if (results != null && results.count > 0) {
                    // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
                    List<?> result = (List<?>) results.values;
                    for (Object object : result) {
                        if (object instanceof UserList) {
                            items.add((UserList) object);
                        }
                    }
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    items.addAll(itemsAll);
                }
                notifyDataSetChanged();
            }
        };
    }
}
