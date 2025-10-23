import java.util.*;

public class Equipa {

    private String nome;
    private List<Jogador> membros;

    public Equipa(String nome) {
        this.nome = nome;
        this.membros = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Jogador> getMembros() {
        return membros;
    }

    public void setMembros(List<Jogador> membros) {
        this.membros = membros;
    }
}
