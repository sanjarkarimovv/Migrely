package com.migrely.app.datasource.billing.datasource

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * The simulated store's sheet as a dialog over [currentActivity]. It blocks its caller, a
 * background thread, until an option is chosen, the dialog is dismissed or the activity goes away,
 * which counts as no choice.
 */
class DialogSimulatedSheet(private val currentActivity: () -> Activity?) : SimulatedSheet {
    override fun choose(title: String, options: List<String>): Int? = runBlocking {
        val answer = CompletableDeferred<Int?>()
        withContext(Dispatchers.Main) {
            val activity = currentActivity()
            if (activity == null) {
                answer.complete(null)
                return@withContext
            }
            val dialog = MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setItems(options.toTypedArray()) { _, which -> answer.complete(which) }
                .setOnDismissListener { answer.complete(null) }
                .show()
            (activity as? LifecycleOwner)?.lifecycle?.addObserver(
                LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        dialog.dismiss()
                        answer.complete(null)
                    }
                }
            )
        }
        answer.await()
    }
}
