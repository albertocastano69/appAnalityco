package co.tecno.sersoluciones.analityco.recognition;

import java.io.Serializable;

public class BarcodeData implements Serializable {
    private int barcodeType;
    private byte[] scannedDocumentData;

    public BarcodeData(int barcodeType, byte[] scannedDocumentData) {
        this.barcodeType = barcodeType;
        this.scannedDocumentData = scannedDocumentData;
    }

    public byte[] getScannedDocumentData() {
        return this.scannedDocumentData;
    }

}
