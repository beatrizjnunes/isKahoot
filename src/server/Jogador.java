public class Jogador {

    private String nome;
    private Equipa equipa;
    private boolean respondeu;
    private int pontos;

    public Jogador(String nome, Equipa equipa) {
        this.nome = nome;
        this.equipa = equipa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    public boolean isRespondeu() {
        return respondeu;
    }

    public void setRespondeu(boolean respondeu) {
        this.respondeu = respondeu;
    }

    public Equipa getEquipa() {
        return equipa;
    }

    public void setEquipa(Equipa equipa) {
        this.equipa = equipa;
    }
}
