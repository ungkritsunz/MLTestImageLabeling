package com.sunsosay.mltestimagelabeling

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private var arrText = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun init() {
        button.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun analyzeText(bitmap:Bitmap){
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
//        val detector = FirebaseVision.getInstance().cloudTextRecognizer
        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
            .setLanguageHints(listOf("en", "th"))
            .build()
        textRecognizer.processImage(image)
            .addOnSuccessListener { result ->
                val resultText = result.text
                for (block in result.textBlocks) {
                    val blockText = block.text
                    val blockConfidence = block.confidence
                    val blockLanguages = block.recognizedLanguages
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    for (line in block.lines) {
                        val lineText = line.text
                        val lineConfidence = line.confidence
                        val lineLanguages = line.recognizedLanguages
                        val lineCornerPoints = line.cornerPoints
                        val lineFrame = line.boundingBox
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementConfidence = element.confidence
                            val elementLanguages = element.recognizedLanguages
                            val elementCornerPoints = element.cornerPoints
                            val elementFrame = element.boundingBox
                        }
                    }
                }
                // Task completed successfully
            }
            .addOnFailureListener {
                it
                // Task failed with an exception
            }
    }

    private fun callFirebase(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
//        val labeler = FirebaseVision.getInstance().onDeviceImageLabeler
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.8f)
            .build()
        val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)

        labeler.processImage(image)
            .addOnSuccessListener { labels ->
                // Task completed successfully
                // ...
                for (label in labels) {
                    val text = label.text
                    val entityId = label.entityId
                    val confidence = label.confidence
                    arrText.add("$text\nconfidence : $confidence\n")
                }
                if (arrText.size > 0) {
                    var strConcat: String = ""
                    arrText.forEach { arr ->
                        strConcat += "$arr\n"
                    }
                    textView.text = strConcat
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                Log.e("failCall", e.localizedMessage, e)
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
//            callFirebase(imageBitmap)
            analyzeText(imageBitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

}
