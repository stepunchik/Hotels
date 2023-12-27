package ru.startandroid.hotels.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.startandroid.hotels.databinding.BookingItemBinding

class BookingAdapter(private var bookingList: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {
    inner class BookingViewHolder(var binding: BookingItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    var onDeleteItemClick: ((Booking) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = BookingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        with(holder.binding) {
            bookingItemHotelName.text = booking.hotelName
            bookingItemBookingDates.text = buildString {
                append(booking.checkIn.toString())
                append(" - ")
                append(booking.checkOut.toString())
            }
            bookingItemPersonQuantity.text = booking.personQuantity.toString()

            bookingItemDeleteButton.setOnClickListener {
                onDeleteItemClick?.invoke(booking)
            }
        }
    }
}