package com.zebrascannerdemo.zebra;

import com.zebra.scannercontrol.DCSScannerInfo;

public interface ScannerSessionTerminationEvent {
    void onSessionTerminated(DCSScannerInfo scannerInfo);
}
