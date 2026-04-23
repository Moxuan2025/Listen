package com.demo.listen.Layout.guardian

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.demo.listen.R
import com.demo.listen.net.SessionStore
import com.google.android.material.bottomnavigation.BottomNavigationView

class GuardianActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    private var guardianUsername: String = ""
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian)

        guardianUsername = intent.getStringExtra("uname") ?: SessionStore.name(this) ?: ""
        token = intent.getStringExtra("token") ?: SessionStore.token(this) ?: ""

        // 处理系统窗口边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.guardian_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewPager()
        initBottomNav()
    }

    private fun initViewPager() {
        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = GuardianPagerAdapter(this)

        // ViewPager2 页面切换时同步底部导航选中项
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun initBottomNav() {
        bottomNav = findViewById(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_children -> viewPager.currentItem = 0
                R.id.nav_assistant -> viewPager.currentItem = 1
                R.id.nav_profile -> viewPager.currentItem = 2
                else -> false
            }
            true
        }
    }

    // 提供给 Fragment 获取当前监护人信息的方法
    fun getGuardianUsername(): String = guardianUsername
    fun getToken(): String = token
}