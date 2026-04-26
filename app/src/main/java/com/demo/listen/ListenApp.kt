package com.demo.listen

import android.app.Application
import com.demo.listen.net.ServerApi

class ListenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServerApi.init(this)
    }
}
