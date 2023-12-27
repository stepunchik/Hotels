package ru.startandroid.hotels.home

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ru.startandroid.hotels.databinding.HotelItemBinding

class HotelAdapter(private var hotelList: List<Hotel>) :
    RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {
    private lateinit var storageReference: StorageReference
    var TAG = "HotelAdapter"

    var onItemClick: ((Hotel) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(filteredList: List<Hotel>) {
        this.hotelList = filteredList
        notifyDataSetChanged()
    }

    inner class HotelViewHolder(var binding: HotelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val binding = HotelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotelViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return hotelList.size
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotelList[position]
        with(holder.binding) {
            itemViewHotelName.text = hotel.name
            itemViewGrade.text = hotel.grade.toString()
            itemViewFeedbackQuantity.text = buildString {
                append(hotel.feedbackQuantity)
                append(" отзывов")
            }
            itemViewCost.text = buildString {
                append(hotel.cost)
                append("₽ за ночь")
            }

            storageReference =
                FirebaseStorage.getInstance().reference.child("images/" + hotel.image)
            storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                Glide.with(itemViewItem).load(uri).into(itemViewHotelImage)
            }

            itemViewItem.setOnClickListener {
                onItemClick?.invoke(hotel)
            }
        }
    }
}
