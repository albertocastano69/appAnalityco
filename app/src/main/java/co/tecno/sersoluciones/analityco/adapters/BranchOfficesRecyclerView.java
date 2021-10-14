package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.BranchOffice;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BranchOffice} and makes a call to the
 * specified {@link OnBranchOfficeInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 * <p>
 * Adaptador con un cursor para poblar la lista desde SQLite
 **/

public class BranchOfficesRecyclerView extends RecyclerView.Adapter<BranchOfficesRecyclerView.ViewHolder> {

    public interface OnBranchOfficeInteractionListener {
        void onListOrderInteraction(int position, boolean remove);
    }

    private final ArrayList<BranchOffice> mItems;
    private final Context context;
    private final OnBranchOfficeInteractionListener listener;

    public BranchOfficesRecyclerView(Context context, OnBranchOfficeInteractionListener listener) {
        this.context = context;
        this.mItems = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public BranchOfficesRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.branch_recycler_fragment, parent, false);
        return new BranchOfficesRecyclerView.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final BranchOfficesRecyclerView.ViewHolder holder, int position) {

        final int pos = holder.getAdapterPosition();
        holder.mItem = mItems.get(pos);

        holder.mText1View.setText(holder.mItem.Name);
        holder.mIconLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = holder.mItem.Address + ", " + holder.mItem.State + ", " + holder.mItem.City;
                String map = "http://maps.google.co.in/maps?q=" + address;
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(map));
                context.startActivity(i);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != listener && pos > 0) {
                    holder.mView.setBackgroundColor(Color.CYAN);
                    holder.mCancelView.setVisibility(View.VISIBLE);
                    listener.onListOrderInteraction(pos, true);
                    //notifyDataSetChanged();
                }
                return false;
            }
        });
        holder.mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener && pos > 0) {
                    holder.mView.setBackgroundColor(Color.WHITE);
                    holder.mCancelView.setVisibility(View.GONE);
                    listener.onListOrderInteraction(pos, false);
                    //notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    public void swap(ArrayList<BranchOffice> data) {
        if (mItems != null) {
            mItems.clear();
            mItems.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void update() {

        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mText1View;
        final ImageButton mCancelView;
        final MaterialIconView mIconLocation;
        BranchOffice mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mText1View = view.findViewById(android.R.id.text1);
            mCancelView = view.findViewById(R.id.icon_cancel);
            mIconLocation = view.findViewById(R.id.icon_location);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mText1View.getText() + "'";
        }
    }
}