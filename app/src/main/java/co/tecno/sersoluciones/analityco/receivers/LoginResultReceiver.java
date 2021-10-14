package co.tecno.sersoluciones.analityco.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Ser Soluciones SAS on 19/06/2017.
 *  www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class LoginResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    public LoginResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}