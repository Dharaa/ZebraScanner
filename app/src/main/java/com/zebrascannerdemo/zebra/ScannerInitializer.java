package com.zebrascannerdemo.zebra;

import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI;
import static com.zebrascannerdemo.zebra.Constant.DEBUG_TYPE.TYPE_DEBUG;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebrascannerdemo.Utils.Preferences;
import com.zebrascannerdemo.application.ScanApplication;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ScannerInitializer implements ScannerAppEngine, IDcsSdkApiDelegate,
        ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate,
        ScannerAppEngine.IScannerAppEngineDevEventsDelegate, BarcodeEvent, ScannerSessionTerminationEvent {

    private static final String TAG = "ScannerInitializer";
    public static boolean isScannerInitialized = false;
    private static ScannerInitializer INSTANCE;
    DCSSDKDefs.DCSSDK_BT_PROTOCOL selectedProtocol;
    DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG selectedConfig;
    //Zebra Scanner Initialization
    private ArrayList<IScannerAppEngineDevConnectionsDelegate> mDevConnDelegates = new ArrayList<>();
    private ArrayList<IScannerAppEngineDevEventsDelegate> mDevEventsDelegates = new ArrayList<>();
    private ArrayList<DCSScannerInfo> mScannerInfoList;
    private ArrayList<DCSScannerInfo> mOfflineScannerInfoList;
    private BarcodeEvent barcodeEvent;
    private ScannerSessionTerminationEvent sessionTerminationEvent;
    private final Handler dataHandler = new Handler(Looper.getMainLooper()) {
        boolean notification_processed = false;
        boolean result = false;
        boolean found = false;
        
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constant.IMAGE_RECEIVED) {
                Log.e(TAG, "IMAGE_RECEIVED" + msg.what);
                Constant.logAsMessage(TYPE_DEBUG, TAG, "Image Received");
                byte[] imageData = (byte[]) msg.obj;
                //Barcode barcode=(Barcode)msg.obj;
                //Application.barcodeData.add(barcode);
                for (IScannerAppEngineDevEventsDelegate delegate : mDevEventsDelegates) {
                    if (delegate != null) {
                        Constant.logAsMessage(TYPE_DEBUG, TAG, "Show Image Received");
                        delegate.scannerImageEvent(imageData);
                    }
                }
            } else if (msg.what == Constant.VIDEO_RECEIVED) {
                Log.e(TAG, "VIDEO_RECEIVED" + msg.what);
                Constant.logAsMessage(TYPE_DEBUG, TAG, "Video Received");
                byte[] videoEvent = (byte[]) msg.obj;
                for (IScannerAppEngineDevEventsDelegate delegate : mDevEventsDelegates) {
                    if (delegate != null) {
                        Constant.logAsMessage(TYPE_DEBUG, TAG, "Show Video Received");
                        delegate.scannerVideoEvent(videoEvent);
                    }
                }
                //Toast.makeText(getApplicationContext(),"Image event received 000000000",Toast.LENGTH_SHORT).show();
            } else if (msg.what == Constant.FW_UPDATE_EVENT) {
                Log.e(TAG, "FW_UPDATE_EVENT" + msg.what);
                Constant.logAsMessage(TYPE_DEBUG, TAG, "FW_UPDATE_EVENT Received. Client count = " + mDevEventsDelegates.size());
                FirmwareUpdateEvent firmwareUpdateEvent = (FirmwareUpdateEvent) msg.obj;
                for (IScannerAppEngineDevEventsDelegate delegate : mDevEventsDelegates) {
                    if (delegate != null) {
                        Constant.logAsMessage(TYPE_DEBUG, TAG, "Show FW_UPDATE_EVENT Received");
                        delegate.scannerFirmwareUpdateEvent(firmwareUpdateEvent);
                    }
                }
            } else if (msg.what == Constant.BARCODE_RECEIVED) {
                Log.e(TAG, "BARCODE_RECEIVED" + msg.what);
                Constant.logAsMessage(TYPE_DEBUG, TAG, "Barcode Received");
                Barcode barcode = (Barcode) msg.obj;
                ScanApplication.barcodeData.add(barcode);
                barcodeEvent.onBarcodeReceived(barcode);
                for (IScannerAppEngineDevEventsDelegate delegate : mDevEventsDelegates) {
                    if (delegate != null) {
                        Constant.logAsMessage(TYPE_DEBUG, TAG, "Show Barcode Received");
                        delegate.scannerBarcodeEvent(barcode.getBarcodeData(), barcode.getBarcodeType(), barcode.getFromScannerID());
                    }
                }
                if (Constant.MOT_SETTING_NOTIFICATION_BARCODE && !notification_processed) {
                    String scannerName = "";
                    if (mScannerInfoList != null) {
                        for (DCSScannerInfo ex_info : mScannerInfoList) {
                            if (ex_info.getScannerID() == barcode.getFromScannerID()) {
                                scannerName = ex_info.getScannerName();
                                break;
                            }
                        }
                    }
                    if (isInBackgroundMode(ScanApplication.getContext())) {
                        Intent intent = new Intent();
                        intent.setAction(Constant.ACTION_SCANNER_BARCODE_RECEIVED);
                        intent.putExtra(Constant.NOTIFICATIONS_TEXT, "Barcode received from " + scannerName);
                        intent.putExtra(Constant.NOTIFICATIONS_TYPE, Constant.BARCODE_RECEIVED);
                        ScanApplication.getContext().sendOrderedBroadcast(intent, null);
                    }  //POS_Utils.Toast("Barcode received from " + scannerName);

                }

            } else if (msg.what == Constant.SESSION_ESTABLISHED) {
                Log.e(TAG, "SESSION_ESTABLISHED" + msg.what);
                DCSScannerInfo activeScanner = (DCSScannerInfo) msg.obj;
                notification_processed = false;
                result = false;
                //ScannersActivity.curAvailableScanner = new AvailableScanner(activeScanner);
                //ScannersActivity.curAvailableScanner.setConnected(true);
                setAutoReconnectOption(activeScanner.getScannerID(), true);
                /* notify connections delegates */
                if (mDevConnDelegates != null) {
                    for (IScannerAppEngineDevConnectionsDelegate delegate : mDevConnDelegates) {
                        if (delegate != null) {
                            result = delegate.scannerHasConnected(activeScanner.getScannerID());
                            if (result) {
                                /*
                                 DevConnections delegates should NOT display any UI alerts,
                                 so from UI notification side the event is not processed
                                 */
                                notification_processed = false;
                            }
                        }
                    }
                }

                /* update dev list */
                found = false;
                if (mScannerInfoList != null) {
                    for (DCSScannerInfo ex_info : mScannerInfoList) {
                        if (ex_info.getScannerID() == activeScanner.getScannerID()) {
                            mScannerInfoList.remove(ex_info);
                            ScanApplication.barcodeData.clear();
                            found = true;
                            break;
                        }
                    }
                }

                if (mOfflineScannerInfoList != null) {
                    for (DCSScannerInfo off_info : mOfflineScannerInfoList) {
                        if (off_info != null && off_info.getScannerID() != -1) {
                            if (off_info.getScannerID() == activeScanner.getScannerID()) {
                                mOfflineScannerInfoList.remove(off_info);
                                break;
                            }
                        }
                    }
                }

                if (mScannerInfoList != null) {
                    mScannerInfoList.add(activeScanner);
                    ZebraUtils.scannerInfo=activeScanner;
                    ZebraUtils.setScannerID(activeScanner.getScannerID());
                    ZebraUtils.setScannerName(activeScanner.getScannerName());
                    ZebraUtils.setPickListMode(getPickListMode(activeScanner.getScannerID()));
                    isScannerInitialized = true;
                    Intent intent = new Intent();
                    intent.setAction("SESSION_ESTABLISHED");
                    ScanApplication.getContext().sendBroadcast(intent);

                }

                /* notify dev list delegates */
                if (ScanApplication.mDevListDelegates != null) {
                    for (IScannerAppEngineDevListDelegate delegate : ScanApplication.mDevListDelegates) {
                        if (delegate != null) {
                            result = delegate.scannersListHasBeenUpdated();
                            if (result) {
                                    /*
                                     DeList delegates should NOT display any UI alerts,
                                     so from UI notification side the event is not processed
                                     */
                                notification_processed = false;
                            }
                        }
                    }
                }

                //TODO - Showing notifications in foreground and background mode

                if (Constant.MOT_SETTING_NOTIFICATION_ACTIVE && !notification_processed) {
                    StringBuilder notification_Msg = new StringBuilder();
                    if (!found) {
                        notification_Msg.append(activeScanner.getScannerName()).append(" has appeared and connected");
                    } else {
                        notification_Msg.append(activeScanner.getScannerName()).append(" has connected");
                    }
                    if (isInBackgroundMode(ScanApplication.getContext())) {
                        Intent intent = new Intent();
                        intent.setAction(Constant.ACTION_SCANNER_CONNECTED);
                        intent.putExtra(Constant.NOTIFICATIONS_TEXT, notification_Msg.toString());
                        intent.putExtra(Constant.NOTIFICATIONS_TYPE, Constant.SESSION_ESTABLISHED);
                        ScanApplication.getContext().sendOrderedBroadcast(intent, null);
                    }

                }
            } else if (msg.what == Constant.SESSION_TERMINATED) {
                Log.e(TAG, "SESSION_TERMINATED" + msg.what);
                int scannerID = (Integer) msg.obj;
                String scannerName = "";
                notification_processed = false;
                result = false;
                DCSScannerInfo scannerInfo = getScannerByID(scannerID);
                sessionTerminationEvent.onSessionTerminated(scannerInfo);
                ZebraUtils.scannerInfo = null;
                /* notify connections delegates */
                for (IScannerAppEngineDevConnectionsDelegate delegate : mDevConnDelegates) {
                    if (delegate != null) {
                        result = delegate.scannerHasDisconnected(scannerID);
                        if (result) {
                            /*
                             DevConnections delegates should NOT display any UI alerts,
                             so from UI notification side the event is not processed
                             */
                            notification_processed = false;
                        }
                    }
                }

//                DCSScannerInfo scannerInfo = getScannerByID(scannerID);
                mOfflineScannerInfoList.add(scannerInfo);
                if (scannerInfo != null) {
                    scannerName = scannerInfo.getScannerName();
                }
                updateScannersList();

                /* notify dev list delegates */
                for (IScannerAppEngineDevListDelegate delegate : ScanApplication.mDevListDelegates) {
                    if (delegate != null) {
                        result = delegate.scannersListHasBeenUpdated();
                        if (result) {
                                /*
                                 DeList delegates should NOT display any UI alerts,
                                 so from UI notification side the event is not processed
                                 */
                            notification_processed = false;
                        }
                    }
                }
                if (Constant.MOT_SETTING_NOTIFICATION_ACTIVE && !notification_processed) {
                    if (isInBackgroundMode(ScanApplication.getContext())) {
                        Intent intent = new Intent();
                        intent.setAction(Constant.ACTION_SCANNER_DISCONNECTED);
                        intent.putExtra(Constant.NOTIFICATIONS_TEXT, scannerName + " has disconnected");
                        intent.putExtra(Constant.NOTIFICATIONS_TYPE, Constant.SESSION_TERMINATED);
                        ScanApplication.getContext().sendOrderedBroadcast(intent, null);
                    }
                }
            } else if (msg.what == Constant.SCANNER_APPEARED || msg.what == Constant.AUX_SCANNER_CONNECTED) {
                Log.e(TAG, "SCANNER_APPEARED/AUX_SCANNER_CONNECTED" + msg.what);
                notification_processed = false;
                result = false;
                DCSScannerInfo availableScanner = (DCSScannerInfo) msg.obj;

                /* notify connections delegates */
                for (IScannerAppEngineDevConnectionsDelegate delegate : mDevConnDelegates) {
                    if (delegate != null) {
                        result = delegate.scannerHasAppeared(availableScanner.getScannerID());
                        if (result) {
                            /*
                             DevConnections delegates should NOT display any UI alerts,
                             so from UI notification side the event is not processed
                             */
                            notification_processed = false;
                        }
                    }
                }

                /* update dev list */
                if (mScannerInfoList != null) {
                    for (DCSScannerInfo ex_info : mScannerInfoList) {
                        if (ex_info.getScannerID() == availableScanner.getScannerID()) {
                            mScannerInfoList.remove(ex_info);
                            break;
                        }
                    }
                }

                mScannerInfoList.add(availableScanner);

                /* notify dev list delegates */
                for (IScannerAppEngineDevListDelegate delegate : ScanApplication.mDevListDelegates) {
                    if (delegate != null) {
                        result = delegate.scannersListHasBeenUpdated();
                        if (result) {
                            /*
                             DeList delegates should NOT display any UI alerts,
                             so from UI notification side the event is not processed
                             */

                            notification_processed = false;
                        }
                    }
                }

                //TODO - Showing notifications in foreground and background mode
                if (Constant.MOT_SETTING_NOTIFICATION_AVAILABLE && !notification_processed) {
                    if (isInBackgroundMode(ScanApplication.getContext())) {
                        Intent intent = new Intent();
                        intent.setAction(Constant.ACTION_SCANNER_CONNECTED);
                        intent.putExtra(Constant.NOTIFICATIONS_TEXT, availableScanner.getScannerName() + " has appeared");
                        intent.putExtra(Constant.NOTIFICATIONS_TYPE, Constant.SCANNER_APPEARED);
                        ScanApplication.getContext().sendOrderedBroadcast(intent, null);
                    }
                }

                if (availableScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI
                        || availableScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_CDC) {
                    if (availableScanner.isActive()) {
                        // Available scanner is active. Navigate to active scanner
                    } else {
                        // Try to connect available scanner
                        new MyAsyncTask(availableScanner).execute();

                    }
                }
            } else if (msg.what == Constant.SCANNER_DISAPPEARED) {
                Log.e(TAG, "SCANNER_DISAPPEARED" + msg.what + "------" + (mScannerInfoList.isEmpty() ? mScannerInfoList.size() : "Blank List"));
                String scannerName;
                int scannerID;
                notification_processed = false;
                result = false;
                scannerID = (Integer) msg.obj;
                scannerName = "";
                /* notify connections delegates */
                for (IScannerAppEngineDevConnectionsDelegate delegate : mDevConnDelegates) {
                    if (delegate != null) {
                        result = delegate.scannerHasDisappeared(scannerID);
                        if (result) {
                            /*
                             DevConnections delegates should NOT display any UI alerts,
                             so from UI notification side the event is not processed
                             */
                            notification_processed = false;
                        }
                    }
                }

                /* update dev list */
                found = false;
                for (DCSScannerInfo ex_info : mScannerInfoList) {
                    if (ex_info.getScannerID() == scannerID) {
                        /* find scanner with ID in dev list */
                        mScannerInfoList.remove(ex_info);
                        isScannerInitialized = false;
                        ZebraUtils.setScannerID(-1);
                        ZebraUtils.setScannerName("");
                        scannerName = ex_info.getScannerName();
                        Intent intent = new Intent();
                        intent.setAction("SESSION_ESTABLISHED");
                        ScanApplication.getContext().sendBroadcast(intent);
                        found = true;
                        break;
                    }
                }

                /* notify dev list delegates */
                for (IScannerAppEngineDevListDelegate delegate : ScanApplication.mDevListDelegates) {
                    if (delegate != null) {
                        result = delegate.scannersListHasBeenUpdated();
                        if (result) {
                            /*
                             DeList delegates should NOT display any UI alerts,
                             so from UI notification side the event is not processed
                             */
                            notification_processed = false;
                        }
                    }
                }

                //TODO - Showing notifications in foreground and background mode
                if (Constant.MOT_SETTING_NOTIFICATION_AVAILABLE && !notification_processed) {
                    StringBuilder notification_Msg = new StringBuilder();
                    notification_Msg.append(scannerName).append(" has disappeared");
                    if (isInBackgroundMode(ScanApplication.getContext())) {
                        Intent intent = new Intent();
                        intent.setAction(Constant.ACTION_SCANNER_CONNECTED);
                        intent.putExtra(Constant.NOTIFICATIONS_TEXT, notification_Msg.toString());
                        intent.putExtra(Constant.NOTIFICATIONS_TYPE, Constant.SCANNER_DISAPPEARED);
                        ScanApplication.getContext().sendOrderedBroadcast(intent, null);
                    }
                }
            }
        }
    };

    private ScannerInitializer() {
    }


    public static ScannerInitializer getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new ScannerInitializer();
        }
        return INSTANCE;
    }

    public void getBarcodeEventReceived(BarcodeEvent barcodeEvent) {
        this.barcodeEvent = barcodeEvent;
    }

    public void getScannerSessionTerminatedEvent(ScannerSessionTerminationEvent sessionTerminationEvent) {
        this.sessionTerminationEvent = sessionTerminationEvent;
    }

    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {

    }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {

    }

    @Override
    public void scannerImageEvent(byte[] imageData) {

    }

    @Override
    public void scannerVideoEvent(byte[] videoData) {

    }

    public void initialize() {
        if (!isScannerInitialized) {
            mOfflineScannerInfoList = new ArrayList<>();
            mScannerInfoList = ScanApplication.mScannerInfoList;
            ScanApplication.sdkHandler.dcssdkSetDelegate(this);
            initializeDcsSdkWithAppSettings();
            initializeDcsSdk();
            addDevConnectionsDelegate(this);
            broadcastSCAisListening();
            addDevEventsDelegate(this);
        }
    }

    public void removeDelegate() {
        removeDevEventsDelegate(this);
        removeDevConnectiosDelegate(this);
    }

    private void broadcastSCAisListening() {
        Intent intent = new Intent();
        intent.setAction("com.zebra.scannercontrol.LISTENING_STARTED");
        ScanApplication.getContext().sendBroadcast(intent);
    }

    private void initializeDcsSdk() {
        ScanApplication.sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        ScanApplication.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        ScanApplication.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);
    }


    @Override
    public boolean scannerHasAppeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        Log.e(TAG, "scannerHasConnected" + scannerID);
        ArrayList<DCSScannerInfo> activeScanners = new ArrayList<>();
        ScanApplication.sdkHandler.dcssdkGetActiveScannersList(activeScanners);

        for (DCSScannerInfo scannerInfo : ScanApplication.mScannerInfoList) {
            if (scannerInfo.getScannerID() == scannerID) {
                ZebraUtils.setScannerID(scannerID);
                ZebraUtils.setScannerName(scannerInfo.getScannerName());
                ZebraUtils.setPickListMode(getPickListMode(scannerID));
                isScannerInitialized = true;
            }
        }
        return true;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        isScannerInitialized = false;
        ZebraUtils.setScannerID(-1);
        ZebraUtils.setScannerName("");
        return false;
    }

    public void generatePairingBarcode(BarcodeInterface barcodeInterface) {
        if (barcodeInterface == null) {
            throw new NullPointerException();
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        selectedProtocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST;
        selectedConfig = DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.SET_FACTORY_DEFAULTS;

        BarCodeView barCodeView = ScanApplication.sdkHandler.dcssdkGetPairingBarcode(selectedProtocol, selectedConfig);
        if (barCodeView != null) {
            barcodeInterface.updateBarcodeView(barCodeView, layoutParams);
        } else {
            // SDK was not able to determine Bluetooth MAC. So call the dcssdkGetPairingBarcode with BT Address.

            String btAddress = Constant.bluetoothAddress;
            Log.e(TAG, "Bluetooth Mac Address:" + btAddress);
            if (btAddress.equals("")) {
                barcodeInterface.clearView();
            } else {
                ScanApplication.sdkHandler.dcssdkSetBTAddress(btAddress);
                barCodeView = ScanApplication.sdkHandler.dcssdkGetPairingBarcode(selectedProtocol, selectedConfig, btAddress);
                if (barCodeView != null) {
                    barcodeInterface.updateBarcodeView(barCodeView, layoutParams);
                }
            }
        }
    }

    /**
     * Zebra Scanner SDK implementation
     */
    @Override
    public void initializeDcsSdkWithAppSettings() {
        Constant.MOT_SETTING_OPMODE = Preferences.getInstance().getInteger(Constant.PREF_OPMODE, DCSSDK_CONNTYPE_USB_SNAPI.value);

        Constant.MOT_SETTING_SCANNER_DETECTION = Preferences.getInstance().getBoolean(Constant.PREF_SCANNER_DETECTION, true);
        Constant.MOT_SETTING_EVENT_IMAGE = Preferences.getInstance().getBoolean(Constant.PREF_EVENT_IMAGE, true);
        Constant.MOT_SETTING_EVENT_VIDEO = Preferences.getInstance().getBoolean(Constant.PREF_EVENT_VIDEO, true);
        Constant.MOT_SETTING_EVENT_BINARY_DATA = Preferences.getInstance().getBoolean(Constant.PREF_EVENT_BINARY_DATA, true);

        Constant.MOT_SETTING_EVENT_ACTIVE = Preferences.getInstance().getBoolean(Constant.PREF_EVENT_ACTIVE, true);
        Constant.MOT_SETTING_EVENT_AVAILABLE = Preferences.getInstance().getBoolean(Constant.PREF_EVENT_AVAILABLE, true);
        Constant.MOT_SETTING_EVENT_BARCODE = Preferences.getInstance().getBoolean(Constant.PREF_EVENT_BARCODE, true);

        Constant.MOT_SETTING_NOTIFICATION_AVAILABLE = Preferences.getInstance().getBoolean(Constant.PREF_NOTIFY_AVAILABLE, false);
        Constant.MOT_SETTING_NOTIFICATION_ACTIVE = Preferences.getInstance().getBoolean(Constant.PREF_NOTIFY_ACTIVE, false);
        Constant.MOT_SETTING_NOTIFICATION_BARCODE = Preferences.getInstance().getBoolean(Constant.PREF_NOTIFY_BARCODE, false);

        Constant.MOT_SETTING_NOTIFICATION_IMAGE = Preferences.getInstance().getBoolean(Constant.PREF_NOTIFY_IMAGE, false);
        Constant.MOT_SETTING_NOTIFICATION_VIDEO = Preferences.getInstance().getBoolean(Constant.PREF_NOTIFY_VIDEO, false);
        Constant.MOT_SETTING_NOTIFICATION_BINARY_DATA = Preferences.getInstance().getBoolean(Constant.PREF_NOTIFY_BINARY_DATA, false);

        int notifications_mask = 0;
        if (Constant.MOT_SETTING_EVENT_AVAILABLE) {
            notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value);
        }
        if (Constant.MOT_SETTING_EVENT_ACTIVE) {
            notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value);
        }
        if (Constant.MOT_SETTING_EVENT_BARCODE) {
            notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value);
        }
        if (Constant.MOT_SETTING_EVENT_IMAGE) {
            notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_IMAGE.value);
        }
        if (Constant.MOT_SETTING_EVENT_VIDEO) {
            notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_VIDEO.value);
        }
        if (Constant.MOT_SETTING_EVENT_BINARY_DATA) {
            notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BINARY_DATA.value);
        }
        ScanApplication.sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
    }

    /* ###################################################################### */
    /* ########## Utility functions ######################################### */
    /* ###################################################################### */
    @Override
    public void showMessageBox(String message) {
        // - Handle the callback from SDK Handler
    }

    @Override
    public int showBackgroundNotification(String text) {
        // - Handle the callback from SDK Handler
        return 0;
    }

    @Override
    public int dismissBackgroundNotifications() {
        // - Handle the callback from SDK Handler
        return 0;
    }

    /**
     * Checks if the application is being sent in the background (i.e behind
     * another application's Activity).
     *
     * @param context the context
     * @return <code>true</code> if another application will be above this one.
     */

    @Override
    public boolean isInBackgroundMode(final Context context) {
        return Foreground.get().isBackground();
    }

    /* ###################################################################### */
    /* ########## API calls for UI View Controllers ######################### */
    /* ###################################################################### */
    @Override
    public void addDevListDelegate(IScannerAppEngineDevListDelegate delegate) {
        if (ScanApplication.mDevListDelegates == null)
            ScanApplication.mDevListDelegates = new ArrayList<>();
        ScanApplication.mDevListDelegates.add(delegate);
    }

    @Override
    public void addDevConnectionsDelegate(IScannerAppEngineDevConnectionsDelegate delegate) {
        if (mDevConnDelegates == null)
            mDevConnDelegates = new ArrayList<>();
        mDevConnDelegates.add(delegate);
    }

    @Override
    public void addDevEventsDelegate(IScannerAppEngineDevEventsDelegate delegate) {
        if (mDevEventsDelegates == null)
            mDevEventsDelegates = new ArrayList<>();
        mDevEventsDelegates.add(delegate);
    }

    @Override
    public void removeDevListDelegate(IScannerAppEngineDevListDelegate delegate) {
        if (ScanApplication.mDevListDelegates != null)
            ScanApplication.mDevListDelegates.remove(delegate);
    }

    @Override
    public void removeDevConnectiosDelegate(IScannerAppEngineDevConnectionsDelegate delegate) {
        if (mDevConnDelegates != null)
            mDevConnDelegates.remove(delegate);
    }

    @Override
    public void removeDevEventsDelegate(IScannerAppEngineDevEventsDelegate delegate) {
        if (mDevEventsDelegates != null)
            mDevEventsDelegates.remove(delegate);
    }

    @Override
    public List<DCSScannerInfo> getActualScannersList() {
        return mScannerInfoList;
    }

    @Override
    public DCSScannerInfo getScannerInfoByIdx(int dev_index) {
        if (mScannerInfoList != null)
            return mScannerInfoList.get(dev_index);
        else
            return null;
    }

    @Override
    public DCSScannerInfo getScannerByID(int scannerId) {
        if (mScannerInfoList != null) {
            for (DCSScannerInfo scannerInfo : mScannerInfoList) {
                if (scannerInfo != null && scannerInfo.getScannerID() == scannerId)
                    return scannerInfo;
            }
        }
        return null;
    }

    @Override
    public void raiseDeviceNotificationsIfNeeded() {

    }

    /* ###################################################################### */
    /* ########## Interface for DCS SDK ##################################### */
    /* ###################################################################### */
    @Override
    public void updateScannersList() {
        if (ScanApplication.sdkHandler != null) {
            mScannerInfoList.clear();
            ArrayList<DCSScannerInfo> scannerTreeList = new ArrayList<>();
            ScanApplication.sdkHandler.dcssdkGetAvailableScannersList(scannerTreeList);
            ScanApplication.sdkHandler.dcssdkGetActiveScannersList(scannerTreeList);
            createFlatScannerList(scannerTreeList);
        }
    }

    private void createFlatScannerList(ArrayList<DCSScannerInfo> scannerTreeList) {
        for (DCSScannerInfo s : scannerTreeList) {
            addToScannerList(s);
        }
    }

    private void addToScannerList(DCSScannerInfo s) {
        mScannerInfoList.add(s);
        if (s.getAuxiliaryScanners() != null) {
            for (DCSScannerInfo aux : s.getAuxiliaryScanners().values()) {
                addToScannerList(aux);
            }
        }
    }


    @Override
    public DCSSDKDefs.DCSSDK_RESULT connect(int scannerId) {
        if (ScanApplication.sdkHandler != null) {
            if (ZebraUtils.getScannerID() != -1) {
                ScanApplication.sdkHandler.dcssdkTerminateCommunicationSession(ZebraUtils.getScannerID());
            }
            return ScanApplication.sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
        } else {
            return DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
        }
    }

    @Override
    public void disconnect(int scannerId) {
        if (ScanApplication.sdkHandler != null) {
            DCSSDKDefs.DCSSDK_RESULT ret = ScanApplication.sdkHandler.dcssdkTerminateCommunicationSession(scannerId);
            //ScannersActivity.curAvailableScanner=null;
            updateScannersList();
        }
    }

    @Override
    public DCSSDKDefs.DCSSDK_RESULT setAutoReconnectOption(int scannerId, boolean enable) {
        DCSSDKDefs.DCSSDK_RESULT ret;
        if (ScanApplication.sdkHandler != null) {
            ret = ScanApplication.sdkHandler.dcssdkEnableAutomaticSessionReestablishment(enable, scannerId);
            return ret;
        }
        return DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
    }

    @Override
    public void enableScannersDetection(boolean enable) {
        if (ScanApplication.sdkHandler != null) {
            ScanApplication.sdkHandler.dcssdkEnableAvailableScannersDetection(enable);
        }
    }

    @Override
    public void configureNotificationAvailable(boolean enable) {
        if (ScanApplication.sdkHandler != null) {
            if (enable) {
                ScanApplication.sdkHandler.dcssdkSubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value);
            } else {
                ScanApplication.sdkHandler.dcssdkUnsubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value);
            }
        }
    }

    @Override
    public void configureNotificationActive(boolean enable) {
        if (ScanApplication.sdkHandler != null) {
            if (enable) {
                ScanApplication.sdkHandler.dcssdkSubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value);
            } else {
                ScanApplication.sdkHandler.dcssdkUnsubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value |
                        DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value);
            }
        }
    }

    @Override
    public void configureNotificationBarcode(boolean enable) {
        if (ScanApplication.sdkHandler != null) {
            if (enable) {
                ScanApplication.sdkHandler.dcssdkSubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value);
            } else {
                ScanApplication.sdkHandler.dcssdkUnsubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value);
            }
        }
    }

    @Override
    public void configureNotificationImage(boolean enable) {
        if (ScanApplication.sdkHandler != null) {
            if (enable) {
                ScanApplication.sdkHandler.dcssdkSubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_IMAGE.value);
            } else {
                ScanApplication.sdkHandler.dcssdkUnsubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_IMAGE.value);
            }
        }
    }

    @Override
    public void configureNotificationVideo(boolean enable) {
        if (ScanApplication.sdkHandler != null) {
            if (enable) {
                ScanApplication.sdkHandler.dcssdkSubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_VIDEO.value);
            } else {
                ScanApplication.sdkHandler.dcssdkUnsubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_VIDEO.value);
            }
        }
    }

    @Override
    public void configureOperationalMode(DCSSDKDefs.DCSSDK_MODE mode) {
        if (ScanApplication.sdkHandler != null) {
            ScanApplication.sdkHandler.dcssdkSetOperationalMode(mode);
        }
    }

    @Override
    public boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
        if (ScanApplication.sdkHandler != null) {
            if (outXML == null) {
                outXML = new StringBuilder();
            }
            DCSSDKDefs.DCSSDK_RESULT result = ScanApplication.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode, inXML, outXML, scannerID);
            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
                return true;
            else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
                return false;
        }
        return false;
    }


    /* ###################################################################### */
    /* ########## IDcsSdkApiDelegate Protocol implementation ################ */
    /* ###################################################################### */
    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo availableScanner) {
        dataHandler.obtainMessage(Constant.SCANNER_APPEARED, availableScanner).sendToTarget();
    }

    @Override
    public void dcssdkEventScannerDisappeared(int scannerID) {
        dataHandler.obtainMessage(Constant.SCANNER_DISAPPEARED, scannerID).sendToTarget();
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo activeScanner) {
        dataHandler.obtainMessage(Constant.SESSION_ESTABLISHED, activeScanner).sendToTarget();
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int scannerID) {
        dataHandler.obtainMessage(Constant.SESSION_TERMINATED, scannerID).sendToTarget();
    }

    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {

        Barcode barcode = new Barcode(barcodeData, barcodeType, fromScannerID);
        dataHandler.obtainMessage(Constant.BARCODE_RECEIVED, barcode).sendToTarget();
    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
        dataHandler.obtainMessage(Constant.FW_UPDATE_EVENT, firmwareUpdateEvent).sendToTarget();
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo newTopology, DCSScannerInfo auxScanner) {
        dataHandler.obtainMessage(Constant.AUX_SCANNER_CONNECTED, auxScanner).sendToTarget();
    }


    @Override
    public void dcssdkEventImage(byte[] imageData, int fromScannerID) {
        dataHandler.obtainMessage(Constant.IMAGE_RECEIVED, imageData).sendToTarget();
    }

    @Override
    public void dcssdkEventVideo(byte[] videoFrame, int fromScannerID) {
        dataHandler.obtainMessage(Constant.VIDEO_RECEIVED, videoFrame).sendToTarget();
    }

    @Override
    public void dcssdkEventBinaryData(byte[] binaryData, int fromScannerID) {
        // todo: implement this
        Constant.logAsMessage(TYPE_DEBUG, TAG, "BinaryData Event received no.of bytes : " + binaryData.length + " for Scanner ID : " + fromScannerID);
    }

    private int getPickListMode(int scannerID) {
        int attrVal = 0;
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>402</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET, in_xml, outXML, scannerID);

        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(new StringReader(outXML.toString()));
            int event = parser.getEventType();
            String text = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("value")) {
                            attrVal = Integer.parseInt(text != null ? text.trim() : null);
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return attrVal;
    }

    @Override
    public void onBarcodeReceived(Barcode barcode) {

    }

    @Override
    public void onSessionTerminated(DCSScannerInfo scannerInfo) {

    }

    public boolean enableScanning(int scannerID) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        StringBuilder outXML = new StringBuilder();
        return executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE, in_xml, outXML, scannerID);
    }

    public boolean disableScanning(int scannerID) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        StringBuilder outXML = new StringBuilder();
        return executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE, in_xml, outXML, scannerID);
    }

    public interface BarcodeInterface {
        void updateBarcodeView(BarCodeView barCodeView, ViewGroup.LayoutParams layoutParams);

        void clearView();
    }

    private class MyAsyncTask extends AsyncTask<Void, DCSScannerInfo, Boolean> {
        private final DCSScannerInfo scanner;

        public MyAsyncTask(DCSScannerInfo scn) {
            this.scanner = scn;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DCSSDKDefs.DCSSDK_RESULT result = connect(scanner.getScannerID());
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            Intent returnIntent = new Intent();
            if (!b) {
                //setResult(RESULT_CANCELED, returnIntent);
                Toast.makeText(ScanApplication.getContext(), "Unable to communicate with scanner", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
