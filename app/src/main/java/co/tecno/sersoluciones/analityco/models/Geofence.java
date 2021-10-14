package co.tecno.sersoluciones.analityco.models;

/**
 * Created by Ser SOluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class Geofence {
    private final String type;
    public final double[][][] coordinates;

    public Geofence(double[][] coordinates) {
        this.type = "Polygon";
        this.coordinates = new double[][][]{coordinates};
    }
}
