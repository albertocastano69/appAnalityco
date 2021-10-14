package co.tecno.sersoluciones.analityco.recognition.co;


import com.google.gson.Gson;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import co.tecno.sersoluciones.analityco.recognition.BarcodeData;
import co.tecno.sersoluciones.analityco.recognition.DocumentRecognizer;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.Constantes.SP;
import static co.tecno.sersoluciones.analityco.utilities.Constantes.ZERO;

public class CedulaColombianaRecognizer implements DocumentRecognizer {

    private DecodeBarcode.InfoUser infoUser;
    private String datos;
    private byte[] rawData;
    public static Properties properties;

    private int getTipoCedula() {
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
    private String getTextoData(int i, int j, boolean textoV) {
        String texto = "";
        int posicion = (i + j) - 1;
        while (posicion >= i && this.datos.charAt(posicion) == '\u0000' && this.datos.charAt(posicion) == SP) {
            posicion--;
        }
        int k = i;
        logW("DATOS DE DATOS: " + this.datos.charAt(160));
        while (k <= posicion) {
            if (!(this.datos.charAt(k) == '\u0000' || this.datos.charAt(k) == SP)) {
                texto = texto + this.datos.charAt(k);
                logW("DATOS DE K: " + k);
                logW("DATOS DE RECONOCIMIENTO: " + texto);
            }
            k++;
        }
        if (!textoV) {
            try {
                texto = String.valueOf(Long.parseLong(texto));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return texto;
    }
    private void procesarCedula() {
        if (this.rawData.length < 531) {
            logE("Longitud del codigo de barra, erroneo");
            return;
        }
        int delta = 0;
        int deltaDepartment = 0;
        int deltaAfis = 0;
        if (getTextoData(48, 10, false).matches("\\d+(?:\\.\\d+)?")) {
            infoUser.dni = Integer.parseInt(getTextoData(48, 10, false));
        } else return;
        int tipo = getTipoCedula();
        switch (tipo) {
            case 2:
                if (getTextoData(22, 12, false).matches("\\d+(?:\\.\\d+)?")) {
                    deltaDepartment = 1;
                    deltaAfis = 2;
                }
                break;
            case 3:
                if ("PubDSK_1".equals(getTextoData(24, 8, false))) {
                    delta = 0;
                    deltaDepartment = 0;
                    deltaAfis = 0;
                    break;
                }
            case 4:
                if ("PubDSK_1".equals(getTextoData(24, 8, false))) {
                    delta = 1;
                    deltaDepartment = 1;
                    deltaAfis = 0;
                    break;
                }
            default:
                logW("procesarCedula: ERROR NONE");
                
                return;
        }
        StringBuilder name = new StringBuilder();
        StringBuilder lastName = new StringBuilder();
        String primerApellido = getTextoData(delta + 58, 23, false);
        String segundoApellido = getTextoData(delta + 81, 23, false);
        String primerNombre = getTextoData(delta + 104, 23, false);
        String segundoNombre = getTextoData(delta + 127, 23, false);
        name.append(primerNombre);
        if (!segundoNombre.isEmpty()) {
            name.append(" ");
            name.append(segundoNombre);
        }

        lastName.append(primerApellido);
        if (!segundoApellido.isEmpty()) {
            lastName.append(" ");
            lastName.append(segundoApellido);
        }
        String sex = getTextoData(delta + 151, 1, false);
        String fechaNacimiento = getTextoData(delta + 152, 8, false);
        String fechaNacimiento2 = fechaNacimiento.substring(6, 8) + "-" + getMesTexto(fechaNacimiento.substring(4, 6)) + "-" + fechaNacimiento.substring(0, 4);
        logW("fechaNacimiento: " + fechaNacimiento2);
        String tipoSangre = getTextoData(delta + 166, 3, false);
        String placeBirth = getTextoData(deltaDepartment + 160, 5, true);
        if (placeBirth == null || "".equals(placeBirth)) {
            infoUser.stateCode = "";
            infoUser.cityCode = "";
        } else {
            String[] placeBirthArray = getParameter(placeBirth);
            if (placeBirthArray == null || placeBirthArray.length != 2 || placeBirthArray[0] == null || placeBirthArray[1] == null) {
                infoUser.stateCode = placeBirth;
                infoUser.cityCode = placeBirth;
            } else {
                infoUser.stateCode = placeBirthArray[0];
                infoUser.cityCode = placeBirthArray[1];
            }
        }
        logW("placeBirthArray: " + placeBirth);
        infoUser.name = name.toString();
        infoUser.lastname = lastName.toString();
        infoUser.rh = tipoSangre;
        infoUser.birthDate = fechaNacimiento.matches("\\d+(?:\\.\\d+)?") ? Integer.parseInt(fechaNacimiento) : 0;
        logW("dateBirth: " + infoUser.birthDate);
        infoUser.DocumentRaw = String.format("%040x", new BigInteger(1, rawData));
        infoUser.sex = sex;
        infoUser.type = tipo;
        infoUser.afisCode = getTextoData(2, deltaAfis + 9, true); // getTextoData(delta + 2, 11);
        infoUser.documentType = "CC";
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

    private static String[] getParameter(String parameter) {
        try {
            if (properties != null) {
                String property = properties.getProperty(parameter);
                if (property != null && !"".equals(property)) {
                    return property.split("\\|");
                }
                return new String[]{parameter.substring(0, 2), parameter.substring(2)};
            }
            return new String[]{parameter.substring(0, 2), parameter.substring(2)};
        } catch (Exception e) {
            return null;
        }
    }
    public String process(BarcodeData barcodeData) {
        this.rawData = barcodeData.getScannedDocumentData();
        this.datos = new String(rawData, StandardCharsets.UTF_8);
        logW("DATOS DE RECONOCIMIENTODATOS: " + datos);
        this.infoUser = new DecodeBarcode.InfoUser();
        try {
            procesarCedula();
            String userJson = new Gson().toJson(infoUser);
            return userJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
