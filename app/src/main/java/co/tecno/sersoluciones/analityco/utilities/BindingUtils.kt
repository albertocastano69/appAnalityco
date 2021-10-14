package co.tecno.sersoluciones.analityco.utilities

import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.nav.LoadingApiStatus
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import net.steamcrafted.materialiconlib.MaterialIconView
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@BindingAdapter("loadingApiStatus")
fun bindLoadingStatus(view: ConstraintLayout, status: LoadingApiStatus?) {
    status?.let {
        when (status) {
            LoadingApiStatus.LOADING -> {
                view.visibility = View.VISIBLE
            }
            LoadingApiStatus.ERROR -> {
                view.visibility = View.GONE
            }
            LoadingApiStatus.DONE -> {
                view.visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("setFormatUTCDate")
fun TextView.setFormatUTCDate(dateStr: String?) {
    dateStr?.let {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateStr)
        val myFormat = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        text = sdf.format(date!!)
    }
}

@BindingAdapter("setImageCalendar")
fun MaterialIconView.setImageCalendar(isActive: Boolean) {
    if (!isActive) {
        val calendarRed = MaterialDrawableBuilder.with(context) // provide a context
            .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR) // provide an icon
            .setColor(Color.RED) // set the icon color
            .setSizeDp(20) // set the icon size
            .build()
        setImageDrawable(calendarRed)
    } else {
        val calendar = MaterialDrawableBuilder.with(context) // provide a context
            .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR) // provide an icon
            .setColor(Color.parseColor("#b3000000")) // set the icon color
            .setSizeDp(20) // set the icon size
            .build()
        setImageDrawable(calendar)
    }
}

@BindingAdapter("setImageContractType")
fun ImageView.setImageContractType(contractType: String?) {
    contractType?.let {
        when (contractType) {
            "AD" -> setImageResource(R.drawable.ic_02_administrativotrasparente2)
            "FU" -> setImageResource(R.drawable.ic_14_funcionariotrasparente)
            "PR" -> setImageResource(R.drawable.ic_10_proveedor_trasparente)
            "AS" -> setImageResource(R.drawable.ic_04_asociado_trasparente2)
            "VI" -> setImageResource(R.drawable.ic_16_visitante_trasparente)
            "OT" -> setImageResource(R.drawable.ic_18_otros_trasparente)
            "CO" -> setImageResource(R.drawable.ic_06_contratista_trasparente)
        }
    }
}


@BindingAdapter("imgValidateFinishDate")
fun ImageView.imgValidateFinishDate(finishDate: String?) {
    finishDate?.let {
        if (!validateFinishDate(finishDate)) {
            visibility = View.VISIBLE
            setImageResource(R.drawable.state_icon_red)
        }
    }
}

@BindingAdapter("setLogoImage")
fun ImageView.setImage(logo: String?) {
    logo?.let {
        val url = Constantes.URL_IMAGES + logo

        val format = logo.split(Pattern.quote(".").toRegex()).toTypedArray()
        if (format[format.size - 1] == "svg") {
            GlideApp.with(context)
                .`as`(PictureDrawable::class.java)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.image_not_available)
                )
                .fitCenter()
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .load(url)
                .into(this)
        } else {
            try {
                val uriImage = Uri.parse(logo)
                context.contentResolver.openFileDescriptor(uriImage, "r").use {

                }

                Picasso.get().load(uriImage)
                    .resize(0, 250)
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.image_not_available)
                    .into(this)
            } catch (e: Exception) {
                Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.image_not_available)
                    .into(this)
            }
        }

    }
}

@BindingAdapter("setFlagBackground")
fun ImageView.setFlagBackground(flag: Int?) {
    flag?.let {
        if (flag == 0) setImageResource(R.drawable.ic_flag_of_colombia)
        else setImageResource(R.drawable.ic_flag_of_venezuela)
    }
}


@BindingAdapter("setLogoCircleImage")
fun ImageView.setCircleImage(logo: String?) {
    logo?.let {
        val url = Constantes.URL_IMAGES + logo
        Picasso.get().load(url)
            .transform(CircleTransform())
//            .resize(0, 250)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.image_not_available)
            .into(this)
    }
}