package com.example.querico.utilities

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import java.io.IOException
import java.util.Locale

object LocationUtils {

    /**
     * Convert a location string to a GeoPoint (latitude, longitude)
     * @param context Context for accessing Geocoder
     * @param locationString String representation of a location
     * @return GeoPoint with latitude and longitude coordinates
     */
    fun convertLocationToGeoPoint(context: Context, locationString: String): GeoPoint {
        // Default location (Tel Aviv) in case geocoding fails
        var latitude = 32.0853
        var longitude = 34.7818

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            //val addresses: List<Address>? = geocoder.getFromLocationName(locationString, 1)
            val addresses : List<Address>? = null
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                latitude = address.latitude
                longitude = address.longitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return GeoPoint(latitude, longitude)
    }

    /**
     * Get a formatted address from latitude and longitude coordinates
     * @param context Context for accessing Geocoder
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return String representation of the address
     */
    fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String {
        var addressText = ""

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val sb = StringBuilder()

                // Get address lines
                for (i in 0..address.maxAddressLineIndex) {
                    if (i > 0) sb.append(", ")
                    sb.append(address.getAddressLine(i))
                }

                addressText = sb.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return addressText
    }

    /**
     * Calculate distance between two points in kilometers
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c // Distance in km
    }
}