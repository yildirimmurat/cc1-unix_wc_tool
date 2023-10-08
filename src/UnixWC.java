import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.stream.Stream;

public class UnixWC {
    private static final String COMMAND_PREFIX = "ccwc";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                break;
            }

            processCommand(input);
        }

        scanner.close();
    }

    private static void processCommand(String input) {
        String[] parts = input.split(" ");

        if (parts.length > 3 || parts.length < 2 || !parts[0].equals(COMMAND_PREFIX)) {
            System.err.println("Invalid command: " + input);
            return;
        }

        String option = parts.length > 2 ? parts[1] : "";
        String fileName = parts[parts.length - 1];
        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            System.err.println("File not found: " + fileName);
            return;
        }

        long[] result;

        if (option.isEmpty()) {
            result = getResultAll(file);
        } else {
            result = getResultFromOption(option, file);
        }

        boolean hasError = false;
        StringBuilder countsToBePrinted = new StringBuilder();

        for (int i= 0; i < result.length; i++) {
            if (result[i] == -1) {
                hasError = true;
                break;
            }
            if (i != 0) {
                countsToBePrinted.append(" ");
            }
            countsToBePrinted.append(result[i]);
        }

        if (hasError) {
            System.err.println("Cannot read counts from file " + fileName);
        } else {
            System.out.println(countsToBePrinted + " " + fileName);
        }
    }

    private static long[] getResultFromOption(String option, File file) {
        long[] count = new long[1];

        switch (option) {
            case "-c":
                count[0] = getCountByte(file);
                break;
            case "-l":
                count[0] = getCountLine(file);
                break;
            case "-w":
                count[0] = getCountWord(file);
                break;
            case "-m":
                count[0] = getCountCharacter(file);
                break;
            default:
                count[0] = -1;
                break;
        }

        return count;
    }

    private static long[] getResultAll(File file) {
        Charset charset = StandardCharsets.UTF_8;
        long[] counts = { 0, 0, 0};

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            int c;
            boolean inWord = false;
            boolean isNewLine = true;

            while ((c = reader.read()) != -1) {
                byte[] bytes = Character.toString((char) c).getBytes(charset);
                counts[2] += bytes.length;

                if (Character.isWhitespace(c)) {
                    if (inWord) {
                        inWord = false;
                    }
                    if (c == '\n') {
                        counts[0]++;
                        isNewLine = true;
                    }
                } else {
                    if (!inWord) {
                        inWord = true;
                        counts[1]++;
                    }
                    isNewLine = false;
                }
            }

            if (!isNewLine) {
                counts[0]++;
            }
        } catch (IOException e) {
            counts[0] = -1;
        }

        return counts;
    }

    private static long getCountByte(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.getChannel().size();
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountLine(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines.count();
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountWord(File file) {
        Charset charset = StandardCharsets.UTF_8;
        int c;
        boolean inWord = false;
        long wordCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            while((c = reader.read()) != -1) {
                if (Character.isWhitespace(c)) {
                    if (inWord) {
                        inWord = false;
                    }
                } else {
                    if (!inWord) {
                        inWord = true;
                        wordCount++;
                    }
                }
            }

            return wordCount;
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountCharacter(File file) {
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
            long charCount = 0;
            while ((reader.read()) != -1) {
                charCount++;
            }

            return charCount;
        } catch (IOException e) {
            return -1;
        }
    }
}
