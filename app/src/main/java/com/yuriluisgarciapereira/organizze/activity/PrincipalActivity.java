package com.yuriluisgarciapereira.organizze.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.yuriluisgarciapereira.organizze.R;
import com.yuriluisgarciapereira.organizze.adapter.AdapterMovimentacao;
import com.yuriluisgarciapereira.organizze.config.ConfiguracaoFirebase;
import com.yuriluisgarciapereira.organizze.helper.Base64Custom;
import com.yuriluisgarciapereira.organizze.model.Movimentacao;
import com.yuriluisgarciapereira.organizze.model.Usuario;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    //Front-End
    private Toolbar toolbar;
    private TextView textViewNomeUsuario, textViewSaldoUsuario;
    private MaterialCalendarView calendarViewMes;

    //Back-End
    private DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference usuarioRef;
    private DatabaseReference movimentacoesReferencia;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;
    private RecyclerView recyclerViewMovimentacoes;
    private String mesAnoSelecionado;


    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private AdapterMovimentacao adapterMovimentacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        iniciaToolbar();
        instanciaLayoutComXml();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaMovimentacoes();
        recuperarValorSaldo();
        configuraRecyclerViewMovimentacoes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menusair:
                this.autenticacao.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void recuperaMovimentacoes() {

        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());
        movimentacoesReferencia = databaseReference
                .child("movimentacao")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        valueEventListenerMovimentacoes = movimentacoesReferencia
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        movimentacoes.clear();
                        for (DataSnapshot dados : dataSnapshot.getChildren()) {
                            Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                            movimentacoes.add(movimentacao);
                        }
                        adapterMovimentacao.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void recuperarValorSaldo() {

        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());

        this.usuarioRef = databaseReference.child("usuarios").child(idUsuario);

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.##");
                String resumoFormatado = decimalFormat.format(resumoUsuario);

                textViewNomeUsuario.setText("Olá, " + usuario.getNome());
                textViewSaldoUsuario.setText("R$: " + resumoFormatado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void configurarCalendarView() {
        final CharSequence meses[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        calendarViewMes.setTitleMonths(meses);

        CalendarDay dataAtual = calendarViewMes.getCurrentDate();
        String mesSelecionado = "0" + dataAtual.getMonth();
        mesAnoSelecionado = String.valueOf(mesSelecionado + "" + dataAtual.getYear());

        calendarViewMes.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = "0" + date.getMonth();
                mesAnoSelecionado = String.valueOf(mesSelecionado + "" + date.getYear());
                Log.i("MES", "Mes " + mesSelecionado);
            }
        });
    }

    private void iniciaToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Organizze");
    }

    public void instanciaLayoutComXml() {
        textViewNomeUsuario = findViewById(R.id.textViewNomeUsuario);
        textViewSaldoUsuario = findViewById(R.id.textViewSaldo);
        recyclerViewMovimentacoes = findViewById(R.id.recyclerViewMovimentacoes);
        calendarViewMes = findViewById(R.id.calendarViewMes);
        configurarCalendarView();

    }

    public void adicionarDespesa(View view) {
        startActivity(new Intent(this, DespesasActivity.class));
    }

    public void adicionarReceita(View view) {
        startActivity(new Intent(this, ReceitasActivity.class));
    }

    private void configuraRecyclerViewMovimentacoes() {

        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMovimentacoes.setLayoutManager(layoutManager);
        recyclerViewMovimentacoes.setHasFixedSize(true);
        recyclerViewMovimentacoes.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        recyclerViewMovimentacoes.setAdapter(adapterMovimentacao);
    }


    @Override
    protected void onStop() {
        super.onStop();
        this.usuarioRef.removeEventListener(valueEventListenerUsuario);
        this.movimentacoesReferencia.removeEventListener(valueEventListenerMovimentacoes);
    }

}
