package com.example.a04;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TelaCadastro extends AppCompatActivity {

    EditText edtNome;
    EditText edtEmail;
    EditText edtSenha;
    EditText edtConfirmaSenha;
    Button btCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmaSenha = findViewById(R.id.edtConfirmaSenha);
        btCadastrar = findViewById(R.id.btCadastrar);

        btCadastrar.setOnClickListener(v -> {
            verificarDados(v);
        });
    }

    private void verificarDados(View v) {
        String nome = edtNome.getText().toString();
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();
        String confirmarSenha = edtConfirmaSenha.getText().toString();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Snackbar snac = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_SHORT);
            snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
        } else if (!senha.equals(confirmarSenha)) {
            Snackbar snac = Snackbar.make(v, "As senhas não estão iguais", Snackbar.LENGTH_SHORT);
            snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
        } else {
            criarConta(v);
        }
    }

    public void criarConta(View v) {
        String email = edtEmail.getText().toString();
        String senha = edtSenha.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Snackbar snac = Snackbar.make(v, "Usuário cadastrado com sucesso", Snackbar.LENGTH_SHORT);
                    snac.setTextColor(Color.BLACK).setBackgroundTint(Color.WHITE).show();
                    salvarDados();
                } else {
                    String erro;

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erro = "A senha deve conter pelo menos 6 caracteres";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erro = "Já existe um usuário com esse email";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "Formato inválido de email";
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

    private void salvarDados() {
        String nome = edtNome.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> usuarios = new HashMap<>();
        usuarios.put("nome", nome);

        String usuario_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference document = db.collection("Usuários").document(usuario_id);
        document.set(usuarios).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("db", "Sucesso ao salvar dados");
                    trocarTela(MainActivity.class);
                } else {
                    Log.d("db_error", "Erro ao salvar dados: " + task.getException());
                }
            }
        });
    }

    private void trocarTela(Class novaTela) {
        Intent i = new Intent(getApplicationContext(), novaTela);
        startActivity(i);
        finish();
    }

}