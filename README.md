# Fetch All Videos and Folders Containing Videos in Android
## Fast, Simple, Easy to Use
Fetch all videos along with folders containing videos in Android device

## Import

### Add it in your root build.gradle at the end of repositories:

```
allprojects {
  repositories {
  ...
  maven { url 'https://jitpack.io' }
  }
}
```

### Add the dependency

```
dependencies {
  implementation 'com.github.Irfan-Karim:Fetch-All-Videos-Sample:1.0.0'
}
```

## Disclaimer
Get Read Write permission or Manage all storage permission before initialization else fetched folder and videos will be null

## Fetch All Videos

### Create Instance of VideoFetcher

```
val videoFetcher = VideoFetcher(context)
```

### Call getAllVideos to get all video files in device

```
CoroutineScope(Dispathers.IO).launch {
  videoFetcher.getAllVideos { file ->
    Log.i("TAG", "getVideos: ${file?.size}")
  }
}
```

### Sort The Videos

use VideoSortOrder._ as followed

```
CoroutineScope(Dispathers.IO).launch {
  videoFetcher.getAllVideos(VideoSortOrder.LastModifiedAscending) { file ->
    Log.i("TAG", "getVideos: ${file?.size}")
  }
}
```

## Fetch All Folders Containing Videos

### Create Instance of VideoFetcher

```
val videoFetcher = VideoFetcher(context)
```

### Call getDataAndFolders to get all video folders in device

```
imageFetcher.getDataAndFolders { folder ->
  Log.i("TAG", "getImages: ${folder?.size}")
}
```

Folder will contain the name of the folder and all the videos contained in that folder

```
folder.foreach { it ->
  log.i("TAG", ${it.name})
  log.i("TAG", ${it.data.size})
}
```

### Sort the Folders

Use FolderSortOrder._ as followed

```
CoroutineScope(Dispatchers.IO).launch {
  videoFetcher.getDataAndFolders(null,FolderSortOrder.LengthAscending) { folder ->
    Log.i("TAG", "getImages: ${folder?.size}")
  }
}
```

### Sort the both Folders and Videos

Use VideosSortOrder._ and FolderSortOrder._ as followed

```
CoroutineScope(Dispatchers.IO).launch {
  videoFetcher.getDataAndFolders(VideosSortOrder.LastModifiedAscending,FolderSortOrder.LengthAscending) { folder ->
    Log.i("TAG", "getVideos: ${folder?.size}")
  }
}
```
