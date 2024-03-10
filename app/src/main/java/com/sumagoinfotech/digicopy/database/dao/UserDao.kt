package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sumagoinfotech.digicopy.database.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User) : Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User
}