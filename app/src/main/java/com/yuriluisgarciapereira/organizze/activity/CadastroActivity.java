package com.yuriluisgarciapereira.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.yuriluisgarciapereira.organizze.R;
import com.yuriluisgarciapereira.organizze.config.ConfiguracaoFirebase;
import com.yuriluisgarciapereira.organizze.helper.Base64Custom;
import com.yuriluisgarciapereira.organizze.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText inputTextNome, inputTextEmail, inputTextSenha;
    private Button buttonCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        iniciaToobar();
        instanciaLayoutComXml();
        eventoClickBotaoButtonCadastrar();
    }

    private void instanciaLayoutComXml() {

        inputTextNome = findViewById(R.id.editTextNome);
        inputTextEmail = findViewById(R.id.editTextEmailCad);
        inputTextSenha = findViewById(R.id.editTextSenhaCad);
        buttonCadastrar = findViewById(R.id.buttonCadastrarUsuario);
    }

    private void iniciaToobar(){

        getSupportActionBar().setTitle("Cadastro");
    }

    private void eventoClickBotaoButtonCadastrar() {

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = inputTextNome.getText().toString();
                String email = inputTextEmail.getText().toString();
                String senha = inputTextSenha.getText().toString();

                if (validaCampoCadastroUsuarioActivity()) {
                    usuario = new Usuario();
                    usuario.setNome(nome);
                    usuario.setEmail(email);
                    usuario.setSenha(senha);
                    cadastraUsuarioFirebase();
                }
            }
        });
    }

    private boolean validaCampoCadastroUsuarioActivity() {

        if (inputTextNome.getText().toString().isEmpty()) {
            inputTextNome.setError("Campo Obrigatório!");
            return false;
        }

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

    private void cadastraUsuarioFirebase() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(CadastroActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            usuario.setIdUsuario(Base64Custom.codificarBase64(usuario.getEmail()));
                            usuario.salvarUsuarioFirebase();
                            finish();
                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                excecao = "Digite uma senha mais forte!";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Por favor, digite um e-mail válido!";
                            } catch (FirebaseAuthUserCollisionException e) {
                                excecao = "Essa conta já está cadastrada!";
                            } catch (Exception e) {
                                excecao = "Erro ao cadastrar usuario : " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
