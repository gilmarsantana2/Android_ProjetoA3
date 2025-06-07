package data;

import java.io.Serializable;

public class MessageData implements Serializable {
    public enum Colors {
        VERMELHO,
        VERDE,
        AZUL
    }

    public enum Comandos{
        RESULTADO,
        INICIAR,
        PARAR_MOTOR,
        PARAR_ALARME,
        SAIR,
        FALHA,
        CONECTION_OK,
        RESET
    }


    private Colors corSelecionada;
    private Comandos comando;

    private int redCount;
    private int blueCount;
    private int greenCount;

    public MessageData(Comandos comando) {
        this.comando = comando;
    }

    public int getRedCount() {
        return redCount;
    }

    public void setRedCount(int redCount) {
        this.redCount = redCount;
    }

    public int getBlueCount() {
        return blueCount;
    }

    public void setBlueCount(int blueCount) {
        this.blueCount = blueCount;
    }

    public int getGreenCount() {
        return greenCount;
    }

    public void setGreenCount(int greenCount) {
        this.greenCount = greenCount;
    }

    public Colors getCorSelecionada() {
        return corSelecionada;
    }

    public void setCorSelecionada(Colors corSelecionada) {
        this.corSelecionada = corSelecionada;
    }

    public Comandos getComando() {
        return comando;
    }

    public void setComando(Comandos comando) {
        this.comando = comando;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "corSelecionada=" + corSelecionada +
                ", comando=" + comando +
                ", redCount=" + redCount +
                ", blueCount=" + blueCount +
                ", greenCount=" + greenCount +
                '}';
    }
}
