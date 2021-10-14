package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import co.tecno.sersoluciones.analityco.individualContract.ContractSingleListFragment;
import co.tecno.sersoluciones.analityco.GlideApp;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.InfoIndividualContract;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

public class IndivudualContractAdapter extends RecyclerView.Adapter<IndivudualContractAdapter.IndividualContractViewHolder> {
    private List<InfoIndividualContract> infoIndividualContractList;
    private Context mContext;
    private final ContractSingleListFragment.OnListSingleContractInteractionListener mListener;

    public IndivudualContractAdapter(List<InfoIndividualContract> infoIndividualContractList, Context mContext, ContractSingleListFragment.OnListSingleContractInteractionListener mListener) {
        this.infoIndividualContractList = infoIndividualContractList;
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public IndividualContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_contract,parent,false);
        return new IndividualContractViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    public void onBindViewHolder(@NonNull IndividualContractViewHolder holder, int position) {
        holder.item = infoIndividualContractList.get(position);
        holder.NamePerson.setText(holder.item.Name+" "+holder.item.LastName);
        holder.DocumentPerson.setText(holder.item.DocumentType+" "+holder.item.DocumentNumber);
        holder.StatusContract.setText(holder.item.Descripction);
        if(holder.item.Descripction.equals("null") ){
            holder.StatusContract.setText("");
        }
        if(holder.item.CreateDate.equals("null")){
            holder.DateContract.setText("");
        }else{
            holder.DateContract.setText(holder.item.CreateDate);
            if(holder.item.CreateDate != null){
                String Create = "";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date date = simpleDateFormat.parse(holder.item.CreateDate);
                    Create= format.format(date);
                    holder.DateContract.setText(Create);
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }
        if(holder.item.Photo != null){
            String url = Constantes.URL_IMAGES + holder.item.Photo;
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
                        .into(holder.PhotoPerson);

            } else {
                Picasso.get()
                        .load(url)
                        .noFade()
                        .resize(0, 150)
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.profile_dummy)
                        .into(holder.PhotoPerson);
            }
        } else {
            holder.PhotoPerson.setImageResource(R.drawable.profile_dummy);
        }
        switch (holder.item.Descripction){
            case  "null":
            case "INICIADO":
                holder.RequestStatus.setImageResource(R.drawable.graybar);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.gray));
                break;
            case "SOLICITADO":
                holder.RequestStatus.setImageResource(R.drawable.subirandroid);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.accent));
               break;
            case "APROBADO":
                holder.RequestStatus.setImageResource(R.drawable.bar_blue);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                break;
            case "ANULADO":
                holder.RequestStatus.setImageResource(R.drawable.bar_red_anulado);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.anulado));
                break;
            case "LIQUIDADO":
                holder.RequestStatus.setImageResource(R.drawable.bar_red_liquidado);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.bar_undecoded));
                break;
            case "RECHAZADO PARA CORREGIR":
            case "RECHAZADO":
                holder.RequestStatus.setImageResource(R.drawable.bar_orange);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.expiry));
                break;
            case "CONTRATADO":
                holder.RequestStatus.setImageResource(R.drawable.bar_purple_contratado);
                holder.StatusContract.setTextColor(mContext.getResources().getColor(R.color.purple));
                break;

        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    String url = Constantes.URL_IMAGES + holder.item.Photo;
                    mListener.onListFragmentInteraction(url,holder.item);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return infoIndividualContractList.size();
    }

    public static class IndividualContractViewHolder  extends RecyclerView.ViewHolder{
        ImageView PhotoPerson, RequestStatus;
        TextView NamePerson;
        TextView DocumentPerson;
        TextView StatusContract;
        TextView DateContract;
        CardView cardView;
        InfoIndividualContract item;
        public IndividualContractViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_detail);
            PhotoPerson = itemView.findViewById(R.id.photo_person);
            NamePerson = itemView.findViewById(R.id.text_name);
            DocumentPerson = itemView.findViewById(R.id.text_sub);
            StatusContract = itemView.findViewById(R.id.LabelData);
            DateContract = itemView.findViewById(R.id.dateData);
            RequestStatus = itemView.findViewById(R.id.request_status);
        }
    }
    public  void  filtrar(ArrayList<InfoIndividualContract> filtro){
        this.infoIndividualContractList = filtro;
        notifyDataSetChanged();
    }
}
