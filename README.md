
# Java Batch YouTube Downloader

A Java-based batch downloader using yt-dlp and FFmpeg.

## Installation
```bash
winget install yt-dlp.yt-dlp
where yt-dlp
winget install Gyan.FFmpeg
where ffmpeg
```
## Features

- Download multiple videos concurrently
- Configurable worker threads
- Real-time download percentage
- Download speed and ETA
- Different console colors for workers
- Automatic video/audio merging into MP4
- Success and failure tracking

## Requirements

- Java 21+
- yt-dlp
- FFmpeg

## Configuration

Configure executable paths in BatchDownloader.java:

- YT_DLP
- FFMPEG_LOCATION

## Run

```bash
java BatchDownloader.java
```

## Default Settings

- Concurrent workers: 3
- Output folder: downloads/
- Output format: MP4
