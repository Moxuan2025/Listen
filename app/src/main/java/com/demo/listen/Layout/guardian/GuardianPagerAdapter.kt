package com.demo.listen.Layout.guardian

//package com.demo.listen.Layout.guardian?

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.demo.listen.Layout.MainPages.UserFragment

class GuardianPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChildListFragment()
            1 -> AiAssistantFragment()
            2 -> UserFragment()  // 复用现有个人页面
            else -> ChildListFragment()
        }
    }
}