package com.demo.listen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.demo.listen.Layout.Assessment.FragmentPersonalAssessment
import com.demo.listen.Layout.Assessment.FragmentSoundAssess
import com.demo.listen.Layout.MainPages.RankFragment
import com.demo.listen.Layout.MainPages.StudyFragment
import com.demo.listen.Layout.MainPages.UserFragment
import com.demo.listen.Layout.Assessment.SoundAssessPractice
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.demo.listen.net.SessionStore

class MainActivity : AppCompatActivity() {

    private lateinit var bmv: BottomNavigationView  // 导航栏
    // 主要界面
    private val studyFragment = StudyFragment()
    private val userFragment = UserFragment()
    private val rankFragment = RankFragment()

    // 评估选项界面
    private val personalAssessment = FragmentPersonalAssessment()
    private val soundAssess = FragmentSoundAssess()

    private var loged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val currentRole = intent.getStringExtra("role").takeIf { !it.isNullOrBlank() }
            ?: SessionStore.role(this)

        bmv = findViewById<BottomNavigationView>(R.id.navigator)
        if (savedInstanceState == null) {
            if (currentRole == "child") {
                loadFragment(studyFragment)
            } else {
                loadFragment(userFragment)
            }
        }
        bmv.setOnItemSelectedListener { item ->
            if (currentRole != "child") {
                when (item.itemId) {
                    R.id.user_pg -> {
                        loadFragment(userFragment)
                        true
                    }
                    else -> {
                        Toast.makeText(this, "当前账号不是 child，不能进入该页面", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
            } else {
                when (item.itemId) {
                    R.id.study_pg -> {
                        loadFragment(studyFragment)
                        true
                    }
                    R.id.user_pg -> {
                        loadFragment(userFragment)
                        true
                    }
                    R.id.rank_pg -> {
                        loadFragment(rankFragment)
                        true
                    }
                    else -> false
                }
            }
        }

        handleFragmentResult()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 封装加载 Fragment 的方法
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
    }

    private fun handleFragmentResult() {
        // 学习主界面
        supportFragmentManager.setFragmentResultListener("Study", this) {
                _, bundle ->
            val ret = bundle.getString("event")
            when (ret) {
                "PersonalAssessment" -> loadFragment(personalAssessment)
            }
        }

        // 个人评估
        supportFragmentManager.setFragmentResultListener("PersonalAssessment", this) {
                _, bundle ->
            val ret = bundle.getString("event")
            when (ret) {
                "Back" -> loadFragment(studyFragment)
                "SoundAssess" -> loadFragment(soundAssess)
            }
        }
        supportFragmentManager.setFragmentResultListener("SoundAssess", this) {
                _, bundle ->
            val ret = bundle.getString("event")
            when (ret) {
                "Back" -> loadFragment(personalAssessment)
                "SoundPractice" -> startActivity(Intent(this@MainActivity,
                    SoundAssessPractice::class.java).apply {
                    putExtra("feature", "Practice") })
                "ProfessionalAssess"-> startActivity(Intent(this@MainActivity,
                    SoundAssessPractice::class.java).apply {
                    putExtra("feature", "Assess") })
            }
        }
    }
}