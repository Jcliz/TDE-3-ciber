# Parte 1 — O Jantar dos filósofos

## 1. Introdução

O problema do Jantar dos Filósofos é um clássico da computação que ilustra os desafios da sincronização em sistemas concorrentes. Imagine cinco filósofos sentados ao redor de uma mesa circular. Cada um alterna entre três atividades: **pensar**, **com fome** e **comer**. O detalhe importante é que, para comer, cada filósofo precisa de dois garfos, um à sua esquerda e outro à sua direita, e esses garfos são compartilhados com os vizinhos.

## 2. O cenário

Trabalhamos com um cenário de 5 filósofos (numerados de 0 a 4) e 5 garfos (também numerados de 0 a 4). Cada garfo fica entre dois filósofos: o garfo *i* fica entre o filósofo *i* e o filósofo seguinte. Isso significa que cada filósofo compartilha seus garfos com seus vizinhos imediatos.

Cada filósofo pode estar em um de três estados:

1. **Pensando**: O filósofo está concentrado em suas reflexões e não precisa de garfos
2. **Com fome**: O filósofo quer comer e tenta pegar os garfos necessários
3. **Comendo**: O filósofo conseguiu ambos os garfos e está se alimentando

A primeira tentativa de solução parece simples e intuitiva:

```
Dados: 
  N = 5 filósofos 
  Garfos 0..N-1 (garfo i fica entre filósofos i e (i+1) mod N)

Para cada filósofo p: 
  left  = min(garfo_esquerda(p), garfo_direita(p)) 
  right = max(garfo_esquerda(p), garfo_direita(p))

Loop:
    pensar()
    estado <- "com fome"
    pegar_garfo_esquerdo()
    pegar_garfo_direito()
    estado <- "comendo"
    comer()
    soltar_garfo_direito()
    soltar_garfo_esquerdo()
```

## 3. O cenário de deadlock

O deadlock (impasse) surge quando todos os cinco filósofos fazem exatamente a mesma coisa ao mesmo tempo:

1. Todos pegam o garfo da esquerda simultaneamente
2. Todos tentam pegar o garfo da direita
3. Nenhum garfo direito está disponível. Cada um já foi pego como "garfo esquerdo" por outro filósofo
4. Resultado: todos ficam esperando eternamente pelo segundo garfo, e ninguém come

## 4. As quatro condições do deadlock (Condições de Coffman)

Para entender melhor o problema, precisamos conhecer as quatro condições que, quando presentes juntas, causam um deadlock:

1. **Exclusão mútua**: Um garfo só pode ser usado por um filósofo de cada vez (não dá para compartilhar)
2. **Manter e esperar**: Um filósofo segura um garfo enquanto espera pelo outro
3. **Não preempção**: Ninguém pode arrancar um garfo da mão de outro filósofo; ele só pode ser liberado voluntariamente
4. **Espera circular**: Forma-se um ciclo onde cada filósofo espera por um recurso que está nas mãos do próximo (F0 → G1 → F1 → G2 → F2 → G3 → F3 → G4 → F4 → G0 → F0)

O problema da abordagem ingênua é que todas as quatro condições aparecem, tornando o deadlock não apenas possível, mas bastante provável.

## 5. A solução

A base é criar uma hierarquia de recursos.

- Cada garfo tem um número fixo (0 a 4)
- Todos os filósofos devem sempre pegar primeiro o garfo de menor número, depois o de maior número

Essa solução quebra a condição de Espera Circular (a quarta condição de Coffman):

- Com todos seguindo a mesma ordem de aquisição, é impossível formar um ciclo de espera
- Uma ordem global fixa impede que se forme um círculo fechado de dependências
- Sempre haverá pelo menos um filósofo capaz de pegar seus dois garfos e comer

**Por exemplo**: Se todos tentarem pegar garfos ao mesmo tempo seguindo a regra do "menor primeiro", o filósofo que precisa do garfo 0 terá prioridade. Ele eventualmente conseguirá pegar ambos os garfos, quebrando qualquer ciclo que poderia se formar.

## 6. Como implementar a solução

```
Dados:
    N = 5 
    Garfos[0..4]
    estado[0..4] 

Para cada filósofo p (0 até 4) executando em paralelo:
    garfo_esquerdo = p
    garfo_direito = (p + 1) mod 5
    
    primeiro_garfo = min(garfo_esquerdo, garfo_direito)
    segundo_garfo = max(garfo_esquerdo, garfo_direito)
    
    Loop infinito:
        estado[p] <- "pensando"
        pensar() 
        estado[p] <- "com fome"
        esperar(Garfos[primeiro_garfo])  
        esperar(Garfos[segundo_garfo])   
        estado[p] <- "comendo"
        comer() 

        liberar(Garfos[segundo_garfo])
        liberar(Garfos[primeiro_garfo])
```

## 7. Fluxograma da solução

<img width="767" height="740" alt="image" src="https://github.com/user-attachments/assets/79ad9b4e-f752-4c6d-809c-a753652865bd" />

## 8. A Garantia Contra Deadlock

A hierarquia de recursos elimina completamente a espera circular:

- Todos os filósofos seguem a mesma ordem de aquisição
- É impossível formar um ciclo de dependências (F0 → G_x → F1 → G_y → ... → F4 → G_z → F0)
- Sempre haverá pelo menos um filósofo capaz de progredir e comer

**Progresso**: Como não há deadlock possível, o sistema sempre avança. Sempre haverá alguém comendo.

**Justiça**: Nenhum filósofo fica eternamente sem comer. Os mecanismos de semáforos/mutexes geralmente usam FIFO (primeiro que chega, primeiro atendido), garantindo que todos terão sua vez eventualmente.

## 9. Conclusão

A solução por hierarquia de recursos é um exemplo perfeito de como elegância e eficácia andam juntas. Com uma regra simples, "sempre pegue primeiro o garfo de menor número", conseguimos eliminar completamente a possibilidade de deadlock.

- **Sem deadlock**: A espera circular é impossível
- **Progresso contínuo**: Sempre haverá pelo menos um filósofo comendo
- **Justiça**: Nenhum filósofo fica eternamente sem comer
- **Implementação simples**: Só precisamos ordenar a aquisição de recursos