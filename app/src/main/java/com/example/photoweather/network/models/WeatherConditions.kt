package com.example.photoweather.network.models

import com.google.gson.annotations.SerializedName

data class WeatherConditions(
    @SerializedName("weather")
    var condition:ArrayList<WeatherStatus>?,

    @SerializedName("sys")
    var country:Country?,

    @SerializedName("main")
    var tempInfo:Temperature,

    @SerializedName("name")
    var cityName:String?
)
