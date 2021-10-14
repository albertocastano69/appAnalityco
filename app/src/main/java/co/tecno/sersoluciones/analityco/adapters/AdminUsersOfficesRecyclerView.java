package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.UserCustomer;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BranchOffice} and makes a call to the
 * specified {@link OnAdminUserInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 * <p>
 * Adaptador con un cursor para poblar la lista desde SQLite
 **/

public class AdminUsersOfficesRecyclerView extends RecyclerView.Adapter<AdminUsersOfficesRecyclerView.ViewHolder> {


    public interface OnAdminUserInteractionListener {
        void onListAdminUserInteraction(int position, boolean remove);
    }

    private final ArrayList<UserCustomer> mItems;
    private final Context context;
    private final OnAdminUserInteractionListener listener;

    public AdminUsersOfficesRecyclerView(Context context, OnAdminUserInteractionListener listener) {
        this.context = context;
        this.mItems = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_user_recycler_fragment, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final int pos = holder.getAdapterPosition();

        holder.mItem = mItems.get(pos);

        holder.mTextName.setText(String.format("%s %s", holder.mItem.Name, holder.mItem.LastName));
        holder.mTextSubName.setText(holder.mItem.UserEmail);

        Date finishDate = holder.mItem.FinishDate;
        String mDate = "";
        if (finishDate != null) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            mDate = dateFormat.format(finishDate);
        }
        holder.mTextValidity.setText(mDate);

        Drawable drawableIsActive = MaterialDrawableBuilder.with(context)
                .setIcon(holder.mItem.IsActive ? MaterialDrawableBuilder.IconValue.THUMB_UP : MaterialDrawableBuilder.IconValue.THUMB_DOWN)
                .setColor(holder.mItem.IsActive ? Color.GREEN : Color.RED)
                .setSizeDp(25)
                .build();
        holder.iconIsActive.setImageDrawable(drawableIsActive);

        if (holder.mItem.PhotoUrl != null && !holder.mItem.PhotoUrl.equals("null")) {
            String url = Constantes.URL_IMAGES + holder.mItem.PhotoUrl;


            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(holder.mLogoImageView);
        }else{
            holder.mLogoImageView.setImageResource(R.drawable.image_not_available);
        }


        holder.mView.setBackgroundColor(Color.WHITE);
        holder.mCancelView.setVisibility(View.GONE);
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != listener) {
                    holder.mView.setBackgroundColor(Color.CYAN);
                    holder.mCancelView.setVisibility(View.VISIBLE);
                    listener.onListAdminUserInteraction(pos, true);
                    //notifyDataSetChanged();
                }
                return false;
            }
        });
        holder.mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {

                    holder.mView.setBackgroundColor(Color.WHITE);
                    holder.mCancelView.setVisibility(View.GONE);
                    listener.onListAdminUserInteraction(pos, false);
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

    public void swap(ArrayList<UserCustomer> data) {
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
        @BindView(R.id.logo)
        ImageView mLogoImageView;
        @BindView(R.id.text_name)
        TextView mTextName;
        @BindView(R.id.text_sub_name)
        TextView mTextSubName;
        @BindView(R.id.label_validity)
        TextView labelValidity;
        @BindView(R.id.text_validity)
        TextView mTextValidity;
        @BindView(R.id.icon_is_active)
        ImageView iconIsActive;
        @BindView(R.id.text_active)
        TextView mTextActive;
        @BindView(R.id.icon_cancel)
        ImageButton mCancelView;
        @BindView(R.id.card_view_detail)
        CardView cardViewDetail;

        UserCustomer mItem;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
            mTextActive.setVisibility(View.GONE);
            iconIsActive.setVisibility(View.GONE);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextName.getText() + "'";
        }
    }
}