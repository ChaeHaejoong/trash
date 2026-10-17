package com.hjchae.sqlite

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.hjchae.room_lib_test.R

@Entity(tableName = "LOSTFOUND_T")
data class LostFoundItem(

    @PrimaryKey
    var id: Long = 0L,
    var type: String = "",
    var name: String = "",
    var place: String = "",
    var date: String = "",
    var status: String = "",
    var description: String = "",
    var imageUri: Int = R.drawable.img,
    var ownerId: String = "",
    var createdAt: Long = 0L
) {
    companion object {
        const val TYPE_LOST = "LOST"
        const val TYPE_FOUND = "FOUND"

        const val STATUS_OPEN = "OPEN"
        const val STATUS_COMPLETED = "COMPLETED"
    }
}

@Dao
interface LostFoundDao {

    @Query("SELECT * FROM LOSTFOUND_T ORDER BY createdAt DESC")
    fun getAll(): List<LostFoundItem>

    // 특정 id에 해당하는 레코드 찾는 함수 설정
    @Query("SELECT * FROM LOSTFOUND_T WHERE id=:id")
    fun foundItemById(id: Long): LostFoundItem

    @Insert
    fun insert(item: LostFoundItem)

    @Update
    fun update(item: LostFoundItem)

    @Delete
    fun delete(item: LostFoundItem)
}

@Database(
    entities = arrayOf(LostFoundItem::class),
    version = 1,
    exportSchema = false
)
abstract class LostFoundDatabase : RoomDatabase() {
    abstract fun lostFoundDao(): LostFoundDao
}