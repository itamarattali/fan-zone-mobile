package com.example.fan_zone.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fan_zone.repositories.MatchRepository
import com.example.fan_zone.repositories.PostRepository
import com.example.fan_zone.models.Match
import com.example.fan_zone.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MatchDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val matchRepository = MatchRepository(application)
    private val postRepository = PostRepository()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _match = MutableLiveData<Match?>()
    val match: LiveData<Match?> get() = _match

    private val _popularPosts = MutableLiveData<List<Post>>()
    val popularPosts: LiveData<List<Post>> get() = _popularPosts

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts

    fun createPost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.createPost(post)

                val updatedUserPosts = _userPosts.value.orEmpty().toMutableList()
                updatedUserPosts.add(0, post)
                _userPosts.postValue(updatedUserPosts)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to create post")
            }
        }
    }

    fun updatePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.updatePost(post.id, post.content)

                // Manually update the LiveData list to reflect the edited post
                val updatedUserPosts = _userPosts.value?.map {
                    if (it.id == post.id) post.copy() else it
                } ?: emptyList()

                _userPosts.postValue(updatedUserPosts)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to edit post")
            }
        }
    }

    fun likePost(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)

        postRef.update(
            "likedUsers", FieldValue.arrayUnion(userId),
            "likeCount", FieldValue.increment(1)
        )

            // Update LiveData immediately
            updatePostInLists(post.copy(
                likedUsers = post.likedUsers + userId,
                likeCount = post.likeCount + 1
            )
        )
    }

    fun unlikePost(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)

        postRef.update(
            "likedUsers", FieldValue.arrayRemove(userId),
            "likeCount", FieldValue.increment(-1)
        )

        // Update LiveData immediately
        updatePostInLists(post.copy(
            likedUsers = post.likedUsers.filter { it != userId },
            likeCount = post.likeCount - 1
        ))
    }

    fun getMatchDetails(matchId: Int) {
        viewModelScope.launch {
            _match.value = matchRepository.getMatchById(matchId).value
            fetchPosts(matchId)
        }
    }

    private fun fetchPosts(matchId: Int) {
        viewModelScope.launch {
            val posts = postRepository.getPostsByMatchID(matchId)

            _popularPosts.value = posts.sortedByDescending { it.likeCount } // Most liked first
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            _userPosts.value = posts.filter { it.id == currentUserId }
        }
    }

    private fun updatePostInLists(updatedPost: Post) {
        _userPosts.postValue(_userPosts.value?.map { if (it.id == updatedPost.id) updatedPost else it })
        _popularPosts.postValue(_popularPosts.value?.map { if (it.id == updatedPost.id) updatedPost else it })
    }
}
