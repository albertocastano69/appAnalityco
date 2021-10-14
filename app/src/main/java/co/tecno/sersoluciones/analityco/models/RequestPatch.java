package co.tecno.sersoluciones.analityco.models;

/**
 * Created by Ser Soluciones SAS on 19/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class RequestPatch {
    private String op;
    private String path;
    private Object value;

    public RequestPatch() {
    }

    public RequestPatch(String path, Object value) {
        this.op = "replace";
        this.path = String.format("/%s", path);
        this.value = value;
    }
}
