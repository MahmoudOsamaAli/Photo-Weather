package com.example.photoweather.network.api

import com.example.photoweather.network.models.WeatherConditions
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @GET("weather")
    suspend fun getWeather(@Query("lat")lat:Double
                           ,@Query("lon") lon:Double
                           ,@Query("appid") apiKey:String
                           ,@Query("units") unit:String)
    : WeatherConditions

}