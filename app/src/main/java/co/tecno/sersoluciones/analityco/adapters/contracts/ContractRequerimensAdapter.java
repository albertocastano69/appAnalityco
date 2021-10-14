package co.tecno.sersoluciones.analityco.adapters.contracts;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.Requirements;

public class ContractRequerimensAdapter extends RecyclerView.Adapter<ContractRequerimensAdapter.ViewHolder> {

    private final ArrayList<Requirements> mItems;

    public ContractRequerimensAdapter(Context context, ArrayList<Requirements> mItems) {
        this.mItems = mItems;
    }

    @NonNull
    @Override
    public ContractRequerimensAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contract_requeriment_adapter, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.materialIcon)
        public MaterialIconView materialIcon;
        @BindView(R.id.iconLayout)
        public LinearLayout iconLayout;
        @BindView(R.id.typeDocument)
        public TextView typeDocument;
        @BindView(R.id.topText)
        public TextView title;
        @BindView(R.id.bottomText)
        public TextView subTitle;
        Requirements mItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.mItem = mItems.get(position);
        if (holder.mItem.ByEntry ){
            holder.iconLayout.setVisibility(View.GONE);
            holder.title.setText(holder.mItem.Name);
            holder.subTitle.setText(holder.mItem.Description);
        }else {
            holder.typeDocument.setText(holder.mItem.Abrv);
            holder.title.setText(holder.mItem.Name);
            holder.subTitle.setText(holder.mItem.Description);
        }



    }
}
