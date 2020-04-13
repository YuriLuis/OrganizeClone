package com.yuriluisgarciapereira.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.yuriluisgarciapereira.organizze.R;
import com.yuriluisgarciapereira.organizze.config.ConfiguracaoFirebase;
import com.yuriluisgarciapereira.organizze.helper.Base64Custom;
import com.yuriluisgarciapereira.organizze.helper.DateUtil;
import com.yuriluisgarciapereira.organizze.helper.Mascaras;
import com.yuriluisgarciapereira.organizze.helper.ValidaCamposApp;
import com.yuriluisgarciapereira.organizze.model.Movimentacao;
import com.yuriluisgarciapereira.organizze.model.Usuario;

public class ReceitasActivity extends AppCompatActivity {

    //Front-End
    private TextInputEditText textInputEditTextDataReceita, textInputEditTextCategoriaReceita, textInputEditTextDescricaoReceita;
    private EditText editTextValorReceita;

    //Back-End
    private Movimentacao movimentacao;
    private DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double receitaTotal;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);
        instanciaLayoutComXmlReceitasActivity();
    }

    private void instanciaLayoutComXmlReceitasActivity() {

        //Componentes interface...
        textInputEditTextDataReceita = findViewById(R.id.textInputEditTextDataReceita);
        textInputEditTextCategoriaReceita = findViewById(R.id.textInputEditTextCategoriaReceita);
        textInputEditTextDescricaoReceita = findViewById(R.id.textInputEditTextDescricaoReceita);
        editTextValorReceita = findViewById(R.id.editTextValorReceita);

        //Mascaras...
        Mascaras.adicionaMascaraData(textInputEditTextDataReceita);

        //DataAtual...
        textInputEditTextDataReceita.setText(DateUtil.dataAtual());
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarReceitaTotal();
    }

    public void salvarReceita(View view) {

        if (validaCamposReceitaActivity()) {

            String data = textInputEditTextDataReceita.getText().toString();
            Double valorRecuperado = Double.parseDouble(editTextValorReceita.getText().toString());
            movimentacao = new Movimentacao();
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria(textInputEditTextCategoriaReceita.getText().toString());
            movimentacao.setDescricao(textInputEditTextDescricaoReceita.getText().toString());
            movimentacao.setData(data);
            movimentacao.setTipo("receita");

            Double receitaAtualizada = receitaTotal + valorRecuperado;
            atualizarReceita(receitaAtualizada);

            movimentacao.salvarMovimentacaoFirebase(data);
            finish();
        }
    }

    private void atualizarReceita(Double receita){

        String idUsuario = Base64Custom.codificarBase64(firebaseAuth.getCurrentUser().getEmail());
        DatabaseReference usuarioRef = databaseReference.child("usuarios").child(idUsuario);

        usuarioRef.child("receitaTotal").setValue(receita);
    }

    private void recuperarReceitaTotal(){

        String idUsuario = Base64Custom.codificarBase64(firebaseAuth.getCurrentUser().getEmail());
        this.usuarioRef = databaseReference.child("usuarios").child(idUsuario);

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Boolean validaCamposReceitaActivity() {

        String campoObrigatorio = "Campo Obrigatório!";
        String valorReceita = editTextValorReceita.getText().toString();
        String dataEntradaReceita = textInputEditTextDataReceita.getText().toString();
        String categoriaReceita = textInputEditTextCategoriaReceita.getText().toString();
        String descricaoReceita = textInputEditTextDescricaoReceita.getText().toString();

        if (ValidaCamposApp.campoEhVazio(valorReceita)) {
            editTextValorReceita.setError(campoObrigatorio);
            return false;
        }

        if (ValidaCamposApp.campoEhVazio(dataEntradaReceita)) {
            textInputEditTextDataReceita.setError(campoObrigatorio);
            return false;
        }

        if (ValidaCamposApp.campoEhVazio(categoriaReceita)) {
            textInputEditTextCategoriaReceita.setError(campoObrigatorio);
            return false;
        }

        if (ValidaCamposApp.campoEhVazio(descricaoReceita)) {
            textInputEditTextDescricaoReceita.setError(campoObrigatorio);
            return false;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.usuarioRef.removeEventListener(valueEventListenerUsuario);
    }


}
