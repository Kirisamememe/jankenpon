package com.example.janken

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.janken.databinding.ActivityMainBinding


const val COUNT = 7

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var count = 0
    private val score = mutableMapOf("computer" to 0, "user" to 0, "draw" to 0)
    private val handShapes = arrayOf("rock", "scissors", "paper")
    private val handShapeResources = mapOf(
        "rock" to R.drawable.janken_gu,
        "scissors" to R.drawable.janken_choki,
        "paper" to R.drawable.janken_pa
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.countLabel.text = getString(R.string.start)

        setButtonClickListenerWithAnimation(binding.scissorsBtn, "scissors")
        setButtonClickListenerWithAnimation(binding.rockBtn, "rock")
        setButtonClickListenerWithAnimation(binding.paperBtn, "paper")

    }

    private fun setButtonClickListenerWithAnimation(button: ImageButton, choice: String) {
        button.setOnClickListener {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.2f, 1.0f)
            val transY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -60f, 0f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.2f, 1.0f)
            val animator = ObjectAnimator.ofPropertyValuesHolder(button, transY, scaleX, scaleY)
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()

            // アニメーション開始と同時にユーザーの選択を処理
            animator.start()
            onUserChoice(choice)
        }
    }

    private fun animateImageViewInit(imageView: ImageView, transYValue: Float) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f)
        val transY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, transYValue, 0f)

        val animator = ObjectAnimator.ofPropertyValuesHolder(imageView, scaleX, scaleY, transY)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()

        animator.start()
    }

    private fun applyBlackWhiteFilter(imageView: ImageView, intensity: Float) {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(intensity)
        val filter = ColorMatrixColorFilter(colorMatrix)
        imageView.colorFilter = filter
    }

    private fun animateImageViewAlpha(imageView: ImageView, value1: Float, value2: Float, durationValue: Long) {
        val opacityAnimator = ObjectAnimator.ofFloat(imageView, "alpha", value1, value2)
            .apply {
                duration = durationValue
                interpolator = DecelerateInterpolator()
            }

        opacityAnimator.start()
    }

    private fun animateResultImageScale(imageView: ImageView) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.4f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.4f)
        val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(imageView, scaleX, scaleY)
            .apply {
                duration = 300
                interpolator = DecelerateInterpolator()
            }

        scaleAnimator.start()
    }

    private fun animateImageViewWithFilter(imageView: ImageView) {
        animateImageViewAlpha(imageView, 1.0f, 0.5f, 300)

        val filterAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 300 // Duration of the animation
            interpolator = DecelerateInterpolator() // Smooth deceleration
            addUpdateListener { animation ->
                // Updating the grayscale filter based on current animation progress
                val value = animation.animatedValue as Float
                applyBlackWhiteFilter(imageView, value)
            }
        }
        filterAnimator.start()
    }

    private fun computer(): String {
        val computerChoice = handShapes.indices.random()
        handShapeResources[handShapes[computerChoice]]?.let {
            binding.rival.setImageResource(it)
            animateImageViewInit(binding.rival, -500f)
            applyBlackWhiteFilter(binding.rival, 1f)
            animateImageViewAlpha(binding.rival, 1.0f, 1.0f, 0)
        }

        return handShapes[computerChoice]
    }

    private fun onUserChoice(userChoice: String) {
        val computerChoice = computer()

        binding.result.text = getString(R.string.initText2)
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorForeground, typedValue, true)
        val colorForeground = typedValue.data

        binding.result.setTextColor(colorForeground)

        handShapeResources[userChoice]?.let {
            binding.user.setImageResource(it)
            animateImageViewInit(binding.user, 500f)
            applyBlackWhiteFilter(binding.user, 1f)
            animateImageViewAlpha(binding.user, 1.0f, 1.0f, 0)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val result = judge(userChoice, computerChoice)
            binding.result.text = result
            checkCount()
        }, 400)
    }

    private fun judge(userChoice: String, computerChoice: String): String {
        if (userChoice == computerChoice) {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorForeground, typedValue, true)
            val colorForeground = typedValue.data

            binding.result.setTextColor(colorForeground)
            score["draw"]?.let { score["draw"] = it + 1 }
            return "ドロー"
        }
        else if (
            (userChoice == "rock" && computerChoice == "scissors") ||
            (userChoice == "scissors" && computerChoice == "paper") ||
            (userChoice == "paper" && computerChoice == "rock")
        ) {
            binding.result.setTextColor(getColor(R.color.win))

            animateImageViewWithFilter(binding.rival)
            animateResultImageScale(binding.user)

            score["user"]?.let { score["user"] = it + 1 }
            return "あなたの勝ち！"
        }
        else {
            binding.result.setTextColor(getColor(R.color.lose))

            animateImageViewWithFilter(binding.user)
            animateResultImageScale(binding.rival)

            score["computer"]?.let { score["computer"] = it + 1 }
            return "あなたの負け！"
        }
    }

    private fun checkCount() {
        count++
        binding.countLabel.text = getString(R.string.count_label, count)

        if (count == COUNT) {
            animateImageViewAlpha(binding.scissorsBtn, 0.3f,0.2f, 150)
            animateImageViewAlpha(binding.rockBtn, 0.3f,0.2f, 150)
            animateImageViewAlpha(binding.paperBtn, 0.3f,0.2f, 150)

            binding.scissorsBtn.isEnabled = false
            binding.rockBtn.isEnabled = false
            binding.paperBtn.isEnabled = false

            val intent = Intent(this@MainActivity, ResultActivity::class.java)
            intent.putExtra("COMPUTER_SCORE", score["computer"])
            intent.putExtra("USER_SCORE", score["user"])
            intent.putExtra("DRAW_COUNT", score["draw"])

            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(intent)
            }, 1000)
        }
    }
}