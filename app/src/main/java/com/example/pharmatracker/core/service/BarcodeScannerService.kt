package com.example.pharmatracker.core.service

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Interface for barcode scanning result callbacks
 */
interface BarcodeScannerListener {
    fun onBarcodeScanned(barcode: String)
    fun onScanError(error: Exception)
}

/**
 * Service for handling barcode scanning using CameraX and ML Kit
 */
class BarcodeScannerService(private val context: Context) {
    
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var barcodeScanner: BarcodeScanner
    private var listener: BarcodeScannerListener? = null
    private var isScanning = false
    
    init {
        // Configure barcode scanner options to detect common medication barcode formats
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()
        
        barcodeScanner = BarcodeScanning.getClient(options)
    }
    
    /**
     * Set the listener to receive scanning results
     */
    fun setListener(listener: BarcodeScannerListener) {
        this.listener = listener
    }
    
    /**
     * Start the barcode scanner with the given preview view and lifecycle owner
     */
    fun startScanner(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        if (isScanning) return
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Set up the preview use case
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                // Set up the image analyzer
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(cameraExecutor, BarcodeImageAnalyzer { barcode ->
                    // When a barcode is detected, stop scanning and notify the listener
                    if (!isScanning) return@BarcodeImageAnalyzer // Prevent duplicate scans
                    
                    isScanning = false // Pause scanning
                    
                    // Vibrate to indicate successful scan
                    vibrateDevice()
                    
                    // Notify the listener
                    listener?.onBarcodeScanned(barcode)
                })
                
                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                
                isScanning = true
                
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed: ${e.message}", e)
                listener?.onScanError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * Stop the barcode scanner
     */
    fun stopScanner() {
        isScanning = false
        try {
            // Get the camera provider and unbind all use cases
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scanner: ${e.message}", e)
        }
    }
    
    /**
     * Clean up resources
     */
    fun shutdown() {
        stopScanner()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
    
    /**
     * Vibrate the device to indicate a successful scan
     */
    private fun vibrateDevice() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
            Log.e(TAG, "Error vibrating device: ${e.message}", e)
        }
    }
    
    /**
     * Image analyzer class for processing camera frames and detecting barcodes
     */
    private inner class BarcodeImageAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
        
        override fun analyze(imageProxy: ImageProxy) {
            if (!isScanning) {
                imageProxy.close()
                return
            }
            
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                
                // Process the image for barcodes
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty() && isScanning) {
                            // Get the first barcode detected
                            val barcode = barcodes.first()
                            barcode.rawValue?.let { value ->
                                onBarcodeDetected(value)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Barcode scanning failed: ${e.message}", e)
                        listener?.onScanError(e)
                    }
                    .addOnCompleteListener {
                        // Close the image proxy when done
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
    
    companion object {
        private const val TAG = "BarcodeScannerService"
        
        @Volatile
        private var INSTANCE: BarcodeScannerService? = null
        
        fun getInstance(context: Context): BarcodeScannerService {
            return INSTANCE ?: synchronized(this) {
                val instance = BarcodeScannerService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}