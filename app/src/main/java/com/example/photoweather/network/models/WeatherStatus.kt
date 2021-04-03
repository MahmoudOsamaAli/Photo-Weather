package com.example.photoweather.network.models

import com.google.gson.annotations.SerializedName

data class WeatherStatus(
    @SerializedName("main")
    var condition:String?,

    @SerializedName("description")
    var description:String?
)
