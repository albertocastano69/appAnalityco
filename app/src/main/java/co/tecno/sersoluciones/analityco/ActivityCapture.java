package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.dynamite.descriptors.com.google.android.gms.flags.ModuleDescriptor;

import java.io.IOException;

import co.tecno.sersoluciones.analityco.manateeworks.CameraManager;

public final class ActivityCapture extends AppCompatActivity implements Callback, OnRequestPermissionsResultCallback {

    public static int MAX_THREADS = (Runtime.getRuntime().availableProcessors() > 1 ? Runtime.getRuntime().availableProcessors() : 1);

    public static int activeThreads = 0;
    private ImageButton buttonFlash;
    private Handler decodeHandler;
    private int firstZoom = ModuleDescriptor.MODULE_VERSION;
    boolean flashOn = false;
    private boolean hasSurface;
    private String package_name;
    private int secondZoom = 300;
    State state = State.STOPPED;
    private SurfaceHolder surfaceHolder;
    private ImageButton zoomButton;
    private int zoomLevel = 0;


    /* renamed from: com.manateeworks.cameraDemo.ActivityCapture$2 */
    class C11752 implements Handler.Callback {
        C11752() {
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d("FOCUS", "run AutoFoco!!!");
                    Log.d("ActivityCapture::", "2");
                    if (ActivityCapture.this.state == State.PREVIEW || ActivityCapture.this.state == State.DECODING) {
                        Log.d("ActivityCapture::", "3");
                        CameraManager.get().requestAutoFocus(ActivityCapture.this.decodeHandler, 1);
                        break;
                    }
                case 2:
                    Log.d("DECODE", "Decodificar!!!");
                    if (ActivityCapture.this.state != State.STOPPED) {
                        ActivityCapture.this.decode((byte[]) msg.obj, msg.arg1, msg.arg2);
                        break;
                    }
                    break;
                case 4:
                    Log.d("PREVIEW", "Reiniciar Preview!!!");
                    ActivityCapture.this.restartPreviewAndDecode();
                    break;
                case 8:
                    Log.d("DECODE", "Encontrado!!!");
                    ActivityCapture.this.state = State.STOPPED;

                    break;
                case 16:
                    Log.d("DECODE", "Falló!!!");
                    break;
            }
            return false;
        }
    }

    /* renamed from: com.manateeworks.cameraDemo.ActivityCapture$3 */
    class C11763 implements OnClickListener {
        C11763() {
        }

        public void onClick(View v) {
            ActivityCapture.this.zoomLevel = ActivityCapture.this.zoomLevel + 1;
            if (ActivityCapture.this.zoomLevel > 2) {
                ActivityCapture.this.zoomLevel = 0;
            }
            switch (ActivityCapture.this.zoomLevel) {
                case 0:
                    CameraManager.get().setZoom(100);
                    return;
                case 1:
                    CameraManager.get().setZoom(ActivityCapture.this.firstZoom);
                    return;
                case 2:
                    CameraManager.get().setZoom(ActivityCapture.this.secondZoom);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.manateeworks.cameraDemo.ActivityCapture$4 */
    class C11774 implements OnClickListener {
        C11774() {
        }

        public void onClick(View v) {
            ActivityCapture.this.toggleFlash();
        }
    }

    /* renamed from: com.manateeworks.cameraDemo.ActivityCapture$5 */
    class C11785 implements DialogInterface.OnClickListener {
        C11785() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            ActivityCapture.this.onBackPressed();
        }
    }

    /* renamed from: com.manateeworks.cameraDemo.ActivityCapture$6 */
    class C11796 implements DialogInterface.OnClickListener {
        C11796() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            ActivityCompat.requestPermissions(ActivityCapture.this, new String[]{"android.permission.CAMERA"}, 12322);
        }
    }

    /* renamed from: com.manateeworks.cameraDemo.ActivityCapture$7 */
    class C11807 implements Runnable {
        C11807() {
        }

        public void run() {
            switch (ActivityCapture.this.zoomLevel) {
                case 0:
                    CameraManager.get().setZoom(100);
                    return;
                case 1:
                    CameraManager.get().setZoom(ActivityCapture.this.firstZoom);
                    return;
                case 2:
                    CameraManager.get().setZoom(ActivityCapture.this.secondZoom);
                    return;
                default:
                    return;
            }
        }
    }

    public static class DecoderRunnig implements Runnable {
        private ActivityCapture activityCapture;

        public DecoderRunnig(ActivityCapture activityCapture, byte[] data, int width, int height) {
            this.activityCapture = activityCapture;
        }

        public void run() {
            try {
                ActivityCapture.activeThreads++;
                long start = System.currentTimeMillis();
                if (this.activityCapture.state == State.STOPPED) {
                    ActivityCapture.activeThreads--;
                    return;
                }

//                if (this.activityCapture.state != State.STOPPED) {
//                    if (mwResult != null) {
//                        this.activityCapture.state = State.STOPPED;
//                        Message message = Message.obtain(this.activityCapture.getHandler(), 8, mwResult);
//                        message.arg1 = mwResult.type;
//                        message.sendToTarget();
//                    } else {
//                        Message.obtain(this.activityCapture.getHandler(), 16).sendToTarget();
//                    }
//                }
                this.activityCapture = null;
                ActivityCapture.activeThreads--;
            } finally {
                ActivityCapture.activeThreads--;
            }
        }
    }

    private enum OverlayMode {
    }

    private enum State {
        STOPPED,
        PREVIEW,
        DECODING
    }

    public Handler getHandler() {
        return this.decodeHandler;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.package_name = getPackageName();
        getWindow().addFlags(128);
        setContentView(R.layout.capture);

//        if (OVERLAY_MODE == OverlayMode.OM_IMAGE) {
//            ((ImageView) findViewById(R.id.imageOverlay)).setVisibility(View.VISIBLE);
//        }

        CameraManager.init(this);
        this.hasSurface = false;
        this.state = State.STOPPED;
        this.decodeHandler = new Handler(new C11752());
        this.zoomButton = (ImageButton) findViewById(R.id.zoomButton);
        this.zoomButton.setOnClickListener(new C11763());
        this.buttonFlash = (ImageButton) findViewById(R.id.flashButton);
        this.buttonFlash.setOnClickListener(new C11774());
    }

    protected void onResume() {
        super.onResume();
        activeThreads = 0;
        SurfaceView surfaceView = (SurfaceView) findViewById(getResources().getIdentifier("preview_view", "id", this.package_name));
        this.surfaceHolder = surfaceView.getHolder();
//        if (OVERLAY_MODE == OverlayMode.OM_MWOVERLAY) {
//            MWOverlay.addOverlay(this, surfaceView);
//        }
        if (this.hasSurface) {
            Log.i("Init Camera", "On resume");
            initCamera();
            return;
        }
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(3);
    }

    @SuppressLint({"Override"})
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 12322) {
            return;
        }
        if (grantResults[0] == 0) {
            initCamera();
        } else {
            onBackPressed();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        stopScanner();
    }

    protected void onPause() {
        super.onPause();
        stopScanner();
    }

    private void stopScanner() {
        this.flashOn = false;
        updateFlash();
//        if (OVERLAY_MODE == OverlayMode.OM_MWOVERLAY) {
//            MWOverlay.removeOverlay();
//        }
        try {
            ((ImageView) findViewById(R.id.imageOverlay)).setImageDrawable(null);
        } catch (Exception e) {
        }
        CameraManager.get().stopPreview();
        CameraManager.get().closeDriver();
        CameraManager.get().lastHolder = null;
        CameraManager.get().camera = null;
        this.surfaceHolder.removeCallback(this);
        this.state = State.STOPPED;
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void onConfigurationChanged(Configuration config) {
        CameraManager.get().updateCameraOrientation(((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation());
        super.onConfigurationChanged(config);
    }

    private void toggleFlash() {
        this.flashOn = !this.flashOn;
        updateFlash();
    }

    private void updateFlash() {
        if (CameraManager.get().isTorchAvailable()) {
            this.buttonFlash.setVisibility(View.VISIBLE);
            if (this.flashOn) {
                this.buttonFlash.setImageResource(R.drawable.ic_flash_on_white_36dp);
            } else {
                this.buttonFlash.setImageResource(R.drawable.ic_flash_off_white_36dp);
            }
            CameraManager.get().setTorch(this.flashOn);
            this.buttonFlash.postInvalidate();
            return;
        }
        this.buttonFlash.setVisibility(View.GONE);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (!this.hasSurface) {
            this.hasSurface = true;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.hasSurface = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("Init Camera", "On Surface changed");
        initCamera();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 80 || keyCode == 27) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void decode(byte[] data, int width, int height) {
        if (activeThreads < MAX_THREADS && this.state != State.STOPPED) {
            new Thread(new DecoderRunnig(this, data, width, height)).start();
        }
    }

    private void restartPreviewAndDecode() {
        if (this.state == State.STOPPED) {
            this.state = State.PREVIEW;
            Log.i("preview", "requestPreviewFrame.");
            CameraManager.get().requestPreviewFrame(getHandler(), 2);
            Log.d("ActivityCapture::", "1");
            CameraManager.get().requestAutoFocus(getHandler(), 1);
        }
    }

    private void initCamera() {
        if (!isFinishing()) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
                try {
                    boolean z;
                    CameraManager cameraManager = CameraManager.get();
                    SurfaceHolder surfaceHolder = this.surfaceHolder;
                    if (getResources().getConfiguration().orientation == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    cameraManager.openDriver(surfaceHolder, z);
                    int maxZoom = CameraManager.get().getMaxZoom();
                    if (maxZoom < 100) {
                        this.zoomButton.setVisibility(View.GONE);
                    } else {
                        this.zoomButton.setVisibility(View.VISIBLE);
                        if (maxZoom < 300) {
                            this.secondZoom = maxZoom;
                            this.firstZoom = ((maxZoom - 100) / 2) + 100;
                        }
                    }
                    Log.i("preview", "start preview.");
                    this.flashOn = false;
                    new Handler().postDelayed(new C11807(), 300);
                    CameraManager.get().startPreview();
                    restartPreviewAndDecode();


                    updateFlash();
                } catch (IOException ioe) {
                    displayFrameworkBugMessageAndExit(ioe.getMessage());
                } catch (RuntimeException e) {
                    displayFrameworkBugMessageAndExit(e.getMessage());
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.CAMERA")) {
                new Builder(this).setMessage("Necesitas dar permiso de acceso a la cámara").setPositiveButton("OK", new C11796()).setNegativeButton("Cancelar", new C11785()).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 12322);
            }
        }
    }

    private void displayFrameworkBugMessageAndExit(String message) {
//        Builder builder = new Builder(this);
//        builder.setTitle(getResources().getIdentifier("app_name", "string", this.package_name));
//        builder.setMessage(MSG_CAMERA_FRAMEWORK_BUG + message);
//        builder.setPositiveButton("OK", new C11818());
//        builder.show();
    }
}
