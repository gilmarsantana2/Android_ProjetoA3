package com.example.analisador.conexao;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AsyncTask<Params, Result> {

    private final ExecutorService executors;

    public AsyncTask() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    @SafeVarargs
    private final void startBackground(Params... params) {
        onPreExecute();
        executors.execute(() -> {
            Result result = doInBackground(params);
            new Handler(Looper.getMainLooper()).post(() -> onPostExecute(result));
        });
    }

    @SafeVarargs
    public final void execute(Params... params) {
        startBackground(params);
    }

    public void shutdown() {
        executors.shutdown();
    }

    public boolean isShutdown() {
        return executors.isShutdown();
    }

    @SuppressWarnings("unchecked")
    protected abstract Result doInBackground(Params... params);

    protected void onPreExecute() {}

    protected void onPostExecute(Result result) {}

}
