package com.example.weatherapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.network.RetrofitClient
import com.example.weatherapp.storage.PreferencesManager
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.NetworkUtils
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    val weatherData = MutableLiveData<WeatherResponse?>()
    val forecastData = MutableLiveData<ForecastResponse?>()
    val favoriteCities =
        MutableLiveData<List<String>>(PreferencesManager(application).getFavoriteCities())
    val errorMessage = MutableLiveData<String?>()
    val isOfflineData = MutableLiveData<Boolean>()
    val usedOfflineFallback = MutableLiveData<Boolean>()
    val isUsingCache = MutableLiveData<Boolean>()


    private val prefs = PreferencesManager(application)

    fun fetchWeather(city: String, force: Boolean = false) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            Log.d("DEBUG", "Fetching weather for $city")
            val lastUpdate = prefs.getLastWeatherTimestamp(city)
            val cacheDuration = prefs.getCacheDuration()
            val now = System.currentTimeMillis()
            Log.d("DEBUG", "Last update: $lastUpdate,\nnow: $now, cacheDuration: $cacheDuration")

            if (!force && cacheDuration > 0 && now - lastUpdate < cacheDuration) {
                val cached = loadWeatherFromFile(city)
                if (cached != null) {
                    Log.d("DEBUG", "Using cached data for $city")
                    weatherData.value = cached
                    isUsingCache.value = true
                    usedOfflineFallback.value = false
                    isOfflineData.value = false
                    return@launch
                }
            }

            if (NetworkUtils.isInternetAvailable(context)) {
                try {
                    val response = RetrofitClient.apiService.getCurrentWeather(
                        city = city,
                        apiKey = Constants.API_KEY,
                        units = "metric",
                        lang = "pl"
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val weather = response.body()!!
                        weatherData.value = null
                        weatherData.value = weather.copy()
                        isUsingCache.value = false
                        usedOfflineFallback.value = false
                        isOfflineData.value = false
                        saveWeatherToFile(city, weather)
                        prefs.saveLastWeatherTimestamp(city, System.currentTimeMillis())
                    } else {
                        errorMessage.value = "Błąd pobierania danych: ${response.message()}"
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Błąd: ${e.localizedMessage}"
                }
            } else {
                val local = loadWeatherFromFile(city)
                if (local != null) {
                    weatherData.value = local
                    usedOfflineFallback.value = true
                    isUsingCache.value = false
                } else {
                    errorMessage.value = "Brak internetu i brak danych lokalnych"
                }
            }
        }
    }


    fun fetchForecast(lat: Double, lon: Double, force: Boolean = false) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val city = weatherData.value?.name ?: "unknown"

            val lastUpdate = prefs.getLastForecastTimestamp(city)
            val cacheDuration = prefs.getCacheDuration()
            val now = System.currentTimeMillis()
            Log.d("DEBUG", "Last update: $lastUpdate,\n now: $now, cacheDuration: $cacheDuration")

            if (!force && now - lastUpdate < cacheDuration) {
                val cached = loadForecastFromFile(city)
                if (cached != null) {
                    forecastData.value = cached
                    isUsingCache.value = true
                    usedOfflineFallback.value = false
                    isOfflineData.value = false
                    return@launch
                }
            }

            if (NetworkUtils.isInternetAvailable(context)) {
                try {
                    val response = RetrofitClient.apiService.getForecast(
                        lat = lat,
                        lon = lon,
                        apiKey = Constants.API_KEY
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val forecast = response.body()!!
                        isUsingCache.value = false
                        usedOfflineFallback.value = false
                        forecastData.value = forecast
                        saveForecastToFile(city, forecast)
                        prefs.saveLastForecastTimestamp(city, System.currentTimeMillis())
                    } else {
                        errorMessage.value = "Błąd prognozy: ${response.message()}"
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Błąd: ${e.localizedMessage}"
                }
            } else {
                val local = loadForecastFromFile(city)
                if (local != null) {
                    forecastData.value = local
                    usedOfflineFallback.value = true
                    isUsingCache.value = false
                } else {
                    errorMessage.value = "Brak internetu i brak danych lokalnych"
                }
            }
        }
    }


    fun clearError() {
        errorMessage.value = null
    }

    fun addFavoriteCity(city: String) {
        val updated = favoriteCities.value.orEmpty().toMutableList()
        if (!updated.contains(city)) {
            updated.add(city)
            favoriteCities.value = updated
            prefs.saveFavoriteCities(updated)
        }
    }

    private fun saveWeatherToFile(city: String, weather: WeatherResponse) {
        val fileName = "weather_${city.lowercase()}.json"
        val file = File(getApplication<Application>().filesDir, fileName)
        file.writeText(Gson().toJson(weather))
    }

    private fun loadWeatherFromFile(city: String): WeatherResponse? {
        val fileName = "weather_${city.lowercase()}.json"
        val file = File(getApplication<Application>().filesDir, fileName)
        return if (file.exists()) {
            try {
                Gson().fromJson(file.readText(), WeatherResponse::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private fun saveForecastToFile(city: String, forecast: ForecastResponse) {
        val fileName = "forecast_${city.lowercase()}.json"
        val file = File(getApplication<Application>().filesDir, fileName)
        file.writeText(Gson().toJson(forecast))
    }

    private fun loadForecastFromFile(city: String): ForecastResponse? {
        val fileName = "forecast_${city.lowercase()}.json"
        val file = File(getApplication<Application>().filesDir, fileName)
        return if (file.exists()) {
            try {
                Gson().fromJson(file.readText(), ForecastResponse::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun setLastUsedCity(city: String) {
        prefs.saveLastUsedCity(city)
    }

    fun removeFavoriteCity(city: String) {
        val updated = favoriteCities.value.orEmpty().toMutableList()
        if (updated.remove(city)) {
            favoriteCities.value = updated
            prefs.saveFavoriteCities(updated)
        }
    }

    fun fetchWeatherTest(city: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCurrentWeather(
                    city = city,
                    apiKey = Constants.API_KEY,
                    units = "metric",
                    lang = "pl"
                )

                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, "Takie miasto nie istnieje")
                }
            } catch (e: IOException) {
                callback(false, "Brak połączenia z internetem")
            } catch (e: Exception) {
                callback(false, "Wystąpił błąd")
            }
        }
    }


}
