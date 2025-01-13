package com.example.madcamp3jhsj.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Food(
    var name: String,
    var buyDate: String,
    var expirationDate: String,
    var thumbnail: Uri
) : Parcelable
