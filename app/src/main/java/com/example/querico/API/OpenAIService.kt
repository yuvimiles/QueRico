package com.example.querico.API

import android.os.Build
import android.util.Log
import com.example.querico.BuildConfig
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class OpenAIService {
    companion object {
        private const val TAG = "OpenAIService"
        private const val API_KEY = "OPENAI_API_KEY"
        private const val INITIAL_DELAY = 1000L
        private const val MAX_RETRIES = 3
    }

    data class ChatMessage(
        val role: String,
        val content: String
    )

    data class ChatCompletionRequest(
        val model: String = "gpt-3.5-turbo",
        val messages: List<ChatMessage>,
        val temperature: Double = 0.7
    )

    data class ChatCompletionResponse(
        val id: String,
        val choices: List<Choice>
    )

    data class Choice(
        val index: Int,
        val message: ChatMessage,
        @SerializedName("finish_reason") val finishReason: String
    )

    interface OpenAIAPI {
        @POST("v1/chat/completions")
        suspend fun createChatCompletion(
            @Header("Authorization") authorization: String,
            @Body requestBody: ChatCompletionRequest
        ): Response<ChatCompletionResponse>
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val api = retrofit.create(OpenAIAPI::class.java)

    private var lastRequestTime = 0L


    suspend fun analyzeRestaurant(restaurantName: String, content: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting restaurant analysis for: $restaurantName")

                val currentTime = System.currentTimeMillis()
                val timeSinceLastRequest = currentTime - lastRequestTime

                if (lastRequestTime > 0 && timeSinceLastRequest < INITIAL_DELAY) {
                    val waitTime = INITIAL_DELAY - timeSinceLastRequest
                    Log.d(TAG, "Waiting $waitTime ms before sending next request to avoid rate limits")
                    delay(waitTime)
                }

                val messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "You are a helpful assistant that analyzes restaurants based on name and reviews."
                    ),
                    ChatMessage(
                        role = "user",
                        content = """
                            Analyze this restaurant:
                            
                            Restaurant Name: $restaurantName
                            Review/Content: $content
                            
                            Please provide:
                            1. Cuisine type (likely based on name or review)
                            2. Price range estimate (if mentioned in review)
                            3. Notable features or dishes (as mentioned in review)
                            4. Dining experience or atmosphere (as inferred)
                            
                            Format your response in a clean, readable way without ratings or subjective evaluations.
                        """.trimIndent()
                    )
                )

                val request = ChatCompletionRequest(
                    messages = messages
                )

                var retryCount = 0
                var retryDelay = INITIAL_DELAY

                while (retryCount <= MAX_RETRIES) {
                    try {
                        Log.d(TAG, "Sending request to OpenAI API (attempt ${retryCount + 1})")
                        lastRequestTime = System.currentTimeMillis()

                        val response = api.createChatCompletion("Bearer $API_KEY", request)

                        // בדיקת התגובה
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null && responseBody.choices.isNotEmpty()) {
                                val analysisText = responseBody.choices[0].message.content
                                Log.d(TAG, "Received analysis from OpenAI")
                                return@withContext analysisText
                            } else {
                                Log.e(TAG, "Empty response or no choices")
                                return@withContext "Could not analyze restaurant information. Please try again later."
                            }
                        } else if (response.code() == 429) {
                            Log.w(TAG, "Rate limit exceeded (429). Retrying after delay.")
                            retryCount++

                            if (retryCount <= MAX_RETRIES) {
                                delay(retryDelay)
                                retryDelay *= 2
                            } else {
                                Log.e(TAG, "Max retry attempts reached.")
                                return@withContext createFallbackAnalysis(restaurantName, content)
                            }
                        } else {
                            // שגיאה אחרת
                            val errorBody = response.errorBody()?.string()
                            var errorMessage = "Error connecting to analysis service: ${response.code()} - ${response.message()}"

                            if (!errorBody.isNullOrEmpty()) {
                                try {
                                    val jsonObject = JsonParser.parseString(errorBody).asJsonObject
                                    if (jsonObject.has("error")) {
                                        val error = jsonObject.getAsJsonObject("error")
                                        val message = error.get("message")?.asString ?: ""
                                        errorMessage = "Analysis service error: $message"
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing error response", e)
                                }
                            }

                            Log.e(TAG, errorMessage)
                            return@withContext createFallbackAnalysis(restaurantName, content)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception during API call", e)
                        retryCount++

                        if (retryCount <= MAX_RETRIES) {
                            delay(retryDelay)
                            retryDelay *= 2
                        } else {
                            return@withContext createFallbackAnalysis(restaurantName, content)
                        }
                    }
                }

                return@withContext createFallbackAnalysis(restaurantName, content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in restaurant analysis", e)
                return@withContext createFallbackAnalysis(restaurantName, content)
            }
        }
    }

    /**
     * יוצר ניתוח חלופי מקומי כאשר ה-API אינו זמין
     */
    private fun createFallbackAnalysis(restaurantName: String, content: String): String {
        val cuisineType = guessCuisineType(restaurantName, content)
        val priceRange = guessPriceRange(content)

        return """
            Restaurant Analysis for "$restaurantName":
            
            Cuisine Type: $cuisineType
            
            Price Range: $priceRange
            
            Note: This is an offline analysis as the AI service is currently unavailable.
            We've used our best guess based on the information provided.
        """.trimIndent()
    }

    // פונקציות עזר לניחוש מאפייני המסעדה
    private fun guessCuisineType(restaurantName: String, content: String): String {
        val lowerName = restaurantName.lowercase()
        val lowerContent = content.lowercase()

        return when {
            lowerName.contains("pizza") || lowerContent.contains("pizza") -> "Italian"
            lowerName.contains("sushi") -> "Japanese"
            lowerName.contains("burger") -> "American"
            lowerName.contains("taco") -> "Mexican"
            lowerName.contains("thai") -> "Thai"
            else -> "International"
        }
    }

    private fun guessPriceRange(content: String): String {
        val lowerContent = content.lowercase()

        return when {
            lowerContent.contains("expensive") -> "$$$$"
            lowerContent.contains("cheap") -> "$"
            else -> "$$$"
        }
    }
}