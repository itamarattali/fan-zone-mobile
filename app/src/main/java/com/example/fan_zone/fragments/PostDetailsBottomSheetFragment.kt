package com.example.fan_zone.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fan_zone.databinding.FragmentPostDetailsBottomSheetBinding
import com.example.fan_zone.models.Post
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class PostDetailsBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentPostDetailsBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_USERNAME = "username"
        private const val ARG_CONTENT = "content"
        private const val ARG_LIKE_COUNT = "like_count"
        private const val ARG_PROFILE_IMAGE = "profile_image"
        private const val ARG_DATE = "date"

        fun newInstance(post: Post): PostDetailsBottomSheetFragment {
            val fragment = PostDetailsBottomSheetFragment()
            val args = Bundle().apply {
                putString(ARG_USERNAME, post.username)
                putString(ARG_CONTENT, post.content)
                putInt(ARG_LIKE_COUNT, post.likeCount)
                putString(ARG_PROFILE_IMAGE, post.profileImageUrl)

                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(post.timePosted)
                putString(ARG_DATE, formattedDate)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = arguments?.getString(ARG_USERNAME) ?: "Unknown"
        val content = arguments?.getString(ARG_CONTENT) ?: "No Content"
        val likeCount = arguments?.getInt(ARG_LIKE_COUNT) ?: 0
        val profileImage = arguments?.getString(ARG_PROFILE_IMAGE)
        val date = arguments?.getString(ARG_DATE) ?: "Unknown Date"

        binding.authorTextView.text = username
        binding.contentTextView.text = content
        binding.likeCountTextView.text = "${likeCount} likes"
        binding.dateTextView.text = date

        profileImage?.let {
            Picasso.get().load(it).into(binding.profileImageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
