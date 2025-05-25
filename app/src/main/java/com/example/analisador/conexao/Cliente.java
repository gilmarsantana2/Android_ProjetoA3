package com.example.analisador.conexao;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class Cliente extends Thread {

    private final String TAG = "Cliente";

    private ObjectOutputStream output = null;
    private ObjectInputStream input = null;
    private Socket socket = null;
    private String host;
    private Integer port;
    private boolean connected = false;

    public Cliente(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Iniciar Server
     */
    @Override
    public void run() {
        try {
            socket = new Socket(host, port); //9678
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
        } catch (IOException e) {
            Log.e(TAG, "Falha na conexão com o servidor", e);
            connected = false;
        }
    }


    public boolean enviarMSG(String mensagem) {
        if (connected) {
            new Thread(() -> {
                try {
                    //output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    this.output.writeObject(mensagem);
                    this.output.flush();
                    //output.close();
                } catch (IOException e) {
                    Log.e(TAG, "Não foi possível Enviar mensagem ao servidor");
                }
            }).start();
            return true;
        } else {
            Log.e(TAG, "Não foi possível Enviar mensagem ao servidor");
            return false;
        }
    }

    public synchronized String lerMSG() {
        if (connected) {
            String msg = null;
            try {
                msg = (String) this.input.readObject();
            } catch (IOException e) {
                Log.e(TAG, "Não foi possivel ler mensagem do servidor", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return msg;
        } else {
            Log.e(TAG, "Erro de conexão com o servidor");
            return null;
        }
    }

    public void disconnect() {
        this.connected = false;
        close();
    }

    private void close() {
        try {
            if (output != null && input != null && socket != null) {
                output.close();
                input.close();
                socket.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, "Erro ao fechar Conexão com o servidor", ex);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}

