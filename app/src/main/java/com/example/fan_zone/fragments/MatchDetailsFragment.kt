package com.example.fan_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.adapters.PostAdapter
import com.example.fan_zone.databinding.FragmentMatchDetailsBinding
import com.example.fan_zone.fragments.MatchDetailsFragmentArgs
import com.example.fan_zone.viewModel.MatchDetailsViewModel

class MatchDetailsFragment : Fragment() {
    private var _binding: FragmentMatchDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MatchDetailsViewModel by viewModels()
    private val args: MatchDetailsFragmentArgs by navArgs() // Get matchId from navigation arguments

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

        // Fetch match details
        viewModel.getMatchDetails(args.matchId)

        // Observe match details
        viewModel.match.observe(viewLifecycleOwner) { match ->
            match?.let {
                binding.matchTitleTextView.text = "${match.homeTeam} vs ${match.awayTeam}"
                binding.matchDetailsTextView.text = "Location: ${match.location}"
                binding.matchResultTextView.text = "${match.homeTeamGoals} - ${match.awayTeamGoals}"
            }
        }

        // Setup adapters
        popularPostsAdapter = PostAdapter({}, {})
        userPostsAdapter = PostAdapter({}, {})

        binding.recyclerViewPopularPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPopularPosts.adapter = popularPostsAdapter

        binding.recyclerViewYourPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewYourPosts.adapter = userPostsAdapter

        // Observe ViewModel for posts
        viewModel.popularPosts.observe(viewLifecycleOwner) { posts ->
            popularPostsAdapter.submitList(posts)
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            userPostsAdapter.submitList(posts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
