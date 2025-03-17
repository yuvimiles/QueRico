package com.example.querico.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.R
import com.example.querico.ViewModel.MapViewModel
import com.example.querico.utilities.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private val MY_PERMISSIONS_REQUEST_LOCATION = 122
    private lateinit var mapViewModel: MapViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val restaurantMarkers: MutableMap<String, Marker?> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = rootView.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMarkerClickListener(this)
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true

        // Check if location permissions are granted
        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            // Initialize location request
            locationRequest = LocationRequest.create().apply {
                interval = 10000 // Update interval in milliseconds
                fastestInterval = 5000 // Fastest update interval in milliseconds
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            // Initialize location callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    location?.let {
                        Log.d("location", "${it.longitude}########${it.latitude}")

                        // Update the user's current location only once
                        updateUserLocation("currentUser", LatLng(it.latitude, it.longitude))
                        // Move camera to user's location only once
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))

                        // Remove location updates after user's location is obtained
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }

            // Request location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Request location permissions if not granted.
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }

        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        // Fetch and observe posts
        mapViewModel.fetchPosts()
        mapViewModel.posts.observe(viewLifecycleOwner) { posts ->
            // Update the map with markers based on the retrieved posts
            updateMapWithPosts(posts)
        }
    }

    private fun updateMapWithPosts(posts: List<PostEntity>) {
        // Add markers for each post
        posts.forEach { post ->
            // Convert location string to GeoPoint
            val geoPoint = LocationUtils.convertLocationToGeoPoint(requireContext(), post.location)

            // Create LatLng from GeoPoint
            val location = LatLng(geoPoint.latitude, geoPoint.longitude)

            // Add marker for the post
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(post.restaurantName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            marker?.let {
                // You can store additional data with the marker if needed
                it.tag = post
            }
        }
    }

    private fun updateUserLocation(userId: String, location: LatLng) {
        // Check if the marker for the user already exists
        if (restaurantMarkers.containsKey(userId)) {
            // Update the existing marker
            restaurantMarkers[userId]?.position = location
        } else {
            // Add a new marker for the user
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Your Location")
                    .snippet("This is your current location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .flat(true)
            )
            // Store the marker in the map
            restaurantMarkers[userId] = marker
        }
    }

    private fun checkLocationPermission(): Boolean {
        val fineLocationPermission = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val navHostFragment: NavHostFragment = activity?.supportFragmentManager
            ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        val navController = navHostFragment.navController

        val post = marker.tag as? PostEntity
        if (post != null) {
            val bundle = Bundle()
            bundle.putSerializable("post", post)
            navController.navigate(R.id.action_global_singlePostCardFragment, bundle)
        }
        return true
    }
}