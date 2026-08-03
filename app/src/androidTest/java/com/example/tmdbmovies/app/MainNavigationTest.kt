package com.example.tmdbmovies.app

import android.view.View
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import com.example.tmdbmovies.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.junit.Assert.assertEquals
import org.junit.Test

class MainNavigationTest {
    @Test
    fun bottomNavigationSelectsTopLevelAndHidesForSearch() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val navigation = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                val navController = Navigation.findNavController(activity, R.id.nav_host_fragment)

                navigation.selectedItemId = R.id.profileFragment

                assertEquals(R.id.profileFragment, navController.currentDestination?.id)
                assertEquals(View.VISIBLE, navigation.visibility)

                navController.navigate(R.id.searchFragment)

                assertEquals(R.id.searchFragment, navController.currentDestination?.id)
                assertEquals(View.GONE, navigation.visibility)
            }
        }
    }
}
