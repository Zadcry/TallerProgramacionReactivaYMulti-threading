package edu.unisabana.dyas;

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

