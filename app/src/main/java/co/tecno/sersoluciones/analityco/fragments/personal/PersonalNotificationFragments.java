package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.DetailsPersonalActivityTabs;
import co.tecno.sersoluciones.analityco.NotificationList;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.Notification;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

import static com.android.volley.VolleyLog.TAG;

public class PersonalNotificationFragments extends Fragment {
    private Notification notification;
    private static final String ARG_NOTIFICATION = "personal_notification";
    private String modelNotification;
    public static PersonalNotificationFragments newInstance(String StringNotification) {
        PersonalNotificationFragments fragment = new PersonalNotificationFragments();

        Bundle args = new Bundle();
        args.putString(ARG_NOTIFICATION, StringNotification);
        fragment.setArguments(args);
        return fragment;


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mValues = new ArrayList<>();
        if (getArguments() != null) {
            modelNotification = getArguments().getString(ARG_NOTIFICATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_personal_list);
        Gson gson = new Gson();
        notification = gson.fromJson(modelNotification, new TypeToken<Notification>() {
        }.getType());
        LinearLayoutManager mLayoutManager4 = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(mLayoutManager4);

        PersonalRecyclerAdapter adapterPersonal = new PersonalRecyclerAdapter(requireContext(), notification.Personal);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterPersonal);
        return view;

    }

    private class PersonalRecyclerAdapter extends CustomListInfoRecyclerViewUsers<PersonalList> {

        PersonalRecyclerAdapter(Context context, ArrayList<PersonalList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final PersonalList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.LastName);
            holder.textSubName.setText("CC " + mItem.DocumentNumber);
            holder.phone.setVisibility(View.GONE);
            holder.textValidity2.setText("Expira en " + mItem.DaysToExpire + " dias");
            holder.logo.setImageResource(R.drawable.image_not_available);
            if (mItem.Photo != null) {
                String url = Constantes.URL_IMAGES + mItem.Photo;

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }

            holder.btnEdit.setVisibility(View.GONE);
            holder.vigence.setVisibility(View.GONE);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parcelablePersonalInteraction(holder.logo, mItem, DetailsPersonalActivityTabs.class);
                }
            });
        }
    }

    private void parcelablePersonalInteraction(ImageView imageView, Object object, final Class<? extends Activity> ActivityToOpen) {

        Intent i = new Intent(requireContext(), ActivityToOpen);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ITEM_OBJ, (Parcelable) object);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));
        startActivity(i);
    }

}
