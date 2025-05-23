package com.example.weatherapp.data.model

data class ForecastResponse(
    val list: List<ForecastEntry>,
    val city: City
)

data class ForecastEntry(
    val dt: Long,
    val main: MainForecast,
    val weather: List<ForecastWeather>
)

data class MainForecast(
    val temp: Double
)

data class ForecastWeather(
    val icon: String,
    val description: String
)

data class City(
    val name: String,
    val timezone: Int
)
