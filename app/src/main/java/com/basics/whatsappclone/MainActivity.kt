package com.basics.whatsappclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.basics.whatsappclone.adapter.ScreenSlidePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val context:Context = this@MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewPager.adapter =
            ScreenSlidePagerAdapter(this)
        TabLayoutMediator(tabs, viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "CHATS"
                    }
                    1 -> {
                        tab.text = "PEOPLE"
                    }
                }
            }).attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_setting ->{
                val intent = Intent(context,SettingsActivity::class.java)
              context.startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
