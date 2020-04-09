package com.yuriluisgarciapereira.organizze.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.yuriluisgarciapereira.organizze.R;
import com.yuriluisgarciapereira.organizze.config.ConfiguracaoFirebase;

public class MainActivity extends IntroActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        adicionaSlide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        vericaSeUsuarioEstaLogado();
    }

    /**
     * Verifica se o usuário já está logado no sistema,
     * caso estaja logado, o aplicativo mandará o usuario
     * diretamente para a tela princilap do app!
     */
    private void vericaSeUsuarioEstaLogado() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //autenticacao.signOut();
        if (usuarioEstaLogado()) {
            abrirTelaPrincipal();
        }
    }

    private boolean usuarioEstaLogado(){
        return autenticacao.getCurrentUser() != null;
    }

    private void abrirTelaPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
    }

    public void buttonLogar(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void buttonCadastrar(View view) {
        startActivity(new Intent(this, CadastroActivity.class));
    }

    private void adicionaSlide() {

        setButtonNextVisible(false);
        setButtonBackVisible(false);
        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_um)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_dois)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_tres)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_quatro)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_cadastro)
                .canGoForward(false)
                .build()
        );
    }
}
