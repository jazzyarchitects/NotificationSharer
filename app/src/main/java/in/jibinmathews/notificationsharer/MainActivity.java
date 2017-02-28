package in.jibinmathews.notificationsharer;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    Button openSettingsButton, notificationButton, pairingButton, clearPairingButton;
    TextView permissionText, allSetText;
    Toolbar toolbar = null;

    String pairing_chromeId = null;
    ProgressDialog progressDialog;

    SharedPreferences firebasePreferences, applicationPreferences;


    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebasePreferences = getSharedPreferences(Constants.Preferences.Firebase.PREF, MODE_PRIVATE);
        applicationPreferences = getSharedPreferences(Constants.Preferences.Application.PREF, MODE_PRIVATE);

        try {
            socket = IO.socket(Constants.SOCKET_URL);
            setEvents();
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        openSettingsButton = (Button) findViewById(R.id.openSettingsButton);
        notificationButton = (Button) findViewById(R.id.notificationTest);
        pairingButton = (Button) findViewById(R.id.pairingButton);
        clearPairingButton = (Button) findViewById(R.id.clearPairingButton);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        permissionText = (TextView) findViewById(R.id.permissionText);
        allSetText = (TextView) findViewById(R.id.allSet);

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
                TestNotification.notify(MainActivity.this, "Some sample string", "Lorem Ipsum", 1234);
            }
        });

        pairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPairing();
            }
        });

        clearPairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPairing();
            }
        });
        refreshLayout();
    }

    private void refreshLayout() {
        if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            openSettingsButton.setVisibility(GONE);
            permissionText.setVisibility(GONE);
            allSetText.setVisibility(View.VISIBLE);
            pairingButton.setVisibility(View.VISIBLE);

            if (applicationPreferences.contains(Constants.Preferences.Application.CHROME_UNIQUE_ID)) {
                pairingButton.setVisibility(View.GONE);
                clearPairingButton.setVisibility(VISIBLE);
            } else {
                pairingButton.setVisibility(VISIBLE);
                clearPairingButton.setVisibility(GONE);
            }
        }
    }

    private void startPairing() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Request Permission");
                builder.setMessage("We require Camera permission to pair your chrome and android device.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startPairing();
                    }
                });
                builder.setNegativeButton("Never Mind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1234);
            }
        } else {
            new IntentIntegrator(this).initiateScan();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toolbar != null) {
            refreshLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1234: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPairing();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "App will not function without this permission. Exiting", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void sendJoiningRequests() throws JSONException {
        JSONObject jsonObject = new JSONObject("{}");
        if (applicationPreferences.contains(Constants.Preferences.Application.PHONE_UNIQUE_ID)) {
            jsonObject.put("id", applicationPreferences.getString(Constants.Preferences.Application.PHONE_UNIQUE_ID, ""));
        }
        socket.emit(Constants.Socket.EVENT_JOIN, jsonObject);
    }

    private void setEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket Connected");
                try {
                    sendJoiningRequests();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket Disconnected");
            }
        });
        socket.on(Constants.Socket.EVENT_JOIN_WITH, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String id = (String) args[0];
                if (!applicationPreferences.contains(Constants.Preferences.Application.PHONE_UNIQUE_ID)) {
                    applicationPreferences.edit()
                            .putString(Constants.Preferences.Application.PHONE_UNIQUE_ID, id)
                            .apply();
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit(Constants.Socket.EVENT_JOIN, jsonObject);
            }
        });

        socket.on(Constants.Socket.EVENT_PAIRING_SUCCESSFUL, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (pairing_chromeId == null) {
                    return;
                }
                applicationPreferences.edit()
                        .putString(Constants.Preferences.Application.CHROME_UNIQUE_ID, pairing_chromeId)
                        .apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Invalid QR Code")
                        .setMessage("QR code scanned does not contain any data")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            } else {
                JSONObject jsonObject = new JSONObject();
                String chromeId, phoneId, fcm, secretKey;

                fcm = firebasePreferences.getString(Constants.Preferences.Firebase.REGISTRATION_ID, "");
                phoneId = applicationPreferences.getString(Constants.Preferences.Application.PHONE_UNIQUE_ID, "");

                try {
                    JSONObject qrCode = new JSONObject(result.getContents());

                    chromeId = qrCode.getString("chromeId");
                    secretKey = qrCode.getString("secretKey");

                    jsonObject.put("chromeId", chromeId);
                    jsonObject.put("phoneId", phoneId);
                    jsonObject.put("fcmId", fcm);
                    jsonObject.put("secretKey", secretKey);

                    socket.emit(Constants.Socket.EVENT_PAIRING, jsonObject);

                    pairing_chromeId = chromeId;

                    progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Pairing");
                    progressDialog.setMessage("Please wait while we pair your phone and chrome browser");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

//                    applicationPreferences.edit()
//                            .putString(Constants.Preferences.Application.CHROME_UNIQUE_ID, chromeId)
//                            .apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        refreshLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void clearPairing() {

        try {
            socket.emit(Constants.Socket.EVENT_DELETE_PAIRING, new JSONObject().put("chromeId", applicationPreferences.getString(Constants.Preferences.Application.CHROME_UNIQUE_ID, "")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        applicationPreferences.edit().clear().apply();

        socket.disconnect();
        socket.connect();

        refreshLayout();
    }
}
