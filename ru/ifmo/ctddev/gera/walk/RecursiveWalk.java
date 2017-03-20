package ru.ifmo.ctddev.gera.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Created by penguinni on 08.02.17.
 * penguinni hopes it will work.
 */
public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length != 2) {
            argError("Expected 2 file names, found " + args.length);
            return;
        }

        Path inputPath, outputPath;
        try {
            inputPath = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            argFileError("input", e.getInput() + " (" + e.getReason() + ")");
            return;
        }

        try {
            outputPath = Paths.get(args[1]);

            Path parent = outputPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    argFileError("output", "Could not create directory " + parent + ": " + e.getMessage());
                }
            }
        } catch (InvalidPathException e) {
            argFileError("output", e.getInput() + " (" + e.getReason() + ")");
            return;
        }

        try (BufferedReader input = new BufferedReader(
                new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8))) {
            try (PrintWriter output = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8))) {
                String nextPath;
                while ((nextPath = input.readLine()) != null) {
                    try {
                        recursiveWalk(Paths.get(nextPath), output);
                    } catch (InvalidPathException e) {
                        output.println(String.format("%08x ", 0) + nextPath);
                        hashWarning(e.getInput() + " (" + e.getReason() + ")");
                    }
                }
            } catch (IOException e) {
                argFileError("output", e.getMessage());
            }
        } catch (IOException e) {
            argFileError("input", e.getMessage());
        }
    }

    private static void recursiveWalk(Path path, PrintWriter output) {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path nextName : ds) {
                    recursiveWalk(nextName, output);
                }
            } catch (IOException e) {
                argFileError("input", e.getMessage());
            }
        } else {
            output.println(String.format("%08x ", calcHash(path.toString())) + path.toString());
        }
    }

    private static int calcHash(String fileName) {
        try (FileInputStream hashFile = new FileInputStream(fileName)) {
            byte[] bytes = new byte[1024];
            int hash = 0x811c9dc5, l;
            try {
                while ((l = hashFile.read(bytes)) >= 0) {
                    for (int i = 0; i < l; i++) {
                        hash = (hash * 0x01000193) ^ (bytes[i] & 0xff);
                    }
                }
            } catch (IOException e) {
                hashWarning(e.getMessage());
                return 0;
            }
            return hash;
        } catch (IOException e) {
            hashWarning(e.getMessage());
            return 0;
        }
    }

    private static void argError(String msg) {
        System.err.println("Argument error: " + msg);
    }

    private static void argFileError(String type, String msg) {
        System.err.println("Invalid " + type + " file: " + msg);
    }

    private static void hashWarning(String msg) {
        System.err.println("Hash file warning: " + msg);
    }
}
