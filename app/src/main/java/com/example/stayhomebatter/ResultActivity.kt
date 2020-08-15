package com.example.stayhomebatter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val currentScore = intent.extras?.getInt("SCORE", 0)
        val highScore = intent.extras?.getInt("HIGHSCORE", 0)
        current_score.setText(currentScore.toString())
        high_score.setText(highScore.toString())

        retryButton.setOnClickListener{
            val intentMain = Intent(applicationContext, MainActivity::class.java)
            startActivity(intentMain)
        }
    }
}