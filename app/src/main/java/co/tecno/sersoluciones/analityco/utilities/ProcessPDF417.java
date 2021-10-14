package co.tecno.sersoluciones.analityco.utilities;

import android.database.Cursor;


import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import co.com.sersoluciones.facedetectorser.utilities.DebugLog;
import co.tecno.sersoluciones.analityco.ApplicationContext;
import co.tecno.sersoluciones.analityco.databases.DBHelper;

public class ProcessPDF417 implements Serializable {

    private static final byte ZERO = 48;
    /**
     * US-ASCII SP, space (32)
     */
    private static final char SP = ' ';

    private static final long serialVersionUID = 6355349373843186253L;
    private String datos;
    private String fechaNacimiento;
    private int numero;

    private String primerApellido;
    private String primerNombre;
    private byte[] rawData;
    private String segundoApellido;
    private String segundoNombre;
    private String tipoSangre;

    private String decodeUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public int getTipoCedula() {
        if (this.rawData[0] == ZERO && this.rawData[1] == (byte) 50) {
            return 2;
        }
        if (this.rawData[0] == ZERO && this.rawData[1] == (byte) 51) {
            return 3;
        }
        if (this.rawData[0] == (byte) 73 && this.rawData[1] == (byte) 51) {
            return 4;
        }
        return 0;
    }

    private String getTextoData(int i, int j) {
        String texto = "";
        int posicion = (i + j) - 1;
        while (posicion >= i && this.datos.charAt(posicion) == '\u0000' && this.datos.charAt(posicion) == SP) {
            posicion--;
        }
        int k = i;
        while (k <= posicion) {
            if (!(this.datos.charAt(k) == '\u0000' || this.datos.charAt(k) == SP)) {
                texto = texto + this.datos.charAt(k);
            }
            k++;
        }
        return texto;
    }

    private void detectCity(DecodeBarcode.InfoUser infoUser) {
        String code = String.format("%s%s", infoUser.stateCode, infoUser.cityCode);
        DebugLog.logW(String.format("%s", code));
        String select = "(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ? ) and ( " + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? )";
        String[] selectArgs = {code, String.valueOf(0)};

        Cursor cursor = ApplicationContext.applicationContext
                .getContentResolver().query(Constantes.CONTENT_CITY_URI, null, select, selectArgs,
                        DBHelper.CITY_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE));
            String city = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME));
            String state = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
            DebugLog.logW(String.format("city: %s state: %s", city, state));
            if (code.equals(cityCode)) {
                infoUser.cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CITY_CODE));
                infoUser.stateCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE_CODE));
            }
            cursor.close();
        }
    }

    private String getMesTexto(String mes) {
        if ("01".equals(mes)) {
            return "ENE";
        }
        if ("02".equals(mes)) {
            return "FEB";
        }
        if ("03".equals(mes)) {
            return "MAR";
        }
        if ("04".equals(mes)) {
            return "ABR";
        }
        if ("05".equals(mes)) {
            return "MAY";
        }
        if ("06".equals(mes)) {
            return "JUN";
        }
        if ("07".equals(mes)) {
            return "JUL";
        }
        if ("08".equals(mes)) {
            return "AGO";
        }
        if ("09".equals(mes)) {
            return "SEP";
        }
        if ("10".equals(mes)) {
            return "OCT";
        }
        if ("11".equals(mes)) {
            return "NOV";
        }
        if ("12".equals(mes)) {
            return "DIC";
        }
        return "";
    }

    private void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

}

