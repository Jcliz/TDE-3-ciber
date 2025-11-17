## Relatório: Análise de Concorrência com e sem Semáforo

### 1. Objetivo

Este relatório analisa o impacto da sincronização no acesso a um recurso compartilhado. O objetivo foi demonstrar
experimentalmente uma condição de corrida em um contador concorrente e, em seguida, medir a eficácia de um
semáforo binário justo para corrigir o problema, comparando os resultados de correção e o tempo de execução.

### 2. Metodologia e medições

Dois testes foram executados, ambos com o objetivo de atingir um valor final de 2.000.000 (T=8 threads, cada uma
realizando M=250.000 incrementos).

Os resultados medidos foram os seguintes:

| Cenário de Teste                          | Valor Esperado | Valor Obtido   | Tempo de Execução | Correção         |
|:------------------------------------------|:---------------|:---------------|:------------------|:-----------------|
| **1. Sem Semáforo (Condição de Corrida)** | 2.000.000      | 992.187 (varia)| **0,017 s**       |   **Incorreto**  |
| **2. Com Semáforo Binário Justo**         | 2.000.000      | 2.000.000      | **9,397 s**       |   **Correto**    |

---

### 3. Discussão e análise

Os dados expõem o trade-off central da concorrência:

- **Resultado (Correção):**
    - O cenário sem semáforo falhou catastroficamente, perdendo mais da metade dos incrementos (perda de 1.007.813).
    - Isso ocorre porque a operação `count++` não é atômica (ela consiste em ler, incrementar e escrever). Múltiplas
    - threads leram o mesmo valor antigo, e suas escritas subsequentes sobrescreveram o trabalho umas das outras.
    - O cenário com semáforo produziu o resultado perfeitamente correto. Ao usar `sem.acquire()` e
    - `sem.release()` em torno da seção crítica (`count++`), foi garantida a exclusão mútua*. Apenas uma thread pôde
    - executar o incremento por vez, eliminando a perda de dados.

- **Tempo (Desempenho):**
    - O cenário sem semáforo foi extremamente rápido (0,017s). As threads correram livremente, sem bloqueios,
    - executando em paralelo (embora de forma incorreta).
    - O cenário com semáforo foi significativamente mais lento (9,397s), mais de 550 vezes mais lento que a
    - versão incorreta. Essa lentidão não é um "defeito"; é o custo da correção. O tempo foi gasto com as threads
    - bloqueadas, esperando em fila (`acquire`) pela sua vez de acessar a seção crítica.


- **Throughput** (vazão) é a taxa na qual o trabalho é concluído.
- A versão com condição de corrida teve um throughput ilusoriamente alto. Ela "terminou" as operações rapidamente,
- mas o trabalho estava errado.
- A versão com semáforo teve um throughput drasticamente menor. O semáforo binário efetivamente serializou
- o acesso ao contador. Em vez de 8 threads incrementando em paralelo, elas foram forçadas a fazê-lo uma de cada vez. O
- semáforo se tornou um gargalo de contenção intencional para proteger o recurso, o que reduziu a vazão total do
- sistema.

#### 3.3. Fairness (Justiça)

- O semáforo utilizado na correção foi configurado como justo (`new Semaphore(1, true)`).
- **Significado:** Isso garante que as threads que solicitam a permissão (`acquire`) sejam atendidas na ordem em que
- chegaram (FIFO - *First-In, First-Out*).
- **Impacto:** O modo justo previne inanição (starvation), um cenário onde uma thread específica poderia, por
- azar, ser repetidamente "ultrapassada" por outras e nunca conseguir sua vez de executar. A justiça garante
- previsibilidade no escalonamento, embora possa introduzir um pequeno overhead adicional em comparação com um
- semáforo "não justo" (que pode entregar a permissão a qualquer thread em espera para otimizar o *throughput*).

---

### 4. Conclusão

Os resultados experimentais demonstraram com sucesso a natureza perigosa das condições de corrida, levando a resultados
incorretos em um tempo de execução muito rápido (0,017s).

A implementação do semáforo binário justo resolveu completamente o problema de correção (atingindo 100% do valor
esperado), mas ao custo de um aumento significativo no tempo de execução (9,397s).

Isso confirma que a sincronização é essencial para garantir a integridade de dados compartilhados, mas seu uso introduz
contenção e serialização, o que reduz o throughput como uma troca direta pela garantia de correção.