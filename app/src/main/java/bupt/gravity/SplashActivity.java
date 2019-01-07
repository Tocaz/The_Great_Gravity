package bupt.gravity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        init();
    }

    private void init() {
        CircularProgressButton btn = findViewById(R.id.btn);
        EditText editText = findViewById(R.id.username);

        Intent intent = new Intent(this, GravityActivity.class);
        btn.setOnClickListener(v -> {
            btn.startAnimation();

            String username = editText.getText().toString().trim();
            if (username.equals("")){ username = "Anonymous User"; }
            intent.putExtra("username", username);

            int delayMillis = 1000;
            Runnable delay = () -> {
                btn.stopAnimation();
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    SplashActivity.this,
                    new Pair<View, String>(btn, "transition")
                );
                startActivity(intent, activityOptions.toBundle());
                btn.resetProgress();
            }; // delay -END-

            Handler handler = new Handler();
            handler.postDelayed(delay, delayMillis);
        });
    }

    private void handleStartGravityButton() {
        startActivity(new Intent(this, GravityActivity.class));
    }
}
