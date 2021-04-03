package com.example.photoweather.network.api

class ApiHelper(private val apiService: ApiService) {


    suspend fun getWeatherConditions(lat:Double , lon:Double) = apiService.getWeather(lat , lon , URLS.API_KEY , URLS.UNIT_KEY)

}