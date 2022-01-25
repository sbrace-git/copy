import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Copy {

    public static void main(String[] args) throws IOException {
        System.out.println("params = " + Arrays.toString(args));
        if (args.length == 0) {
            throw new RuntimeException("input file path is required");
        }
        Path inputFilePath = Paths.get(args[0]);

        String inputFileName = inputFilePath.getFileName().toString();
        System.out.println("input file name = " + inputFileName);

        Path outputFilePath = inputFilePath.toAbsolutePath().getParent().resolve(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS-")) + inputFileName);
        System.out.println("output file path = " + outputFilePath);

        Files.copy(inputFilePath, outputFilePath);
    }
}
