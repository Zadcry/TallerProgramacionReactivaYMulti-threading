package edu.unisabana.dyas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileProcessor {

    // Clase para el procesamiento de archivos individuales
    static class FileTask implements Callable<Integer> {
        private final File file;

        public FileTask(File file) {
            this.file = file;
        }

        @Override
        public Integer call() throws IOException {
            // Procesa el archivo (ejemplo: cuenta líneas)
            List<String> lines = Files.readAllLines(file.toPath());
            return lines.size();
        }
    }

    public static void main(String[] args) {
        // Directorio de archivos a procesar
        String path = "src/main/java/edu/unisabana/dyas/files";
        File directory = new File(path);

        // Imprimir la ruta completa para verificarla
        System.out.println("Ruta al directorio: " + directory.getAbsolutePath());

        // Listado de archivos en el directorio
        File[] files = directory.listFiles();
        if (files == null) {
            System.out.println("No se encontraron archivos.");
            return;
        }

        // Pool de hilos con un tamaño igual a la cantidad de archivos
        ExecutorService executorService = Executors.newFixedThreadPool(files.length);
        List<Future<Integer>> results = new ArrayList<>();

        // Creación y envío de tareas de procesamiento al ExecutorService
        for (File file : files) {
            FileTask task = new FileTask(file);
            results.add(executorService.submit(task));
        }

        // Recoge y muestra los resultados de cada archivo
        for (Future<Integer> result : results) {
            try {
                System.out.println("Líneas procesadas en archivo: " + result.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Cierra el ExecutorService
        executorService.shutdown();
    }
}