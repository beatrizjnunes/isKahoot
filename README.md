# IsKahoot - Estrutura Inicial do Projeto

Este repositório contém a implementação inicial do projeto IsKahoot, uma versão distribuída e concorrente do jogo Kahoot! adaptada para fins educativos na disciplina de PCD (Programação Concorrente e Distribuída).

Esta primeira entrega cobre as três fases iniciais:
	1.	Desenvolvimento da GUI
	2.	Estrutura do GameState
	3.	Leitura das perguntas a partir de ficheiro JSON

## Classes Criadas

1. Servidor

Responsável por gerir todas as salas (GameState).
Funções principais:
	•	Criar nova sala
	•	Adicionar jogador à sala
	•	Encaminhar respostas para o GameState correspondente

Nota: Nesta fase inicial, ainda não implementa concorrência ou comunicação real com clientes.

⸻

2. GameState

Representa uma sala/jogo.
Guarda todas as informações do estado atual do jogo:
	•	Código da sala
	•	Lista de jogadores
	•	Lista de equipas
	•	Quiz atual
	•	Índice da pergunta atual
	•	Map de respostas da ronda atual
	•	Placar por equipa

Métodos principais:
	•	getPerguntaAtual()
	•	receberResposta()
	•	avancarParaProximaPergunta()
	•	finalizarRonda()

⸻

3. Jogador

Representa um jogador individual com os seguintes atributos:
	•	Nome
	•	Equipa a que pertence
	•	Flag respondeu (opcional nesta fase)

⸻

4. Equipa

Representa uma equipa de jogadores:
	•	Nome da equipa
	•	Lista de jogadores
	•	Método para calcular pontuação da ronda

⸻

5. Quiz

Representa um quiz completo:
	•	Nome do quiz
	•	Lista de perguntas (Pergunta)

⸻

6. Pergunta

Representa uma pergunta individual:
	•	Enunciado
	•	Lista de opções
	•	Índice da resposta correta
	•	Pontos
	•	Flag indicando se é individual ou de equipa (para fases futuras)

⸻

7. Resposta

Representa a resposta de um jogador:
	•	Jogador que respondeu
	•	Opção escolhida
	•	Timestamp (opcional para cálculo de bónus de rapidez)

⸻

8. QuizLoader

Classe utilitária para ler ficheiros JSON e transformá-los em objetos Quiz.

⸻

9. KahootGUI

Interface gráfica do cliente:
	•	Mostra perguntas e opções de resposta
	•	Botões para selecionar respostas
	•	Label para mostrar enunciado
	•	Integração futura com servidor para envio de respostas

⸻

## Relação Entre as Classes
	Servidor
	 └─ GameState
	     ├─ Lista<Jogador>
	     ├─ Lista<Equipa>
	     ├─ Quiz
	     │   └─ Lista<Pergunta>
	     ├─ Map<Jogador, Resposta>
	     └─ Map<Equipa, Integer> placar
	 
	KahootGUI --> Pergunta (mostrada na GUI)
  	QuizLoader --> Quiz (para popular GameState)
