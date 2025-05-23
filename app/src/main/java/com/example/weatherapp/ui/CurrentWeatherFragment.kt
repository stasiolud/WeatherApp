package com.example.weatherapp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.storage.PreferencesManager
import com.example.weatherapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class CurrentWeatherFragment : Fragment() {

    private val viewModel: WeatherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_current_weather, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val iconBackground = view.findViewById<FrameLayout>(R.id.weatherIconBackground)
        val cityText = view.findViewById<TextView>(R.id.cityNameTextView)
        val coordText = view.findViewById<TextView>(R.id.coordinatesTextView)
        val timeText = view.findViewById<TextView>(R.id.timeTextView)
        val tempText = view.findViewById<TextView>(R.id.temperatureTextView)
        val pressureText = view.findViewById<TextView>(R.id.pressureTextView)
        val descText = view.findViewById<TextView>(R.id.descriptionTextView)
        val iconImage = view.findViewById<ImageView>(R.id.weatherIconImageView)

        viewModel.weatherData.observe(viewLifecycleOwner) { data ->
            data?.let {
                cityText.text = it.name
                coordText.text = "${it.coord.lat}, ${it.coord.lon}"
                val prefs = PreferencesManager(requireContext())
                val unit = prefs.getTemperatureUnit()

                val temperature = it.main.temp
                val displayTemp = if (unit == "imperial") {
                    val fahrenheit = (temperature * 9 / 5) + 32
                    "${fahrenheit.toInt()}°F"
                } else {
                    "${temperature.toInt()}°C"
                }
                tempText.text = displayTemp
                pressureText.text = "${it.main.pressure} hPa"
                descText.text = it.weather[0].description

                val now = Date()
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeText.text = format.format(now)

                val iconUrl = "https://openweathermap.org/img/wn/${it.weather[0].icon}@2x.png"
                Glide.with(this).load(iconUrl).into(iconImage)

                iconBackground.visibility = View.VISIBLE
                cityText.visibility = View.VISIBLE
                coordText.visibility = View.VISIBLE
                tempText.visibility = View.VISIBLE
                pressureText.visibility = View.VISIBLE
                descText.visibility = View.VISIBLE
                timeText.visibility = View.VISIBLE
                iconImage.visibility = View.VISIBLE
            }
        }
        val prefs = PreferencesManager(requireContext())
        val lastCity = prefs.getLastUsedCity()

        if (!lastCity.isNullOrBlank() && viewModel.weatherData.value?.name != lastCity) {
            viewModel.fetchWeather(lastCity)
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