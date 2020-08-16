package com.example.stayhomebatter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), SensorEventListener {

    //  音声系の変数
    private lateinit var soundPool: SoundPool
    private var soundResId = 0
    private var prepareSound = 0
    private var pitchingSound = 0
    private var swingSound = 0
    private var smallHitSound = 0
    private var bigHitSound = 0

    //  センサー系の変数
    private var time: Long = 0L
    var swingFlag = false

    // スコア用変数
    private var highScore = 0
    private var currentScore = 0

    // 投球回数
    var pitchingCount = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_game)

        // スコアを初期化
        currentScore = 0

        // 投球回数を初期化
        pitchingCount = 0

        //  SEを設定する枠の初期設定
        val audioAttributes = AudioAttributes.Builder() // USAGE_MEDIA
            // USAGE_GAME
            .setUsage(AudioAttributes.USAGE_GAME) // CONTENT_TYPE_MUSIC
            // CONTENT_TYPE_SPEECH, etc.
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes) // ストリーム数に応じて
            .setMaxStreams(5)
            .build()

        //  SEの登録
        prepareSound = soundPool.load(this, R.raw.prepare_machine, 1)
        pitchingSound = soundPool.load(this, R.raw.pitching_sound, 1)
        swingSound = soundPool.load(this, R.raw.swing_sound, 1)
        smallHitSound = soundPool.load(this, R.raw.small_hit, 1)
        bigHitSound = soundPool.load(this, R.raw.big_hit, 1)


        //  動作していないならピッチングの処理へ，動作中なら無視する（スイッチを無効化）
        startButton.setOnClickListener {
//            Toast.makeText(this, "ボタンが押されました", Toast.LENGTH_SHORT).show()
            soundPool.play(prepareSound, 1.0f, 1.0f, 1, 0, 1.0f)
            startButton.setEnabled(false)
            pitchingButtonTapped(it)
            //  3.5秒後にボタンが再び押せるようになる
            Handler().postDelayed(Runnable {
                startButton.setEnabled(true)
            }, 3500)
        }
    }

    //  ピッチング中の処理
    fun pitchingButtonTapped(view: View?) {
//        Toast.makeText(applicationContext, "func", Toast.LENGTH_SHORT).show()
        // 2秒後に投球音を流す
        Handler().postDelayed(Runnable {
            soundPool.play(pitchingSound, 1.0f, 1.0f, 1, 0, 1.0f)
            //  投球の瞬間の時間を保存
            time = System.currentTimeMillis()
        }, 2000)
        // 投球回数をインクリメント
        pitchingCount++
    }

    //  センサーの監視
    override fun onSensorChanged(event: SensorEvent?) {
        var swingTime: Long = 0L
        //  バイブレーションの変数
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (event == null) return

        if ((event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) && (swingFlag == false)) {
            if((event.values[0] > 4.0 && -1.0 * event.values[1] > 6.0)/* || (-event.values[0] > 10.0 && event.values[1] > 6.0)*/){
                swingFlag = true
//                Toast.makeText(applicationContext, "$time", Toast.LENGTH_SHORT).show()
                swingTime = System.currentTimeMillis()
                if ((400L <= swingTime - time && swingTime - time < 500L)
                    or (600L < swingTime - time && swingTime - time <= 700L)
                ) {
                    soundPool.play(smallHitSound, 1.0f, 1.0f, 1, 0, 1.0f)
                    currentScore += 1
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val vibrationEffect = VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)
                    } else {
                        vibrator.vibrate(100)
                    }
                } else if (500L <= swingTime - time && swingTime - time <= 600L) {
                    soundPool.play(bigHitSound, 1.0f, 1.0f, 1, 0, 1.0f)
                    currentScore += 5
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val vibrationEffect = VibrationEffect.createOneShot(200, DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)
                    } else {
                        vibrator.vibrate(200)
                    }
                } else {
                    soundPool.play(swingSound, 1.0f, 1.0f, 1, 0, 1.0f)
                }

                // 1サイクルが終了→リザルト画面へ
                if (pitchingCount == 10){
                    Handler().postDelayed({
                        val intentResult = Intent(applicationContext, ResultActivity::class.java)
                        // スコア保存用
                        val scoreStore: SharedPreferences = getSharedPreferences("HIGH_SCORE", Context.MODE_PRIVATE)
                        // high score取り出し
                        highScore = scoreStore.getInt("HIGH_SCORE", 0)
                        if (currentScore > highScore){
                            highScore = currentScore
                            // スコア保存用
                            val editor = scoreStore.edit()
                            editor.putInt("HIGH_SCORE", highScore).apply()
                        }

                        // intentへscoreとhigh scoreの受け渡し
                        intentResult.putExtra("SCORE", currentScore)
                        intentResult.putExtra("HIGHSCORE", highScore)
                        startActivity(intentResult)
                    }, 500)
                }
                // 2秒後にスイングのフラグを戻す
                Handler().postDelayed(Runnable {
                    swingFlag = false
                }, 2000)
            }
        }
    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()

        //  SEを設定する枠の初期設定
        val audioAttributes = AudioAttributes.Builder() // USAGE_MEDIA
            // USAGE_GAME
            .setUsage(AudioAttributes.USAGE_GAME) // CONTENT_TYPE_MUSIC
            // CONTENT_TYPE_SPEECH, etc.
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes) // ストリーム数に応じて
            .setMaxStreams(5)
            .build()
        //  音声の再登録
        prepareSound = soundPool.load(this, R.raw.prepare_machine, 1)
        pitchingSound = soundPool.load(this, R.raw.pitching_sound, 1)
        swingSound = soundPool.load(this, R.raw.swing_sound, 1)
        smallHitSound = soundPool.load(this, R.raw.small_hit, 1)
        bigHitSound = soundPool.load(this, R.raw.big_hit, 1)

        //  センサーの監視の再開
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()

        //  音声のメモリ解放
        soundPool.release()

        //  センサーの停止
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }
}