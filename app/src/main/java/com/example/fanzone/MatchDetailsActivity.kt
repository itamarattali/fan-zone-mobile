package com.example.fanzone.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fanzone.adapters.PostAdapter
import com.example.fanzone.databinding.ActivityMatchDetailsBinding
import com.example.fanzone.viewmodel.MatchDetailsViewModel

class MatchDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchDetailsBinding
    private val viewModel: MatchDetailsViewModel by viewModels()

    private lateinit var popularPostsAdapter: PostAdapter
    private lateinit var userPostsAdapter: PostAdapter
    private var matchId: String = "1" // TODO: Placeholder, replace with actual match ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()

        // Fetch posts for the selected match
        viewModel.fetchPosts(matchId)

        // Observe popular posts
        viewModel.popularPosts.observe(this) { posts ->
            popularPostsAdapter.submitList(posts)
        }

        // Observe current user posts
        viewModel.userPosts.observe(this) { posts ->
            userPostsAdapter.submitList(posts)
        }
    }

    private fun setupRecyclerViews() {
        // Popular Posts RecyclerView
        popularPostsAdapter = PostAdapter { post ->
            viewModel.likePost(post)
        }
        binding.popularPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MatchDetailsActivity)
            adapter = popularPostsAdapter
        }

        // User Posts RecyclerView
        userPostsAdapter = PostAdapter { post ->
            viewModel.likePost(post)
        }
        binding.yourPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MatchDetailsActivity)
            adapter = userPostsAdapter
        }
    }
}
