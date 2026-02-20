package com.example.kipia

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.VideoView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)

        try {
            // Указываем путь к видео
            val videoPath = "android.resource://" + packageName + "/" + R.raw.splash_video
            videoView.setVideoPath(videoPath)

            // Обработчик завершения видео
            videoView.setOnCompletionListener {
                // Плавный переход на MainActivity
                startMainActivity()
            }

            // Обработчик ошибок видео
            videoView.setOnErrorListener { _, what, extra ->
                // Если видео не загружается, сразу переходим на MainActivity
                startMainActivity()
                true // обработали ошибку
            }

            // Запускаем видео
            videoView.start()

        } catch (e: Exception) {
            // Если возникла любая ошибка, переходим на MainActivity
            startMainActivity()
        }

        // Резервный таймер на случай если видео зависнет
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                startMainActivity()
            }
        }, 10000) // 10 секунд максимум
    }

    private fun startMainActivity() {
        if (!isFinishing) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            // Плавная анимация перехода
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем ресурсы видео
        val videoView = findViewById<VideoView>(R.id.videoView)
        videoView.stopPlayback()
    }
}