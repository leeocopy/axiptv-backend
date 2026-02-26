package com.matrix.iptv.data.repository

import com.matrix.iptv.data.local.db.ProfileDao
import com.matrix.iptv.data.local.db.ProfileEntity
import com.matrix.iptv.data.local.prefs.SecurePrefs
import com.matrix.iptv.domain.model.Profile
import com.matrix.iptv.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_PROFILES = 10

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dao: ProfileDao,
    private val securePrefs: SecurePrefs
) : ProfileRepository {

    override fun observeProfiles(): Flow<List<Profile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getProfile(id: String): Profile? =
        dao.getById(id)?.toDomain()

    override suspend fun profileCount(): Int = dao.count()

    override suspend fun saveProfile(profile: Profile, password: String): String? {
        val count = dao.count()
        val isNew = dao.getById(profile.id) == null

        if (isNew && count >= MAX_PROFILES) return "Maximum 10 profiles reached"

        val duplicates = dao.countDuplicates(
            host = profile.host.trim().lowercase(),
            username = profile.username.trim(),
            excludeId = profile.id
        )
        if (duplicates > 0) return "A profile with this host and username already exists"

        dao.upsert(profile.toEntity())
        securePrefs.savePassword(profile.id, password)
        return null
    }

    override suspend fun deleteProfile(id: String) {
        dao.deleteById(id)
        securePrefs.deletePassword(id)
    }

    override suspend fun getPassword(profileId: String): String =
        securePrefs.getPassword(profileId)

    private fun ProfileEntity.toDomain() = Profile(
        id = id, name = name, host = host,
        username = username, createdAt = createdAt, lastUsed = lastUsed
    )

    private fun Profile.toEntity() = ProfileEntity(
        id = id, name = name, host = host,
        username = username, createdAt = createdAt, lastUsed = lastUsed
    )
}
