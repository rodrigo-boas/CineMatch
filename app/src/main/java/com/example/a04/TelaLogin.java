package com.example.a04;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class TelaLogin extends AppCompatActivity {

    EditText edtEmail;
    EditText edtSenha;
    Button btLogin;
    TextView txtCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (temInternet()) {
                trocarTela(MainActivity.class, true);
            } else {
                Snackbar snac = Snackbar.make(findViewById(R.id.main), "Sem conexão com a internet para entrar!", Snackbar.LENGTH_LONG);
                snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
            }
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btLogin = findViewById(R.id.btLogin);
        txtCadastrar = findViewById(R.id.txtCadastrar);

        btLogin.setOnClickListener(v -> {
            verificarDados(v);
        });

        txtCadastrar.setOnClickListener(v -> {
            trocarTela(TelaCadastro.class, false);
        });
    }

    private void verificarDados(View v) {
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();

        if (senha.isEmpty() || email.isEmpty()) {
            Snackbar snac = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_SHORT);
            snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
        } else {
            logar(v);
        }
    }

    private void logar(View v) {
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Snackbar snac = Snackbar.make(v, "Login realizado", Snackbar.LENGTH_SHORT);
                    snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
                    trocarTela(MainActivity.class, true);
                } else {
                    String erro;

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "Email ou senha incorretos";
                    } catch (FirebaseNetworkException e) {
                        erro = "Erro de rede: verifique sua conexão e tente novamente";
                    } catch (Exception e) {
                        erro = "Erro ao cadastrar conta";
                    }

                    Snackbar snac = Snackbar.make(v, erro, Snackbar.LENGTH_SHORT);
                    snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
                }
            }
        });
    }

    private void trocarTela(Class novaTela, boolean finish) {
        Intent i = new Intent(getApplicationContext(), novaTela);
        startActivity(i);
        if (finish) finish();
    }

    private boolean temInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        }
        return false;
    }

}