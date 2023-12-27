package ru.startandroid.hotels.account

import java.time.LocalDate

data class Booking(
    val id: String?,
    val hotelName: String?,
    val personQuantity: Int,
    val checkIn: LocalDate?,
    val checkOut: LocalDate?,
    val userId: String?,
    val hotelId: String?
)
