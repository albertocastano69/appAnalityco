package co.tecno.sersoluciones.analityco.adapters.company;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.company.CompanyListFragment.OnListFragmentInteractionListener;
import co.tecno.sersoluciones.analityco.models.BranchOfficeLite;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Cursor} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyCompanyRecyclerViewAdapter extends RecyclerView.Adapter<MyCompanyRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private final Context mContext;
    private final ArrayList<CompanyList> mValues;


    public MyCompanyRecyclerViewAdapter(Context context, OnListFragmentInteractionListener listener, ArrayList<CompanyList> values) {
        mListener = listener;
        mContext = context;
        mValues = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_company, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
//        mValues.moveToPosition(position);
//        holder.mItem = cursorToCompany(mValues);
        holder.mItem = mValues.get(position);
        if (holder.mItem != null) {
            holder.mNameView.setText(holder.mItem.Name);
            holder.label_active.setVisibility(View.GONE);
            holder.textNit.setVisibility(View.VISIBLE);
            holder.mAddressView.setText(holder.mItem.DocumentNumber);
            holder.textNit.setText("Nit:");
            holder.mLabelView.setVisibility(View.GONE);
            holder.mValidityView.setText(holder.mItem.Address + ", " + holder.mItem.NameCity );

            final ArrayList<BranchOfficeLite> branchOfficeLites = new Gson().fromJson(holder.mItem.BranchOffices, new TypeToken<ArrayList<BranchOfficeLite>>() {
            }.getType());

            Drawable drawablePhone = MaterialDrawableBuilder.with(mContext)
                    .setIcon(MaterialDrawableBuilder.IconValue.PHONE)
                    .setColor(Color.rgb(73, 73, 73))
                    .setSizeDp(35)
                    .build();
            holder.mActiveImageView.setImageDrawable(drawablePhone);
            //holder.mActiveImageView.setImageDrawable(drawableIsActive);
            final PopupMenu popup = new PopupMenu(mContext, holder.mActiveImageView);
            for (BranchOfficeLite branchOfficeLite : branchOfficeLites) {
                popup.getMenu().add(String.format("%s: %s", branchOfficeLite.Name, branchOfficeLite.Phone));
            }

            holder.stateIcon.setVisibility(View.VISIBLE);
            if (holder.mItem.IsActive) {
                if (holder.mItem.Expiry)
                    holder.stateIcon.setImageResource(R.drawable.state_icon);
                else
                    holder.stateIcon.setVisibility(View.GONE);
            } else {
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
            }

            if (holder.mItem.Logo != null && !holder.mItem.Logo.equals("null")) {
                String url = Constantes.URL_IMAGES + holder.mItem.Logo;
                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.mNetworkImageView);
            } else {
                holder.mNetworkImageView.setImageResource(R.drawable.image_not_available);
            }

            //holder.mNetworkImageView.setDefaultImageResId(R.mipmap.ic_launcher_round);
            //holder.mNetworkImageView.setErrorImageResId(R.mipmap.ic_launcher_round);

            //holder.btnPhone.setOnClickListener(new View.OnClickListener() {
            holder.mActiveImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //registering popup with OnMenuItemClickListener
                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            String phoneNumber = item.getTitle().toString().split(":")[1];
                            phoneNumber = phoneNumber.trim();
                            log("phoneNumber: " + phoneNumber);
                            Intent i = new Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + phoneNumber));
                            mContext.startActivity(i);
                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction( holder.mItem, holder.mNetworkImageView);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNameView;
        final TextView mAddressView;
        final TextView mValidityView;
        final TextView mLabelView;
        final CardView cardView;
        final Button btnPhone;
        final ImageView mNetworkImageView;
        final ImageView mActiveImageView;
        final ImageView stateIcon;
        final TextView label_active;
        final TextView textNit;

        CompanyList mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.text_name);
            mAddressView = view.findViewById(R.id.text_address);
            mValidityView = view.findViewById(R.id.text_validity);
            mLabelView = view.findViewById(R.id.label_validity);
            cardView = view.findViewById(R.id.card_view_detail);
            btnPhone = view.findViewById(R.id.icon_phone);
            mNetworkImageView = view.findViewById(R.id.logo);
            mActiveImageView = view.findViewById(R.id.icon_is_active);
            stateIcon = view.findViewById(R.id.state_icon);
            label_active = view.findViewById(R.id.label_active);
            textNit = view.findViewById(R.id.text_sub);
            //mImageLoader = MySingleton.getInstance(mContext).getImageLoader();
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }

    public void swap() {
        notifyDataSetChanged();
    }
}
