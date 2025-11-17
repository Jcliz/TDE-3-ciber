import java.util.concurrent.*;

public class CorridaComSemaphore {

    static int count = 0;
    // Semáforo binário (1 permissão) e justo (true)
    static final Semaphore sem = new Semaphore(1, true);

    public static void main(String[] args) throws Exception {
        int T = 8; // Número de Threads
        int M = 250_000; // Incrementos por Thread

        ExecutorService pool = Executors.newFixedThreadPool(T);

        Runnable r = () -> {
            for (int i = 0; i < M; i++) {
                try {
                    // 1. Adquire a permissão (bloqueia se não estiver disponível)
                    sem.acquire();

                    // 2. Seção Crítica PROTEGIDA
                    count++;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 3. Libera a permissão (essencial estar no finally)
                    sem.release();
                }
            }
        };

        long t0 = System.nanoTime();

        for (int i = 0; i < T; i++) pool.submit(r);

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        long t1 = System.nanoTime();

        System.out.println("--- Versão Corrigida com Semáforo ---");
        System.out.printf("Valor Esperado = %d%n", T * M);
        System.out.printf("Valor Obtido   = %d%n", count); // Resultado será == Esperado
        System.out.printf("Tempo          = %.3fs%n", (t1 - t0) / 1e9);
    }
}