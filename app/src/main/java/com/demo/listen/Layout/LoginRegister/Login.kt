package com.demo.listen.Layout.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.demo.listen.MainActivity
import com.demo.listen.R

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun login(view: View) {
        if (findViewById<EditText>(R.id.uname).text.isEmpty())
            Toast.makeText(this, "name is empty", Toast.LENGTH_SHORT).show()
        else if (findViewById<EditText>(R.id.passwd).text.isEmpty())
            Toast.makeText(this, "passwd is empty", Toast.LENGTH_SHORT).show()
        else {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("uname", findViewById<EditText>(R.id.uname).text)
            })
        }
    }
}