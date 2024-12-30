package com.example.fanzone

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.fanzone.adapters.PostAdapter
import com.example.fanzone.databinding.ActivityMatchDetailsBinding
import com.example.fanzone.viewmodel.MatchDetailsViewModel

class MatchDetailsActivity : AppCompatActivity() {
    private val viewModel: MatchDetailsViewModel by viewModels()
    private lateinit var binding: ActivityMatchDetailsBinding

    private lateinit var yourPostsAdapter: PostAdapter
    private lateinit var popularPostsAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize adapters
        yourPostsAdapter = PostAdapter(emptyList())
        popularPostsAdapter = PostAdapter(emptyList())

        binding.yourPostsRecyclerView.adapter = yourPostsAdapter
        binding.popularPostsRecyclerView.adapter = popularPostsAdapter


        // Inflate layout
        binding = ActivityMatchDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assume matchId is passed via intent extras
        val matchId = intent.getStringExtra("matchId") ?: "1"

        // Initialize ViewModel
        viewModel.initialize(matchId)

        // Observe LiveData
        viewModel.matchDetails.observe(this, Observer { matchDetails ->
            binding.matchTitleTextView.text = "${matchDetails.homeTeam} - ${matchDetails.awayTeam}"
            binding.matchDetailsTextView.text = "${matchDetails.matchTime}\n${matchDetails.matchLocation}"
            binding.matchResultTextView.text = matchDetails.result
        })

        // Observe and update your posts
        viewModel.yourPosts.observe(this, Observer { posts ->
            yourPostsAdapter.updateData(posts)
        })

        // Observe and update popular posts
        viewModel.popularPosts.observe(this, Observer { posts ->
            popularPostsAdapter.updateData(posts)
        })

        // Handle new post submission
        binding.sendCommentButton.setOnClickListener {
            val content = binding.commentEditText.text.toString()
            if (content.isNotBlank()) {
                viewModel.addPost(content, matchId, "Guest User")
                binding.commentEditText.text?.clear()
            }
        }
    }
}
