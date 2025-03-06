package com.example.fan_zone.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.adapters.PostAdapter
import com.example.fan_zone.databinding.FragmentMatchDetailsBinding
import com.example.fan_zone.models.Post
import com.example.fan_zone.viewModels.MatchDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.example.fan_zone.R
import com.example.fan_zone.models.Match
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import java.util.Date

class MatchDetailsFragment : Fragment() {
    private var _binding: FragmentMatchDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MatchDetailsViewModel by viewModels()
    private val args: MatchDetailsFragmentArgs by navArgs()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var popularPostsAdapter: PostAdapter
    private lateinit var userPostsAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchDetailsBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupClickListeners()
        setupErrorMsgListener()
        observeViewModel()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMatchDetailsObserver()
        setupRecyclerViews()
        setupAdapters()
        observePostData()
    }

    private fun setupMatchDetailsObserver() {
        viewModel.fetchMatchDetails(args.matchId.toInt())
        viewModel.match.observe(viewLifecycleOwner) { match ->
            match?.let { updateMatchDetails(it) }
        }
    }

    private fun setupRecyclerViews() {
        binding.recyclerViewPopularPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPopularPosts.adapter = popularPostsAdapter

        binding.recyclerViewYourPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewYourPosts.adapter = userPostsAdapter
    }

    private fun setupAdapters() {
        popularPostsAdapter = PostAdapter(
            onLikeClicked = { post -> viewModel.likePost(post) },
            onUnlikeClicked = { post -> viewModel.unlikePost(post) },
            onEditPost = { post -> viewModel.updatePost(post) }
        )

        userPostsAdapter = PostAdapter(
            onLikeClicked = { post -> viewModel.likePost(post) },
            onUnlikeClicked = { post -> viewModel.unlikePost(post) },
            onEditPost = { post -> viewModel.updatePost(post) }
        )
    }

    private fun observePostData() {
        viewModel.popularPosts.observe(viewLifecycleOwner) { posts ->
            popularPostsAdapter.submitList(posts)
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            userPostsAdapter.submitList(posts)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMatchDetails(match: Match) {
        binding.matchTitleTextView.text = "${match.homeTeam} vs ${match.awayTeam}"
        binding.matchResultTextView.text = "${match.homeTeamGoals} - ${match.awayTeamGoals}"
        binding.matchDetailsTextView.text = "Date: ${match.date}"

        if (match.matchImage.isNotEmpty()) {
            Picasso.get()
                .load(match.matchImage)
                .placeholder(R.drawable.ic_matches)
                .error(R.drawable.ic_matches)
                .fit()
                .centerCrop()
                .into(binding.matchImageView)
        } else {
            binding.matchImageView.setImageResource(R.drawable.ic_matches)
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentUser(): String {
        val currentUser = auth.currentUser
        return currentUser?.displayName ?: currentUser?.email ?: "Unknown User"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchUserLocation { location ->
                    val content = binding.postEditText.text.toString().trim()
                    createPost(content, args.matchId, location)
                }
            } else {
                Toast.makeText(requireContext(), "Please enable location permissions in settings", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingPermission")
    private fun fetchUserLocation(onLocationRetrieved: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    onLocationRetrieved(location)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                    onLocationRetrieved(null)
                }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun createPost(content: String, matchId: String, location: Location?) {
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Post content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        val newPost = Post(
            username = getCurrentUser(),
            content = content,
            timePosted = Date(),
            matchId = matchId,
            location = location
        )

        viewModel.createPost(newPost)
    }

    private fun setupClickListeners() {
        binding.postEditText.addTextChangedListener {
            binding.postButton.isEnabled = it.toString().trim().isNotEmpty()
        }

        binding.postButton.setOnClickListener {
            val content = binding.postEditText.text.toString().trim()
            val matchId = args.matchId

            fetchUserLocation { location ->
                createPost(content, matchId, location)
            }
        }
    }

    private fun setupErrorMsgListener() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Failed to create post. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
