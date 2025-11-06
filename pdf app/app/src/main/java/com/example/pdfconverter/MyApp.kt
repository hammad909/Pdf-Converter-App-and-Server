package com.example.pdfconverter

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            System.loadLibrary("opencv_java4")
            Log.d("OpenCV", "Native OpenCV library loaded ✅")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("OpenCV", "Failed to load OpenCV native lib ❌", e)
        }

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed ❌")
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully ✅")
        }
    }
}