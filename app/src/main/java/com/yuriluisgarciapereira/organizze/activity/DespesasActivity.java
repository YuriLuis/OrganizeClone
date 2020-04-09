package com.yuriluisgarciapereira.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.yuriluisgarciapereira.organizze.model.Movimentacao;
import com.yuriluisgarciapereira.organizze.model.Usuario;

public class DespesasActivity extends AppCompatActivity {

    //Front-End
    private TextInputEditText textInputEditTextDataDespesa, textInputEditTextCategoriaDespesa,
                        textInputEditTextDescricaoDespesa;
    private EditText editTextValorDespesa;

    //Back-End
    private Movimentacao movimentacao;
    private DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        instanciaLayoutComXmlDespesasActivity();
    }

    private void instanciaLayoutComXmlDespesasActivity(){
        //Componentes interface...
        textInputEditTextDataDespesa = findViewById(R.id.textInputEditTextDataDespesa);
        textInputEditTextCategoriaDespesa = findViewById(R.id.textInputEditTextCategoriaDespesa);
        textInputEditTextDescricaoDespesa = findViewById(R.id.textInputEditTextDescricaoDespesa);;
        editTextValorDespesa = findViewById(R.id.editTextValorDespesa);

        //Mascara...
        Mascaras.adicionaMascaraData(textInputEditTextDataDespesa);

        //Data atual...
        textInputEditTextDataDespesa.setText(DateUtil.dataAtual());
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDespesaTotal();
    }

    public void salvarDespesa(View view){

        if (validaCamposDespesasActivity()){
            String data = textInputEditTextDataDespesa.getText().toString();
            Double valorRecuperado = Double.parseDouble(editTextValorDespesa.getText().toString());

            movimentacao = new Movimentacao();
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria( textInputEditTextCategoriaDespesa.getText().toString() );
            movimentacao.setDescricao( textInputEditTextDescricaoDespesa.getText().toString());
            movimentacao.setData( data );
            movimentacao.setTipo("despesa");

            Double despesaAtualizada = despesaTotal + valorRecuperado;
            atualizarDespesa(despesaAtualizada);

            movimentacao.salvarMovimentacaoFirebase(data);
            finish();
        }
    }

    private void recuperarDespesaTotal(){

        String idUsuario = Base64Custom.codificarBase64(firebaseAuth.getCurrentUser().getEmail());
        this.usuarioRef = databaseReference.child("usuarios").child(idUsuario);

       valueEventListenerUsuario =  usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void atualizarDespesa(Double despesa){

        String idUsuario = Base64Custom.codificarBase64(firebaseAuth.getCurrentUser().getEmail());
        DatabaseReference usuarioRef = databaseReference.child("usuarios").child(idUsuario);

        usuarioRef.child("despesaTotal").setValue(despesa);
    }

    private Boolean validaCamposDespesasActivity(){

        if (editTextValorDespesa.getText().toString().isEmpty()){
            editTextValorDespesa.setError("Campo Obrigatório!");
            return false;
        }

        if (textInputEditTextDataDespesa.getText().toString().isEmpty()){
            textInputEditTextDataDespesa.setError("Campo Obrigatório!");
            return false;
        }

        if (textInputEditTextDescricaoDespesa.getText().toString().isEmpty()){
            textInputEditTextDescricaoDespesa.setError("Campo Obrigatório!");
            return false;
        }

        if (textInputEditTextCategoriaDespesa.getText().toString().isEmpty()){
            textInputEditTextCategoriaDespesa.setError("Campo Obrigatório!");
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
