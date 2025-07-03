# CHANGELOG

## **Scelta dell'algoritmo:**

Scelta iniziale: **MonteCarloTreeSearch**
* Famoso per vittoria su campione mondiale di GO
* Algoritmo che si basa sulla simulazione pseudo-casuale per scegliere la strada che porta, statisticamente, a più vittorie
Tuttavia poiché l'hardware e il tempo di simulazione sono limitati la scelta viene basata su un campione troppo piccolo, il che è autodistruttivo in un gioco con molte pessime mosse e solo qualcuna giusta fin dallo stato iniziale.

Scelta finale: **IterativeDeepeningAlphaBetaSearch**
* Deterministico, si cerca la vittoria anche nel caso l'avversario faccia le mosse migliori a sua disposizione
* Usando la logica AlphaBeta si evita di valutare molte ramificazioni
* Si espande l'albero in larghezza fino a una profondità limite, simulando il gioco fino a tot mosse nel futuro (a seconda delle risorse e tempo disponibili)
Tuttavia, poiché non si simula il gioco fino agli stati finali è essenziale una buona euristica per valutare la bontà dei nodi.
