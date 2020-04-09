package com.yuriluisgarciapereira.organizze.helper;

import android.widget.EditText;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

public class Mascaras {

    public static void adicionaMascaraData(EditText editText) {
        SimpleMaskFormatter smf = new SimpleMaskFormatter("NN/NN/NNNN");
        MaskTextWatcher mtw = new MaskTextWatcher(editText, smf);
        editText.addTextChangedListener(mtw);
    }
}
