package com.basics.whatsappclone.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.basics.whatsappclone.MainActivity
import com.basics.whatsappclone.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit


const val PHONE_NUMBER = "phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    var phoneNumber: String? = null

    lateinit var countDownTimer: CountDownTimer

    private lateinit var progressDialog: ProgressDialog

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        if(auth.uid != null){

            openMainActivity()

        }

        progressDialog = onCreateProgressDialog("Sending a verification code", false)
        progressDialog.show()

        initViews()


        startAuthentication()


    }

    private fun startAuthentication() {
        showTimer(60000)


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber!!, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks


    }

    private fun showTimer(milSecondFutureTime: Long) {
        resendCodeBtn.isEnabled = false

        countDownTimer = object : CountDownTimer(milSecondFutureTime, 1000) {
            override fun onFinish() {
                resendCodeBtn.isEnabled = true

                counterTxt.isVisible = false
            }

            override fun onTick(millisUntilFinished: Long) {
                counterTxt.isVisible = true

                counterTxt.text = "Seconds Remaining: " + millisUntilFinished / 1000
            }

        }.start()


    }

    override fun onDestroy() {
        if (countDownTimer != null)
            countDownTimer.cancel()
        super.onDestroy()
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        vrfyTxt.text = getString(R.string.verfiy_number, phoneNumber)

        setSpannableString()

        verificationCodeBtn.setOnClickListener(this)
        resendCodeBtn.setOnClickListener(this)


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if (progressDialog.isShowing)
                    progressDialog.dismiss()

                if (!credential.smsCode.isNullOrEmpty())
                    otpEt.setText(credential.smsCode)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                if (progressDialog.isShowing)
                    progressDialog.dismiss()


                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.


                // Save verification ID and resending token so we can use them later
                if (progressDialog.isShowing)
                    progressDialog.dismiss()

                storedVerificationId = verificationId
                resendToken = token


            }
        }


    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()

        var mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    sharedPreferences = getSharedPreferences(getString(R.string.shared_preference),
                        Context.MODE_PRIVATE)

                    sharedPreferences.edit().putString(getString(R.string.mobile_no),phoneNumber).apply()
                    startActivity(Intent(this,
                        SignUpActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this, "some error ocurred", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber))

        val spannable = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }

            override fun onClick(widget: View) {
                startLoginActivity()
            }

        }

        span.setSpan(spannable, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        msgTxt.movementMethod = LinkMovementMethod.getInstance()
        msgTxt.text = span


    }

    private fun startLoginActivity() {
        // also add intent.flags so that there is nothing remains in call stack
        startActivity(Intent(this, LoginActivity::class.java))
        finish()

    }

    override fun onBackPressed() {

    }


    private fun onCreateProgressDialog(msg: String, cancelable: Boolean): ProgressDialog {

        val progressDialog = ProgressDialog(this)

        progressDialog.setMessage(msg)
        progressDialog.setCancelable(cancelable)


        return progressDialog
    }

    override fun onClick(v: View?) {
        when (v) {
            verificationCodeBtn -> {

                progressDialog = onCreateProgressDialog("Please wait......", false)
                progressDialog.show()

                val code = otpEt.text.toString()

                print("code"+code)


                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)


                signInWithPhoneAuthCredential(credential)


            }

            resendCodeBtn -> {

                progressDialog = onCreateProgressDialog("Sending verification code...", false)
                progressDialog.show()


                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber!!, // Phone number to verify
                    60, // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    this, // Activity (for callback binding)
                    callbacks,
                    resendToken
                ) // OnVerificationStateChangedCallbacks


            }

        }
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
