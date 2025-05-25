package activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RadioGroup radioGroup;
    private TextView txtRecebido;
    private Button btnScanear;

    public AtomicBoolean running;

    private Cliente cliente;
    /*
     * 0 -> Scanear
     * 1 -> Parar
     * 2 -> Rescanear
     * */
    private int status = 0;
    private String corSelecionada;

    private ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 36) {
                    cliente.enviarMSG("#a");//todo
                    running.set(false);
                    status = 0;
                    btnScanear.setText("Scanear");
                } else {
                    Log.i("Result", "Voltou pra MainActivity");
                }
            }
    );

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


        txtRecebido = findViewById(R.id.textViewInformacoes);
        btnScanear = findViewById(R.id.buttonScanear);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroup.getCheckedRadioButtonId() == R.id.radioAzul) {
                corSelecionada = "Azul";
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioVerde) {
                corSelecionada = "Verde";
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioVermelho) {
                corSelecionada = "Vermelho";
            } else {
                Toast.makeText(this, "Selecione uma cor", Toast.LENGTH_SHORT).show();
            }
        });

        running = new AtomicBoolean();

        String endereco = getIntent().getStringExtra("endereco");

        if (endereco != null && !endereco.isEmpty()) {
            cliente = new Cliente(endereco, 3333); //"192.168.1.40"
            cliente.start();
        } else {
            Toast.makeText(this, "Endereço do servidor não fornecido.", Toast.LENGTH_LONG).show();
            finish();
        }

        btnScanear.setOnClickListener(v -> {
            if (status == 0) {
                running.set(true);
                cliente.enviarMSG("#s||" + corSelecionada);
                status = 1;
                btnScanear.setText("Parar");
                resultado();
            } else {
                running.set(false);
                cliente.enviarMSG("#p");
                status = 0;
                btnScanear.setText("Scanear");
            }
        });
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
                cliente.enviarMSG("#x"); // Envia mensagem de desconexão para o servidor
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


    public void resultado() {
        Executors.newSingleThreadExecutor().execute(() -> {
            while (running.get()) {
                String resultado = cliente.lerMSG();
                if (resultado == null) continue;
                if (resultado.startsWith("#r")) {
                    new Handler(Looper.getMainLooper()).post(() -> txtRecebido.setText(resultado.substring(2)));
                }
                if (resultado.startsWith("#f")) {
                    new Handler(Looper.getMainLooper()).post(() -> txtRecebido.setText(resultado.substring(2)));
                    Intent intent = new Intent(MainActivity.this, AlarmeActivity.class);
                    startForResult.launch(intent);
                    break;
                }
            }
        });
    }

}