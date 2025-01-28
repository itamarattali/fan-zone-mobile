package com.example.fan_zone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.adapters.PostAdapter
import com.example.fan_zone.viewModel.MatchDetailsViewModel
import com.example.fan_zone.databinding.FragmentMatchDetailsBinding

class MatchDetailsFragment : Fragment() {

    private var _binding: FragmentMatchDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchDetailsViewModel by viewModels()
    private val args: MatchDetailsFragmentArgs by navArgs()

    private lateinit var popularPostsAdapter: PostAdapter
    private lateinit var userPostsAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        val matchId = args.matchId
        viewModel.fetchPosts(matchId)

        viewModel.popularPosts.observe(viewLifecycleOwner) { posts ->
            popularPostsAdapter.submitList(posts)
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            userPostsAdapter.submitList(posts)
        }
    }

    private fun setupRecyclerViews() {
        popularPostsAdapter = PostAdapter { post ->
            viewModel.likePost(post)
        }
        binding.popularPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = popularPostsAdapter
        }

        userPostsAdapter = PostAdapter { post ->
            viewModel.likePost(post)
        }
        binding.yourPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userPostsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
