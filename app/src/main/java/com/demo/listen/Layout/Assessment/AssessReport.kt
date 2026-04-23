package com.demo.listen.Layout.Assessment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.Layout.EnjoyStudy.Enjoyment
import com.demo.listen.R

class AssessReport : AppCompatActivity() {

    private var choice: ArrayList<String>? = null
    private var target: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assess_report)

        choice = intent.getStringArrayListExtra("choice")
        target = intent.getStringArrayListExtra("target")

        handleClick()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleClick() {
        findViewById<Button>(R.id.assess_report_go_study).setOnClickListener {
            startActivity(Intent(this@AssessReport,
                Enjoyment::class.java))
            finish()
        }
    }
}