package com.zebrascannerdemo.zebra;

public interface BarcodeEvent {
    void onBarcodeReceived(Barcode barcode);
}