package com.example.fan_zone.models

data class GeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    constructor() : this(0.0, 0.0)
}