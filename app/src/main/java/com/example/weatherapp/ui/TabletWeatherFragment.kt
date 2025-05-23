package com.example.weatherapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weatherapp.R

class TabletWeatherFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tablet_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction()
            .replace(R.id.forecastTabletContainer, ForecastFragment())
            .replace(R.id.currentTabletContainer, CurrentWeatherFragment())
            .replace(R.id.advancedTabletContainer, AdvancedWeatherFragment())
            .commit()
    }
}
