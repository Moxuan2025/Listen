package com.demo.listen.Layout.Assessment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 共享 音/视频 播放状态，播放 音/视频 后，才可以做选择
class ShareListenState : ViewModel() {
    private val _played = MutableLiveData<Boolean>(false)
    private val _choice = MutableLiveData<String>("")       // 选择
    private val _options = MutableLiveData<List<String>>(emptyList<String>()) // 选项

    val played: LiveData<Boolean> = _played
    val choice: LiveData<String> = _choice
    val options: LiveData<List<String>> = _options

    fun updatePlayState(input: Boolean) {           // 更新播放状态
        _played.value = input
    }

    fun updateChoice(input: String) {               // 更新选择
        _choice.value = input
    }

    fun updateOptions(input: List<String>) {        // 更新选项
        _options.value = input
    }
}