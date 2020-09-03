package com.basics.whatsappclone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basics.whatsappclone.EmptyViewHolder
import com.basics.whatsappclone.R
import com.basics.whatsappclone.model.User
import com.basics.whatsappclone.model.UserViewHolder
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_people.view.*

private val NORMAL_VIEW_HOLDER  = 2
private val DELETED_VIEW_HOLDER = 1

class PeopleFragment : Fragment() {

    lateinit var mAdapter:FirestorePagingAdapter<User,RecyclerView.ViewHolder>

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance().collection("users").orderBy("name",Query.Direction.DESCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUpAdapter()
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    private fun setUpAdapter() {
        val config = PagedList.Config.Builder()
                                    .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(10)
            .build()


        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database,config,User::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<User,RecyclerView.ViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                return when(viewType){
                    NORMAL_VIEW_HOLDER ->UserViewHolder(layoutInflater.inflate(R.layout.item_list,parent,false))
                    else -> EmptyViewHolder(layoutInflater.inflate(R.layout.item_empty,parent,false))
                }



            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)!!.toObject(User::class.java)

                return if(auth.uid.toString() == item!!.uid){
                        DELETED_VIEW_HOLDER
                }else{
                    NORMAL_VIEW_HOLDER
                }


            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if(holder is UserViewHolder){
                holder.bind(model,requireContext())
                }else{

                }
            }


            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)

                when (state) {
                    LoadingState.LOADING_INITIAL ->{

                    }
                    LoadingState.LOADING_MORE ->{

                    }
                    LoadingState.LOADED ->{

                    }
                    LoadingState.FINISHED ->{

                    }

                    LoadingState.ERROR ->{

                    }
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with(view.recyclerView){
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

}
