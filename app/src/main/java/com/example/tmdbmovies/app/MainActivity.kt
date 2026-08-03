package com.example.tmdbmovies.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.example.tmdbmovies.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            navigateToTopLevel(navController, item.itemId)
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in TOP_LEVEL_DESTINATIONS
            bottomNavigation.visibility = if (isTopLevel) View.VISIBLE else View.GONE
            if (isTopLevel && bottomNavigation.selectedItemId != destination.id) {
                bottomNavigation.menu.findItem(destination.id)?.isChecked = true
            }
        }
    }

    private fun navigateToTopLevel(navController: NavController, destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return
        navController.navigate(
            destinationId,
            null,
            navOptions {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            },
        )
    }

    private companion object {
        val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.moviesFragment,
            R.id.favoritesFragment,
            R.id.profileFragment,
        )
    }
}
