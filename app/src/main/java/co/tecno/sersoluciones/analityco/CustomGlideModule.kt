package co.tecno.sersoluciones.analityco

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.*
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.*


@GlideModule
class CustomGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        super.registerComponents(context, glide, registry)

        registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
                .prepend(SVG::class.java, SvgEncoder())
                .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }
}

class SvgDrawableTranscoder : ResourceTranscoder<SVG?, PictureDrawable?> {

    override fun transcode(toTranscode: Resource<SVG?>, options: Options): Resource<PictureDrawable?>? {
        val svg: SVG = toTranscode.get()
        val picture = svg.renderToPicture()
        val drawable = PictureDrawable(picture)
        return SimpleResource(drawable)
    }
}

class SvgEncoder : ResourceEncoder<SVG> {
    override fun getEncodeStrategy(options: Options): EncodeStrategy {
        return EncodeStrategy.SOURCE
    }

    override fun encode(data: Resource<SVG>, file: File, options: Options): Boolean {
        try {
            val svg = data.get()
            val picture: Picture = svg.renderToPicture()
            val bitmap = Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888)

            val os: OutputStream
            try {
                os = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            picture.writeToStream(FileOutputStream(file))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}

class SvgDecoder : ResourceDecoder<InputStream?, SVG?> {


    val id: String
        get() = "SvgDecoder.com.bumptech.svgsample.app"

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG?>? {
        return try {
            val svg = SVG.getFromInputStream(source)
            SimpleResource(svg)
        } catch (ex: SVGParseException) {
            throw IOException("Cannot load SVG from stream", ex)
        }
    }

    override fun handles(source: InputStream, options: Options): Boolean {
        return true
    }
}

class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<PictureDrawable>?, isFirstResource: Boolean): Boolean {
        val view: ImageView = (target as ImageViewTarget<*>).view
        view.setLayerType(ImageView.LAYER_TYPE_NONE, null)
        return false
    }

    override fun onResourceReady(resource: PictureDrawable?, model: Any?, target: Target<PictureDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        val view: ImageView = (target as ImageViewTarget<*>).view
        view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
        return false
    }

}