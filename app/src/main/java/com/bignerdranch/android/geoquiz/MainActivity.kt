package com.bignerdranch.android.geoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    private lateinit var trueBtn: Button
    private lateinit var falseBtn: Button
    private lateinit var cheatBtn: Button
    private lateinit var nextBtn: ImageButton
    private lateinit var prevBtn: ImageButton
    private lateinit var questionTxt: TextView
    private lateinit var tokenTxt: TextView

    @SuppressLint("RestrictedApi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        // resultLauncher is the new way of getting results from child activities.
        /*var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == Activity.RESULT_OK) {
                quizViewModel.isCheater =
                    result.data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            }
        }*/

        // init views
        trueBtn = findViewById(R.id.trueBtn)
        falseBtn = findViewById(R.id.falseBtn)
        cheatBtn = findViewById(R.id.cheatBtn)
        nextBtn = findViewById(R.id.nextBtn)
        prevBtn = findViewById(R.id.prevBtn)
        questionTxt = findViewById(R.id.questionTxt)
        tokenTxt = findViewById(R.id.tokenTxt)

        // programmatically set init cheat token text, the same call is used in updateCheatTokenText()
        tokenTxt.text = getString(R.string.cheat_tokens, quizViewModel.cheatTokens)


        // set event listeners
        trueBtn.setOnClickListener {
            checkAnswer(true)
            if (quizViewModel.numberOfQuestionsAnswered == quizViewModel.questionBank.size) {
                val percentage = quizViewModel.numberOfCorrectAnswers * 100 / quizViewModel.questionBank.size
                Toast.makeText(this, "you scored $percentage %", Toast.LENGTH_LONG).show()
            }
        }
        falseBtn.setOnClickListener {
            checkAnswer(false)
        }

        cheatBtn.setOnClickListener {
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)

            //resultLauncher.launch(intent)
        }

        nextBtn.setOnClickListener{
            quizViewModel.currentIndex = (quizViewModel.currentIndex + 1) % quizViewModel.questionBank.size
            updateQuestion()
        }

        prevBtn.setOnClickListener{
            quizViewModel.currentIndex = (quizViewModel.currentIndex - 1)
            if (quizViewModel.currentIndex < 0) {quizViewModel.currentIndex = 0}
            updateQuestion()
        }

        questionTxt.setOnClickListener{
            quizViewModel.currentIndex = (quizViewModel.currentIndex + 1) % quizViewModel.questionBank.size
            updateQuestion()
        }

        updateQuestion()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_CODE_CHEAT) {
            val userCheated: Boolean = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            quizViewModel.questionBank[quizViewModel.currentIndex].cheated = userCheated
            if (userCheated) {
                quizViewModel.cheatTokens--
            }

            setCheatBtnVisibility()
            updateCheatTokenText()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun checkAnswer(response: Boolean){
        val correctAnswer: Boolean = quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.questionBank[quizViewModel.currentIndex].cheated -> R.string.judgement_toast
            response == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        if (response == correctAnswer) quizViewModel.numberOfCorrectAnswers++
        quizViewModel.numberOfQuestionsAnswered++
        quizViewModel.questionBank[quizViewModel.currentIndex].isAnswered = true
        setAnswerBtnVisibility()
    }

    private fun setAnswerBtnVisibility() {
        if (quizViewModel.questionBank[quizViewModel.currentIndex].isAnswered) {
            trueBtn.visibility = View.INVISIBLE
            falseBtn.visibility = View.INVISIBLE
        } else {
            trueBtn.visibility = View.VISIBLE
            falseBtn.visibility = View.VISIBLE
        }
    }

    private fun setCheatBtnVisibility() {
        if (quizViewModel.cheatTokens < 1) cheatBtn.visibility = View.INVISIBLE
    }

    private fun updateCheatTokenText() {
        tokenTxt.text = getString(R.string.cheat_tokens, quizViewModel.cheatTokens)
    }

    private fun updateQuestion() {
        questionTxt.setText(quizViewModel.questionBank[quizViewModel.currentIndex].textResId)
        setAnswerBtnVisibility()

    }
}