package com.npetrov.morsecodeflash

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.npetrov.morsecodeflash.databinding.ActivityMainBinding
import com.npetrov.morsecodeflash.fragments.Characters
import com.npetrov.morsecodeflash.fragments.SendMessage

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(SendMessage())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.characters -> replaceFragment(Characters())
                R.id.send_message -> replaceFragment(SendMessage())
                else -> {

                }
            }
            true
        }

    }

    private fun replaceFragment (fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}