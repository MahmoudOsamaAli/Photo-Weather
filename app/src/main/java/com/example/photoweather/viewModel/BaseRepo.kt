package com.example.photoweather.viewModel

import com.example.photoweather.network.api.ApiHelper
import com.example.photoweather.network.api.RetrofitBuilder
import com.example.photoweather.network.api.URLS
import com.example.photoweather.network.models.WeatherConditions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

object BaseRepo {
    private val apiHelper = ApiHelper(RetrofitBuilder.getApiService(URLS.weatherUrl))

    suspend fun getWeatherConditions(lat: Double, lon: Double):WeatherConditions{
        return CoroutineScope(Dispatchers.IO).async {
            apiHelper.getWeatherConditions(lat , lon )
        }.await()
    }


}