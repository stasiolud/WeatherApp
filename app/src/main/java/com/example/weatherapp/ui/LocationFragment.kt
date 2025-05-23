package com.example.weatherapp.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.ui.adapter.LocationAdapter
import com.example.weatherapp.viewmodel.WeatherViewModel

class LocationFragment : Fragment() {

    private val viewModel: WeatherViewModel by activityViewModels()
    private lateinit var adapter: LocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val locationEditText = view.findViewById<EditText>(R.id.locationEditText)
        val addLocationButton = view.findViewById<Button>(R.id.addLocationButton)
        val locationListView = view.findViewById<ListView>(R.id.locationListView)

        adapter = LocationAdapter(
            requireContext(),
            mutableListOf(),
            onDelete = { city -> viewModel.removeFavoriteCity(city) },
            onClick = { city ->
                viewModel.setLastUsedCity(city)
                viewModel.fetchWeather(city)
                val isTablet = resources.getBoolean(R.bool.isTablet)
                if (isTablet) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TabletWeatherFragment()).commit()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CurrentWeatherFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        )

        locationListView.adapter = adapter

        viewModel.favoriteCities.observe(viewLifecycleOwner) { cities ->
            adapter.updateData(cities)
        }

        addLocationButton.setOnClickListener {
            val city = locationEditText.text.toString()
            if (city.isNotBlank()) {
                viewModel.fetchWeatherTest(city) { success, errorMessage ->
                    if (success) {
                        viewModel.addFavoriteCity(city)
                        locationEditText.text.clear()
                    } else {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Wpisz nazwę miasta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}