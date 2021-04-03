package com.example.photoweather.network.models

import com.google.gson.annotations.SerializedName

data class Country(

    @SerializedName("country")
    var countryName:String?
)
