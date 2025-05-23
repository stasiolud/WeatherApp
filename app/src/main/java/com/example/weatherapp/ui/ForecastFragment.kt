package com.example.weatherapp.ui

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.storage.PreferencesManager
import com.example.weatherapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class ForecastFragment : Fragment() {

    private val viewModel: WeatherViewModel by activityViewModels()

    private val containerIds = listOf(
        R.id.forecastItem1,
        R.id.forecastItem2,
        R.id.forecastItem3,
        R.id.forecastItem4,
        R.id.forecastItem5
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_forecast, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.weatherData.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                viewModel.fetchForecast(it.coord.lat, it.coord.lon)
            }
        }

        viewModel.forecastData.observe(viewLifecycleOwner) { forecast ->
            forecast?.let {
                val formatter = SimpleDateFormat("EEEE", Locale("pl"))
                val calendar = Calendar.getInstance()

                val dailyEntries = it.list
                    .filter { entry ->
                        calendar.timeInMillis = entry.dt * 1000L
                        calendar.get(Calendar.HOUR_OF_DAY) == 12
                    }
                    .distinctBy { entry ->
                        val date = Date(entry.dt * 1000L)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    }
                    .take(5)

                val forecastItems = dailyEntries.map { entry ->
                    val date = Date(entry.dt * 1000L)
                    val dayOfWeek = formatter.format(date).replaceFirstChar { it.uppercase() }
                    val unit = PreferencesManager(requireContext()).getTemperatureUnit()
                    val temp = if (unit == "imperial") {
                        ((entry.main.temp * 9 / 5) + 32).toInt().toString() + "°F"
                    } else {
                        "${entry.main.temp.toInt()}°C"
                    }
                    ForecastItem(
                        day = dayOfWeek,
                        temperature = temp,
                        iconCode = entry.weather[0].icon,
                        description = entry.weather[0].description
                    )
                }

                containerIds.forEachIndexed { index, frameId ->
                    val container = view?.findViewById<FrameLayout>(frameId)
                    container?.removeAllViews()

                    if (index < forecastItems.size && container != null) {
                        val item = forecastItems[index]
                        val itemView =
                            layoutInflater.inflate(R.layout.forecast_item_static, container, false)

                        itemView.findViewById<TextView>(R.id.dayTextView).text = item.day
                        itemView.findViewById<TextView>(R.id.tempTextView).text = item.temperature

                        val iconUrl = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png"
                        Glide.with(this).load(iconUrl)
                            .into(itemView.findViewById(R.id.iconImageView))

                        container.addView(itemView)
                    }
                }
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
