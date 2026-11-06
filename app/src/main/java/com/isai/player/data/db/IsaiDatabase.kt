package com.isai.player.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isai.player.data.db.dao.AlbumDao
import com.isai.player.data.db.dao.ArtistDao
import com.isai.player.data.db.dao.FavoriteDao
import com.isai.player.data.db.dao.FolderDao
import com.isai.player.data.db.dao.HistoryDao
import com.isai.player.data.db.dao.PlaylistDao
import com.isai.player.data.db.dao.QueueDao
import com.isai.player.data.db.dao.TrackDao
import com.isai.player.data.db.entity.FavoriteEntity
import com.isai.player.data.db.entity.PlayHistoryEntity
import com.isai.player.data.db.entity.PlaylistEntity
import com.isai.player.data.db.entity.PlaylistTrackEntity
import com.isai.player.data.db.entity.QueueItemEntity
import com.isai.player.data.db.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        FavoriteEntity::class,
        PlayHistoryEntity::class,
        QueueItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class IsaiDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun folderDao(): FolderDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun queueDao(): QueueDao

    companion object {
        const val DATABASE_NAME = "isai_player.db"
    }
}
