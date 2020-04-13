package com.yuriluisgarciapereira.organizze.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.yuriluisgarciapereira.organizze.config.ConfiguracaoFirebase;
import com.yuriluisgarciapereira.organizze.helper.Base64Custom;
import com.yuriluisgarciapereira.organizze.helper.DateUtil;

public class Movimentacao {

    private String data;
    private String categoria;
    private String descricao;
    private String tipo;
    private double valor;
    private String id;

    public Movimentacao(){

    }

    public void salvarMovimentacaoFirebase(String dataEscolhida){
        FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();

        String idUusuario = ConfiguracaoFirebase.referenciaDoUsuario();
        String mesAno = DateUtil.mesAnoDataEscolhida(dataEscolhida);

        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDataBase();
        databaseReference.child("movimentacao")
                .child( idUusuario )
                .child( mesAno )
                .push()
                .setValue(this);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
