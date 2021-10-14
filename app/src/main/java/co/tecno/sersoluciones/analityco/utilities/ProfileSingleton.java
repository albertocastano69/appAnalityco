package co.tecno.sersoluciones.analityco.utilities;

import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

/**
 * Created by Ser Soluciones SAS on 14/12/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProfileSingleton {

    private ArrayList<IProfile> iProfiles;

    private static ProfileSingleton instance = null;

    private ProfileSingleton() {
        // Exists only to defeat instantiation.
        //Prevent form the reflection api.
        if (instance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        iProfiles = new ArrayList<>();
    }

}
