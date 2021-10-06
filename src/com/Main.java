package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.stream.Stream;

class Main {

    public static void main(String[] args) {
        cleanResultDirectory();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Phaser phaser = new Phaser(1);

        MyTask.submit(getDirectoryPath(), executorService, phaser);

        phaser.arriveAndAwaitAdvance();
        executorService.shutdown();

        System.out.println("done");
    }

    private static void cleanResultDirectory() {
        try {
            Path result = Path.of("result");
            if (Files.exists(result)) {
                try (Stream<Path> paths = Files.walk(result)) {
                    for (Path path : ((Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator)) {
                        Files.delete(path);
                    }
                }
            }
            Files.createDirectory(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getDirectoryPath() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("enter directory path: ");
                Path ret = Path.of(scanner.nextLine());
                if (Files.isDirectory(ret)) {
                    return ret;
                } else {
                    System.out.println("not a directory");
                }
            }
        }
    }

}