package com.example.weatherapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.storage.PreferencesManager
import com.example.weatherapp.ui.*
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    val viewModel: WeatherViewModel by viewModels()

    private val handler = Handler(Looper.getMainLooper())
    private var autoRefreshRunnable: Runnable? = null
    private var hasShownNoInternetToast = false
    var hasShownTabletOfflineToast = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        bottomNav = findViewById(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_weather -> {
                    val isTablet = resources.getBoolean(R.bool.isTablet)
                    if (isTablet) {
                        replaceFragment(TabletWeatherFragment())
                    } else {
                        replaceFragment(CurrentWeatherFragment())
                    }
                    true
                }

                R.id.nav_forecast -> replaceFragment(ForecastFragment())
                R.id.nav_advanced -> replaceFragment(AdvancedWeatherFragment())
                R.id.nav_location -> replaceFragment(LocationFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_weather
        }


        startAutoRefresh()
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    private fun startAutoRefresh() {
        val prefs = PreferencesManager(this)
        val interval = prefs.getCacheDuration()
        val city = prefs.getLastUsedCity()

        if (!city.isNullOrBlank()) {
            autoRefreshRunnable = object : Runnable {
                override fun run() {
                    val hasInternet = com.example.weatherapp.utils.NetworkUtils.isInternetAvailable(this@MainActivity)

                    if (hasInternet) {
                        hasShownTabletOfflineToast = false
                        hasShownNoInternetToast = false

                        viewModel.fetchWeather(city, force = true)
                        viewModel.weatherData.value?.let { weather ->
                            viewModel.fetchForecast(weather.coord.lat, weather.coord.lon, force = true)
                        }
                    } else if (!hasShownNoInternetToast) {
                        Toast.makeText(
                            this@MainActivity,
                            "Brak połączenia z internetem – automatyczne odświeżanie wyłączone",
                            Toast.LENGTH_SHORT
                        ).show()
                        hasShownNoInternetToast = true
                    }

                    handler.postDelayed(this, interval)
                }
            }

            val lastUpdate = prefs.getLastWeatherTimestamp(city)
            val now = System.currentTimeMillis()
            val delay = interval - (now - lastUpdate).coerceAtLeast(0)
            handler.postDelayed(autoRefreshRunnable!!, delay)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        autoRefreshRunnable?.let { handler.removeCallbacks(it) }
    }

    fun restartAutoRefresh() {
        autoRefreshRunnable?.let { handler.removeCallbacks(it) }
        startAutoRefresh()
    }
}
