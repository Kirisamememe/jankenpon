package com.example.janken

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.janken.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        val computerScore = intent.getIntExtra("COMPUTER_SCORE", 0)
        val userScore = intent.getIntExtra("USER_SCORE", 0)
        val drawCount = intent.getIntExtra("DRAW_COUNT", 0)

        binding.computerScore.text = getString(R.string.computer_score, computerScore)
        binding.userScore.text = getString(R.string.user_score, userScore)
        binding.drawCount.text = getString(R.string.draw_count, drawCount)


        binding.tryAgainBtn.setOnClickListener {
            startActivity(Intent(this@ResultActivity, MainActivity::class.java))
        }

        checkResult(userScore, computerScore)
    }

    private fun checkResult(userScore: Int, computerScore: Int) {
        if (userScore > computerScore){
            binding.resultTitle.text = "あなたの勝ち！"
            binding.resultMsg.text = getString(R.string.win_text)
            binding.resultImg.setImageResource(R.drawable.pose_win_girl)
        } else if (userScore < computerScore) {
            binding.resultTitle.text = "あなたの負け！"
            binding.resultMsg.text = getString(R.string.lose_text)
            binding.resultImg.setImageResource(R.drawable.pose_lose_boy)
        } else {
            binding.resultTitle.text = "ドロー！"
            binding.resultMsg.text = getString(R.string.draw_text)
            binding.resultImg.setImageResource(R.drawable.high_touch_man_woman)
        }
    }
}