package app.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface AppItemDao {

    @RawQuery(observedEntities = [AppItem::class])
    fun getAll(query: SupportSQLiteQuery): Flowable<List<AppItem>>

    @Query("SELECT * FROM apps")
    fun getAll() : Flowable<List<AppItem>>

    @RawQuery(observedEntities = [AppItem::class])
    fun getAllSingle(query: SupportSQLiteQuery): Single<List<AppItem>>

    @Query("SELECT * FROM apps WHERE title = :name AND author = :vendor")
    operator fun get(name: String, vendor: String): AppItem?

    @Query("SELECT * FROM apps WHERE id = :id")
    operator fun get(id: Int): AppItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: AppItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(items: List<AppItem>)

    @Update
    fun update(item: AppItem)

    @Delete
    fun delete(item: AppItem)

    @Delete
    fun delete(items: List<AppItem>)

    @Query("DELETE FROM apps")
    fun deleteAll()

}