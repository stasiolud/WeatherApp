package com.example.weatherapp.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.storage.PreferencesManager

class SettingsFragment : Fragment() {

    private val options = listOf(
        "Odświeżaj co 15 sekund" to 15 * 1000L,
        "Odświeżaj co 1 minutę" to 60 * 1000L,
        "Odświeżaj co 3 godziny" to 3 * 60 * 60 * 1000L
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spinner = view.findViewById<Spinner>(R.id.cacheTimeSpinner)
        val refreshButton = view.findViewById<Button>(R.id.refreshButton)
        val prefs = PreferencesManager(requireContext())

        val labels = options.map { it.first }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val current = prefs.getCacheDuration()
        val selectedIndex = options.indexOfFirst { it.second == current }.takeIf { it != -1 } ?: 0
        spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedDuration = options[position].second
                prefs.saveCacheDuration(selectedDuration)

                (activity as? MainActivity)?.restartAutoRefresh()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        refreshButton.setOnClickListener {
            val lastCity = prefs.getLastUsedCity()
            if (!lastCity.isNullOrBlank()) {
                val mainActivity = activity as? MainActivity
                val viewModel = mainActivity?.viewModel

                viewModel?.fetchWeather(lastCity, force = true)

                viewModel?.weatherData?.observe(viewLifecycleOwner) { weather ->
                    if (weather != null) {
                        viewModel.fetchForecast(weather.coord.lat, weather.coord.lon, force = true)
                    }
                }

                val hasInternet =
                    com.example.weatherapp.utils.NetworkUtils.isInternetAvailable(requireContext())

                Toast.makeText(
                    requireContext(),
                    if (hasInternet) "Odświeżono dane dla: $lastCity" else "Brak połączenia z internetem – użyto danych lokalnych",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "Brak zapisanej lokalizacji", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val unitSpinner = view.findViewById<Spinner>(R.id.unitSpinner)
        val unitOptions = listOf("Celsjusz (°C)", "Fahrenheit (°F)")
        val unitAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitOptions)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = unitAdapter

        val currentUnit = prefs.getTemperatureUnit()
        val selectedUnitIndex = if (currentUnit == "imperial") 1 else 0
        unitSpinner.setSelection(selectedUnitIndex)

        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = if (position == 1) "imperial" else "metric"
                prefs.saveTemperatureUnit(selectedUnit)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

}
