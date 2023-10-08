import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class UnixToolCcwc {
    public static void main(String[] args) throws IOException {
        if (isPipedInput()) {
            processPipedInput(args);
        } else {
            processInteractiveInput(args);
        }
    }
    private static boolean isPipedInput() {
        try {
            if (System.console() != null) {
                // There is a console, so it's not piped input
                return false;
            }

            // Check if System.in is connected to a terminal or a pipe
            // If available() returns 0 or less, it's likely piped input
            return System.in.available() <= 0;

            // If available() returns a positive value, it's likely terminal input
        } catch (IOException e) {
            return false;
        }
    }

    private static void processPipedInput(String[] args) throws IOException {
        String option = args.length == 1 ? args[0] : "";
        processCommand(option, System.in, null);
    }

    private static void processInteractiveInput(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: ccwc [option] [filename]");
            return;
        }

        String option = args.length == 2 ? args[0] : "";
        String fileName = args[args.length - 1];
        InputStream inputStream;

        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            System.err.println("File not found: " + fileName);
            return;
        }

        try {
            inputStream = Files.newInputStream(file.toPath());
        } catch (IOException e) {
            System.err.println("Error opening file: " + fileName);
            return;
        }

        processCommand(option, inputStream, fileName);
    }

    private static void processCommand(String option, InputStream inputStream, String fileName) {
        long[] result;

        if (option.isEmpty()) {
            result = getResultAll(inputStream);
        } else {
            result = getResultFromOption(option, inputStream);
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
            System.err.println("Cannot read counts from input stream");
        } else {
            String textToBePrinted = "";
            if (fileName != null) {
                textToBePrinted += " " + fileName;
            }
            System.out.println(countsToBePrinted + textToBePrinted);
        }
    }

    private static long[] getResultFromOption(String option, InputStream inputStream) {
        long[] count = new long[1];

        switch (option) {
            case "-c":
                count[0] = getCountByte(inputStream);
                break;
            case "-l":
                count[0] = getCountLine(inputStream);
                break;
            case "-w":
                count[0] = getCountWord(inputStream);
                break;
            case "-m":
                count[0] = getCountCharacter(inputStream);
                break;
            default:
                count[0] = -1;
                break;
        }

        return count;
    }

    private static long[] getResultAll(InputStream inputStream) {
        Charset charset = StandardCharsets.UTF_8;
        long[] counts = { 0, 0, 0};

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
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

    private static long getCountByte(InputStream inputStream) {
        try {
            long count = 0;
            int bytesRead;
            byte[] buffer = new byte[1024];

            while((bytesRead = inputStream.read(buffer)) != -1) {
                count += bytesRead;
            }

            return count;
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountLine(InputStream inputStream) {
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            long lineCount = 0;

            while(reader.readLine() != null) {
                lineCount++;
            }

            return lineCount;
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountWord(InputStream inputStream) {
        Charset charset = StandardCharsets.UTF_8;
        int c;
        boolean inWord = false;
        long wordCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
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

    private static long getCountCharacter(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
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
