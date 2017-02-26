package in.jibinmathews.notificationsharer;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    Button openSettingsButton, notificationButton;
    TextView permissionText, allSetText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openSettingsButton = (Button)findViewById(R.id.openSettingsButton);
        notificationButton = (Button)findViewById(R.id.notificationTest);

        permissionText = (TextView)findViewById(R.id.permissionText);
        allSetText = (TextView)findViewById(R.id.allSet);

        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
        });

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestNotification.notify(MainActivity.this, "Some sample string", 1234);
            }
        });

        if(Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())){
            openSettingsButton.setVisibility(GONE);
            permissionText.setVisibility(GONE);
            allSetText.setVisibility(View.VISIBLE);
        }
    }
}
