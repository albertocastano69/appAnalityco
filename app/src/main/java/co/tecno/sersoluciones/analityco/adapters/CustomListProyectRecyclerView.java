package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.BranchOffice;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BranchOffice} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 * <p>
 * Adaptador con un cursor para poblar la lista desde SQLite
 **/

public abstract class CustomListProyectRecyclerView<T> extends RecyclerView.Adapter<CustomListProyectRecyclerView.ViewHolder> {

    protected final ArrayList<T> mItems;
    protected final Context mContext;

    protected CustomListProyectRecyclerView(Context context, ArrayList<T> mItems) {
        this.mItems = mItems;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_proyect_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.logo)
        public ImageView logo;
        @BindView(R.id.text_name)
        public TextView textName;
        @BindView(R.id.text_sub_name)
        public TextView textSubName;
        @BindView(R.id.text_date)
        public TextView textDate;
        @BindView(R.id.state_icon)
        public ImageView stateIcon;
        @BindView(R.id.icon_date)
        public MaterialIconView iconDate;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textName.getText() + "'";
        }
    }
}