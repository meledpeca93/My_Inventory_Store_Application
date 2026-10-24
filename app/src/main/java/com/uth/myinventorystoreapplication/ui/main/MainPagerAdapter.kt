package com.uth.myinventorystoreapplication.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uth.myinventorystoreapplication.ui.movements.MovementsFragment
import com.uth.myinventorystoreapplication.ui.products.ProductsFragment
import com.uth.myinventorystoreapplication.ui.summary.SummaryFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    val productsFragment = ProductsFragment()
    val movementsFragment = MovementsFragment()
    val summaryFragment = SummaryFragment()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> productsFragment
            1 -> movementsFragment
            2 -> summaryFragment
            else -> productsFragment
        }
    }
}
