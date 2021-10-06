package com;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.stream.Stream;

class MyTask implements Runnable {

    private final Path dir;
    private final ExecutorService executorService;
    private final Phaser phaser;

    MyTask(Path dir, ExecutorService executorService, Phaser phaser) {
        this.dir = dir;
        this.executorService = executorService;
        this.phaser = phaser;
    }

    @Override
    public void run() {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir)) {
            for (Path path : paths) {
                if (Files.isDirectory(path)) {
                    submit(path, executorService, phaser);
                } else if (Files.isRegularFile(path)) {
                    if (path.toString().endsWith(".java")) {
                        processJavaSourceFile(path);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            phaser.arrive();
        }
    }

    private void processJavaSourceFile(Path file) throws IOException {
        System.out.println("processing " + file + "...");
        try (Stream<String> lines = Files.lines(file)) {
            Files.write(Path.of("result", file.getFileName().toString()),
                    (Iterable<String>) lines.map(this::reverseString)::iterator);
        }
    }

    private String reverseString(String str) {
        return new StringBuilder(str).reverse().toString();
    }

    static void submit(Path dir, ExecutorService executorService, Phaser phaser) {
        phaser.register();
        executorService.submit(new MyTask(dir, executorService, phaser));
    }

}