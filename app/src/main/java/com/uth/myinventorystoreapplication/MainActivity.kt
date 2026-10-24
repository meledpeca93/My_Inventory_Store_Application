package com.uth.myinventorystoreapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uth.myinventorystoreapplication.ui.login.LoginActivity
import com.uth.myinventorystoreapplication.ui.main.MainPagerAdapter
import com.uth.myinventorystoreapplication.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Verificar si la sesión es válida
        if (!sessionManager.isLoggedIn()) {
            openLoginActivity()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        topAppBar = findViewById(R.id.topAppBar)
        viewPager = findViewById(R.id.viewPager)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        topAppBar.subtitle = "Admin: ${sessionManager.getUsername()}"
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    sessionManager.logout()
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    openLoginActivity()
                    true
                }
                else -> false
            }
        }

        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Prevents fragments from being recreated unnecessarily
        viewPager.offscreenPageLimit = 2

        // Sync ViewPager swipe with BottomNavigationView
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        bottomNavigation.selectedItemId = R.id.nav_products
                        pagerAdapter.productsFragment.loadProducts()
                    }
                    1 -> {
                        bottomNavigation.selectedItemId = R.id.nav_movements
                        pagerAdapter.movementsFragment.loadProductSpinner()
                        pagerAdapter.movementsFragment.loadMovementsHistory()
                    }
                    2 -> {
                        bottomNavigation.selectedItemId = R.id.nav_summary
                        pagerAdapter.summaryFragment.loadSummaryData()
                    }
                }
            }
        })

        // Sync BottomNavigationView clicks with ViewPager
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_movements -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_summary -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }

    private fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
