package edu.unisabana.dyas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParallelSum {

    // Configuramos el Logger
    private static final Logger logger = Logger.getLogger(ParallelSum.class.getName());

    // Función principal que inicia el proceso de cálculo de la suma paralelamente
    public static void main(String[] args) {
        // Creamos una lista de enteros
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }

        // Definimos el número de hilos
        int numThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try {
            // Dividimos la lista en partes y lanzamos cada una en un hilo
            int partSize = numbers.size() / numThreads;
            List<Future<Integer>> futures = new ArrayList<>();

            // Configuramos el Logger para mostrar en consola
            ConsoleHandler consoleHandler = new ConsoleHandler();
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.INFO);

            for (int i = 0; i < numThreads; i++) {
                // Definimos el índice de inicio y fin para cada parte
                int start = i * partSize;
                int end = (i + 1) * partSize;

                // Creamos una tarea Callable para sumar la parte asignada
                Callable<Integer> task = () -> {
                    int sum = 0;
                    logger.info("Thread " + Thread.currentThread().getName() + " está procesando el rango [" + start + ", " + end + ")");
                    for (int j = start; j < end; j++) {
                        sum += numbers.get(j);
                    }
                    logger.info("Thread " + Thread.currentThread().getName() + " ha completado su tarea con suma parcial: " + sum);
                    return sum;
                };
                futures.add(executorService.submit(task));
            }

            // Recogemos los resultados parciales de cada hilo y los sumamos
            int totalSum = 0;
            for (Future<Integer> future : futures) {
                totalSum += future.get(); // Future.get() bloquea hasta que el cálculo esté listo
            }

            System.out.println("La suma total es: " + totalSum);

        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Ocurrió un error en la ejecución: " + e.getMessage());
        } finally {
            // Cerramos el ExecutorService para liberar recursos
            executorService.shutdown();
        }
    }
}
