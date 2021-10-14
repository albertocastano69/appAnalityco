package co.tecno.sersoluciones.analityco.manateeworks;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Build.VERSION;
import android.os.Handler;
import androidx.core.view.ViewCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public final class CameraManager {
    public static boolean DEBUG = false;
    public static int REFOCUSING_DELAY = 2500;
    public static String TAG = "CameraManager";
    public static boolean USE_FRONT_CAMERA = false;
    public static boolean USE_SAMSUNG_FOCUS_ZOOM_PATCH = false;
    private static CameraManager cameraManager;
    public static int mDesiredHeight = 720;
    public static int mDesiredWidth = 1280;
    public static boolean refocusingActive = false;
    public static boolean useBufferedCallback = true;
    public Camera camera;
    private PreviewCallback cb;
    public final CameraConfigurationManager configManager;
    private final Context context;
    public Timer focusTimer;
    private boolean initialized;
    public SurfaceHolder lastHolder;
    public final PreviewCallback previewCallback;
    public boolean previewing;
    private final boolean useOneShotPreviewCallback = true;

    /* renamed from: com.manateeworks.CameraManager$3 */
    class C11683 extends TimerTask {
        C11683() {
        }

        public void run() {
            if (CameraManager.this.camera != null) {
                try {
                    CameraManager.this.camera.autoFocus(null);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    public static CameraManager get() {
        return cameraManager;
    }

    private CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        useBufferedCallback = true;
        this.previewCallback = new PreviewCallback(this.configManager, this.useOneShotPreviewCallback);
    }

    public void openDriver(SurfaceHolder holder, boolean isPortrait) throws IOException {
        int i = 0;
        if (this.camera == null) {
            if (DEBUG) {
                Log.i(TAG, "Camera opening...");
            }
            if (USE_FRONT_CAMERA) {
                this.camera = Camera.open(1);
            } else {
                this.camera = Camera.open(0);
            }
            if (this.camera == null) {
                if (DEBUG) {
                    Log.i(TAG, "First camera open failed");
                }
                this.camera = Camera.open(0);
                if (this.camera == null) {
                    if (DEBUG) {
                        Log.i(TAG, "Secoond camera open failed");
                    }
                    throw new IOException();
                }
            }
            if (DEBUG) {
                Log.i(TAG, "Camera open success");
            }
            if (VERSION.SDK_INT >= 9) {
                if (USE_FRONT_CAMERA) {
                    i = 1;
                }
                setCameraDisplayOrientation(i, this.camera, isPortrait);
            } else if (isPortrait) {
                this.camera.setDisplayOrientation(90);
            }
            if (holder != null) {
                this.lastHolder = holder;
                this.camera.setPreviewDisplay(holder);
                if (DEBUG) {
                    Log.i(TAG, "Set camera current holder");
                }
            } else {
                this.camera.setPreviewDisplay(this.lastHolder);
                if (DEBUG) {
                    Log.i(TAG, "Set camera last holder");
                }
                if (this.lastHolder == null && DEBUG) {
                    Log.i(TAG, "Camera last holder is NULL");
                }
            }
            if (!this.initialized) {
                this.initialized = true;
                this.configManager.initFromCameraParameters(this.camera);
                if (DEBUG) {
                    Log.i(TAG, "configManager initialized");
                }
            }
            this.configManager.setDesiredCameraParameters(this.camera);
            if (DEBUG) {
                Log.i(TAG, "Camera set desired parameters");
            }
        } else if (DEBUG) {
            Log.i(TAG, "Camera already opened");
        }
    }

    public int getMaxZoom() {
        if (this.camera == null) {
            return -1;
        }
        Parameters cp = this.camera.getParameters();
        if (!cp.isZoomSupported()) {
            return -1;
        }
        List<Integer> zoomRatios = cp.getZoomRatios();
        return ((Integer) zoomRatios.get(zoomRatios.size() - 1)).intValue();
    }

    public void setZoom(int zoom) {
        if (this.camera != null) {
            final Parameters cp = this.camera.getParameters();
            int minDist = 100000;
            int bestIndex = 0;
            if (zoom == -1) {
                int zoomIndex = cp.getZoom() - 1;
                if (zoomIndex >= 0) {
                    zoom = ((Integer) cp.getZoomRatios().get(zoomIndex)).intValue();
                }
            }
            List<Integer> zoomRatios = cp.getZoomRatios();
            if (zoomRatios != null) {
                for (int i = 0; i < zoomRatios.size(); i++) {
                    int z = ((Integer) zoomRatios.get(i)).intValue();
                    if (Math.abs(z - zoom) < minDist) {
                        minDist = Math.abs(z - zoom);
                        bestIndex = i;
                    }
                }
                final int fBestIndex = bestIndex;
                if (!USE_SAMSUNG_FOCUS_ZOOM_PATCH) {
                    stopFocusing();
                    cp.setZoom(fBestIndex);
                    this.camera.setParameters(cp);
                    startFocusing();
                } else if (bestIndex > 10) {
                    stopFocusing();
                    cp.setZoom(fBestIndex - 5);
                    this.camera.setParameters(cp);
                    this.camera.autoFocus(null);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (CameraManager.this.camera != null) {
                                CameraManager.this.camera.cancelAutoFocus();
                                cp.setZoom(fBestIndex);
                                CameraManager.this.camera.setParameters(cp);
                            }
                            CameraManager.this.startFocusing();
                        }
                    }, 200);
                } else {
                    stopFocusing();
                    cp.setZoom(fBestIndex);
                    this.camera.setParameters(cp);
                    startFocusing();
                }
            }
        }
    }

    public boolean isTorchAvailable() {
        if (this.camera == null) {
            return false;
        }
        List<String> flashModes = this.camera.getParameters().getSupportedFlashModes();
        if (flashModes == null || !flashModes.contains("torch")) {
            return false;
        }
        return true;
    }

    public void setTorch(final boolean enabled) {
        if (this.camera != null) {
            try {
                final Parameters cp = this.camera.getParameters();
                List<String> flashModes = cp.getSupportedFlashModes();
                if (flashModes != null && flashModes.contains("torch")) {
                    this.camera.cancelAutoFocus();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (CameraManager.this.camera != null) {
                                if (enabled) {
                                    cp.setFlashMode("torch");
                                } else {
                                    cp.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                }
                                CameraManager.this.camera.setParameters(cp);
                            }
                        }
                    }, 300);
                }
            } catch (Exception e) {
            }
        }
    }

    public void closeDriver() {
        if (this.camera != null) {
            if (useBufferedCallback) {
                this.camera.release();
                this.camera = null;
            } else {
                this.camera.release();
                this.camera = null;
            }
        }
    }

    public void startFocusing() {
        if (!refocusingActive) {
            refocusingActive = true;
            this.focusTimer = new Timer();
            this.focusTimer.schedule(new C11683(), 700, (long) REFOCUSING_DELAY);
        }
    }

    public void stopFocusing() {
        this.camera.cancelAutoFocus();
        if (refocusingActive) {
            if (this.focusTimer != null) {
                this.focusTimer.cancel();
                this.focusTimer.purge();
            }
            refocusingActive = false;
        }
    }

    public void startPreview() {
        if (this.camera != null && !this.previewing) {
            this.camera.startPreview();
            this.previewing = true;
            startFocusing();
        }
    }

    public void stopPreview() {
        if (this.camera != null && this.previewing) {
            if (useBufferedCallback) {
                this.previewCallback.setPreviewCallback(this.camera, null, 0, 0);
            }
            if (!this.useOneShotPreviewCallback) {
                this.camera.setPreviewCallback(null);
            }
            stopFocusing();
            this.camera.stopPreview();
            this.previewCallback.setHandler(null, 0);
            this.previewing = false;
        }
    }

    public void requestPreviewFrame(Handler handler, int message) {
        if (this.camera != null && this.previewing) {
            this.previewCallback.setHandler(handler, message);
            if (useBufferedCallback) {
                this.cb = (PreviewCallback) this.previewCallback.getCallback();
                this.previewCallback.setPreviewCallback(this.camera, this.cb, this.configManager.cameraResolution.x, this.configManager.cameraResolution.y);
            } else if (this.useOneShotPreviewCallback) {
                this.camera.setOneShotPreviewCallback(this.previewCallback);
            } else {
                this.camera.setPreviewCallback(this.previewCallback);
            }
        }
    }

    public void requestAutoFocus(Handler handler, int message) {
    }

    public int getDeviceDefaultOrientation() {
        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = this.context.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if ((rotation == 0 || rotation == 2) && config.orientation == 2) {
            return 2;
        }
        if ((rotation == 1 || rotation == 3) && config.orientation == 1) {
            return 2;
        }
        return 1;
    }

    public void updateCameraOrientation(int rotation) {
        if (this.camera != null) {
            if (getDeviceDefaultOrientation() == 1) {
                switch (rotation) {
                    case 0:
                        this.camera.setDisplayOrientation(90);
                        return;
                    case 1:
                        this.camera.setDisplayOrientation(0);
                        return;
                    case 2:
                        this.camera.setDisplayOrientation(0);
                        return;
                    case 3:
                        this.camera.setDisplayOrientation(180);
                        return;
                    default:
                        return;
                }
            }
            switch (rotation) {
                case 0:
                    this.camera.setDisplayOrientation(0);
                    return;
                case 1:
                    this.camera.setDisplayOrientation(0);
                    return;
                case 2:
                    this.camera.setDisplayOrientation(180);
                    return;
                case 3:
                    this.camera.setDisplayOrientation(90);
                    return;
                default:
                    return;
            }
        }
    }

    @TargetApi(9)
    public void setCameraDisplayOrientation(int cameraId, Camera camera, boolean isPortrait) {
        int result;
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;

        switch (((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 360;
                break;
        }
        if (info.facing == 1) {
            result = (360 - ((info.orientation + degrees) % 360)) % 360;
        } else {
            result = ((info.orientation - degrees) + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
