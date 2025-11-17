import java.util.concurrent.*;

public class CorridaSemControle {

    static int count = 0;

    public static void main(String[] args) throws Exception {
        int T = 8; // Número de Threads
        int M = 250_000; // Incrementos por Thread

        ExecutorService pool = Executors.newFixedThreadPool(T);

        Runnable r = () -> {
            for (int i = 0; i < M; i++) {
                // Seção Crítica NÃO PROTEGIDA
                // count++ não é atômico (leitura, incremento, escrita)
                count++;
            }
        };

        long t0 = System.nanoTime();

        for (int i = 0; i < T; i++) pool.submit(r);

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        long t1 = System.nanoTime();

        System.out.println("--- Versão com Condição de Corrida ---");
        System.out.printf("Valor Esperado = %d%n", T * M);
        System.out.printf("Valor Obtido   = %d%n", count); // Resultado será < Esperado
        System.out.printf("Tempo          = %.3fs%n", (t1 - t0) / 1e9);
    }
}