package com.udacity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)


        fileName_textView.text = intent.getStringExtra("fileName")
        status_textView.text = intent.getStringExtra("status")
        when (intent.getStringExtra("status")) {
            getString(R.string.success) -> {
                fileName_textView.setTextColor(getColor(R.color.colorPrimaryDark))
                status_textView.setTextColor(getColor(R.color.colorPrimaryDark))
            }
            getString(R.string.fail) -> status_textView.setTextColor(Color.RED)
        }
        OK_button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
