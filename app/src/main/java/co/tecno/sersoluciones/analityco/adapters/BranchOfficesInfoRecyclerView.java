package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.BranchOffice;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BranchOffice} and makes a call to the
 * specified {@link OnBranchOfficeListener}.
 * TODO: Replace the implementation with code for your data type.
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 * <p>
 * Adaptador con un cursor para poblar la lista desde SQLite
 **/

public class BranchOfficesInfoRecyclerView extends RecyclerView.Adapter<BranchOfficesInfoRecyclerView.ViewHolder> {

    public interface OnBranchOfficeListener {
        void onClickBrach(BranchOffice mItem);
    }

    private final ArrayList<BranchOffice> mItems;
    private final Context mContext;
    private final OnBranchOfficeListener mListener;

    public BranchOfficesInfoRecyclerView(Context context, OnBranchOfficeListener listener, ArrayList<BranchOffice> branchOffices) {
        this.mItems = branchOffices;
        this.mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.branch_info_recycler, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int pos = holder.getAdapterPosition();
        holder.mItem = mItems.get(pos);
        holder.mText1View.setText(holder.mItem.Name);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onClickBrach(holder.mItem);
                }
            }
        });

        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String map = "http://maps.google.co.in/maps?q=" + holder.mItem.Address;
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(map));
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(android.R.id.text1)
        TextView mText1View;
        @BindView(R.id.icon)
        MaterialIconView icon;
        BranchOffice mItem;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mText1View.getText() + "'";
        }
    }
}