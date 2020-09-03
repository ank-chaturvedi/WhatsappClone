package com.basics.whatsappclone

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import com.basics.whatsappclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.nameTv
import kotlinx.android.synthetic.main.activity_settings.view.*
import kotlinx.android.synthetic.main.popup_change_details.view.*

class SettingsActivity : AppCompatActivity() {

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val database by lazy {
        FirebaseFirestore.getInstance()
    }
    private var user: User? = null
    lateinit var sharPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharPref = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)

        database.collection("users").whereEqualTo("uid", auth.uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents)
                    user = document.toObject(User::class.java)

                setDetails()
                progressBar.visibility = View.GONE
                rlLayout.visibility = View.GONE

            }

        val view = RelativeLayout(this)

        editNameImg.setOnClickListener {
            showPopUpWindow(view,this)
        }


    }

    private fun setDetails() {
        if (user == null)
            Toast.makeText(this, "user is null", Toast.LENGTH_SHORT).show()
        val imageUrl = sharPref.getString(getString(R.string.user_thumbed_image_url), null)
        Picasso.get().load(imageUrl)
            .error(R.drawable.defaultuseravatar)
            .placeholder(R.drawable.defaultuseravatar)
            .into(userImageView)

        nameTv.text = user?.name
        phoneNumberTv.text = sharPref.getString(getString(R.string.mobile_no), null)
        statusTv.text = user?.status
    }


    private fun showPopUpWindow(view:View,context:Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        Log.i("some erro","error")
        val popUpView = inflater.inflate(R.layout.popup_change_details, null)

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popUpView,width, height, focusable)
        popupWindow.setBackgroundDrawable(getDrawable(R.color.teal200))

        popupWindow.showAtLocation(view,Gravity.BOTTOM,0,0)

        popUpView.saveBtn.setOnClickListener {
            nameTv.text = popUpView.detailsEt.text.toString()
            popupWindow.dismiss()
        }
    }


}
