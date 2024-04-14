package com.sipl.egs.database.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs.database.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): User

    @Query("UPDATE users SET isActive = :isActive WHERE email = :email")
    suspend fun updateUserStatus(email: String, isActive: Boolean)

    @Transaction
    suspend fun insertInitialRecords() {
        // Check if any records exist
        if (getAllUsers().isEmpty()) {
            // Insert initial records
            insertUser(User(fullName = "Gram Panchayat 1", email = "grampanchayat1@egs.com", password = "12345678", isActive = true))
            insertUser(User(fullName = "Gram Panchayat 2", email = "grampanchayat2@egs.com", password = "12345678", isActive = true))
            insertUser(User(fullName = "Gram Panchayat 3", email = "grampanchayat3@egs.com", password = "12345678", isActive = true))
            insertUser(User(fullName = "Gram Panchayat 4", email = "grampanchayat4@egs.com", password = "12345678", isActive = true))
            insertUser(User(fullName = "Gram Panchayat 5", email = "grampanchayat5@egs.com", password = "12345678", isActive = true))


        }
    }
}
