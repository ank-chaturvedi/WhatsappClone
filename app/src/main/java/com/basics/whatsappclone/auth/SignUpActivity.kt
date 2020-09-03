package com.basics.whatsappclone.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.basics.whatsappclone.MainActivity
import com.basics.whatsappclone.R
import com.basics.whatsappclone.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var downloadUri:String
    lateinit var sharedPreferences: SharedPreferences




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)



        userImageView.setOnClickListener {

            checkPermissionForImage()
        }


        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            val name:String = nameEt.text.toString()
            if(name.isNullOrEmpty()){
                Toast.makeText(this,"Text field can't be empty",Toast.LENGTH_SHORT).show()

            } else if(!::downloadUri.isInitialized){
                Toast.makeText(this,"Image field can't be empty",Toast.LENGTH_SHORT).show()

        } else{
                sharedPreferences.edit().putString(getString(R.string.user_name),name).apply()
                sharedPreferences.edit().putString(getString(R.string.user_image_url),downloadUri).apply()
                sharedPreferences.edit().putString(getString(R.string.user_thumbed_image_url),downloadUri).apply()
                sharedPreferences.edit().putString(getString(R.string.auth_uid),auth.uid.toString()).apply()

                val user = User(
                    name,
                    downloadUri,
                    downloadUri,
                    auth.uid.toString()
                )
                val ref = database.collection("users").add(user)

                ref.addOnSuccessListener {
                    sharedPreferences.edit().putBoolean(getString(R.string.is_logged),true).apply()
                    startActivity(Intent(this,
                        MainActivity::class.java))
                    finish()
                } . addOnFailureListener{
                    nextBtn.isEnabled = true
                }
            }

        }



    }

    private fun checkPermissionForImage() {


        if((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_DENIED
            && (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_DENIED){


            val permissionRead = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


            requestPermissions(permissionRead,
            1001)

            requestPermissions(permissionWrite,
            1002)



        } else{
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            startActivityForResult(intent,
            1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == 1000){

            data?.data?.let {
                userImageView.setImageURI(it)

                uploadImage(it)
            }
        }
    }

    private fun uploadImage(it: Uri) {

        val ref = storage.reference.child("uploads/"+auth.uid.toString())

        val uploadTask = ref.putFile(it)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>> {task ->
            if(!task.isSuccessful()){
                task.exception?.let {
                    throw it
                }
            }

            return@Continuation ref.downloadUrl

        }).addOnCompleteListener { task ->
            nextBtn.isEnabled = true
            if(task.isSuccessful){
                downloadUri = task.result.toString()
                Log.i("uri",downloadUri)
            }else{

            }
        }.addOnFailureListener {

        }




    }
}
