package com.example.pharmatracker.core.util

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

/**
 * Extension functions to simplify common operations
 */

/**
 * Simplified LiveData observation
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

/**
 * Extension function to show toast easily
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension function to show toast with a resource string
 */
fun Context.showToast(@StringRes stringResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, getString(stringResId), duration).show()
}

/**
 * Extension function to show a Snackbar
 */
fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    
    if (actionText != null && action != null) {
        snackbar.setAction(actionText) { action() }
    }
    
    snackbar.show()
}

/**
 * Extension function to get a color from resources
 */
fun Context.getColorCompat(@ColorRes colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

/**
 * Extension function to set visibility to VISIBLE
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Extension function to set visibility to GONE
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Extension function to set visibility to INVISIBLE
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Extension function to toggle visibility between VISIBLE and GONE
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Extension function to check if view is visible
 */
fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

/**
 * Extension function to set visibility based on a condition
 */
fun View.setVisibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}