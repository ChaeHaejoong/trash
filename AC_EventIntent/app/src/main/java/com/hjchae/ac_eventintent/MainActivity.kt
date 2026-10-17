package com.hjchae.ac_eventintent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_COOL_DOWN
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_GOAL_STRATEGY
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_INTENSITY
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_MEMO
import com.hjchae.ac_eventintent.ExerciseContract.EXTRA_WARM_UP
import com.hjchae.ac_eventintent.databinding.ActivityMainBinding
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // two-way data transmission
        // ActivityResultAPI - contract, callback, launcher
        // 1. create contract object
        // contract object knows input, output type
        val contract = ActivityResultContracts.StartActivityForResult()
        // 2. create callback object
        val cb = object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_CANCELED) {
                    Toast.makeText(baseContext, "canceled", Toast.LENGTH_LONG).show()
                    return
                }

                val data = result.data

                val goalStrategy = data?.getStringExtra(EXTRA_GOAL_STRATEGY)
                val nextGoalMinutes = data?.getIntExtra(ExerciseContract.EXTRA_GOAL_MINUTES, 30)!!
                val reminderEnabled =
                    data?.getBooleanExtra(ExerciseContract.EXTRA_REMINDER_ENABLED, true)!!

                val reminderText = if (reminderEnabled) {
                    "enabled"
                } else {
                    "disabled"
                }
                binding.tvNextGoalSummary.text =
                    "next goal: ${goalStrategy}, ${nextGoalMinutes}min $reminderText"
            }
        }
        val launcher = registerForActivityResult(contract, cb)

        binding.btnOpenGoal.setOnClickListener {
            val duration = binding.etDuration.text.toString().trim().toIntOrNull()
            val memo = binding.etMemo.text.toString().trim()
            if (memo.length < 10) {
                Toast.makeText(baseContext, "write memo", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (duration == null || duration !in 1..600) {
                binding.etDuration.error = "운동 시간을 1~600분 사이로 입력하세요."
                binding.etDuration.requestFocus()
                return@setOnClickListener
            }

            if (binding.ratingIntensity.rating == 0f) {
                Toast.makeText(this, "운동 강도를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedExerciseType = when {
                binding.rbRunning.isChecked -> getString(R.string.type_running)
                binding.rbCycling.isChecked -> getString(R.string.type_cycling)
                binding.rbWalking.isChecked -> getString(R.string.type_walking)
                else -> ""
            }

            if (selectedExerciseType.isEmpty()) {
                Toast.makeText(baseContext, "운동 종류를 선택하세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val sIntent1 = Intent(baseContext, PreviewActivity::class.java)
            sIntent1.putExtra("type", selectedExerciseType)
            sIntent1.putExtra(EXTRA_MEMO, memo)
            sIntent1.putExtra(EXTRA_INTENSITY, binding.ratingIntensity.rating)
            sIntent1.putExtra(EXTRA_WARM_UP, binding.cbWarmUp.isChecked)
            sIntent1.putExtra(EXTRA_COOL_DOWN, binding.cbCoolDown.isChecked)

            // startActivity(sIntent1)
            launcher.launch(sIntent1)
        }

        // 1. define class
        class ClkListener : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(baseContext, "운동기록 저장 완료", Toast.LENGTH_LONG).show()
            }
        }

        val listener1 = ClkListener()
        binding.textView.setOnClickListener(listener1)

        // 2. anonymous class
        val listner2 = object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val type = when (checkedId) {
                    R.id.rbCycling -> "cycle"
                    R.id.rbRunning -> getString(R.string.type_running)
                    R.id.rbWalking -> getString(R.string.type_walking)
                    else -> "hello world"
                }
                binding.tvExerciseType.text = "${getString(R.string.sub_title1)}: $type"
            }
        }
        binding.rgExerciseType.setOnCheckedChangeListener(listner2)

        // 4. anonymous class lambda
        binding.cbWarmUp.setOnCheckedChangeListener { button: CompoundButton, isChecked: Boolean ->
            if (isChecked) Toast.makeText(baseContext, button.text, Toast.LENGTH_LONG).show()
            updateCheckSummary()
        }

        // 3. anonymous class method2
        binding.cbCoolDown.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(
                buttonView: CompoundButton, isChecked: Boolean
            ) {
                if (isChecked) Log.d("onCheckedChanged", buttonView.text.toString())
                updateCheckSummary()
            }
        })

        binding.ratingIntensity.setOnRatingBarChangeListener(object :
            RatingBar.OnRatingBarChangeListener {
            override fun onRatingChanged(
                ratingBar: RatingBar?, rating: Float, fromUser: Boolean
            ) {
                binding.tvIntensityValue.text = if (rating == 0f) {
                    getString(R.string.edit_hint2)
                } else {
                    "${getString(R.string.sub_title3)}: ${rating.toInt()}"
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    } // onCreate

    private fun updateCheckSummary() {
        val completedItems = StringBuffer()

        if (binding.cbWarmUp.isChecked) {
            completedItems.append(getString(R.string.check_waramup))
        }

        if (binding.cbCoolDown.isChecked) {
            if (!completedItems.isEmpty()) completedItems.append(",")
            completedItems.append(getString(R.string.check_cooldown))
        }
        binding.tvCheck.text = if (completedItems.isEmpty()) {
            "${getString(R.string.sub_title5)} : 완료 항목이 없습니다."
        } else {
            "${getString(R.string.sub_title5)} : ${completedItems.toString()}"
        }
    }

} // MainActivity
