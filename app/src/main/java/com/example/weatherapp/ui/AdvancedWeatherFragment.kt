package com.example.weatherapp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.storage.PreferencesManager
import com.example.weatherapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdvancedWeatherFragment : Fragment() {

    private val viewModel: WeatherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_advanced_weather, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val city = view.findViewById<TextView>(R.id.cityTextView)
        val coord = view.findViewById<TextView>(R.id.coordTextView)
        val temp = view.findViewById<TextView>(R.id.tempTextView)
        val feelsLike = view.findViewById<TextView>(R.id.feelsLikeTextView)
        val humidity = view.findViewById<TextView>(R.id.humidityTextView)
        val pressure = view.findViewById<TextView>(R.id.pressureTextView)
        val windSpeed = view.findViewById<TextView>(R.id.windSpeedTextView)
        val windDeg = view.findViewById<TextView>(R.id.windDegTextView)
        val clouds = view.findViewById<TextView>(R.id.cloudsTextView)
        val sunrise = view.findViewById<TextView>(R.id.sunriseTextView)
        val sunset = view.findViewById<TextView>(R.id.sunsetTextView)
        val description = view.findViewById<TextView>(R.id.descriptionTextView)

        viewModel.weatherData.observe(viewLifecycleOwner) { data ->
            data?.let {
                city.text = "Miejscowość: ${it.name}";
                coord.text = "Współrzędne: ${it.coord.lat}, ${it.coord.lon}"
                val prefs = PreferencesManager(requireContext())
                val unit = prefs.getTemperatureUnit()

                val tempValue = if (unit == "imperial") {
                    ((it.main.temp * 9 / 5) + 32).toInt().toString() + "°F"
                } else {
                    "${it.main.temp.toInt()}°C"
                }
                temp.text = "Temperatura: $tempValue"
                val feelsLikeValue = if (unit == "imperial") {
                    ((it.main.feels_like * 9 / 5) + 32).toInt().toString() + "°F"
                } else {
                    "${it.main.feels_like.toInt()}°C"
                }
                feelsLike.text = "Odczuwalna temperatura: ${feelsLikeValue}"
                humidity.text = "Wilgotność powietrza: ${it.main.humidity}%"
                pressure.text = "Ciśnienie: ${it.main.pressure} hPa"
                val windKmh = it.wind.speed * 3.6
                windSpeed.text = "Prędkość wiatru: ${"%.1f".format(windKmh)} km/h"
                windDeg.text = "Kierunek wiatru: ${it.wind.deg}°"
                clouds.text = "Zachmurzenie: ${it.clouds.all}%"
                description.text = "Opis pogody: ${it.weather[0].description}"
                val timezoneOffset = it.timezone

                val sunriseMillis = (it.sys.sunrise + timezoneOffset) * 1000L
                val sunsetMillis = (it.sys.sunset + timezoneOffset) * 1000L
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sunrise.text = "Wschód słońca: ${sdf.format(Date(sunriseMillis))}"
                sunset.text = "Zachód słońca: ${sdf.format(Date(sunsetMillis))}"

            }


        }
    }

    override fun onResume() {
        super.onResume()

        val mainActivity = activity as? MainActivity
        if (viewModel.usedOfflineFallback.value == true && mainActivity?.hasShownTabletOfflineToast == false) {
            Toast.makeText(
                requireContext(),
                "Brak internetu – dane mogą być nieaktualne",
                Toast.LENGTH_LONG
            ).show()
            mainActivity?.hasShownTabletOfflineToast = true
        }
    }


}
