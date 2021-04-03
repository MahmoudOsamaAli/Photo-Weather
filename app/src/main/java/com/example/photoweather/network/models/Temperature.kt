package com.example.photoweather.network.models

import com.google.gson.annotations.SerializedName

data class Temperature(
    @SerializedName("temp")
    var currentTemperature: String
)
