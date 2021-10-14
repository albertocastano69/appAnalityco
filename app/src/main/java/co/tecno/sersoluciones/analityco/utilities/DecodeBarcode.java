package co.tecno.sersoluciones.analityco.utilities;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.google.gson.Gson;


import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.tecno.sersoluciones.analityco.ApplicationContext;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.recognition.BarcodeData;
import co.tecno.sersoluciones.analityco.recognition.co.CedulaColombianaRecognizer;
import kotlin.ULong;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static java.lang.Integer.parseInt;

/**
 * Created by Ser Soluciones SAS on 14/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class DecodeBarcode {

    //private static final int[] STATES = new int[]{1, 3, 5, 7, 9, 11, 12, 13, 15, 16, 17, 19, 21, 23, 24,
    //        25, 26, 27, 28, 29, 31, 40, 44, 46, 48, 50, 52, 54, 56, 60, 64, 68, 72};

    public static class InfoUser implements Serializable {
        public long dni;
        public String afisCode;
        public String lastname;
        public String name;
        public String date;
        public String dateExp;
        public String dateDue;
        public int birthDate;
        public String rh;
        public String sex;
        public String nationality;
        public String stateCode;
        public String cityCode;
        public String cityOfBirthCode;
        public String DocumentRaw;
        public String documentType;
        public int type;

        public void setDni(Long dni) {
            this.dni = dni;
        }

        public void setAfisCode(String afisCode) {
            this.afisCode = afisCode;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setBirthDate(int birthDate) {
            this.birthDate = birthDate;
        }

        public void setRh(String rh) {
            this.rh = rh;
        }
        public void setSex(String sex) {
            this.sex = sex;
        }

        public void setStateCode(String stateCode) {
            this.stateCode = stateCode;
        }

        public void setCityCode(String cityCode) {
            this.cityCode = cityCode;
        }

        public void setDocumentRaw(String documentRaw) {
            DocumentRaw = documentRaw;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public static String processBarcode(String raw) {
//        return processBarcodeOld(raw);
//        log(String.format("%040x", new BigInteger(1, raw.getBytes())));
        raw = raw.trim().replaceAll(new String(new byte[]{(byte) 0xEF, (byte) 0xBF, (byte) 0xBD, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD}), new String(new byte[]{(byte) 0x00}));
//        log(String.format("%040x", new BigInteger(1, raw.getBytes())));
        byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
        CedulaColombianaRecognizer processPDF417 = new CedulaColombianaRecognizer();
        return processPDF417.process(new BarcodeData(1, bytes));
    }

    private static String detectCity(InfoUser infoUser) {
        String code = String.format("%s%s", infoUser.stateCode, infoUser.cityCode);
        logW(String.format("%s", code));
        String select = "(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {code};
        @SuppressLint("Recycle")
        Cursor cursor = ApplicationContext.applicationContext
                .getContentResolver().query(Constantes.CONTENT_CITY_URI, null, select, selectArgs,
                        DBHelper.CITY_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE));
            String city = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME));
            String state = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
            logW(String.format("city: %s state: %s", city, state));
            if (code.equals(cityCode)) {
                return code;
            }
        }
        return "";
    }
}
