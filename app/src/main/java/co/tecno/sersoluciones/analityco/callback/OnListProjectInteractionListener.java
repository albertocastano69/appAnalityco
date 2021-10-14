package co.tecno.sersoluciones.analityco.callback;

import android.widget.ImageView;

import co.tecno.sersoluciones.analityco.models.ProjectList;

public interface OnListProjectInteractionListener {
    void onProjectInteraction(ProjectList item, ImageView imageView);
}
