package com.example.fan_zone.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.fan_zone.models.Match
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.MatchRepository
import com.example.fan_zone.repositories.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MatchDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val matchRepository = MatchRepository(application)
    private val postRepository = PostRepository()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _matchId = MutableLiveData<Int>()

    val match: LiveData<Match> = _matchId.switchMap{ id ->
        matchRepository.getMatchById(id)
    }

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
                _errorMessage.postValue("Failed to update post")
            }
        }
    }

    fun likePost(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)

        postRef.update(
            "likedUserIds", FieldValue.arrayUnion(userId),
        )

        // Update LiveData immediately
        updatePostInLists(post.copy(likedUserIds = post.likedUserIds + userId))
    }

    fun unlikePost(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)

        postRef.update("likedUserIds", FieldValue.arrayRemove(userId))

        // Update LiveData immediately
        updatePostInLists(post.copy(likedUserIds = post.likedUserIds.filter { it != userId }))
    }

    fun fetchMatchDetails(matchId: Int) {
        _matchId.value = matchId
        fetchPosts(matchId)
    }

    private fun fetchPosts(matchId: Int) {
        viewModelScope.launch {
            val posts = postRepository.getPostsByMatchID(matchId)

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            _popularPosts.value =
                posts.filter { it.userId != currentUserId }.sortedByDescending { it.likedUserIds.size }
            _userPosts.value = posts.filter { it.userId == currentUserId }
        }
    }

    private fun updatePostInLists(updatedPost: Post) {
        _userPosts.postValue(_userPosts.value?.map { if (it.id == updatedPost.id) updatedPost else it })
        _popularPosts.postValue(_popularPosts.value?.map { if (it.id == updatedPost.id) updatedPost else it })
    }
}
