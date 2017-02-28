package in.jibinmathews.notificationsharer.Services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import in.jibinmathews.notificationsharer.Constants;

/**
 * Created by jibin on 26/2/17.
 */

public class FirebaseInstanceHandler extends FirebaseInstanceIdService {
    public final String TAG = "FirebaseInstanceHandler";
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "Refreshed token: " + refreshedToken);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.Preferences.Firebase.PREF, MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(Constants.Preferences.Firebase.REGISTRATION_ID, refreshedToken)
                .apply();

    }
}
