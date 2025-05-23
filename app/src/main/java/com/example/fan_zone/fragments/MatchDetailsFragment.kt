package com.example.fan_zone.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.R
import com.example.fan_zone.adapters.PostAdapter
import com.example.fan_zone.databinding.FragmentMatchDetailsBinding
import com.example.fan_zone.models.CloudinaryModel
import com.example.fan_zone.models.GeoPoint
import com.example.fan_zone.models.Match
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.UserRepository
import com.example.fan_zone.viewModels.MatchDetailsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MatchDetailsFragment : Fragment() {
    private var _binding: FragmentMatchDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MatchDetailsViewModel by viewModels()
    private val args: MatchDetailsFragmentArgs by navArgs()
    private val userRepository = UserRepository()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var popularPostsAdapter: PostAdapter
    private lateinit var userPostsAdapter: PostAdapter

    private var selectedImageUri: Uri? = null
    private var currentEditingPost: Post? = null
    private var editImageUri: Uri? = null
    private var userLocation: GeoPoint? = null

    private val getContentForNewPost =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.postImagePreview.apply {
                    visibility = View.VISIBLE
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                    setImageBitmap(bitmap)
                }
            }
        }

    private val getContentForEdit =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentEditingPost?.let { post ->
                    editImageUri = it
                    val postAdapter =
                        if (post.userId == getCurrentUser()) userPostsAdapter else popularPostsAdapter
                    postAdapter.showImagePreview(post.id, uri)
                }
            }
        }

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

        setupSwipeRefresh()
        setupUserLocation()

        binding.returnToFeed.setOnClickListener {
            val action =
                MatchDetailsFragmentDirections.actionMatchDetailsFragmentToMatchesFeedFragment()
            findNavController().navigate(action)
        }
        setupMatchDetailsObserver()
        setupAdapters()
        setupRecyclerViews()
        observePostData()
        observeLoadingState()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.fetchMatchDetails(args.matchId.toInt())
    }

    private fun setupUserLocation() {
        fetchUserLocation { location ->
            userLocation = location
        }
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
            onEditPost = { postId, content, imageUrl ->
                viewModel.updatePost(postId, content, imageUrl)
                currentEditingPost = null
                editImageUri = null
            },
            onDeletePost = { post -> viewModel.deletePost(post) },
            onImageEditRequest = { post ->
                currentEditingPost?.let { currentPost ->
                    if (currentPost.id != post.id) {
                        popularPostsAdapter.cancelEdit(currentPost.id)
                        userPostsAdapter.cancelEdit(currentPost.id)
                    }
                }
                currentEditingPost = post
                getContentForEdit.launch("image/*")
            },
            onLoadingStateChanged = { isLoading ->
                viewModel.setLoading(isLoading)
            }
        )

        userPostsAdapter = PostAdapter(
            onLikeClicked = { post -> viewModel.likePost(post) },
            onUnlikeClicked = { post -> viewModel.unlikePost(post) },
            onEditPost = { postId, content, imageUrl ->
                viewModel.updatePost(postId, content, imageUrl)
                currentEditingPost = null
                editImageUri = null
            },
            onDeletePost = { post -> viewModel.deletePost(post) },
            onImageEditRequest = { post ->
                currentEditingPost?.let { currentPost ->
                    if (currentPost.id != post.id) {
                        popularPostsAdapter.cancelEdit(currentPost.id)
                        userPostsAdapter.cancelEdit(currentPost.id)
                    }
                }
                currentEditingPost = post
                getContentForEdit.launch("image/*")
            },
            onLoadingStateChanged = { isLoading ->
                viewModel.setLoading(isLoading)
            }
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
        binding.homeTeamNameText.text = match.homeTeam
        binding.awayTeamNameText.text = match.awayTeam

        if (match.homeTeamGoals != null && match.awayTeamGoals != null) {
            binding.scoreText.text = "${match.homeTeamGoals} - ${match.awayTeamGoals}"
        } else {
            binding.scoreText.visibility = View.GONE
        }

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.matchDateTextView.text = dateFormatter.format(match.date)

        if (match.homeTeamImage.isNotEmpty()) {
            Picasso.get()
                .load(match.homeTeamImage)
                .placeholder(R.drawable.ic_matches)
                .error(R.drawable.ic_matches)
                .fit()
                .centerCrop()
                .into(binding.homeTeamImageView)
        } else {
            binding.homeTeamImageView.setImageResource(R.drawable.ic_matches)
        }

        if (match.awayTeamImage.isNotEmpty()) {
            Picasso.get()
                .load(match.awayTeamImage)
                .placeholder(R.drawable.ic_matches)
                .error(R.drawable.ic_matches)
                .fit()
                .centerCrop()
                .into(binding.awayTeamImageView)
        } else {
            binding.awayTeamImageView.setImageResource(R.drawable.ic_matches)
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getCurrentUser(): String {
        return userRepository.getCurrentUserId()
            ?: throw RuntimeException("Firebase currentUser is not defined!")
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setupUserLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable location permissions in settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun fetchUserLocation(onLocationRetrieved: (GeoPoint?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        onLocationRetrieved(geoPoint)
                    } else {
                        fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
                        )
                            .addOnSuccessListener { freshLocation ->
                                val geoPoint =
                                    freshLocation?.let { GeoPoint(it.latitude, it.longitude) }
                                onLocationRetrieved(geoPoint)
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to get location",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Failed to get last known location",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun createPost(content: String, matchId: String, location: GeoPoint?) {
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Post content cannot be empty", Toast.LENGTH_SHORT)
                .show()
            return
        }

        viewModel.setLoading(true)

        if (binding.postImagePreview.visibility == View.VISIBLE) {
            uploadPostImage(content, matchId, location)
        } else {
            createPostWithoutImage(content, matchId, location)
        }
    }

    private fun uploadPostImage(content: String, matchId: String, location: GeoPoint?) {
        val bitmap = (binding.postImagePreview.drawable as? BitmapDrawable)?.bitmap ?: run {
            createPostWithoutImage(content, matchId, location)
            return
        }

        CloudinaryModel.shared.uploadImage(
            bitmap = bitmap,
            name = "post_${System.currentTimeMillis()}",
            onSuccess = { imageUrl ->
                val newPost = Post(
                    userId = getCurrentUser(),
                    content = content,
                    timePosted = Date(),
                    matchId = matchId,
                    location = location,
                    imageUrl = imageUrl
                )
                viewModel.createPost(newPost)
                clearPostForm()
                viewModel.setLoading(false)
            },
            onError = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload image: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.setLoading(false)
                }
            }
        )
    }

    private fun createPostWithoutImage(content: String, matchId: String, location: GeoPoint?) {
        val newPost = Post(
            userId = getCurrentUser(),
            content = content,
            timePosted = Date(),
            matchId = matchId,
            location = location
        )
        viewModel.createPost(newPost)
        clearPostForm()
        viewModel.setLoading(false)
    }

    private fun clearPostForm() {
        binding.postEditText.text?.clear()
        binding.postImagePreview.apply {
            setImageBitmap(null)
            visibility = View.GONE
        }
        selectedImageUri = null
        currentEditingPost = null
        editImageUri = null
    }

    private fun setupClickListeners() {
        binding.postEditText.addTextChangedListener {
            binding.postButton.isEnabled = it.toString().trim().isNotEmpty()
        }

        binding.postButton.setOnClickListener {
            val content = binding.postEditText.text.toString().trim()
            val matchId = args.matchId

            if (userLocation != null) {
                createPost(content, matchId, userLocation)
            } else {
                Toast.makeText(
                    context,
                    "trying to retrieve location, make sure location permissions are turned on",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.selectImageButton.setOnClickListener {
            getContentForNewPost.launch("image/*")
        }
    }

    private fun setupErrorMsgListener() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                "Failed to create post. Try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}