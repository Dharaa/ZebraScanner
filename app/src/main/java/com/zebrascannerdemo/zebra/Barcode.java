package com.zebrascannerdemo.zebra;

import android.os.Parcel;
import android.os.Parcelable;

public class Barcode implements Parcelable {
    public static final Creator<Barcode> CREATOR = new Creator<Barcode>() {

        @Override
        public Barcode createFromParcel(Parcel source) {
            return new Barcode(source);
        }

        @Override
        public Barcode[] newArray(int size) {
            return new Barcode[size];
        }
    };
    byte[] barcodeData;
    int barcodeType;
    int fromScannerID;

    public Barcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
        this.barcodeData = barcodeData;
        this.barcodeType = barcodeType;
        this.fromScannerID = fromScannerID;
    }

    public Barcode(Parcel in) {
        this.barcodeData = in.readString().getBytes();
        this.barcodeType = in.readInt();
        this.fromScannerID = in.readInt();
    }

    public byte[] getBarcodeData() {
        return barcodeData;
    }

    public void setBarcodeData(byte[] barcodeData) {
        this.barcodeData = barcodeData;
    }

    public int getFromScannerID() {
        return fromScannerID;
    }

    public void setFromScannerID(int fromScannerID) {
        this.fromScannerID = fromScannerID;
    }

    public int getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(int barcodeType) {
        this.barcodeType = barcodeType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(new String(barcodeData));
        parcel.writeInt(barcodeType);
        parcel.writeInt(fromScannerID);
    }
}
