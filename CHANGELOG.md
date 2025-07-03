# CHANGELOG

## **Scelta dell'algoritmo:**

### Scelta iniziale: **MonteCarloTreeSearch**
* Famoso per vittoria su campione mondiale di GO
* Algoritmo che si basa sulla simulazione pseudo-casuale per scegliere la strada che porta, statisticamente, a più vittorie

Tuttavia, poiché l'hardware e il tempo di simulazione sono limitati, la scelta viene basata su un campione troppo piccolo, il che è autodistruttivo in un gioco con molte pessime mosse e solo qualcuna giusta fin dallo stato iniziale.



### Scelta finale: **IterativeDeepeningAlphaBetaSearch**
* Deterministico, si cerca la vittoria anche nel caso l'avversario faccia le mosse migliori a sua disposizione
* Usando la logica AlphaBeta si evita di valutare molte ramificazioni
* Si espande l'albero in larghezza fino a una profondità limite, simulando il gioco fino a tot mosse nel futuro (a seconda delle risorse e tempo disponibili)
  
Tuttavia, poiché non si simula il gioco fino agli stati finali è essenziale una buona euristica per valutare la bontà dei nodi.

A parità di euristica e algoritmo, vince la modellazione più efficiente dei metodi getResult() e getAction(). Per massimizzare l'efficienza si è provato a sfruttare la struttura simmetrica del tabellone e un meccanismo di caching. 

---

L'implementazione base di IterativeDeepening è quella fornita da **AIMA** (Artificial Intelligence: A Modern Approach), grazie all'uso della libreria [aima-java](https://github.com/aimacode/aima-java), e sono state incluse le euristiche e le implementazioni dei metodi astratti introdotte da [Gionnino9000](https://github.com/Gionnino9000/Gionnino9000) nel suo progetto.

Sono state poi apportate delle modifiche a questo codice, in particolare:
* Modifica dei metodi `getResult()` e `getAction()` per introdurre il caching e le simmetrie.
* Modifica nel calcolo dell'euristica per correggere la funzione di valutazione degli stati.
* Modifica nella verifica delle condizioni di pareggio (drawConditions) per renderla compatibile con l'introduzione delle simmetrie.
