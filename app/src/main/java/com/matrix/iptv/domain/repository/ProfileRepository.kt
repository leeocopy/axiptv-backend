package com.matrix.iptv.domain.repository

import com.matrix.iptv.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Interface for local Xtream profile management.
 * Implemented by ProfileRepositoryImpl using Room + EncryptedSharedPreferences.
 */
interface ProfileRepository {
    fun observeProfiles(): Flow<List<Profile>>
    suspend fun getProfile(id: String): Profile?
    /** Returns error message or null on success */
    suspend fun saveProfile(profile: Profile, password: String): String?
    suspend fun deleteProfile(id: String)
    suspend fun getPassword(profileId: String): String
    suspend fun profileCount(): Int
}
