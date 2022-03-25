package com.zebrascannerdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.zebrascannerdemo.databinding.ActivityMainBinding
import com.zebrascannerdemo.zebra.ScannerInitializer
import com.zebrascannerdemo.zebra.ZebraUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        runOnUiThread {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }
//        setUi()
//        initialize()
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
        }
        Log.d("onBarcodeReceived", "Barcode Data = $barcodeData")
        if(barcodeData!=null && barcodeData.isNotEmpty()) {
            mBinding.mEdtScanResult.setText(barcodeData)
            mBinding.mBtnNext.performClick()
            mBinding.mEdtScanResult.setSelection(mBinding.mEdtScanResult.length())
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
}