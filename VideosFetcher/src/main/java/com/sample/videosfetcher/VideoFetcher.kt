package com.sample.videosfetcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sample.videosfetcher.models.Folder
import com.sample.videosfetcher.models.FolderSortOrder
import com.sample.videosfetcher.models.VideosSortOrder
import java.io.File
import java.text.Collator

class VideoFetcher(context: Context) {
    private var mContext = context
    private var allVideos: MutableList<File> = mutableListOf()
    private var isFetching = false

    fun getAllVideos(order: VideosSortOrder? = null, callback: (MutableList<File>?) -> Unit) {
        if (checkPermissionForReadExternalStorage() || globalPermissionCheck()) {
            val columns = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_MODIFIED
            )
            try {
                mContext.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, null
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val file = File(cursor.getString(dataColumn))
                        if (file.length() > 100) {
                            allVideos.add(file)
                        }
                    }
                }
            } catch (e: Exception) {
            } catch (e: java.lang.Exception) {
            } catch (e: java.lang.IllegalArgumentException) {
            } finally {
                if (allVideos.isNotEmpty()) {
                    val collator = Collator.getInstance()
                    when (order) {
                        null -> {
                            callback(allVideos)
                        }
                        VideosSortOrder.NameAscending -> {
                            allVideos = allVideos.sortedWith { a1, a2 ->
                                collator.compare(a1.nameWithoutExtension, a2.nameWithoutExtension)
                            } as MutableList<File>
                            callback(allVideos)
                        }
                        VideosSortOrder.NameDescending -> {
                            allVideos = allVideos.sortedWith { a1, a2 ->
                                collator.compare(
                                    a2.nameWithoutExtension,
                                    a1.nameWithoutExtension
                                )
                            } as MutableList<File>
                            callback(allVideos)
                        }
                        VideosSortOrder.SizeAscending -> {
                            allVideos =
                                allVideos.sortedByDescending { it.length() } as MutableList<File>
                            callback(allVideos)
                        }
                        VideosSortOrder.SizeDescending -> {
                            allVideos = allVideos.sortedBy { it.length() } as MutableList<File>
                            callback(allVideos)
                        }
                        VideosSortOrder.LastModifiedAscending -> {
                            allVideos = allVideos.sortedByDescending { it.lastModified() } as MutableList<File>
                            callback(allVideos)
                        }
                        VideosSortOrder.LastModifiedDescending -> {
                            allVideos = allVideos.sortedBy { it.lastModified() } as MutableList<File>
                            callback(allVideos)
                        }
                    }
                } else {
                    callback(null)
                }
            }
        } else {
            Log.i(TAG, "FileFetcher: Permission not found")
            callback(null)
        }
    }


    private fun checkPermissionForReadExternalStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED)
        } else false
    }

    fun getDataAndFolders(imageSortOrder: VideosSortOrder? = null, foldersSortOrder: FolderSortOrder? = null, callback: (list: MutableList<Folder>?) -> Unit) {
        getAllVideos(imageSortOrder) { files ->
            if (files != null) {
                var grouped = allVideos.groupBy { it.parentFile }.map {
                    it.key?.let { it1 ->
                        Folder(
                            it1.nameWithoutExtension,
                            it.value as MutableList<File>
                        )
                    }
                }
                when (foldersSortOrder) {
                    null -> {
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.NameAscending -> {
                        val collator = Collator.getInstance()
                        grouped = grouped.sortedWith { c1, c2 -> collator.compare(c1?.name, c2?.name) }
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.NameDescending -> {
                        val collator = Collator.getInstance()
                        grouped = grouped.sortedWith { c1, c2 -> collator.compare(c2?.name, c1?.name) }
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.LengthAscending -> {
                        grouped = grouped.sortedBy {
                            it?.folderLength
                        }
                        callback(grouped as MutableList<Folder>)
                    }
                    FolderSortOrder.LengthDescending -> {
                        grouped = grouped.sortedByDescending {
                            it?.folderLength
                        }
                        callback(grouped as MutableList<Folder>)
                    }
                }
            } else {
                callback(null)
            }
        }
    }


    private fun globalPermissionCheck(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (checkPermissionForReadExternalStorage()) {
                return true
            }
        } else {
            if (Environment.isExternalStorageManager()) {
                return true
            }
        }
        return false
    }

    companion object {
        const val TAG = "VideoFetcherTAG"
    }
}