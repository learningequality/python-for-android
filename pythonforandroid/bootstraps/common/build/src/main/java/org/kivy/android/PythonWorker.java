package org.kivy.android;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker.Result;
import androidx.work.multiprocess.RemoteListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.Executors;

import org.kivy.android.PythonUtil;

public class PythonWorker extends RemoteListenableWorker {
    private static final String TAG = "PythonWorker";

    // WorkRequest data key for python service argument
    public static final String ARGUMENT_SERVICE_ARGUMENT = "PYTHON_SERVICE_ARGUMENT";

    // Python environment variables
    private String androidPrivate;
    private String androidArgument;
    private String pythonName;
    private String pythonHome;
    private String pythonPath;
    private String workerEntrypoint;

    public PythonWorker(
        @NonNull Context context,
        @NonNull WorkerParameters params) {
        super(context, params);
        Log.d(TAG, "PythonWorker constructor");

        String appRoot = PythonUtil.getAppRoot(context);

        androidPrivate = appRoot;
        androidArgument = appRoot;
        pythonHome = appRoot;
        pythonPath = appRoot + ":" + appRoot + "/lib";

        File appRootFile = new File(appRoot);
        PythonUtil.unpackAsset(context, "private", appRootFile, false);
        PythonUtil.loadLibraries(
            appRootFile,
            new File(getApplicationContext().getApplicationInfo().nativeLibraryDir)
        );
    }

    public void setPythonName(String value) {
        pythonName = value;
    }

    public void setWorkerEntrypoint(String value) {
        workerEntrypoint = value;
    }

    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            String dataArg = getInputData().getString(ARGUMENT_SERVICE_ARGUMENT);
            final String serviceArg;
            if (dataArg != null) {
                Log.d(TAG, "Setting python service argument to " + dataArg);
                serviceArg = dataArg;
            } else {
                serviceArg = "";
            }

            // onStopped runs in the local process, so it can't be used
            // to stop the remote work. Unfortunately, this doesn't seem
            // to get executed when the job is cancelled.
            //
            // https://issuetracker.google.com/issues/234211438
            completer.addCancellationListener(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Killing work process");
                    Process.killProcess(Process.myPid());
                }
            }, Executors.newSingleThreadExecutor());

            Log.i(TAG, "Starting python thread");

            // Is a thread even needed here if we're just going to block
            // on it?
            Thread pythonThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int res = nativeStart(
                        androidPrivate, androidArgument,
                        workerEntrypoint, pythonName,
                        pythonHome, pythonPath,
                        serviceArg
                    );

                    if (res == 0) {
                        completer.set(Result.success());
                    } else {
                        completer.set(Result.failure());
                    }
                    Log.d(TAG, "PythonWorker thread terminating:" + res);
                }
            });
            pythonThread.setName("python_worker_thread");
            pythonThread.start();
            pythonThread.join();

            Log.d(TAG, "Python thread completed");
            return "PythonWorker started";
        });
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped()");
        super.onStopped();
    }

    // Native part
    public static native int nativeStart(
        String androidPrivate, String androidArgument,
        String workerEntrypoint, String pythonName,
        String pythonHome, String pythonPath,
        String pythonServiceArgument
    );
}
