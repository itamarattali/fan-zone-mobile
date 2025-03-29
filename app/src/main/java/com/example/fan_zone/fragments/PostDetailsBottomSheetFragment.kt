package com.example.fan_zone.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fan_zone.R
import com.example.fan_zone.databinding.FragmentPostDetailsBottomSheetBinding
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.UserRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PostDetailsBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentPostDetailsBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()
    private lateinit var post: Post

    companion object {
        fun newInstance(post: Post): PostDetailsBottomSheetFragment {
            val fragment = PostDetailsBottomSheetFragment()
            fragment.post = post
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

        // Only access the binding if it's non-null
        _binding?.let { binding ->
            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserById(post.userId)

                binding.usernameTextView.text = if (user != null) user.fullName else "Unknown User"

                val profileImageUrl = if (user != null && !user.profilePicUrl.isNullOrEmpty()) {
                    user.profilePicUrl
                } else {
                    null
                }

                Picasso.get()
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.profileImageView)

                if (post != null && !post.imageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(post.imageUrl)
                        .into(binding.contentImage)
                } else {
                    binding.contentImage.visibility = View.GONE
                }

                binding.contentTextView.text = post.content
                binding.likeCountTextView.text = post.likedUserIds.size.toString()
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(post.timePosted)
                binding.dateTextView.text = formattedDate
            }
        } ?: run {
            // You can log or handle this scenario if needed
            Log.e("PostDetailsFragment", "Binding is null, skipping view setup.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
