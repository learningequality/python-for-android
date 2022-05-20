package {{ args.package }};

import android.content.Context;
import androidx.work.Configuration;
import androidx.work.multiprocess.RemoteWorkerService;
import androidx.work.WorkManager;

public class {{ name|capitalize }}WorkerService extends RemoteWorkerService {

    @Override
    public void onCreate() {
        try {
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
