package com.yuriluisgarciapereira.organizze.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private Movimentacao movimentacao;
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
    protected void onStop() {
        super.onStop();
        this.usuarioRef.removeEventListener(valueEventListenerUsuario);
        this.movimentacoesReferencia.removeEventListener(valueEventListenerMovimentacoes);
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

    private void atualizarSaldo(){

        String idUsuario = ConfiguracaoFirebase.referenciaDoUsuario();

        this.usuarioRef = databaseReference.child("usuarios").child(idUsuario);

        if ( movimentacao.getTipo().equals("receita")){

            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(receitaTotal);
        }

        if (movimentacao.getTipo().equals("despesa")){

            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue(despesaTotal);
        }
    }

    private void recuperaMovimentacoes() {

        String idUsuario = ConfiguracaoFirebase.referenciaDoUsuario();

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
                            movimentacao.setId(dados.getKey());
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

        String idUsuario = ConfiguracaoFirebase.referenciaDoUsuario();

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

                textViewNomeUsuario.setText(getString(R.string.ola) + usuario.getNome());
                textViewSaldoUsuario.setText(getString(R.string.real) + resumoFormatado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void swipe() {

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE; //Desabilita o dragAndDrop
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                excluirMovimentacao(viewHolder);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerViewMovimentacoes);
    }

    private void excluirMovimentacao(@NonNull final RecyclerView.ViewHolder viewHolder) {

        AlertDialog.Builder alertDialogExcluir = new AlertDialog.Builder(this)
                .setTitle(R.string.excluirMovimentacaoConta)
                .setMessage(getString(R.string.certezaExcluirMovimentacao) +
                        "da sua conta??")
                .setCancelable(false)
                .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int position = viewHolder.getAdapterPosition();
                        movimentacao = movimentacoes.get(position);

                        String idUsuario = ConfiguracaoFirebase.referenciaDoUsuario();
                        movimentacoesReferencia = databaseReference
                                .child("movimentacao")
                                .child(idUsuario)
                                .child(mesAnoSelecionado);

                        movimentacoesReferencia.child(movimentacao.getId()).removeValue();
                        adapterMovimentacao.notifyItemRemoved(position);
                        atualizarSaldo();
                    }
                })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(PrincipalActivity.this,
                                "Cancelado!", Toast.LENGTH_SHORT).show();
                        adapterMovimentacao.notifyDataSetChanged();
                    }
                });

        AlertDialog alert = alertDialogExcluir.create();
        alert.show();

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

                movimentacoesReferencia.removeEventListener(valueEventListenerMovimentacoes);
                recuperaMovimentacoes();
            }
        });
    }

    private void iniciaToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.tituloAplicacao);
    }

    public void instanciaLayoutComXml() {

        textViewNomeUsuario = findViewById(R.id.textViewNomeUsuario);
        textViewSaldoUsuario = findViewById(R.id.textViewSaldo);
        recyclerViewMovimentacoes = findViewById(R.id.recyclerViewMovimentacoes);
        calendarViewMes = findViewById(R.id.calendarViewMes);
        configurarCalendarView();
        swipe();
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
}
