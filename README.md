# Universidad de la Sabana
## Diseño y Arquitectura de Software
### Taller 3 - Programacion Reactiva & Multi-threading
Taller de: Julian Mauricio Zafra Moreno

**ATENCION!** La parte 2 del taller se encuentra en la rama "Multithreading"

#### Parte 1: Programacion Reactiva
Caso de Uso: Monitoreo en tiempo real de transacciones financieras.
Imagina un sistema que recibe transacciones financieras de una API externa en tiempo real, y se quiere:

- Filtrar solo las transacciones de alto valor.
- Convertir los montos de las transacciones a otra moneda (por ejemplo, a USD).
- Agrupar estas transacciones por origen (banco, tarjeta de crédito, etc.).
- Procesar y guardar las transacciones de alto valor en una base de datos para su análisis posterior.

Se utiliza RxJava y los operadores filter, map, groupBy, flatMap, doOnNext.

```java

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

public class ReactiveTransactionMonitor {

    public static void main(String[] args) {

        // Simulación de transacciones entrantes
        Observable<Transaction> transactions = Observable.interval(1, TimeUnit.SECONDS)
                .map(i -> new Transaction(i, (double) (i * 150), "Bank" + (i % 3)))
                .doOnNext(transaction -> System.out.println("##### Transacción original: " + transaction)) // Log de transacciones originales
                .subscribeOn(Schedulers.io());

        transactions
                // Filtrar solo las transacciones con un monto mayor a 500
                .filter(transaction -> transaction.getAmount() > 500)
                // Convertir el monto a USD (asumamos una tasa de conversión)
                .map(transaction -> new Transaction(transaction.getId(), transaction.getAmount() * 0.1, transaction.getOrigin()))
                // Agrupar las transacciones por origen
                .groupBy(Transaction::getOrigin)
                .flatMap(groupedObservable ->
                        // Procesar cada grupo de transacciones de forma separada
                        groupedObservable.doOnNext(transaction -> {
                            System.out.println("Procesando transacción de: " + transaction.getOrigin());
                            // Aquí podríamos guardar en la base de datos
                        })
                )
                .doOnError(error -> System.err.println("Error: " + error))
                // Suscribirse para comenzar a procesar el flujo
                .subscribe(
                        transaction -> System.out.println("Transacción procesada: " + transaction),
                        Throwable::printStackTrace
                );

        // Mantener la aplicación corriendo para ver el procesamiento continuo
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Transaction {
        private final Long id;
        private final Double amount;
        private final String origin;

        public Transaction(Long id, Double amount, String origin) {
            this.id = id;
            this.amount = amount;
            this.origin = origin;
        }

        public Long getId() {
            return id;
        }

        public Double getAmount() {
            return amount;
        }

        public String getOrigin() {
            return origin;
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "id=" + id +
                    ", amount=" + amount +
                    ", origin='" + origin + '\'' +
                    '}';
        }
    }
}
```

#### Parte 2: Multi-threading
**El codigo de esta parte se encuentra en la rama "multithreading"**

Caso de uso: Imagina que se necesita procesar una lista grande de archivos de texto. Cada archivo contiene datos que deben analizarse y los resultados deben guardarse en una base de datos. Procesar los archivos secuencialmente puede ser ineficiente, así que usar multithreading permitirá dividir la carga entre varios hilos, procesando varios archivos simultáneamente.

Se usan Executor Service y Callable. ExecutorService manejará la ejecución de cada Callable en paralelo y nos permitirá monitorear y obtener los resultados de cada hilo.

```java
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
```
