package com.quyunshuo.androidcustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.quyunshuo.androidcustomview.R.layout.activity_main
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        arc_progress_bar.setOnClickListener {
            arc_progress_bar.setCurrentValue(Random.nextFloat() * arc_progress_bar.maxValue)
        }
    }
}
