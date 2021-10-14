package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class InfoUserScan implements Serializable {
    public long dni;
    private String afisCode;
    public String lastname;
    public String name;
    public String birthDate;
    public String rh;
    public String sex;
    public String stateCode;
    public String cityCode;
    public String DocumentRaw;


    public InfoUserScan() {
    }
}
