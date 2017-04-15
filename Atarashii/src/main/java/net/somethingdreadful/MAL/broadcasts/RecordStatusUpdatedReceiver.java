package net.somethingdreadful.MAL.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecordStatusUpdatedReceiver extends BroadcastReceiver {
    public static final String RECV_IDENT = "net.somethingdreadful.MAL.broadcasts.RecordStatusUpdatedReceiver";
    private final RecordStatusUpdatedListener callback;

    public RecordStatusUpdatedReceiver(RecordStatusUpdatedListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(RECV_IDENT)) {
            if (callback != null) {
                boolean isAnime = (boolean) intent.getSerializableExtra("type");
                callback.onRecordStatusUpdated(isAnime);
            }
        }
    }

    public interface RecordStatusUpdatedListener {
        void onRecordStatusUpdated(boolean isAnime);
    }
}
