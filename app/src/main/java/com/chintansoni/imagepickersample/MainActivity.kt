package com.chintansoni.imagepickersample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.chintansoni.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val imagePicker = ImagePicker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onImageAsFileClick(view: View) {
        imagePicker.show(this) { image ->
            Log.e("MainActivity", "Received Result")
            Glide
                .with(this)
                .load(image.asFile())
                .into(imageView)
        }.onFailure {
            Log.e("MainActivity", it.message)
        }
    }

    fun onImageAsUriClick(view: View) {
        imagePicker.show(this) { image ->
            Glide
                .with(this)
                .load(image.asUri())
                .into(imageView)
        }.onFailure {
            Log.e("MainActivity", it.message)
        }
    }

    fun onImageAsBitmapClick(view: View) {
        imagePicker.show(this) { image ->
            image.asBitmap {
                Glide
                    .with(this)
                    .load(it)
                    .into(imageView)
            }
        }.onFailure {
            Log.e("MainActivity", it.message)
        }
    }
}
