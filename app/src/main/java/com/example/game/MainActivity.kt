package com.example.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import android.widget.Button
import android.media.AudioAttributes
import android.media.SoundPool
import android.widget.FrameLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var mole: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var timeRemainingTextView: TextView
    private lateinit var falhasTextView: TextView
    private lateinit var highscoreTextView: TextView
    private lateinit var victoryImage: ImageView
    private lateinit var gameOverImage: ImageView
    private lateinit var moleArea: FrameLayout
    private lateinit var resetButton: Button

    private lateinit var soundPool: SoundPool //gerenciador dos efeitos sonoros
    private var clickSoundId: Int = 0
    private var victorySoundId: Int = 0
    private var gameOverSoundId: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private var hits = 0
    private var falhas = 0
    private var highscore = 0
    private var gameRunning = true
    private var gameDuration = 20000L //  (20000 milisegundos)
    private var moveInterval = 1500L // Velocidade da Toupeira inicialmente
    private val difficultyIncreaseRate = 0.95 // quantidade a ser decrementada para aumentar a velocidade de aparecimento
    private var moleClicked = false
    private var timeRemaining = gameDuration / 1000 // Tempo em segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mole = findViewById(R.id.imagemTopeira)
        scoreTextView = findViewById(R.id.textView)
        timeRemainingTextView = findViewById(R.id.tempoRestante)
        falhasTextView = findViewById(R.id.falhasTextView)
        highscoreTextView = findViewById(R.id.highscoreTextView)
        victoryImage = findViewById(R.id.victoryImage)
        gameOverImage = findViewById(R.id.gameOverImage)
        moleArea = findViewById(R.id.moleArea)
        resetButton = findViewById(R.id.resetButton)

        // Inicializa SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(3)
            .build()

        // carrea os efeitos sonoros
        clickSoundId = soundPool.load(this, R.raw.click_sound, 1)
        victorySoundId = soundPool.load(this, R.raw.victory_sound, 1)
        gameOverSoundId = soundPool.load(this, R.raw.game_over_sound, 1)

        mole.setOnClickListener {
            if (gameRunning) {
                hits++
                scoreTextView.text = "Acertos: $hits"
                soundPool.play(clickSoundId, 1f, 1f, 0, 0, 1f)
                animateMoleClick()
                moleClicked = true
            }
        }

        resetButton.setOnClickListener {
            resetGame()
        }

        startGame()
    }

    private fun startGame() {
        gameRunning = true
        hits = 0
        falhas = 0
        moveInterval = 1500L
        timeRemaining = gameDuration / 1000
        scoreTextView.text = "Acertos: $hits"
        falhasTextView.text = "Perdas: $falhas"
        timeRemainingTextView.text = "Tempo Restante: ${timeRemaining}s"
        victoryImage.visibility = ImageView.GONE
        gameOverImage.visibility = ImageView.GONE
        mole.visibility = ImageView.VISIBLE

        handler.post(moleMoverRunnable)
        handler.post(updateTimeRunnable)


        handler.postDelayed({
            endGame()
        }, gameDuration)
    }

    private val moleMoverRunnable = object : Runnable {
        override fun run() {
            if (gameRunning) {
                if (!moleClicked) {
                    falhas++
                    falhasTextView.text = "Perdas: $falhas"
                }
                moleClicked = false
                moveMole()
                handler.postDelayed(this, moveInterval)
                moveInterval = (moveInterval * difficultyIncreaseRate).toLong()
            }
        }
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (gameRunning) {
                timeRemaining--
                timeRemainingTextView.text = "Tempo Restante: ${timeRemaining}s"
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun moveMole() {
        val moleAreaWidth = moleArea.width
        val moleAreaHeight = moleArea.height
        val maxX = moleAreaWidth - mole.width
        val maxY = moleAreaHeight - mole.height
        val randomX = Random.nextInt(maxX)
        val randomY = Random.nextInt(maxY)
        mole.x = randomX.toFloat()
        mole.y = randomY.toFloat()
    }

    private fun animateMoleClick() {
        mole.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
            mole.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }
    }

    private fun endGame() {
        gameRunning = false
        scoreTextView.text = "Acertos: $hits"
        falhasTextView.text = "Perdas: $falhas"
        mole.visibility = ImageView.GONE

        // Verifica se os acertos são maiores que as falhas
        if (hits > falhas) {
            victoryImage.visibility = ImageView.VISIBLE
            soundPool.play(victorySoundId, 1f, 1f, 0, 0, 1f)
            // Update highscore se necessario
            if (hits > highscore) {
                highscore = hits
            }
        } else {
            gameOverImage.visibility = ImageView.VISIBLE
            soundPool.play(gameOverSoundId, 1f, 1f, 0, 0, 1f)
        }

        // Update highscore display
        highscoreTextView.text = "Highscore: $highscore"
    }

    private fun resetGame() {
        handler.removeCallbacksAndMessages(null)
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
