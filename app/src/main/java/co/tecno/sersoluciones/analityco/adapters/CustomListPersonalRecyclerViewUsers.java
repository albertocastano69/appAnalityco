package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;

public abstract class CustomListPersonalRecyclerViewUsers<T> extends RecyclerView.Adapter<CustomListPersonalRecyclerViewUsers.ViewHolder> {

    protected final ArrayList<T> mItems;
    public final Context mContext;

    protected CustomListPersonalRecyclerViewUsers(Context context, ArrayList<T> mItems) {
        this.mItems = mItems;
        this.mContext = context;
    }

    @Override
    public CustomListPersonalRecyclerViewUsers.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_user_recycler, parent, false);
        return new CustomListPersonalRecyclerViewUsers.ViewHolder(view);
    }

    public int limit = 10;
    @Override
    public int getItemCount() {
        if (mItems != null){
        if(mItems.size() > limit){
            return limit;
        }
        else
        {
            return mItems.size();
        }
        }else{
            return 0;
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
        @BindView(R.id.text_validity)
        public TextView textValidity;
        @BindView(R.id.btn_edit)
        public MaterialIconView btnEdit;
        @BindView(R.id.card_view_detail)
        public CardView cardViewDetail;
        @BindView(R.id.state_icon)
        public ImageView stateIcon;
        @BindView(R.id.phone)
        public ImageView phone;
        @BindView(R.id.user)
        public MaterialIconView user_icon;
        @BindView(R.id.icon_mail)
        public MaterialIconView calendar;
        @BindView(R.id.profile)
        public TextView profile;
        @BindView(R.id.text_validity2)
        public TextView textValidity2;
        @BindView(R.id.vigence)
        public RelativeLayout vigence;
        @BindView(R.id.imgStage)
        public ImageView imgStage;
        @BindView(R.id.branch_office_line)
        public View viewDivider;
        @BindView(R.id.text_icon1)
        public TextView textViewIcon1;
        @BindView(R.id.text_icon2)
        public TextView textViewIcon2;
        @BindView(R.id.icon1)
        public ImageView imageViewIcon1;
        @BindView(R.id.icon_is_active)
        public ImageView iconIsActive;
        @BindView(R.id.section_icon)
        public LinearLayout linearLayoutSectionIcon;
        @BindView(R.id.iconDate)
        public MaterialIconView iconDate;
        @BindView(R.id.image_dots)
        public ImageView imageDots;

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
