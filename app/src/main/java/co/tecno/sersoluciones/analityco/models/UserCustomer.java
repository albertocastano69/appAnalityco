package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ser Soluciones SAS on 25/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class UserCustomer implements Serializable{

    public String Name;
    public String LastName;
    public String UserEmail;
    public String UserId;
    public Date StartDate;
    public Date FinishDate;
    public boolean IsActive;
    public String PhotoUrl;
    private String RoleId;

    public UserCustomer(String userId, Date startDate, Date finishDate, String roleId) {
        UserId = userId;
        StartDate = startDate;
        FinishDate = finishDate;
        RoleId = roleId;
    }
    public UserCustomer() {

    }
}
