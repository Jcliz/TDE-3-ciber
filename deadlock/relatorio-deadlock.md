# Deadlock em recursos compartilhados

## 1. Introdução

O problema de deadlock (travamento) é uma situação crítica em sistemas concorrentes onde duas ou mais threads ficam permanentemente bloqueadas, cada uma esperando por recursos que estão sob controle das outras. Os códigos apresentados — `SituacaoTravamento.java` e `SolucaoOrdenada.java` — ilustram de forma prática tanto o problema quanto sua solução, usando o cenário de compartilhamento de uma impressora e um scanner.

## 2 Como funciona

Trabalhamos com um cenário de 2 threads (Tarefa 1 e Tarefa 2) e 2 recursos compartilhados (impressora e scanner). Ambas as tarefas precisam acessar os dois recursos para completar seu trabalho, mas apenas uma thread pode usar cada recurso por vez.

Os recursos são representados como objetos que servem de travas (locks):

```java
static final Object impressora = new Object();
static final Object scanner = new Object();
```

Cada recurso pode estar em um de dois estados:

1. **Disponível**: Nenhuma thread está usando o recurso
2. **Em uso**: Uma thread adquiriu o lock e está usando o recurso

A primeira implementação (`SituacaoTravamento.java`) parece razoável à primeira vista:

```java
Thread tarefa1 = new Thread(() -> {
    synchronized (impressora) {
        System.out.println("Tarefa 1: Acessando impressora");
        pausar(100);
        
        synchronized (scanner) {
            System.out.println("Tarefa 1: Acesso finalizado");
        }
    }
});

Thread tarefa2 = new Thread(() -> {
    synchronized (scanner) {
        System.out.println("Tarefa 2: Acessando scanner");
        pausar(100);
        
        synchronized (impressora) {
            System.out.println("Tarefa 2: Acesso finalizado");
        }
    }
});
```

## 3. Por que o deadlock acontece

O deadlock surge quando ambas as threads fazem exatamente o seguinte ao mesmo tempo:

1. **Tarefa 1** adquire o lock da impressora
2. **Tarefa 2** adquire o lock do scanner
3. **Tarefa 1** tenta adquirir o lock do scanner (mas está ocupado pela Tarefa 2)
4. **Tarefa 2** tenta adquirir o lock da impressora (mas está ocupado pela Tarefa 1)
5. Resultado: ambas ficam esperando eternamente, e nenhuma consegue prosseguir

Para entender melhor o problema, precisamos reconhecer as quatro condições que, quando presentes juntas, causam um deadlock:

1. **Exclusão Mútua**: Cada recurso (impressora e scanner) só pode ser usado por uma thread de cada vez
2. **Manter e Esperar**: Uma thread segura um recurso enquanto espera pelo outro
3. **Não Preempção**: Ninguém pode forçar uma thread a liberar seu recurso; ele só pode ser liberado voluntariamente
4. **Espera Circular**: Forma-se um ciclo onde cada thread espera por um recurso que está nas mãos da outra (Tarefa 1 → scanner → Tarefa 2 → impressora → Tarefa 1)

O problema da abordagem ingênua é que todas as quatro condições aparecem, tornando o deadlock não apenas possível, mas praticamente garantido.

O código implementa um mecanismo simples de detecção:

```java
Thread.sleep(3000);
if (tarefa1.isAlive() || tarefa2.isAlive()) {
    System.out.println("\nDeadlock detectado");
    System.out.println("→ Tarefa 1 tem a impressora e quer o scanner");
    System.out.println("→ Tarefa 2 tem o scanner e quer a impressora");
    System.exit(1);
}
```

Após 3 segundos, se alguma thread ainda estiver viva (bloqueada), o programa reconhece o deadlock e encerra forçadamente.

## 4. A solução

A solução implementada em `SolucaoOrdenada.java` é elegantemente simples: criar uma ordem global de aquisição de recursos. Funciona assim:

- Ambas as threads devem adquirir os recursos na mesma ordem
- Sempre adquira primeiro a impressora, depois o scanner

Essa solução quebra a condição de Espera Circular (a quarta condição de Coffman):

- Com todas as threads seguindo a mesma ordem de aquisição, é impossível formar um ciclo de espera
- Uma ordem global fixa impede que se forme um círculo fechado de dependências
- Sempre haverá pelo menos uma thread capaz de adquirir ambos os recursos e completar seu trabalho

**Exemplo prático**: Se ambas as threads tentarem adquirir recursos ao mesmo tempo seguindo a ordem "impressora → scanner", apenas uma conseguirá pegar a impressora primeiro. A outra terá que esperar, mas quando a primeira terminar e liberar ambos os recursos, a segunda poderá pegá-los sem problemas.

## 5. Implementação

```java
static final Object impressora = new Object();
static final Object scanner = new Object();
```

```java
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
```

```java
static void pausar(long tempo) {
    try {
        Thread.sleep(tempo);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```
A ordem global de aquisição elimina completamente a espera circular:

- Todas as threads seguem a mesma ordem de aquisição
- É impossível formar um ciclo de dependências
- Se uma thread tem a impressora, a outra não pode ter o scanner sem também ter a impressora
- Sempre haverá pelo menos uma thread capaz de progredir e completar seu trabalho

**Progresso**: Como não há deadlock possível, o sistema sempre avança. Pelo menos uma thread estará executando ou completando seu trabalho.

**Justiça**: Nenhuma thread fica eternamente bloqueada. O mecanismo de `synchronized` do Java usa filas internas que garantem que todas as threads terão sua vez.

**Completude**: Ambas as threads eventualmente completarão sua execução, como demonstrado pelas chamadas `join()` bem-sucedidas.

**Situação de Travamento**:
- Tarefa 1: impressora → scanner
- Tarefa 2: scanner → impressora
- Resultado: **DEADLOCK** (espera circular)

**Solução Ordenada**:
- Tarefa 1: impressora → scanner
- Tarefa 2: impressora → scanner
- Resultado: **SUCESSO** (sem espera circular)

A diferença fundamental está na consistência da ordem de aquisição.

## 6. Conclusão

A solução por ordem global de aquisição de recursos é um exemplo perfeito de como uma regra simples pode resolver um problema complexo. Com uma única mudança — "ambas as threads devem adquirir recursos na mesma ordem" — conseguimos eliminar completamente a possibilidade de deadlock.

O que a solução nos garante:
- ✓ **Sem deadlock**: A espera circular é impossível
- ✓ **Progresso contínuo**: Sempre haverá pelo menos uma thread executando
- ✓ **Justiça**: Nenhuma thread fica eternamente bloqueada
- ✓ **Implementação simples**: Só precisamos padronizar a ordem de aquisição de recursos
- ✓ **Determinismo**: O programa sempre termina com sucesso

Esta solução demonstra um princípio fundamental da programação concorrente: a ordem importa. Quando múltiplas threads competem por recursos compartilhados, estabelecer uma hierarquia ou ordem global de aquisição é uma das técnicas mais eficazes para prevenir deadlocks.
