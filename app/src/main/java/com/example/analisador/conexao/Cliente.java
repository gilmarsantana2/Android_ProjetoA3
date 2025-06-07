package com.example.analisador.conexao;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import data.MessageData;

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

    public boolean enviarMSG(MessageData mensagem) {
        if (connected) {
            new Thread(() -> {
                try {
                    this.output.writeUnshared(mensagem);
                    this.output.flush();
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

    public synchronized MessageData lerMSG() {
        if (connected) {
            MessageData data = null;
            try {
                data = (MessageData) input.readUnshared();
                Log.d("Mensagem", data.toString());
                return data;
            } catch (IOException e) {
                //Log.e("Mensagem", "Não foi possível receber mensagem do servidor " + e.getMessage());
                return null;

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
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

