package com.example.fanzone

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fanzone.databinding.ActivityMatchDetailsBinding
import com.example.fanzone.viewmodel.MatchDetailsViewModel

class MatchDetailsActivity : AppCompatActivity() {

    private val viewModel: MatchDetailsViewModel by viewModels()
    private lateinit var binding: ActivityMatchDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityMatchDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind ViewModel data
        viewModel.matchTitle.observe(this, Observer { binding.matchTitleTextView.text = it })
        viewModel.matchDetails.observe(this, Observer { binding.matchDetailsTextView.text = it })

        // RecyclerView setup
        binding.yourPostsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.popularPostsRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.yourPosts.observe(this, Observer { posts ->
            // Update Your Posts RecyclerView
        })

        viewModel.popularPosts.observe(this, Observer { posts ->
            // Update Popular Posts RecyclerView
        })

        // Handle comment submission
        binding.sendCommentButton.setOnClickListener {
            val newComment = binding.commentEditText.text.toString()
            if (newComment.isNotBlank()) {
                viewModel.addPost(newComment)
                binding.commentEditText.text?.clear()
            }
        }
    }
}
