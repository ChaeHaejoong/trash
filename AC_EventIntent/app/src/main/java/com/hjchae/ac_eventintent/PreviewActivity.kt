package com.hjchae.ac_eventintent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.v
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_GOAL_MINUTES
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_GOAL_STRATEGY
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_REMINDER_ENABLED
import com.hjchae.ac_eventintent.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {
    lateinit var binding: ActivityPreviewBinding

    private var exerciseType = ""
    private var durationMinutes = 0
    private var memo = ""
    private var intensity = 0f
    private var warmUp = false
    private var coolDown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity가 보내준 데이터 추출
        // 운동종류 추출
        val rIntent = getIntent()
        exerciseType = rIntent.getStringExtra("type")!!
        durationMinutes = intent.getIntExtra(ExerciseContract.EXTRA_DURATION_MINUTES, 0)
        memo = intent.getStringExtra(ExerciseContract.EXTRA_MEMO)!!
        intensity = intent.getFloatExtra(ExerciseContract.EXTRA_INTENSITY, 0f)
        warmUp = intent.getBooleanExtra(ExerciseContract.EXTRA_WARM_UP, false)
        coolDown = intent.getBooleanExtra(ExerciseContract.EXTRA_COOL_DOWN, false)

        // 추출한 데이터 화면에 보여주기
        // 운동종류 보여주기
        binding.tvPreviewDuration.text = "운동 시간: ${durationMinutes}분"
        binding.tvPreviewIntensity.text = "운동 강도: ${intensity.toInt()}점"
        binding.tvPreviewMemo.text = memo

        val completedItems = mutableListOf<String>()
        if (warmUp) completedItems.add("준비운동 완료")
        if (coolDown) completedItems.add("정리운동 완료")

        binding.tvPreviewChecks.text = if (completedItems.isEmpty()) {
            "완료 항목 없음"
        } else {
            "완료 항목: ${completedItems.joinToString(", ")}"
        }

       // 취소버튼 클릭 처리
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // ok 버튼 클릭 처리
        binding.btnApplyGoal.setOnClickListener {
            val goalMinutes = binding.etGoalMinutes.text.toString().trim().toIntOrNull()

            if (goalMinutes == null || goalMinutes !in 1..600) {
                binding.etGoalMinutes.error = "목표 시간을 1~600분 사이로 입력하세요."
                binding.etGoalMinutes.requestFocus()
                return@setOnClickListener
            }
            val goalStrategy = when(binding.rgGoalStrategy.checkedRadioButtonId) {
                R.id.rbMoreTime -> getString(R.string.prb_moretime)
                R.id.rbMaintain -> getString(R.string.prb_maintain)
                else -> getString(R.string.prb_moreintensity)
            }

            val sIntent2 = Intent()
            sIntent2.putExtra(EXTRA_GOAL_MINUTES, goalMinutes)
            sIntent2.putExtra(EXTRA_REMINDER_ENABLED, binding.switchReminder.isChecked)
            sIntent2.putExtra(EXTRA_GOAL_STRATEGY, goalStrategy)
            setResult(RESULT_OK, sIntent2)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    } // onCreate

    private fun selectedGoalStrategy(): String {
        return when {
            binding.rbMoreTime.isChecked -> getString(R.string.prb_moretime)
            binding.rbMoreIntensity.isChecked -> getString(R.string.prb_moreintensity)
            else -> getString(R.string.prb_maintain)
        }
    }
}
