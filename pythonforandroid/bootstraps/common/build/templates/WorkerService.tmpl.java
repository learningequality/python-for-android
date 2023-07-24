package {{ args.package }};

import android.content.Context;
import android.util.Log;

import androidx.work.Configuration;
import androidx.work.multiprocess.RemoteWorkerService;
import androidx.work.WorkManager;

import java.lang.System;

public class {{ name|capitalize }}WorkerService extends RemoteWorkerService {
    private static final String TAG = "{{ name|capitalize }}WorkerService";

    @Override
    public void onCreate() {
        try {
            Log.v(TAG, "Initializing WorkManager");
            Context context = getApplicationContext();
            Configuration configuration = new Configuration.Builder()
                .setDefaultProcessName(context.getPackageName())
                .build();
            WorkManager.initialize(context, configuration);
        } catch (IllegalStateException e) {
        }
        super.onCreate();
    }
}
