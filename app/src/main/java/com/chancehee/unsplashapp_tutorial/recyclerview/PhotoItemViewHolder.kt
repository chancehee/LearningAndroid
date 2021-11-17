package com.chancehee.unsplashapp_tutorial.recyclerview

import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chancehee.unsplashapp_tutorial.App
import com.chancehee.unsplashapp_tutorial.R
import com.chancehee.unsplashapp_tutorial.model.Photo
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG
import kotlinx.android.synthetic.main.layout_photo_item.view.*

class PhotoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val photoImageView = itemView.photo_image
    private val photoCreatedAtText = itemView.created_at_text
    private val photoLikesCountText = itemView.likes_count_text

    // 데이터와 뷰를 묶는다.
    fun bindWithView(photoItem: Photo){
        Log.d(TAG, "PhotoItemViewHolder - bindWithView() is called")

        photoCreatedAtText.text = photoItem.createAt
        photoLikesCountText.text = photoItem.likeCount.toString()

        // 이미지를 설정한다.
        Glide.with(App.instance)
            .load(photoItem.thumbnail)
            .placeholder(R.drawable.ic_baseline_add_24)
            .into(photoImageView)

    }


}