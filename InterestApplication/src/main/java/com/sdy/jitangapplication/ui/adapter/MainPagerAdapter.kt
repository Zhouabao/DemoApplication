package com.sdy.jitangapplication.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 *    author : ZFM
 *    date   : 2019/7/1818:08
 *    desc   :
 *    version: 1.0
 */
class MainPagerAdapter(fragmentManager: FragmentManager, private val fragments: List<Fragment>,private val titles:Array<String>) :
    FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
