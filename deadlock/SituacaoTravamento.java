public class SituacaoTravamento {
    static final Object impressora = new Object();
    static final Object scanner = new Object();
    
    public static void main(String[] args) {
        Thread tarefa1 = new Thread(() -> {
            synchronized (impressora) {
                System.out.println("Tarefa 1: Acessando impressora");
                System.out.println("Tarefa 1: Acessando scanner");
                
                pausar(100);
                
                synchronized (scanner) {
                    System.out.println("Tarefa 1: Acesso finalizado");
                }
            }
        });
        
        Thread tarefa2 = new Thread(() -> {
            synchronized (scanner) {
                System.out.println("Tarefa 2: Acessando scanner");
                System.out.println("Tarefa 2: Acessando impressora");
                
                pausar(100);
                
                synchronized (impressora) {
                    System.out.println("Tarefa 2: Acesso finalizado");
                }
            }
        });
        
        tarefa1.start();
        tarefa2.start();
        
        try {
            Thread.sleep(3000);
            if (tarefa1.isAlive() || tarefa2.isAlive()) {
                System.out.println("\nDeadlock detectado");
                System.out.println("Aguardando liberação dos recursos");
                System.out.println("→ Tarefa 1 tem a impressora e quer o scanner");
                System.out.println("→ Tarefa 2 tem o scanner e quer a impressora");
                System.out.println("\nNenhuma pode avançar. Programa será encerrado.");
                System.exit(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    static void pausar(long tempo) {
        try {
            Thread.sleep(tempo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
