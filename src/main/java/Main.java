import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Option OPT_ENCRYPT = Option.builder("e")
            .longOpt("encrypt")
            .desc("encrypt file")
            .build();

    private static final Options OPTIONS = new Options().addOption(OPT_ENCRYPT);

    private static final DefaultParser DEFAULT_PARSE = new DefaultParser();

    private static Path INPUT_PATH;

    private static Path OUTPUT_PATH;

    private static boolean OPT_ENCRYPT_VALUE = false;

    public static void main(String[] args) throws IOException, ParseException {

        processOptionValues(args);

        generateOutputPath();

        if (Files.isDirectory(INPUT_PATH)) {
            copyDirectory();
        } else {
            copyFile(INPUT_PATH, OUTPUT_PATH);
        }

        System.out.println("finish");
    }

    private static void generateOutputPath() {
        OUTPUT_PATH = INPUT_PATH.getParent().resolve(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS-")) + INPUT_PATH.getFileName());
    }

    private static void processOptionValues(String[] args) throws ParseException {
        System.out.println("params = " + Arrays.toString(args));

        final CommandLine commandLine = DEFAULT_PARSE.parse(OPTIONS, args);
        OPT_ENCRYPT_VALUE = commandLine.hasOption(OPT_ENCRYPT);

        final String[] commandLinArgs = commandLine.getArgs();
        if (commandLinArgs.length != 1) {
            throw new RuntimeException("require ONE path");
        }

        System.out.println("path = " + commandLinArgs[0]);
        INPUT_PATH = Paths.get(args[args.length - 1]).toAbsolutePath();
        if (!Files.exists(INPUT_PATH)) {
            throw new RuntimeException("input path is not exists");
        }
    }

    private static void copyDirectory() throws IOException {
        final List<Path> createDirectoryList = new LinkedList<>();
        final List<Path> createFileList = new LinkedList<>();
        final List<Path> visitFileFailedList = new LinkedList<>();

        final Pattern pattern = Pattern.compile(INPUT_PATH.toString(), Pattern.LITERAL);
        final String quoteReplacement = Matcher.quoteReplacement(OUTPUT_PATH.toString());

        Files.walkFileTree(INPUT_PATH, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final Path directoryPath = getTargetPath(dir);
                Files.createDirectories(directoryPath);
                createDirectoryList.add(directoryPath);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final Path outPutPath = getTargetPath(file);
                copyFile(file, outPutPath);
                createFileList.add(outPutPath);
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

    }

    private static void copyFile(Path inputPath, Path outputPath) throws IOException {
        if (OPT_ENCRYPT_VALUE) {
            try (
                    final FileInputStream fileInputStream = new FileInputStream(inputPath.toFile());
                    final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                    final FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile());
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
            Files.copy(inputPath, outputPath);
        }
        System.out.println("create file : " + outputPath);
    }

}
