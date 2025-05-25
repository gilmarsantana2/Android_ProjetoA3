package activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.analisador.R;

public class ConnexionActivity extends AppCompatActivity {

    private TextView txtConexao;
    private Button btnConectar;
    private EditText editEndereco;

    private ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 78) {
                    txtConexao.setText("Desconectado");
                    Log.i("Result", "Voltou pra MainActivity");
                }
            }
    );


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conexao_layout);

        txtConexao = findViewById(R.id.txtConexao);
        btnConectar = findViewById(R.id.btnConectar);
        editEndereco = findViewById(R.id.editEndereco);
        editEndereco.setText("192.168.1.40");
        carregarPermissoes();

        btnConectar.setOnClickListener(v -> {
            String endereco = editEndereco.getText().toString().trim();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("endereco", endereco);
            startForResult.launch(intent);
        });
    }


    public void carregarPermissoes() {
        String[] permissions = {Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.VIBRATE
        };
        for (String permissao : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permissao) != PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission", "Permission not granted");
                ActivityCompat.requestPermissions(this,
                        new String[]{permissao},
                        1);
            } else {
                Log.i("Permission", "Permission granted");
            }
        }

    }
}
