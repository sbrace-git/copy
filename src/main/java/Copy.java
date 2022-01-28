package src.main.java;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Copy {

    public static void main(String[] args) throws IOException {
        System.out.println("params = " + Arrays.toString(args));
        if (args.length == 0) {
            System.out.println("input path is required");
            return;
        }

        Path inputPath = Paths.get(args[0]).toAbsolutePath();

        if (!Files.exists(inputPath)) {
            System.out.println("input path is not exists");
            return;
        }

        Path outputFilePath = inputPath.getParent().resolve(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS-")) + inputPath.getFileName());

        if (Files.isDirectory(inputPath)) {
            final String inputAbsolutePath = inputPath.toAbsolutePath().toString();
            final String outputAbsolutePath = outputFilePath.toAbsolutePath().toString();

            Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path directoryPath = Paths.get(dir.toString().replace(inputAbsolutePath, outputAbsolutePath));
                    Files.createDirectories(directoryPath);
                    System.out.println("create directory : " + directoryPath.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path filePath = Paths.get(file.toString().replace(inputAbsolutePath, outputAbsolutePath));
                    Files.copy(file, filePath);
                    System.out.println("create file : " + filePath.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.out.println("visitFileFailed file = " + file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(inputPath, outputFilePath);
            System.out.println("create file : " + outputFilePath.toAbsolutePath());
        }

        System.out.println("finish");
    }
}
