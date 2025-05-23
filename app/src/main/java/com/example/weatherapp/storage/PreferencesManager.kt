package com.example.weatherapp.storage

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    fun saveFavoriteCities(cities: List<String>) {
        prefs.edit().putStringSet("favorite_cities", cities.toSet()).apply()
    }

    fun getFavoriteCities(): List<String> {
        return prefs.getStringSet("favorite_cities", emptySet())?.toList() ?: emptyList()
    }

    fun saveLastUsedCity(city: String) {
        prefs.edit().putString("last_used_city", city).apply()
    }

    fun getLastUsedCity(): String? {
        return prefs.getString("last_used_city", null)
    }

    fun saveCacheDuration(durationMillis: Long) {
        prefs.edit().putLong("cache_duration", durationMillis).apply()
    }

    fun getCacheDuration(): Long {
        return prefs.getLong("cache_duration", 0L)
    }

    fun saveLastWeatherTimestamp(city: String, timestamp: Long) {
        prefs.edit().putLong("weather_time_${city.lowercase()}", timestamp).apply()
    }

    fun getLastWeatherTimestamp(city: String): Long {
        return prefs.getLong("weather_time_${city.lowercase()}", 0L)
    }

    fun saveLastForecastTimestamp(city: String, timestamp: Long) {
        prefs.edit().putLong("forecast_time_${city.lowercase()}", timestamp).apply()
    }

    fun getLastForecastTimestamp(city: String): Long {
        return prefs.getLong("forecast_time_${city.lowercase()}", 0L)
    }

    fun saveTemperatureUnit(unit: String) {
        prefs.edit().putString("temperature_unit", unit).apply()
    }

    fun getTemperatureUnit(): String {
        return prefs.getString("temperature_unit", "metric") ?: "metric"
    }


}
