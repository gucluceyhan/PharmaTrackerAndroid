package com.example.pharmatracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pharmatracker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    // Request permission launcher for camera
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Camera permission granted, can proceed with barcode scanning
        } else {
            // Camera permission denied
        }
    }
    
    // Request permission launcher for notifications (Android 13+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Notification permission granted
        } else {
            // Notification permission denied
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up navigation controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Set up bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        setupWithNavController(bottomNav, navController)
        
        // Set up app bar
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.medicationListFragment, R.id.settingsFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Listen for navigation changes to show/hide bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Show bottom nav only for main screens
            bottomNav.isVisible = when (destination.id) {
                R.id.medicationListFragment, R.id.settingsFragment -> true
                else -> false
            }
        }
        
        // Request camera permission if needed
        requestCameraPermissionIfNeeded()
        
        // Request notification permission for Android 13+ 
        requestNotificationPermissionIfNeeded()
        
        // Check if there's a medication ID in the intent
        intent.getStringExtra("MEDICATION_ID")?.let { medicationId ->
            // Navigate to medication detail
            val bundle = Bundle().apply {
                putString("medicationId", medicationId)
            }
            navController.navigate(R.id.medicationDetailFragment, bundle)
        }
    }
    
    /**
     * Handle up button press with the navigation controller
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    /**
     * Request camera permission if not already granted
     */
    private fun requestCameraPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    /**
     * Request notification permission for Android 13+
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    /**
     * Handle options menu item selection
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle back button press
                navController.navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}