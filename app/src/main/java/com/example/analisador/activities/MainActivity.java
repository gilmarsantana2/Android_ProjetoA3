package com.example.analisador.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.analisador.R;
import com.example.analisador.conexao.Cliente;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import data.MessageData;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RadioGroup radioGroup;
    private TextView txtWaiting, txtCorSelecionada, txtRed, txtGreen, txtBlue;
    private Button btnScanear, btnResetar;
    private LinearLayout layout;

    public AtomicBoolean running;

    private Cliente cliente;
    /*
     * 0 -> Scanear
     * 1 -> Parar
     * 2 -> Rescanear
     * */
    private int status = 0;
    private MessageData.Colors corSelecionada;

    private ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 36) {
                    cliente.enviarMSG(new MessageData(MessageData.Comandos.PARAR_ALARME));//todo
                    //txtRecebido.setText("");
                    running.set(true);
                    resultado();
                    status = 0;
                    btnScanear.setText("Scanear");
                } else {
                    Log.i("Result", "Voltou pra MainActivity");
                }
            }
    );

    /**
     * android:layout_width="409dp"
     * android:layout_height="531dp"
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.scanner_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        radioGroup = findViewById(R.id.radioGroupCores);


        //txtRecebido = findViewById(R.id.textViewInformacoes);
        txtCorSelecionada = findViewById(R.id.txtCorSelecionada);
        txtRed = findViewById(R.id.txtRed);
        txtGreen = findViewById(R.id.txtGreen);
        txtBlue = findViewById(R.id.txtBlue);
        txtWaiting = findViewById(R.id.txtWaiting);
        layout = findViewById(R.id.layout);

        btnResetar = findViewById(R.id.btnResetar);

        btnScanear = findViewById(R.id.buttonScanear);
        radioColor();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            radioColor();
        });

        running = new AtomicBoolean(true);

        String endereco = getIntent().getStringExtra("endereco");

        if (endereco != null && !endereco.isEmpty()) {
            cliente = new Cliente(endereco, 3333); //"192.168.1.40"
            cliente.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Executors.newSingleThreadExecutor().execute(() -> {
                if (cliente.lerMSG().getComando().equals(MessageData.Comandos.CONECTION_OK)) {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "Conectado ao servidor", Toast.LENGTH_LONG).show());
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "Erro ao conectar no servidor", Toast.LENGTH_LONG).show());
                }
            });

        } else {
            Toast.makeText(this, "Endereço do servidor não fornecido.", Toast.LENGTH_LONG).show();
            finish();
        }

        btnScanear.setOnClickListener(v -> {
            if (status == 0) {
                running.set(true);
                resultado();
                MessageData data = new MessageData(MessageData.Comandos.INICIAR);
                data.setCorSelecionada(corSelecionada);
                cliente.enviarMSG(data);
                txtWaiting.setText("Aguardando \nInformações do \nSeletor");
                btnResetar.setVisibility(View.INVISIBLE);
                status = 1;
                btnScanear.setText("Parar");
            } else {
                running.set(false);
                cliente.enviarMSG(new MessageData(MessageData.Comandos.PARAR_MOTOR));
                layout.setVisibility(View.GONE);
                txtWaiting.setVisibility(View.VISIBLE);
                txtWaiting.setText("Motor Pausado\npelo Operador");
                btnResetar.setVisibility(View.VISIBLE);
                status = 0;
                btnScanear.setText("Scanear");
            }
        });

        btnResetar.setOnClickListener( v -> {
            cliente.enviarMSG(new MessageData(MessageData.Comandos.RESET));
            Toast.makeText(this, "Leitura Reiniciada", Toast.LENGTH_SHORT).show();
        });

    }

    private void radioColor() {
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioAzul) {
            corSelecionada = MessageData.Colors.AZUL;
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioVerde) {
            corSelecionada = MessageData.Colors.VERDE;
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioVermelho) {
            corSelecionada = MessageData.Colors.VERMELHO;
        } else {
            Toast.makeText(this, "Selecione uma cor", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Implementação do Menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu); // Infla o seu arquivo de menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Lida com cliques nos itens do menu
        if (item.getItemId() == R.id.action_desconectar) {
            // Lógica de desconexão
            if (running != null) {
                running.set(false);
            }
            if (cliente != null) {
                cliente.enviarMSG(new MessageData(MessageData.Comandos.SAIR)); // Envia mensagem de desconexão para o servidor
                try {
                    Thread.sleep(500); // Dá um tempo para a mensagem ser enviada
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e("MainActivity", "Thread interrompida", e);
                }
                cliente.disconnect();
                cliente = null; // Garante que não será usado após desconectar
            }
            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show();
            setResult(78); // Código de resultado para a activity anterior, se houver
            finish();      // Fecha esta activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public synchronized void resultado() {
        Executors.newSingleThreadExecutor().execute(() -> {
            while (running.get()) {
                MessageData resultado = cliente.lerMSG();

                if (resultado == null) continue;

                switch (resultado.getComando()) {
                    case RESULTADO: {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            txtWaiting.setVisibility(View.GONE);
                            btnResetar.setVisibility(View.GONE);
                            layout.setVisibility(View.VISIBLE);
                            txtCorSelecionada.setText(resultado.getCorSelecionada().toString());
                            txtBlue.setText(String.valueOf(resultado.getBlueCount()));
                            txtGreen.setText(String.valueOf(resultado.getGreenCount()));
                            txtRed.setText(String.valueOf(resultado.getRedCount()));
                        });
                        break;
                    }
                    case FALHA: {
                        Intent intent = new Intent(MainActivity.this, AlarmeActivity.class);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            txtCorSelecionada.setText(resultado.getCorSelecionada().toString());
                            txtBlue.setText(String.valueOf(resultado.getBlueCount()));
                            txtGreen.setText(String.valueOf(resultado.getGreenCount()));
                            txtRed.setText(String.valueOf(resultado.getRedCount()));
                        });
                        startForResult.launch(intent);
                        running.set(false);
                        break;
                    }
                    default: {
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "Erro de Comunicação", Toast.LENGTH_LONG).show());
                        break;
                    }
                }
            }
        });
    }

}