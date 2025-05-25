package activities;

import android.content.Context;
import android.os.Bundle;

import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.example.analisador.R;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmeActivity extends AppCompatActivity {

    private TextView textViewAlarmeMensagem;
    private Button buttonPararAlarme;
    private Animation blinkAnimation;
    private boolean isBlinking = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarme_layout);
        vibrarComPadrao(AlarmeActivity.this);

        textViewAlarmeMensagem = findViewById(R.id.textViewAlarmeMensagem);

        buttonPararAlarme = findViewById(R.id.buttonPararAlarme);
        buttonPararAlarme.setOnClickListener(v -> {
            pararVibracao(AlarmeActivity.this);
            stopBlinking();
            setResult(36);
            finish();
        });

        // Configurar a animação de piscar
        blinkAnimation = new AlphaAnimation(0.0f, 1.0f); // De transparente para opaco
        blinkAnimation.setDuration(700); // Duração de cada "piscada" (700ms)
        blinkAnimation.setInterpolator(new android.view.animation.LinearInterpolator());
        blinkAnimation.setRepeatCount(Animation.INFINITE); // Repetir indefinidamente
        blinkAnimation.setRepeatMode(Animation.REVERSE);   // Inverter a animação para piscar

        startBlinking();

    }

    public void vibrarComPadrao(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 100, 200, 300, 400};
            vibrator.vibrate(pattern, 0);
        }
    }

    public void pararVibracao(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void startBlinking() {
        if (!isBlinking) {
            textViewAlarmeMensagem.startAnimation(blinkAnimation);
            isBlinking = true;
        }
    }

    private void stopBlinking() {
        if (isBlinking) {
            textViewAlarmeMensagem.clearAnimation(); // Remove a animação
            textViewAlarmeMensagem.setVisibility(View.VISIBLE); // Garante que o texto esteja visível
            isBlinking = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBlinking();
        pararVibracao(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isBlinking && textViewAlarmeMensagem.getAnimation() == null){
            startBlinking();
            vibrarComPadrao(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBlinking();
        pararVibracao(this);
    }
}
