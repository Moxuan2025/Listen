package com.demo.listen.Layout.EnjoyStudy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.R

class Enjoyment : AppCompatActivity() {

    private lateinit var tvClockIn: TextView
    private lateinit var tvSyllable: TextView
    private lateinit var tvWord: TextView
    private lateinit var tvPractice: TextView
    private lateinit var tvPhrase: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enjoyment)

        mapWidget()
        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mapWidget() {
        tvClockIn = findViewById<TextView>(R.id.enjoyment_clockin_days)
        tvSyllable = findViewById<TextView>(R.id.tv_enjoy_syllable)
        tvWord = findViewById<TextView>(R.id.tv_enjoy_word)
        tvPractice = findViewById<TextView>(R.id.tv_condition_practice)
        tvPhrase = findViewById<TextView>(R.id.tv_enjoy_phrase)
    }

    private fun handleClick() {
        tvSyllable.setOnClickListener {
            startActivity(Intent(this@Enjoyment, SyllableList::class.java))
        }
    }
}