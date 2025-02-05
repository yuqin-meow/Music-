package com.jagrosh.jmusicbot.playlist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileManager {

	public static boolean folderExists(Path path) {
        return Files.exists(path);
    }

    public static void createFolder(Path path) throws IOException {
        Files.createDirectory(path);
    }

    public static void createFile(Path path) throws IOException {
        Files.createFile(path);
    }

    public static void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    public static void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    public static List<String> readFileLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    public static File[] listFiles(Path folder, String extension) {
        File dir = folder.toFile();
        return dir.listFiles((pathname) -> pathname.getName().endsWith(extension));
    }
}
