package com.yuriluisgarciapereira.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.yuriluisgarciapereira.organizze.R;
import com.yuriluisgarciapereira.organizze.config.ConfiguracaoFirebase;
import com.yuriluisgarciapereira.organizze.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    //Front-End
    private EditText inputTextEmail, inputTextSenha;
    private Button buttonLogar;

    //Back-End
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        instanciaLayoutComXml();
        eventoClickButtonLogarUsuario();
    }

    public void instanciaLayoutComXml() {
        inputTextEmail = findViewById(R.id.editTextEmailLog);
        inputTextSenha = findViewById(R.id.editTextSenhaLog);
        buttonLogar = findViewById(R.id.buttonLogarUsuario);
    }

    public boolean validaCampoLoginActitivy() {
        if (inputTextEmail.getText().toString().isEmpty()) {
            inputTextEmail.setError("Campo Obrigatório!");
            return false;
        }

        if (inputTextSenha.getText().toString().isEmpty()) {
            inputTextSenha.setError("Campo Obrigatório!");
            return false;
        }

        return true;
    }

    public void eventoClickButtonLogarUsuario() {
        buttonLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputTextEmail.getText().toString();
                String senha = inputTextSenha.getText().toString();
                if (validaCampoLoginActitivy()) {
                    usuario = new Usuario();
                    usuario.setEmail(email);
                    usuario.setSenha(senha);
                    logarUsuarioFirebase(usuario);
                }
            }
        });
    }

    public void logarUsuarioFirebase(Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            abrirTelaPrincipal();
                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                excecao = "E-mail ou senha não correspondem a um usuário cadastrado!";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Usuário não está cadastrado!";
                            } catch (Exception e) {
                                excecao = "Erro ao cadastrar usuario : " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, PrincipalActivity.class));
    }
}
