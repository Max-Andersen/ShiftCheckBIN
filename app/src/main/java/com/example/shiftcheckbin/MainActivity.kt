package com.example.shiftcheckbin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
        if (currentFragment == null) {
            val fragment = MainFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.frameContainer, fragment)
                .commit()
        }
    }
}