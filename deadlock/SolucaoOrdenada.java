public class SolucaoOrdenada {
    static final Object impressora = new Object();
    static final Object scanner = new Object();
    
    public static void main(String[] args) throws InterruptedException {
        Thread tarefa1 = new Thread(() -> {
            synchronized (impressora) {
                System.out.println("Task 1: Acessando impressora");
                
                pausar(100);
                
                synchronized (scanner) {
                    System.out.println("Task 1: Acessando scanner");
                    System.out.println("Task 1: Acesso completo");
                }
                System.out.println("Task 1: Devolvi o scanner");
            }
            System.out.println("Task 1: Devolvi a impressora");
        });
        
        Thread tarefa2 = new Thread(() -> {
            synchronized (impressora) {
                System.out.println("Task 2: Acessando impressora");
                
                pausar(100);
                
                synchronized (scanner) {
                    System.out.println("Task 2: Acessando scanner");
                    System.out.println("Task 2: Acesso completo");
                }
                System.out.println("Task 2: Devolvi o scanner");
            }
            System.out.println("Task 2: Devolvi a impressora");
        });
        
        tarefa1.start();
        tarefa2.start();
        
        tarefa1.join();
        tarefa2.join();
        
        System.out.println("\nExecução completa");
        System.out.println("Nenhum deadlock ocorreu");
    }
    
    static void pausar(long tempo) {
        try {
            Thread.sleep(tempo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
