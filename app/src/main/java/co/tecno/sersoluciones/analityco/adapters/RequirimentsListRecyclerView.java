package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;


public abstract class RequirimentsListRecyclerView<T> extends RecyclerView.Adapter<RequirimentsListRecyclerView.ViewHolder> {

    public final ArrayList<T> mItems;
    private final Context mContext;

    protected RequirimentsListRecyclerView(Context context, ArrayList<T> mItems) {
        this.mItems = mItems;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.requerimets_list_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.btn_edit)
        public ImageView btnEdit;
        @BindView(R.id.typeDocument)
        public TextView typeDocument;
        @BindView(R.id.description)
        public TextView description;
        @BindView(R.id.iconFile)
        public LinearLayout iconFile;
        @BindView(R.id.icon_calendar)
        public MaterialIconView icon_calendar;
        @BindView(R.id.date)
        public TextView date;

        //informacion exclusiva de arl
        @BindView(R.id.imgArl)
        public ImageView imgArl;
        @BindView(R.id.textArl)
        public TextView textArl;
        @BindView(R.id.arlContent)
        public LinearLayout arlContent;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            Drawable drawablePen = MaterialDrawableBuilder.with(mContext)
                    .setIcon(MaterialDrawableBuilder.IconValue.PENCIL)
                    .setColor(Color.rgb(106, 202, 37))
                    .setSizeDp(20)
                    .build();
            btnEdit.setImageDrawable(drawablePen);
        }

    }
}