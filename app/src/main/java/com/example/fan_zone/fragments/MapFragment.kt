package com.example.fan_zone.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fan_zone.R
import com.example.fan_zone.databinding.FragmentMapBinding
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.PostRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val postRepository = PostRepository()
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

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        loadPostsOnMap()

        googleMap.setOnMarkerClickListener { marker ->
            val post = marker.tag as? Post
            post?.let { showPostDetails(it) }
            true
        }
    }

    private fun loadPostsOnMap() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val posts = postRepository.getAllPosts()
                posts.forEach { post ->
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
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    private fun showPostDetails(post: Post) {
        val existingFragment = parentFragmentManager.findFragmentByTag("PostDetailsFragment")

        // Only show the fragment if it's not already visible
        if (existingFragment == null) {
            val postDetailsFragment = PostDetailsBottomSheetFragment.newInstance(post)
            postDetailsFragment.show(parentFragmentManager, "PostDetailsFragment")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
