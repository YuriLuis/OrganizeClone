package com.yuriluisgarciapereira.organizze.helper;

import com.yuriluisgarciapereira.organizze.exceptions.CampoVazioException;

public class ValidaCamposApp {

    public static  boolean campoEhVazio(String campo){
        return campo.isEmpty();
    }
}
