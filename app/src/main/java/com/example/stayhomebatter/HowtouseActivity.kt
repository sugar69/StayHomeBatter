package com.example.stayhomebatter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_credit.*

class HowtouseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howtouse)

        //  元の画面に戻る
        backButton.setOnClickListener{
            finish()
        }
    }
}