package com.example.proyectofinalmapslemm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.proyectofinalmapslemm.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.common.io.Files.append
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var  fusedLocationClient: FusedLocationProviderClient
    private var LOCACIONES: ArrayList<LatLng> = ArrayList<LatLng>()
    private var contadorLugares =1
    private val database = Firebase.database(R.string.urlBD.toString())
    private val referencia = database.reference


    companion object {
        private  const val LOCATION_REQUEST_CODE =1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //LEer de Firebase


        val buttonGuardar = findViewById<Button>(R.id.buttonGuardar)
        val buttonBorrar = findViewById<Button>(R.id.buttonBorrarTodo)

        buttonGuardar.setOnClickListener{

            LOCACIONES.forEach{
                val data = referencia.push().child("lugares").setValue(it)
            }
            Toast.makeText(this,"Se enviaron $contadorLugares locaciones a Firebase",Toast.LENGTH_SHORT).show()
        }


        //Métodos de borrado
        buttonBorrar.setOnClickListener {

        }


        //Obtener localización
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        // Add a marker in CIC and move the camera
        //val ciudadActual = LatLng(19.503242999307474, -99.14756839981246)

        //mMap.addMarker(MarkerOptions().position(ciudadActual).title("Bienvenido al CIC"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ciudadActual,15f))

        setMapLongClick(mMap)

        mMap.uiSettings.isZoomControlsEnabled=true
        setUpMap()
        mMap.setOnMarkerClickListener(this)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled=true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location!=null){
                lastLocation=location
                val currentLatLng =LatLng(location.latitude,location.longitude)
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,20f))
        }

        }
    }

    private fun placeMarkerOnMap(currentLatLng: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("$currentLatLng")
        mMap.addMarker(markerOptions)

    }

    fun setMapLongClick(map: GoogleMap){
            map.setOnMapClickListener {
                val miSnippet = String.format(Locale.getDefault(),
                    format = "Lat: %1$.3f, Lng: %2$.3f",
                    it.latitude,
                it.longitude)

                map.addMarker(MarkerOptions().position(it).title("Marcador $contadorLugares").snippet(miSnippet))

                val database= Firebase.database("https://proyectofinalmapslemm-88012-default-rtdb.firebaseio.com/")
                val referencia=database.reference

                //val data = referencia.push().child("lugares").setValue(it)
                LOCACIONES.add(it)
                //println(it)
                contadorLugares+=1

            }
        }

    override fun onMarkerClick(p0: Marker?)=false

}

