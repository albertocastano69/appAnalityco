package co.tecno.sersoluciones.analityco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.FileDescriptor;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;
import co.tecno.sersoluciones.analityco.utilities.VolleyMultipartRequest;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROFILE_HEADER = 0;
    private static final int TYPE_PROFILE_OPTIONS = 1;

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;

    private static final int MIN_ITEMS_COUNT = 1;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private final User user;
    private final Context context;
    private final int avatarSize;

    private float initPos;
    private float endPos;
    private final MyPreferences preferences;

    public UserProfileAdapter(Context context, User user) {
        this.context = context;
        int cellSize = Utils.getScreenWidth(context) / 3;
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        preferences = new MyPreferences(context);
        initPos = 0;
        endPos = 0;
        this.user = user;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PROFILE_HEADER;
        }
        return TYPE_PROFILE_HEADER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_PROFILE_HEADER == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_header, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileHeaderViewHolder(view, user);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_options, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileOptionsViewHolder(view, user);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (TYPE_PROFILE_HEADER == viewType) {
            bindProfileHeader((ProfileHeaderViewHolder) holder);
        } /*else if (TYPE_PROFILE_OPTIONS == viewType) {
            bindProfileOptions((ProfileOptionsViewHolder) holder);
        }*/
    }

    private void bindProfileHeader(final ProfileHeaderViewHolder holder) {

        if (user.avatar != null && !user.avatar.equals("null")) {
            String url = Constantes.URL_IMAGES + user.avatar;
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.profile_dummy)
                    .resize(avatarSize, avatarSize)
                    .centerCrop()
                    .transform(new CircleTransformation())
                    .into(holder.ivUserProfilePhoto);
        } else {
            Drawable iconProfileDrawer = ContextCompat.getDrawable(context, R.drawable.profile_dummy);
            if (user.imagePath != null && !user.imagePath.isEmpty()) {

                Uri imageUri = Uri.parse(user.imagePath);
                String readOnlyMode = "r";
                try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(imageUri, readOnlyMode)) {
                    assert pfd != null;
                    FileDescriptor fileDescriptor = Objects.requireNonNull(pfd).getFileDescriptor();
                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    //Bitmap bitmap = MetodosPublicos.scaleImage(user.imagePath, 5);
//                    bitmap = MetodosPublicos.rotateImage(bitmap, user.imageRotate);

                    iconProfileDrawer = new BitmapDrawable(context.getResources(), bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            holder.ivUserProfilePhoto.setImageDrawable(iconProfileDrawer);
        }

        holder.vUserProfileRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vUserProfileRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                animateUserProfileHeader(holder);
                return false;
            }
        });
    }

    private void animateUserProfileHeader(ProfileHeaderViewHolder viewHolder) {

        //long profileHeaderAnimationStartTime = System.currentTimeMillis();

        viewHolder.vUserProfileRoot.setTranslationY(-viewHolder.vUserProfileRoot.getHeight());
        viewHolder.ivUserProfilePhoto.setTranslationY(-viewHolder.ivUserProfilePhoto.getHeight());
        viewHolder.vUserDetails.setTranslationY(-viewHolder.vUserDetails.getHeight());
        //viewHolder.vUserStats.setAlpha(0);

        viewHolder.vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        viewHolder.ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        viewHolder.vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        //viewHolder.vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();

    }

    private void animateUserProfileOptions(ProfileOptionsViewHolder viewHolder) {

        viewHolder.vButtons.setTranslationY(-viewHolder.vButtons.getHeight());
        viewHolder.vUnderline.setScaleX(0);

        viewHolder.vButtons.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
        viewHolder.vUnderline.animate().scaleX(1).setDuration(200).setStartDelay(USER_OPTIONS_ANIMATION_DELAY + 300).setInterpolator(INTERPOLATOR).start();

    }

    private void animateUnderline(ProfileOptionsViewHolder viewHolder) {
        viewHolder.vUnderline.setTranslationX(initPos);
        viewHolder.vUnderline.animate().translationX(endPos)
                .setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);

    }

    @Override
    public int getItemCount() {
        return MIN_ITEMS_COUNT;
    }

    static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.profile_image)
        CircleImageView ivUserProfilePhoto;
        @BindView(R.id.vUserDetails)
        View vUserDetails;
        @BindView(R.id.btnFollow)
        Button btnFollow;
        /*@BindView(R.id.vUserStats)
        View vUserStats;*/
        @BindView(R.id.vUserProfileRoot)
        View vUserProfileRoot;
        @BindView(R.id.profile_name)
        TextView mNameView;
        @BindView(R.id.profile_email)
        TextView mEmailView;
        @BindView(R.id.profile_phone)
        TextView mPhoneView;

        @SuppressLint("SetTextI18n")
        ProfileHeaderViewHolder(View view, User user) {
            super(view);
            ButterKnife.bind(this, view);
            mNameView.setText(user.Name + "\n" + user.LastName);
            btnFollow.setText("CC " + user.DocumentNumber);
            mEmailView.setText("Email: " + user.Email);
            mPhoneView.setText("Telefóno: " + user.PhoneNumber);
        }
    }

    static class ProfileOptionsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.btnPhone)
        ImageButton btnPhone;
        @BindView(R.id.btnEmail)
        ImageButton btnEmail;
        @BindView(R.id.vUnderline)
        View vUnderline;
        @BindView(R.id.vButtons)
        View vButtons;
        final User user;

        ProfileOptionsViewHolder(View view, User user) {
            super(view);
            ButterKnife.bind(this, view);
            this.user = user;
        }
    }

    @SuppressWarnings("unused")
    public class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
    }

    private class CircleTransformation implements Transformation {

        private static final int STROKE_WIDTH = 6;

        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);

            Paint avatarPaint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            avatarPaint.setShader(shader);

            Paint outlinePaint = new Paint();
            outlinePaint.setColor(Color.WHITE);
            outlinePaint.setStyle(Paint.Style.STROKE);
            outlinePaint.setStrokeWidth(STROKE_WIDTH);
            outlinePaint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, avatarPaint);
            canvas.drawCircle(r, r, r - STROKE_WIDTH / 2, outlinePaint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circleTransformation()";
        }
    }
}