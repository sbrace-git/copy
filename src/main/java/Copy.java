import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Copy {
    private static boolean OPT_ENCRYPT = false;

    public static void main(String[] args) throws IOException, ParseException {
        final Option optEncrypt = Option.builder("e")
                .longOpt("encrypt")
                .desc("encrypt file")
                .build();
        final Options options = new Options();
        options.addOption(optEncrypt);
        final DefaultParser defaultParser = new DefaultParser();
        final CommandLine commandLine = defaultParser.parse(options, args);
        OPT_ENCRYPT = commandLine.hasOption(optEncrypt);

        System.out.println("params = " + Arrays.toString(args));
        final String[] commandLinArgs = commandLine.getArgs();
        if (commandLinArgs.length != 1) {
            System.err.println("require ONE path");
            return;
        }
        System.out.println("path = " + commandLinArgs[0]);

        final Path inputPath = Paths.get(args[args.length - 1]).toAbsolutePath();

        if (!Files.exists(inputPath)) {
            System.out.println("input path is not exists");
            return;
        }

        final Path outputFilePath = inputPath.getParent().resolve(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS-")) + inputPath.getFileName());

        if (Files.isDirectory(inputPath)) {

            final List<Path> createDirectoryList = new LinkedList<>();
            final List<Path> createFileList = new LinkedList<>();
            final List<Path> visitFileFailedList = new LinkedList<>();

            final Pattern pattern = Pattern.compile(inputPath.toString(), Pattern.LITERAL);
            final String quoteReplacement = Matcher.quoteReplacement(outputFilePath.toString());

            Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final Path directoryPath = getTargetPath(dir);
                    Files.createDirectories(directoryPath);
                    createDirectoryList.add(directoryPath);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path filePath = getTargetPath(file);
                    Files.copy(file, filePath);
                    createFileList.add(filePath);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println("visit file failed path = " + file);
                    exc.printStackTrace();
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
            visitFileFailedList.forEach(System.err::println);
        } else {
            if (OPT_ENCRYPT) {
                try (
                        final FileInputStream fileInputStream = new FileInputStream(inputPath.toFile());
                        final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        final FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath.toFile());
                        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                ) {
                    int n;
                    while ((n = bufferedInputStream.read()) != -1) {
                        bufferedOutputStream.write(255 - n);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Files.copy(inputPath, outputFilePath);
            }
            System.out.println("create file : " + outputFilePath);
        }

        System.out.println("finish");
    }
}
