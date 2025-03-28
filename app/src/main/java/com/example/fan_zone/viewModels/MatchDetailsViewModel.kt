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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun createPost(post: Post) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val savedPost = postRepository.createPost(post)
                val updatedUserPosts = _userPosts.value.orEmpty().toMutableList()
                updatedUserPosts.add(0, savedPost)
                _userPosts.postValue(updatedUserPosts)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to create post")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updatePost(post: Post) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                postRepository.updatePost(post.id, post.content)
                val updatedUserPosts = _userPosts.value?.map { if (it.id == post.id) post.copy() else it } ?: emptyList()
                _userPosts.postValue(updatedUserPosts)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to edit post")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                postRepository.deletePost(post.id)
                _userPosts.postValue(_userPosts.value?.filter { it.id != post.id })
                _popularPosts.postValue(_popularPosts.value?.filter { it.id != post.id })
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to delete post")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun likePost(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)

        postRef.update(
            "likedUserIds", FieldValue.arrayUnion(userId),
        )

        updatePostInLists(post.copy(likedUserIds = post.likedUserIds + userId))
    }

    fun unlikePost(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)

        postRef.update("likedUserIds", FieldValue.arrayRemove(userId))

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

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private fun updatePostInLists(updatedPost: Post) {
        _userPosts.postValue(_userPosts.value?.map { if (it.id == updatedPost.id) updatedPost else it })
        _popularPosts.postValue(_popularPosts.value?.map { if (it.id == updatedPost.id) updatedPost else it })
    }
}
