package com.basics.whatsappclone.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.basics.whatsappclone.fragment.ChatsFragment
import com.basics.whatsappclone.fragment.PeopleFragment

class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {

        return when(position){
            0-> ChatsFragment()
            else -> PeopleFragment()
        }
    }

}
