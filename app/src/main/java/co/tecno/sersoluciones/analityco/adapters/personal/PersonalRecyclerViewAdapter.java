package co.tecno.sersoluciones.analityco.adapters.personal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;

import androidx.core.view.ViewCompat;
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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import co.tecno.sersoluciones.analityco.GlideApp;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.personal.PersonalListFragment;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Cursor} and makes a call to the
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class PersonalRecyclerViewAdapter extends RecyclerView.Adapter<PersonalRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<PersonalList> mValues;
    private final PersonalListFragment.OnListPersonalInteractionListener mListener;
    private final Context mContext;

    public PersonalRecyclerViewAdapter(Context context, PersonalListFragment.OnListPersonalInteractionListener listener, ArrayList<PersonalList> values) {
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

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //        mValues.moveToPosition(position);
//        holder.mItem = cursorToEmployer(mValues);
        holder.mItem = mValues.get(position);
        holder.labelActive.setVisibility(View.GONE);
        holder.labelValidity.setVisibility(View.GONE);
        if (holder.mItem != null) {

            holder.mNameView.setText(String.format("%s %s", holder.mItem.Name, holder.mItem.LastName));
            holder.mAddressView.setText(holder.mItem.JobName);
            holder.mValidityView.setText(String.format("%s %s", holder.mItem.DocumentType, holder.mItem.DocumentNumber));

            holder.stateIcon.setVisibility(View.VISIBLE);
            if (holder.mItem.IsActive) {
                if (holder.mItem.Expiry)
                    holder.stateIcon.setImageResource(R.drawable.state_icon);
                else
                    holder.stateIcon.setVisibility(View.GONE);
            } else {
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
            }
            holder.mActiveImageView.setImageResource(R.drawable.ic_phone);
            if (holder.mItem.Photo != null && !holder.mItem.Photo.equals("null")) {
                String url = Constantes.URL_IMAGES + holder.mItem.Photo;
                String[] format = url.split(Pattern.quote("."));
                if (format[format.length - 1].equals("svg")) {
                    GlideApp.with(mContext)
                            .as(PictureDrawable.class)
                            .apply(
                                    new RequestOptions()
                                            .placeholder(R.drawable.loading_animation)
                                            .error(R.drawable.profile_dummy)
                            )
                            .fitCenter()
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .load(url)
                            .into(holder.mNetworkImageView);

                } else {
                    Picasso.get()
                            .load(url)
                            .noFade()
                            .resize(0, 150)
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.profile_dummy)
                            .into(holder.mNetworkImageView);
                }
            } else {
                holder.mNetworkImageView.setImageResource(R.drawable.profile_dummy);
            }
        }
        ViewCompat.setTransitionName(holder.mNetworkImageView, String.valueOf(holder.mItem.Id));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem, holder.mNetworkImageView);
                }
            }
        });
        holder.mActiveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mItem.PhoneNumber != null) {
                    PopupMenu popup = new PopupMenu(mContext, holder.mActiveImageView);
                    popup.getMenuInflater()
                            .inflate(R.menu.menu_call, popup.getMenu());
                    popup.getMenu().getItem(0).setTitle("Marcar");
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent i = new Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + holder.mItem.PhoneNumber));
                            mContext.startActivity(i);
                            return true;
                        }
                    });
                    popup.show();
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

//    public void swapCursor(Cursor nuevoCursor) {
//        log("Actualizacion cursor exitosa");
//        if (nuevoCursor != null) {
//            mValues = nuevoCursor;
//        }
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getItemCount() {
//        if (mValues != null)
//            return mValues.getCount();
//        return 0;
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNameView;
        final TextView mAddressView;
        final TextView mValidityView;
        final CardView cardView;
        final Button btnPhone;
        final ImageView mNetworkImageView;
        final ImageView mActiveImageView;
        final ImageView stateIcon;
        final TextView labelActive;
        final TextView labelValidity;

        PersonalList mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.text_name);
            mAddressView = view.findViewById(R.id.text_address);
            mValidityView = view.findViewById(R.id.text_validity);
            cardView = view.findViewById(R.id.card_view_detail);
            btnPhone = view.findViewById(R.id.icon_phone);
            mNetworkImageView = view.findViewById(R.id.logo);
            mActiveImageView = view.findViewById(R.id.icon_is_active);
            stateIcon = view.findViewById(R.id.state_icon);
            labelActive = view.findViewById(R.id.label_active);
            labelValidity = view.findViewById(R.id.label_validity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
