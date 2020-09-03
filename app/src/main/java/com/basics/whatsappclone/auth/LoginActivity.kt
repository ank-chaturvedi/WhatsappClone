package com.basics.whatsappclone.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.basics.whatsappclone.MainActivity
import com.basics.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneNo: String
    private lateinit var countryCode: String


    lateinit var sharedPreferences: SharedPreferences

    val auth by lazy {
        FirebaseAuth.getInstance()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)

        val isLogged = sharedPreferences.getBoolean(getString(R.string.is_logged),false)


        if(auth.uid != null){

            openMainActivity()

        }

        //add hint assignment
        phoneNumberEt.addTextChangedListener {
            nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }



        nextBtn.setOnClickListener {

            notifyUser()


        }
    }



    private fun notifyUser() {
        countryCode = ccp.selectedCountryCodeWithPlus

        phoneNo = countryCode + phoneNumberEt.text.toString()



        MaterialAlertDialogBuilder(this).setTitle("confirm").setMessage(
            "We will be " +
                    "verifying the phone number:$phoneNo \n" +
                    "Is this ok,or would you like to edit the number"
        ).setPositiveButton("Ok"
        ) { _, _ ->
            opeOtpActivity()

        }.setNegativeButton("Edit") { dialog, _ ->
            dialog.dismiss()
        }.setCancelable(false)
            .create().show()


    }

    private fun opeOtpActivity() {
        val i = Intent(this, OtpActivity::class.java)
        i.putExtra(PHONE_NUMBER,phoneNo)
        startActivity(i)

        finish()


    }


    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}
