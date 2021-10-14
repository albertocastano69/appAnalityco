package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import co.tecno.sersoluciones.analityco.callback.LoadProject;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.ProjectDetails;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.UpdateDBService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

public class AdapterProjectDetails extends RecyclerView.Adapter<AdapterProjectDetails.ViewHolder> {

    ArrayList<ProjectDetails>mValues;
    Context mcontext;
    public boolean online;
    LoadProject callback;

    public AdapterProjectDetails(ArrayList<ProjectDetails> mValues, Context mcontext, boolean online, LoadProject callback) {
        this.mValues = mValues;
        this.mcontext = mcontext;
        this.online = online;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_details,parent,false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"ResourceAsColor", "WrongConstant"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String dateSync = "";
        String dateNow = "";
        ProjectDetails projectDetails=mValues.get(position);
        holder.textViewNombre.setText(projectDetails.getName());
        holder.textViewfecha.setText(projectDetails.getDate());
        String Idproject = projectDetails.getIdProject();
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1=simpleDateFormat.parse(projectDetails.getDate());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat=new SimpleDateFormat("yyy/MMM/dd");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MMM/dd");
                dateSync=dateFormat.format(date1);
                dateNow=dtf.format(LocalDateTime.now());
            } catch (ParseException e) {
                e.printStackTrace();
            }
       if(!dateSync.equals(dateNow)){
            holder.textViewfecha.setTextColor(ContextCompat.getColor(
                    mcontext,
                    R.color.bar_undecoded
            ));
            holder.warining.setVisibility(View.VISIBLE);
        }else{
            holder.textViewfecha.setTextColor(R.color.black_alpha);
            holder.warining.setVisibility(View.GONE);
        }
       holder.warining.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Animation animation= AnimationUtils.loadAnimation(mcontext,R.anim.rotatesync);
               holder.sync.startAnimation(animation);
               callback.LoadProjectByids(Idproject,holder.textViewNombre.getText().toString());
           }
       });
        holder.sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation= AnimationUtils.loadAnimation(mcontext,R.anim.rotatesync);
                UpdateDBService.startRequest(mcontext, false);
                holder.sync.startAnimation(animation);
                //holder.sync.animate().rotationBy(360).setDuration(100000).start();
                callback.LoadProjectByids(Idproject,holder.textViewNombre.getText().toString());
            }
        });
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }
    private void LoadProjectByids(String projecids) {
        CRUDService.startRequest(
                mcontext, Constantes.LIST_PROJECTS_URL+projecids,
                Request.Method.GET, "", false
        );
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewNombre, textViewfecha;
        ImageView sync;
        LinearLayout warining;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre=itemView.findViewById(R.id.name_project_details);
            textViewfecha=itemView.findViewById(R.id.datesync);
            sync=itemView.findViewById(R.id.sync);
            warining= (LinearLayout) itemView.findViewById(R.id.warning);
        }
    }

}
