package com.shshy.progressimageview.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progress_iv.showProgress(true)
        Timer().schedule(object : TimerTask() {
            var i = 0
            override fun run() {
                progress_iv.setProgress(if (i++ < 100) i else 100)
            }
        }, 1000, 1000)
    }
}
