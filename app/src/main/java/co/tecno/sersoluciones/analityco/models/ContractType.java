package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;


public class ContractType implements Serializable {
    public int Id;
    private String Type;
    public String Value;
    public String Description;
    private String File;

    public ContractType() {
    }

    public ContractType(int id, String type, String value, String description, String file) {
        Id = id;
        Type = type;
        Value = value;
        Description = description;
        File = file;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }
}
