package com.yuriluisgarciapereira.organizze.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yuriluisgarciapereira.organizze.helper.Base64Custom;

public class ConfiguracaoFirebase {

    private static FirebaseAuth autenticacao;
    private static DatabaseReference databaseReference;
    private static String referenciaDoUsuario;

    static public FirebaseAuth getFirebaseAutenticacao(){

        if ( autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }

    static public DatabaseReference getFirebaseDataBase(){

        if (databaseReference == null){
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    static public String referenciaDoUsuario(){

        referenciaDoUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());
        return referenciaDoUsuario;
    }

}
