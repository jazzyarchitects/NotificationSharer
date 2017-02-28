package in.jibinmathews.notificationsharer;

/**
 * Created by jibin on 26/2/17.
 */

public class Constants {
    public static class Preferences{
        public static class Firebase{
            public static final String PREF = "FirebasePref";
            public static final String REGISTRATION_ID = "firebaseId";
        }

        public static class Application{
            public static final String PREF = "ApplicationPref";
            public static final String PHONE_UNIQUE_ID = "PhoneUniqueId";
            public static final String CHROME_UNIQUE_ID = "ChromeId";
            public static final String NOTIFICATION_SHARING = "NotificationSharing";
        }
    }

    public static final String SOCKET_URL = "http://jibinmathews.in";

    public static class Socket{
        public static final String EVENT_JOIN = "phone-join";
        public static final String EVENT_JOIN_WITH = "join-with";
        public static final String EVENT_NOTIFICATION = "notification";
        public static final String EVENT_PAIRING = "pairing";
        public static final String EVENT_PAIRING_SUCCESSFUL = "pairing-successful";
        public static final String EVENT_DELETE_PAIRING = "deletePairing";
    }
}
