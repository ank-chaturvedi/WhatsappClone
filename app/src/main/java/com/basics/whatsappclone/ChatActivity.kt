package com.basics.whatsappclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.basics.whatsappclone.adapter.ChatAdapter
import com.basics.whatsappclone.model.Inbox
import com.basics.whatsappclone.model.User
import com.basics.whatsappclone.util.KeyboardVisibilityUtil
import com.basics.whatsappclone.util.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val UID = "uid"
const val NAME = "name"
const val IMAGE = "image"

class ChatActivity : AppCompatActivity() {
    private val friendId: String by lazy {
        intent.getStringExtra(UID)
    }
    private val name: String by lazy {
        intent.getStringExtra(NAME)
    }
    private val image: String by lazy {
        intent.getStringExtra(IMAGE)
    }
    private lateinit var mCurrentUid: String
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser: User
    lateinit var chatAdapter: ChatAdapter

    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()
    private val mLinearLayout: LinearLayoutManager by lazy { LinearLayoutManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
       supportActionBar?.title = ""
        backNavigationClickListener()

        keyboardVisibilityHelper = KeyboardVisibilityUtil(rootView) {
            msgRv.scrollToPosition(mutableItems.size - 1)
        }
        val pref = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)
        mCurrentUid = pref.getString(getString(R.string.auth_uid),"uid").toString()
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid",mCurrentUid).get()
            .addOnSuccessListener {documents ->
                for(document in documents)
                currentUser = document.toObject(User::class.java)
            }.addOnFailureListener {
                Toast.makeText(this,"error occurred",Toast.LENGTH_SHORT).show()
            }


        chatAdapter = ChatAdapter(mutableItems, mCurrentUid)

        msgRv.apply {
            layoutManager = mLinearLayout
            adapter = chatAdapter
        }

        nameTv.text = name
        Picasso.get().load(image).into(userImgView)

        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
        swipeToLoad.setOnRefreshListener {
//
        }


        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if (it.isNotEmpty()) {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

        listenMessages() { msg, update ->
            if (update) {
                updateMessage(msg)
            } else {
                addMessage(msg)
            }
        }

        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }
        updateReadCount()
    }

    private fun backNavigationClickListener() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
//    }

    private fun updateReadCount() {
        getInbox(mCurrentUid, friendId).child("count").setValue(0)
    }

    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId).child(id).updateChildren(mapOf("liked" to status))
    }

    private fun addMessage(event: Message) {
        val eventBefore = mutableItems.lastOrNull()

        // Add date header if it's a different day
        if ((eventBefore != null
                    && !eventBefore.sentAt.isSameDayAs(event.sentAt))
            || eventBefore == null
        ) {
            mutableItems.add(
                DateHeader(
                    event.sentAt, this
                )
            )
        }
        mutableItems.add(event)

        chatAdapter.notifyItemInserted(mutableItems.size)
        msgRv.scrollToPosition(mutableItems.size + 1)
    }

    private fun updateMessage(msg: Message) {
        val position = mutableItems.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        mutableItems[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun listenMessages(newMsg: (msg: Message, update: Boolean) -> Unit) {
        getMessages(friendId)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(data: DataSnapshot, p1: String?) {
                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, true)
                }

                override fun onChildAdded(data: DataSnapshot, p1: String?) {
                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, false)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

            })

    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key
        checkNotNull(id) { "Cannot be null" }
        val msgMap = Message(msg, mCurrentUid, id)
        getMessages(friendId).child(id).setValue(msgMap)
        updateLastMessage(msgMap, mCurrentUid)
    }

    private fun updateLastMessage(message: Message, mCurrentUid: String) {
        val inboxMap = Inbox(
            message.msg,
            friendId,
            name,
            image,
            message.sentAt,
            0
        )

        getInbox(mCurrentUid, friendId).setValue(inboxMap)

        getInbox(friendId, mCurrentUid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.getValue(Inbox::class.java)
                inboxMap.apply {
                    from = message.senderId
                    name = currentUser.name
                    image = currentUser.thumbImage
                    count = 1
                }
                if (value?.from == message.senderId) {
                    inboxMap.count = value.count + 1
                }
                getInbox(friendId, mCurrentUid).setValue(inboxMap)
            }

        })
    }


    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")


    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }

    override fun onResume() {
        super.onResume()
        rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(UID, id)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)

            return intent
        }
    }
}