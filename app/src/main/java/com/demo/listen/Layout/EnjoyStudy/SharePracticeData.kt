package com.demo.listen.Layout.EnjoyStudy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharePracticeData: ViewModel() {
    private val _action = MutableLiveData<List<String>>(listOf())     // 发声动作内容
    val action: LiveData<List<String>> = _action

    private val _index = MutableLiveData<Int>(0)                // 当前进度
    val index: LiveData<Int> = _index

    private val _nextPage = MutableLiveData<String>("")   // 下一页
    val nextPage: LiveData<String> = _nextPage

    private val _target = MutableLiveData<List<String>>(listOf())      // 学习/练习的对象，比如：bā、拔河
    val target: LiveData<List<String>> = _target

    fun changeAction(input: List<String>) {
        _action.value = input
    }

    fun changeIndex(input: Int) {
        _index.value = input
    }

    fun setNext(input: String) {
        _nextPage.value = input
    }

    fun changeTarget(input: List<String>) {
        _target.value = input
    }
}