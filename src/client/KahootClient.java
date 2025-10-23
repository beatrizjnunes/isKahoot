package src.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class KahootClient {
    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Uso: java iskahoot.client.KahootClient <IP> <PORT> <Sala> <Equipa> <Username>");
            return;
        }
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String sala = args[2];
        String equipa = args[3];
        String username = args[4];

        SwingUtilities.invokeLater(() -> new KahootGUI(ip, port, sala, equipa, username).setVisible(true));
    }
}

class KahootGUI extends JFrame {
    private final String ip;
    private final int port;
    private final String sala;
    private final String equipa;
    private final String username;

    // UI components
    private final JLabel lblHeader = new JLabel();
    private final JLabel lblQuestion = new JLabel("Pergunta", SwingConstants.LEFT);
    private final JRadioButton[] optionBtns = new JRadioButton[4];
    private final ButtonGroup group = new ButtonGroup();
    private final JButton btnSubmit = new JButton("Responder");
    private final JButton btnNext = new JButton("Próxima");
    private final JLabel lblTimer = new JLabel("00:30", SwingConstants.CENTER);
    private final JProgressBar timeBar = new JProgressBar(0, 30);
    private final JLabel lblScore = new JLabel("Pontuação: 0");

    private Timer swingTimer;
    private int remaining = 30;

    // Estado simples local (substituir por dados vindos do servidor)
    private int score = 0;
    private int currentIndex = 0;
    private final List<Question> demoQuestions = DemoData.sample();

    private final ClientTransport transport;

    public KahootGUI(String ip, int port, String sala, String equipa, String username) {
        this.ip = ip; this.port = port; this.sala = sala; this.equipa = equipa; this.username = username;
        this.transport = new ClientTransport(ip, port, sala, equipa, username);
        setTitle("IsKahoot – " + equipa + " / " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        buildUI();
        wireEvents();
        // Simular ligação inicial (no futuro: handshake real com servidor)
        transport.connectAsync();
        // Carregar 1ª pergunta
        if (!demoQuestions.isEmpty()) startRound(demoQuestions.get(0), 30);
    }

    private void buildUI() {
        lblHeader.setText("Sala: " + sala + "  |  Equipa: " + equipa + "  |  Jogador: " + username);
        lblHeader.setFont(lblHeader.getFont().deriveFont(Font.BOLD, 14f));

        lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 18f));
        lblQuestion.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        for (int i = 0; i < optionBtns.length; i++) {
            optionBtns[i] = new JRadioButton("Opção " + (i + 1));
            optionBtns[i].setFont(optionBtns[i].getFont().deriveFont(16f));
            group.add(optionBtns[i]);
            optionsPanel.add(wrap(optionBtns[i]));
        }

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        lblTimer.setFont(lblTimer.getFont().deriveFont(Font.BOLD, 24f));
        lblTimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeBar.setValue(30);
        timeBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblScore.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblScore.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNext.setEnabled(false);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(lblTimer);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(timeBar);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(lblScore);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(btnSubmit);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(btnNext);
        rightPanel.add(Box.createVerticalStrut(10));

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(lblQuestion, BorderLayout.NORTH);
        center.add(new JScrollPane(optionsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(lblHeader, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);
        setContentPane(root);
    }

    private JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10))
        );
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void wireEvents() {
        btnSubmit.addActionListener(e -> onSubmit());
        btnNext.addActionListener(e -> onNext());
        // ESC para submeter, Espaço para próxima
        getRootPane().registerKeyboardAction(e -> onSubmit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> onNext(),
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void startRound(Question q, int seconds) {
        group.clearSelection();
        for (JRadioButton b : optionBtns) { b.setEnabled(true); }
        btnSubmit.setEnabled(true);
        btnNext.setEnabled(false);

        // Atualizar UI
        lblQuestion.setText("<html>" + escape(q.question) + "</html>");
        for (int i = 0; i < optionBtns.length; i++) {
            String text = i < q.options.size() ? q.options.get(i) : "";
            optionBtns[i].setText(text);
            optionBtns[i].setVisible(!text.isEmpty());
        }

        // Cronómetro
        if (swingTimer != null && swingTimer.isRunning()) swingTimer.stop();
        remaining = seconds;
        timeBar.setMaximum(seconds);
        timeBar.setValue(seconds);
        updateTimerLabel();
        swingTimer = new Timer(1000, e -> {
            remaining--;
            timeBar.setValue(remaining);
            updateTimerLabel();
            if (remaining <= 0) {
                ((Timer) e.getSource()).stop();
                onTimeUp();
            }
        });
        swingTimer.setInitialDelay(0);
        swingTimer.start();
    }

    private void onSubmit() {
        btnSubmit.setEnabled(false);
        for (JRadioButton b : optionBtns) { b.setEnabled(false); }
        if (swingTimer != null && swingTimer.isRunning()) swingTimer.stop();

        int chosen = getSelectedIndex();
        Question q = demoQuestions.get(currentIndex);

        // Enviar resposta ao servidor (placeholder)
        transport.sendAnswerAsync(currentIndex, chosen);

        // Lógica local de pontuação (apenas para demo) – substituir por placar do servidor
        boolean correct = (chosen == q.correct);
        int gained = correct ? q.points : 0;
        score += gained;
        lblScore.setText("Pontuação: " + score + (correct ? "  ( +" + gained + " )" : ""));

        // Mostrar feedback simples
        highlightCorrectAndChosen(q.correct, chosen);
        btnNext.setEnabled(true);
    }

    private void onNext() {
        if (currentIndex + 1 < demoQuestions.size()) {
            currentIndex++;
            startRound(demoQuestions.get(currentIndex), 30);
        } else {
            JOptionPane.showMessageDialog(this, "Fim do jogo (demo). Pontuação: " + score,
                    "IsKahoot", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onTimeUp() {
        int chosen = getSelectedIndex();
        onSubmit(); // trata como submissão automática ao expirar tempo
    }

    private void highlightCorrectAndChosen(int correctIdx, int chosenIdx) {
        for (int i = 0; i < optionBtns.length; i++) {
            JRadioButton b = optionBtns[i];
            if (!b.isVisible()) continue;
            if (i == correctIdx) {
                b.setBackground(new Color(198, 239, 206)); // verde claro
                b.setOpaque(true);
            } else if (i == chosenIdx && chosenIdx != correctIdx) {
                b.setBackground(new Color(255, 199, 206)); // vermelho claro
                b.setOpaque(true);
            } else {
                b.setBackground(null);
                b.setOpaque(false);
            }
        }
    }

    private int getSelectedIndex() {
        for (int i = 0; i < optionBtns.length; i++) {
            if (optionBtns[i].isVisible() && optionBtns[i].isSelected()) return i;
        }
        return -1; // não respondeu
    }

    private void updateTimerLabel() {
        int m = remaining / 60, s = remaining % 60;
        lblTimer.setText(String.format("%02d:%02d", m, s));
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

/**
 * Placeholder de transporte de mensagens Cliente-Servidor.
 * No futuro: sockets, Object streams, protocolo de mensagens.
 */
class ClientTransport {
    private final String ip; private final int port; private final String sala; private final String equipa; private final String username;
    public ClientTransport(String ip, int port, String sala, String equipa, String username) {
        this.ip = ip; this.port = port; this.sala = sala; this.equipa = equipa; this.username = username;
    }
    public void connectAsync() {
        // TODO: abrir Socket, enviar mensagem de ligação inicial com (sala, equipa, username)
        System.out.printf("[DEMO] Conectar a %s:%d | sala=%s equipa=%s user=%s%n", ip, port, sala, equipa, username);
    }
    public void sendAnswerAsync(int questionIndex, int optionIndex) {
        // TODO: enviar resposta; no servidor, contabilizar com semáforo/barreira
        System.out.printf("[DEMO] Resposta enviada: q=%d opt=%d%n", questionIndex, optionIndex);
    }
}

/** Modelo simples de perguntas (alinha com o enunciado). */
class Question {
    final String question;
    final int points;
    final int correct; // índice da opção correta (0..n-1)
    final List<String> options;
    Question(String question, int points, int correct, List<String> options) {
        this.question = question; this.points = points; this.correct = correct; this.options = options;
    }
}

/** Dados de demonstração (até integrar JSON + servidor). */
class DemoData {
    static List<Question> sample() {
        List<Question> qs = new ArrayList<>();
        qs.add(new Question(
                "O que é uma thread?", 5, 3,
                List.of("Processo", "Aplicação", "Programa", "Processo Ligeiro")
        ));
        qs.add(new Question(
                "Qual destas opções NÃO é um método bloqueante?", 3, 2,
                List.of("join()", "sleep(<millis>)", "interrupted()", "wait()")
        ));
        return qs;
    }
}

