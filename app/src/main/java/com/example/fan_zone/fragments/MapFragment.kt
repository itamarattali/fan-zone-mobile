package com.example.fan_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fan_zone.R
import com.example.fan_zone.databinding.FragmentMapBinding
import com.example.fan_zone.models.Post
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val firebase = FirebaseFirestore.getInstance()
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it)
                    .commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        loadPostsOnMap()

        googleMap.setOnMarkerClickListener { marker ->
            val post = marker.tag as? Post
            post?.let { showPostDetails(it) }
            true
        }
    }

    private fun loadPostsOnMap() {
        firebase.collection("posts").get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                    .forEach { post ->
                        if (post.location != null) {
                            val location = LatLng(post.location!!.latitude, post.location!!.longitude)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(post.content.take(20) + "...")
                            )
                            marker?.tag = post
                        }
                    }
            }
    }

    private fun showPostDetails(post: Post) {
        val postDetailsFragment = PostDetailsBottomSheetFragment.newInstance(post)
        postDetailsFragment.show(parentFragmentManager, "PostDetailsFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
