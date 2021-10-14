package co.tecno.sersoluciones.analityco.adapters.contracts;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.GlideApp;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.fragments.contract.ContractsListFragment;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

/**
 * Created by Ser Soluciones SAS on 01/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class ContractRecyclerViewAdapter extends RecyclerView.Adapter<ContractRecyclerViewAdapter.ViewHolder> {

    private final ContractsListFragment.OnListContractInteractionListener mListener;
    private final Context mContext;
    private final ArrayList<ObjectList> mValues;

    public ContractRecyclerViewAdapter(Context context, ContractsListFragment.OnListContractInteractionListener listener, ArrayList<ObjectList> values) {
        mListener = listener;
        mContext = context;
        mValues = values;
    }

    @Override
    public ContractRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_company, parent, false);
        return new ContractRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContractRecyclerViewAdapter.ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);

        if (holder.mItem != null) {
//            if (holder.mItem.IsRegister)
//                holder.textName.setText(holder.mItem.ContractReview);
//            else
            holder.textName.setText(holder.mItem.ContractReview);
            holder.textName.setAllCaps(false);
            holder.textAddress.setText(holder.mItem.ContractorName);
            holder.textValidity.setText(String.format(holder.mItem.ContractNumber));
            holder.labelActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
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

            if (holder.mItem.FormImageLogo != null && !holder.mItem.FormImageLogo.equals("null")) {
                String url = Constantes.URL_IMAGES + holder.mItem.FormImageLogo;
//
                chargeImage(url, holder);
            } else {
                holder.logo.setImageResource(R.drawable.image_not_available);
            }
        }
        ViewCompat.setTransitionName(holder.logo, holder.mItem.Id);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onContractInteraction(holder.mItem, holder.logo);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void chargeImage(String url, ViewHolder holder) {

        String[] format = url.split(Pattern.quote("."));
        if (format[format.length - 1].equals("svg")) {

            GlideApp.with(mContext)
                    .as(PictureDrawable.class)
                    .apply(
                            new RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.image_not_available)
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
                    .error(R.drawable.image_not_available)
                    .into(holder.logo);
        }

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
        ObjectList mItem;

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
