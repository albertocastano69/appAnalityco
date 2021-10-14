package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.regex.Pattern;

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

public abstract class CustomListInfoRecyclerView<T> extends RecyclerView.Adapter<CustomListInfoRecyclerView.ViewHolder> {

    public final ArrayList<T> mItems;
    public final Context mContext;

    protected CustomListInfoRecyclerView(Context context, ArrayList<T> mItems) {
        this.mItems = mItems;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_info_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    protected void chargeImage(String url, ViewHolder holder) {
        String[] format = url.split(Pattern.quote("."));
        if (format[format.length - 1].equals("svg")) {
//            Uri uri = Uri.parse(url);
            Glide.with(mContext)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.image_not_available))
                    .into(holder.logo);
        } else {
            Picasso.get().load(url)
                    .resize(0, 150)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(holder.logo);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.logo)
        public ImageView logo;
        @BindView(R.id.text_name)
        public TextView textName;
        @BindView(R.id.text_sub_name)
        public TextView textSubName;
        @BindView(R.id.label_validity)
        public TextView labelValidity;
        @BindView(R.id.text_validity)
        public TextView textValidity;
        @BindView(R.id.icon_is_active)
        public ImageView iconIsActive;
        @BindView(R.id.text_active)
        public TextView textActive;
        @BindView(R.id.btn_edit)
        public ImageView btnEdit;
        @BindView(R.id.card_view_detail)
        public CardView cardViewDetail;
        @BindView(R.id.state_icon)
        public ImageView stateIcon;
        @BindView(R.id.icon_date)
        public MaterialIconView dateIcon;
        @BindView(R.id.top_layout)
        public ConstraintLayout topLayout;
        @BindView(R.id.iconHeader)
        public MaterialIconView topIconDate;
        @BindView(R.id.text_date)
        public TextView textTopDate;
        @BindView(R.id.image_dots)
        public ImageView imageDots;
        @BindView(R.id.job_icon)
        public ImageView jobIcon;
        @BindView(R.id.job_text)
        public TextView textJob;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

            Drawable drawablePen = MaterialDrawableBuilder.with(mContext)
                    .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                    .setColor(Color.rgb(106, 202, 37))
                    .setSizeDp(20)
                    .build();
            btnEdit.setImageDrawable(drawablePen);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textName.getText() + "'";
        }
    }
}