package com.zebrascannerdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.zebrascannerdemo.databinding.ActivityMainBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.mEdtScanResult.visibility = View.GONE
        mBinding.mTvWelCome.visibility = View.VISIBLE
        mBinding.mBtnNext.visibility = View.GONE
    }
}