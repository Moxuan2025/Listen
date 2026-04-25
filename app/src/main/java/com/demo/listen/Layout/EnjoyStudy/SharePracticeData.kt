package com.demo.listen.Layout.EnjoyStudy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharePracticeData: ViewModel() {
    private val _action = MutableLiveData<List<String>>(listOf("发声动作"))
    val action: LiveData<List<String>> = _action

    private val _index = MutableLiveData<Int>(0)
    val index: LiveData<Int> = _index

    fun changeAction(input: List<String>) {
        _action.value = input
    }

    fun changeIndex(input: Int) {
        _index.value = input
    }
}