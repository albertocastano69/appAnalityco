package co.tecno.sersoluciones.analityco.manateeworks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.util.List;

/* compiled from: CameraManager */
final class CameraConfigurationManager {
    private static final String TAG = CameraConfigurationManager.class.getSimpleName();
    public static Point screenResolution;
    public Point cameraResolution;
    private final Context context;
    private int previewFormat;
    private String previewFormatString;

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    void initFromCameraParameters(Camera camera) {
        Parameters parameters = camera.getParameters();
        this.previewFormat = parameters.getPreviewFormat();
        this.previewFormatString = parameters.get("preview-format");
        Log.d(TAG, "Default preview format: " + this.previewFormat + '/' + this.previewFormatString);
        @SuppressLint("WrongConstant")
        Display display = ((WindowManager) this.context.getSystemService("window")).getDefaultDisplay();
        screenResolution = new Point(display.getWidth(), display.getHeight());
        Log.d(TAG, "Screen resolution: " + screenResolution);
        this.cameraResolution = getCameraResolution(parameters, new Point(CameraManager.mDesiredWidth, CameraManager.mDesiredHeight));
        Log.d(TAG, "Camera resolution: " + this.cameraResolution);
    }

    void setDesiredCameraParameters(Camera camera) {
        Parameters parameters = camera.getParameters();
        this.cameraResolution = getCameraResolution(parameters, new Point(CameraManager.mDesiredWidth, CameraManager.mDesiredHeight));
        Log.d(TAG, "Setting preview size: " + this.cameraResolution);
        parameters.setPreviewSize(this.cameraResolution.x, this.cameraResolution.y);
        try {
            String vss = parameters.get("video-stabilization-supported");
            if (vss != null && vss.equalsIgnoreCase("true")) {
                try {
                    if (parameters.get("video-stabilization") != null) {
                        parameters.set("video-stabilization", "true");
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e2) {
        }
        String focusMode = parameters.getFocusMode();
        try {
            parameters.setFocusMode("auto");
            camera.setParameters(parameters);
        } catch (Exception e3) {
            try {
                parameters.setFocusMode("auto");
                camera.setParameters(parameters);
            } catch (Exception e4) {
                parameters.setFocusMode(focusMode);
            }
        }
        try {
            List<int[]> supportedFPS = parameters.getSupportedPreviewFpsRange();
            int maxFps = -1;
            int maxFpsIndex = -1;
            for (int i = 0; i < supportedFPS.size(); i++) {
                int[] sr = (int[]) supportedFPS.get(i);
                if (sr[1] > maxFps) {
                    maxFps = sr[1];
                    maxFpsIndex = i;
                }
            }
            parameters.setPreviewFpsRange(((int[]) supportedFPS.get(maxFpsIndex))[0], ((int[]) supportedFPS.get(maxFpsIndex))[1]);
        } catch (Exception e5) {
        }
        Log.d(TAG, "Camera parameters flat: " + parameters.flatten());
        camera.setParameters(parameters);
    }

    public Point getCameraResolution() {
        return this.cameraResolution;
    }

    public static Point getMaxResolution(Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        int maxIndex = -1;
        int maxSize = 0;
        for (int i = 0; i < sizes.size(); i++) {
            int size = ((Size) sizes.get(i)).width * ((Size) sizes.get(i)).height;
            if (size > maxSize) {
                maxSize = size;
                maxIndex = i;
            }
        }
        return new Point(((Size) sizes.get(maxIndex)).width, ((Size) sizes.get(maxIndex)).height);
    }

    public static Point getCameraResolution(Parameters parameters, Point desiredResolution) {
        int i;
        int i2;
        if (parameters.get("preview-size-values") == null) {
            String previewSizeValueString = parameters.get("preview-size-value");
        }
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        int minDif = 99999;
        int minIndex = -1;
        int X = screenResolution.x;
        int Y = screenResolution.y;
        float resAR = 0;
        if (X > Y) {
            i = X;
        } else {
            i = Y;
        }
        float f = (float) i;
        if (X >= Y) {
            X = Y;
        }
        float screenAR = f / ((float) X);
        for (i2 = 0; i2 < sizes.size(); i2++) {
            resAR = ((float) ((Size) sizes.get(i2)).width) / ((float) ((Size) sizes.get(i2)).height);
            int dif = Math.abs(((Size) sizes.get(i2)).width - desiredResolution.x) + Math.abs(((Size) sizes.get(i2)).height - desiredResolution.y);
            if (dif < minDif) {
                minDif = dif;
                minIndex = i2;
            }
        }
        float desiredTotalSize = (float) (desiredResolution.x * desiredResolution.y);
        float bestARdifference = 100.0f;
        for (i2 = 0; i2 < sizes.size(); i2++) {
            float difference;
            float ARdifference;
            resAR = ((float) ((Size) sizes.get(i2)).width) / ((float) ((Size) sizes.get(i2)).height);
            float totalSize = (float) (((Size) sizes.get(i2)).height * ((Size) sizes.get(i2)).width);
            if (totalSize >= desiredTotalSize) {
                difference = totalSize / desiredTotalSize;
            } else {
                difference = desiredTotalSize / totalSize;
            }
            if (resAR >= screenAR) {
                ARdifference = resAR / screenAR;
            } else {
                ARdifference = screenAR / resAR;
            }
            if (((double) difference) < 1.1d && ARdifference < bestARdifference) {
                bestARdifference = ARdifference;
                minIndex = i2;
            }
        }
        return new Point(((Size) sizes.get(minIndex)).width, ((Size) sizes.get(minIndex)).height);
    }
}
