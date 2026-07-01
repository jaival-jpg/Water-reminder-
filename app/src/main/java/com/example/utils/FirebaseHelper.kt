package com.example.utils

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirebaseHelper {
    private const val TAG = "FirebaseHelper"
    var isFirebaseAvailable = false
        private set

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Firebase initialized successfully with google-services.json configuration!")
            }
            isFirebaseAvailable = true
        } catch (e: Exception) {
            Log.e(TAG, "Standard Firebase initialization failed: ${e.message}. Retrying programmatically as fallback...")
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:648312947105:android:912b338dc0ae0f068f0be0")
                    .setProjectId("hydrated-717c8")
                    .setApiKey("AIzaSyCrKXftHo8WtJo-0Faswecte5Y4THLJS9k")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialized programmatically with fallback options!")
                isFirebaseAvailable = true
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback Firebase initialization also failed: ${fallbackEx.message}")
                isFirebaseAvailable = false
            }
        }
    }

    fun syncUserDetails(
        userId: String,
        name: String,
        dailyGoal: Int,
        cupSize: Int,
        weight: String,
        age: String
    ) {
        if (userId.isEmpty()) {
            Log.w(TAG, "Cannot sync user details: User ID is empty.")
            return
        }
        
        try {
            val db = FirebaseFirestore.getInstance()
            val userMap = hashMapOf(
                "userId" to userId,
                "name" to name,
                "dailyGoal" to dailyGoal,
                "cupSize" to cupSize,
                "weight" to weight,
                "age" to age,
                "lastUpdated" to System.currentTimeMillis()
            )

            db.collection("users").document(userId)
                .set(userMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "User details synced successfully to Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to sync user details to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync exception: ${e.message}")
        }
    }

    fun fetchUserDetails(
        userId: String,
        onSuccess: (name: String, dailyGoal: Int, cupSize: Int, weight: String, age: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (userId.isEmpty() || !isFirebaseAvailable) {
            onFailure(Exception("Firebase not initialized or userId empty"))
            return
        }

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        val dailyGoal = document.getLong("dailyGoal")?.toInt() ?: 3000
                        val cupSize = document.getLong("cupSize")?.toInt() ?: 200
                        val weight = document.getString("weight") ?: ""
                        val age = document.getString("age") ?: ""
                        onSuccess(name, dailyGoal, cupSize, weight, age)
                    } else {
                        onFailure(Exception("Document does not exist"))
                    }
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
