package com.zebrascannerdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.zebrascannerdemo.databinding.ActivityMainBinding
import com.zebrascannerdemo.zebra.ScannerInitializer
import com.zebrascannerdemo.zebra.ZebraUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUi()
        initialize()

        //In case we have been launched by the DataWedge intent plug-in
        val i = intent
        handleDecodeData(i)
    }

    private fun setUi() {
        mBinding.mEdtScanResult.visibility = View.VISIBLE
        mBinding.mTvWelCome.visibility = View.GONE
        mBinding.mBtnNext.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initialize() {
        ScannerInitializer.getInstance().initialize()
        ScannerInitializer.getInstance().getBarcodeEventReceived { barcode ->
            val barcodeData = String(barcode.barcodeData)
            onBarcodeDataReceived(barcodeData)
        }
        ScannerInitializer.getInstance().getScannerSessionTerminatedEvent {
            disconnectZebraScanner()
        }
        if (ZebraUtils.getScannerID() != -1) {
            ScannerInitializer.getInstance().enableScanning(ZebraUtils.getScannerID())
        } else {
            ScannerInitializer.getInstance().removeDelegate()
            GlobalScope.launch {
                ScannerInitializer.getInstance().disableScanning(ZebraUtils.getScannerID())
            }
            ScannerInitializer.getInstance().disconnect(ZebraUtils.getScannerID())
            ZebraUtils.setScannerID(-1)
        }
    }

    private fun onBarcodeDataReceived(barcodeData: String?) {
        GlobalScope.launch {
            ScannerInitializer.getInstance().disableScanning(ZebraUtils.getScannerID())
            Log.d("onBarcodeReceived", "Barcode Data = $barcodeData")
            Toast.makeText(this@MainActivity, "Barcode Data$barcodeData", Toast.LENGTH_LONG).show()
            if(barcodeData!=null && barcodeData.isNotEmpty()) {
                mBinding.mEdtScanResult.setText(barcodeData)
                delay(1000L)
                val intent = Intent(this@MainActivity, SecondActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun disconnectZebraScanner() {
        ScannerInitializer.getInstance().removeDelegate()
        GlobalScope.launch {
            ScannerInitializer.getInstance().disableScanning(ZebraUtils.getScannerID())
        }
        ScannerInitializer.getInstance().disconnect(ZebraUtils.getScannerID())
        ZebraUtils.setScannerID(-1)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopZebraScanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopZebraScanner()
    }

    private fun stopZebraScanner() {
        if (ZebraUtils.getScannerID() != -1) {
            ScannerInitializer.getInstance().disableScanning(ZebraUtils.getScannerID())
        }
    }

    //This function is responsible for getting the data from the intent
    private fun handleDecodeData(i: Intent) {
        //Check the intent action is for us
        if (i.action.contentEquals("com.symbol.emdksample.RECVR")) {
            //Get the source of the data
            val source = i.getStringExtra("com.motorolasolutions.emdk.datawedge.source")

            //Check if the data has come from the Barcode scanner
            if (source.equals("scanner", ignoreCase = true)) {
                //Get the data from the intent
                val data = i.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string")

                //Check that we have received data
                if (data != null && data.isNotEmpty()) {
                    //Display the data to the text view
                    mBinding.mEdtScanResult.setText("DATA: $data")
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}