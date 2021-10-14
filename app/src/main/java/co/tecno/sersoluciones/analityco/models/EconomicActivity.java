package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;

/**
 * Created by Ser Soluciones SAS on 11/08/2017.
 *  www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class EconomicActivity implements Serializable{

    public String Code;
    public String Name;

    public EconomicActivity(String code, String name) {
        Code = code;
        Name = name;
    }

    @Override
    public String toString() { return Name; }
}
