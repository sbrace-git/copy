import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Path outputFilePath = inputPath.getParent().resolve(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS-")) + inputPath.getFileName());

        if (Files.isDirectory(inputPath)) {

            final List<Path> visitFileFailedList = new LinkedList<>();
            final List<Path> createDirectoryList = new LinkedList<>();
            final List<Path> createFileList = new LinkedList<>();

            final Pattern pattern = Pattern.compile(inputPath.toString(), Pattern.LITERAL);
            final String quoteReplacement = Matcher.quoteReplacement(outputFilePath.toString());

            Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path directoryPath = getTargetPath(dir);
                    Files.createDirectories(directoryPath);
                    createDirectoryList.add(directoryPath);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path filePath = getTargetPath(file);
                    Files.copy(file, filePath);
                    createFileList.add(filePath);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    visitFileFailedList.add(file);
                    return FileVisitResult.CONTINUE;
                }

                /**
                 * build a path to the target
                 * @param path
                 *        the path to the file to copy
                 * @return the path to the target
                 */
                private Path getTargetPath(Path path) {
                    return Paths.get(pattern.matcher(path.toString()).replaceFirst(quoteReplacement));
                }
            });

            System.out.println("create directory count = " + createDirectoryList.size());
            createDirectoryList.forEach(System.out::println);

            System.out.println("create file count = " + createFileList.size());
            createFileList.forEach(System.out::println);

            System.out.println("visit file failed count = " + visitFileFailedList.size());
            visitFileFailedList.forEach(System.out::println);
        } else {
            Files.copy(inputPath, outputFilePath);
            System.out.println("create file : " + outputFilePath);
        }

        System.out.println("finish");
    }
}
