package com.basics.whatsappclone.model

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.basics.whatsappclone.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_list.view.*



class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){


    fun bind(user:User,context: Context) = with(itemView){

            timeTv.visibility = View.GONE
            countTv.visibility = View.GONE




        titleTv.text = user.name
        subTitleTv.text = user.status

        Picasso.get().load(user.thumbImage).placeholder(R.drawable.defaultuseravatar).error(R.drawable.defaultuseravatar).into(userImageView)


        setOnClickListener {
            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra(UID,user.uid)
            intent.putExtra(NAME,user.name)
            intent.putExtra(IMAGE,user.thumbImage)

            context.startActivity(intent)


        }



        userImageView.setOnClickListener {

        }


    }





}