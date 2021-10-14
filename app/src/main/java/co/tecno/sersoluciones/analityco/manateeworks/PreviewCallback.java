package co.tecno.sersoluciones.analityco.manateeworks;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import java.lang.reflect.Array;

/* compiled from: CameraManager */
final class PreviewCallback implements android.hardware.Camera.PreviewCallback {
    public boolean callbackActive = false;
    private final CameraConfigurationManager configManager;
    public int fbCounter = 0;
    int fpscount;
    public byte[][] frameBuffers;
    long lasttime = 0;
    public Handler previewHandler;
    public int previewMessage;
    private final boolean useOneShotPreviewCallback;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;

    /* compiled from: CameraManager */
    /* renamed from: com.manateeworks.PreviewCallback$1 */
    class C11731 implements android.hardware.Camera.PreviewCallback {
        C11731() {
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
            PreviewCallback.this.updateFps();
            Point cameraResolution = PreviewCallback.this.configManager.getCameraResolution();
            if (CameraManager.useBufferedCallback) {
                PreviewCallback.this.setPreviewCallback(camera, this, cameraResolution.x, cameraResolution.y);
            }
            if (PreviewCallback.this.previewHandler != null) {
                PreviewCallback.this.previewHandler.obtainMessage(PreviewCallback.this.previewMessage, cameraResolution.x, cameraResolution.y, data).sendToTarget();
                if (!CameraManager.useBufferedCallback) {
                    PreviewCallback.this.previewHandler = null;
                }
            }
        }
    }

    PreviewCallback(CameraConfigurationManager configManager, boolean useOneShotPreviewCallback) {
        this.configManager = configManager;
        this.useOneShotPreviewCallback = useOneShotPreviewCallback;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        updateFps();
        Point cameraResolution = this.configManager.getCameraResolution();
        if (!this.useOneShotPreviewCallback) {
            camera.setPreviewCallback(null);
        }
        if (this.previewHandler != null) {
            this.previewHandler.obtainMessage(this.previewMessage, cameraResolution.x, cameraResolution.y, data).sendToTarget();
            this.previewHandler = null;
        }
    }

    public int setPreviewCallback(Camera camera, android.hardware.Camera.PreviewCallback callback, int width, int height) {
        if (callback != null) {
            if (this.frameBuffers == null) {
                this.frameBuffers = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{2, (((width * height) * 2) * 110) / 100});
                this.fbCounter = 0;
                Log.i("preview resolution", String.valueOf(width) + "x" + String.valueOf(height));
            }
            if (!this.callbackActive) {
                camera.setPreviewCallbackWithBuffer(callback);
                this.callbackActive = true;
            }
            camera.addCallbackBuffer(this.frameBuffers[this.fbCounter]);
            this.fbCounter = 1 - this.fbCounter;
        } else {
            camera.setPreviewCallbackWithBuffer(callback);
            this.callbackActive = false;
        }
        if (callback == null) {
            this.frameBuffers = (byte[][]) null;
            System.gc();
        }
        return 0;
    }

    public android.hardware.Camera.PreviewCallback getCallback() {
        return new C11731();
    }

    private void updateFps() {
        if (this.lasttime == 0) {
            this.lasttime = System.currentTimeMillis();
            this.fpscount = 0;
        } else {
            long delay = System.currentTimeMillis() - this.lasttime;
            if (delay > 2000) {
                this.lasttime = System.currentTimeMillis();
                this.fpscount = 0;
            }
        }
        this.fpscount++;
    }
}
