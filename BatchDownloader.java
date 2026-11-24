
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchDownloader {
    //path of ffmpeg installed
    private static final String FFMPEG_LOCATION =
        System.getProperty("user.home")
        + "\\AppData\\Local\\Microsoft\\WinGet\\Packages\\yt-dlp.FFmpeg_Microsoft.Winget.Source_8wekyb3d8bbwe\\ffmpeg-N-128251-g6d1bfd7609-win64-gpl\\bin";
    //path of yt_dlp install
    private static final String YT_DLP =
        System.getProperty("user.home")
        + "\\AppData\\Local\\Microsoft\\WinGet\\Links\\yt-dlp.exe";

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failedCount = new AtomicInteger(0);

    public static void downloadVideo(
            String url, String outputFolder, int videoNumber) {

        String prefix = "[VIDEO " + videoNumber + "] ";

        try {
            System.out.println( colorLog(videoNumber, "Starting download..."));

            ProcessBuilder builder = new ProcessBuilder(
            YT_DLP,
            "--newline",
            "--no-keep-video",
            "--ffmpeg-location", FFMPEG_LOCATION,
            "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
            "--merge-output-format", "mp4",
            "-o", outputFolder + "/%(title)s.%(ext)s",
            url
            );

            builder.redirectErrorStream(true);

            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.contains("[download]")) {

                        // Download progress
                        if (line.contains("%")) {
                            System.out.println(colorLog(videoNumber, line.trim()));
                        }

                    } else if (line.contains("[Merger]")) {

                        System.out.println(
                            prefix + "Merging video and audio..."
                        );

                    } else if (line.contains("[ExtractAudio]")) {

                        System.out.println(
                            prefix + "Processing audio..."
                        );

                    } else if (line.contains("ERROR")) {

                        System.err.println(prefix + line.trim());

                    } else if (line.contains("Destination:")) {

                        System.out.println(
                            prefix + line.trim()
                        );
                    }
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {

                successCount.incrementAndGet();

                System.out.println(colorLog(videoNumber, "SUCCESS - Download completed!"));

            }

        } catch (Exception e) {

            failedCount.incrementAndGet();

            System.err.println(colorLog(videoNumber, "FAILED - " + e.getMessage()));
            
        }
    }

    public static void main(String[] args) {
        //path of output folder
        String outputFolder = "downloads/";

        new File(outputFolder).mkdirs();
        //Insert url that need to download here
        List<String> urls = Arrays.asList(
            "https://www.youtube.com/watch?Example1",
            "https://www.youtube.com/watch?Example2"
            );

        int maxWorkers = 10;

        ExecutorService executor =
            Executors.newFixedThreadPool(maxWorkers);

        System.out.println("==================================================");
        System.out.println("Starting batch download: " + urls.size() + " videos");
        System.out.println("Concurrent workers: " + maxWorkers);
        System.out.println("==================================================");

        for (int i = 0; i < urls.size(); i++) {

            final int videoNumber = i + 1;
            final String url = urls.get(i);

            executor.submit(() ->
                downloadVideo(url, outputFolder, videoNumber)
            );
        }

        executor.shutdown();

        try {

            executor.awaitTermination(
                Long.MAX_VALUE, TimeUnit.NANOSECONDS
            );

            System.out.println("\n==================================================");
            System.out.println("ALL DOWNLOADS COMPLETED");
            System.out.println("Successful: " + successCount.get());
            System.out.println("Failed: " + failedCount.get());
            System.out.println("Total: " + urls.size());
            System.out.println("==================================================");

        } catch (InterruptedException e) {

            executor.shutdownNow();
            Thread.currentThread().interrupt();

            System.err.println("Downloads interrupted!");
        }
    }

private static final String RESET = "\u001B[0m";

private static final String[] COLORS = {
    "\u001B[31m", // Red
    "\u001B[32m", // Green
    "\u001B[33m", // Yellow
    "\u001B[34m", // Blue
    "\u001B[35m", // Purple
    "\u001B[36m", // Cyan
    "\u001B[91m", // Bright Red
    "\u001B[92m", // Bright Green
    "\u001B[94m", // Bright Blue
    "\u001B[95m"  // Bright Purple
};

private static String colorLog(int worker, String message) {
    String color = COLORS[(worker - 1) % COLORS.length];

    return color + "[WORKER " + worker + "] "
            + message + RESET;
}

}
