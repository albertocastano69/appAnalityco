package co.tecno.sersoluciones.analityco.adapters.project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
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
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.GlideApp;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.callback.OnListProjectInteractionListener;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Cursor} and makes a call to the
 * specified {@link OnListProjectInteractionListener}.
 * Created by Ser Soluciones SAS on 17/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class ProjectRecyclerViewAdapter extends RecyclerView.Adapter<ProjectRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<ProjectList> mValues;
    private final OnListProjectInteractionListener mListener;
    private final Context mContext;
    private final boolean mSelected;

    public ProjectRecyclerViewAdapter(Context context, OnListProjectInteractionListener listener, ArrayList<ProjectList> values) {
        mListener = listener;
        mContext = context;
        mSelected = false;
        mValues = values;
    }

    public ProjectRecyclerViewAdapter(Context context, OnListProjectInteractionListener listener, boolean selected, ArrayList<ProjectList> values) {
        mListener = listener;
        mContext = context;
        mSelected = selected;
        mValues = values;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_company, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
//        mValues.moveToPosition(position);
//        holder.mItem = cursorToObj(mValues);
        holder.mItem = mValues.get(position);
        if (holder.mItem != null) {

            holder.textName.setText(holder.mItem.Name);
            holder.textValidity.setText(holder.mItem.Address + ", " + holder.mItem.CityName + ", " + holder.mItem.StateName);
            String mDate = holder.mItem.FinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
                    mDate = dateFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //holder.textValidity.setText(mDate);
            holder.textAddress.setText(holder.mItem.CompanyName);
           /* Drawable drawableIsActive = MaterialDrawableBuilder.with(mContext)
                    .setIcon(holder.mItem.IsActive ? MaterialDrawableBuilder.IconValue.THUMB_UP : MaterialDrawableBuilder.IconValue.THUMB_DOWN)
                    .setColor(holder.mItem.IsActive ? holder.mItem.Expiry ? Color.rgb(255, 158, 0) : Color.GREEN : Color.RED)
                    .setSizeDp(35)
                    .build();
            holder.iconIsActive.setImageDrawable(drawableIsActive);*/
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
                String[] format = url.split(Pattern.quote("."));
                if (format[format.length - 1].equals("svg")) {
                    GlideApp.with(mContext)
                            .as(PictureDrawable.class)
                            .apply(
                                    new RequestOptions()
                                            .placeholder(R.drawable.loading_animation)
                                            .error(R.drawable.not_project_1)
                            )
                            .fitCenter()
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .load(url)
                            .into(holder.logo);

                } else {
                    Picasso.get()
                            .load(url)
                            .noFade()
                            .resize(0, 150)
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.not_project_1)
                            .into(holder.logo);
                }

            } else {
                holder.logo.setImageResource(R.drawable.not_project_1);
            }
        }
        holder.cardViewDetail.setBackgroundColor(Color.WHITE);
        if (holder.mItem.IsSelected)
            holder.cardViewDetail.setBackgroundColor(ContextCompat.getColor(mContext, R.color.bar_decoded));
        ViewCompat.setTransitionName(holder.logo, holder.mItem.Id);
        holder.labelActive.setVisibility(View.GONE);
        holder.labelValidity.setVisibility(View.GONE);
        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                if (mSelected) {
                    updateItem(holder.mItem.Id);
                }
                mListener.onProjectInteraction(holder.mItem, holder.logo);
            }
        });
    }

    private void updateItem(String localId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_IS_SELECTED, false);
        mContext.getContentResolver().update(Constantes.CONTENT_PROJECT_URI, contentValues, null, null);
        notifyDataSetChanged();

        contentValues = new ContentValues();
        contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_IS_SELECTED, true);
        String selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID + " = ? )";
        String[] selectionArgs = new String[]{localId};
        mContext.getContentResolver().update(Constantes.CONTENT_PROJECT_URI,
                contentValues, selection, selectionArgs);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(R.id.logo)
        ImageView logo;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_address)
        TextView textAddress;
        @BindView(R.id.icon_phone)
        Button iconPhone;
        @BindView(R.id.label_validity)
        TextView labelValidity;
        @BindView(R.id.text_validity)
        TextView textValidity;
        @BindView(R.id.icon_is_active)
        ImageView iconIsActive;
        @BindView(R.id.label_active)
        TextView labelActive;
        @BindView(R.id.card_view_detail)
        CardView cardViewDetail;
        @BindView(R.id.state_icon)
        ImageView stateIcon;

        ProjectList mItem;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
            iconPhone.setVisibility(View.GONE);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textName.getText() + "'";
        }
    }
}
