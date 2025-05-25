package com.example.analisador.conexao;


import activities.MainActivity;

public class Leitura extends AsyncTask<Void, String> {

    private Cliente cliente;
    private MainActivity activity;

    public Leitura(Cliente cliente, MainActivity activity){
        this.cliente = cliente;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return cliente.lerMSG();
    }

    @Override
    protected void onPostExecute(String s) {
        activity.runOnUiThread(() -> activity.update(s));
    }
}
