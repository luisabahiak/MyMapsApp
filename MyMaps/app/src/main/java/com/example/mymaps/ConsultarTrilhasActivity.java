package com.example.mymaps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ConsultarTrilhasActivity extends AppCompatActivity {

    private ListView listViewTrilhas;
    private Button btnLimparIntervalo, btnLimparTudo;
    private TrilhaDB trilhaDB;

    private Button btnVoltar;
    private ArrayList<TrilhaModel> listaTrilhas;
    private ArrayAdapter<TrilhaModel> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_trilhas);

        trilhaDB = new TrilhaDB(this);
        listViewTrilhas = findViewById(R.id.list_view_trilhas);
        btnLimparIntervalo = findViewById(R.id.btn_limpar_intervalo);
        btnLimparTudo = findViewById(R.id.btn_limpar_tudo);
        btnVoltar = findViewById(R.id.buttonVoltar3);

        atualizarLista();

        // Clique curto: Consultar trajeto no mapa
        listViewTrilhas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrilhaModel trilhaSelecionada = listaTrilhas.get(position);
                Intent intent = new Intent(ConsultarTrilhasActivity.this, VisualizarMapaHistoricoActivity.class);
                intent.putExtra("TRILHA_ID", trilhaSelecionada.getId());
                intent.putExtra("TRILHA_NOME", trilhaSelecionada.getNome());
                intent.putExtra("TRILHA_INICIO", trilhaSelecionada.getDataInicio());
                intent.putExtra("TRILHA_FIM", trilhaSelecionada.getDataFim());
                startActivity(intent);
            }
        });

        // Clique longo: Opções de Editar nome ou Apagar trilha específica
        listViewTrilhas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                abrirMenuOpcoesTrilha(listaTrilhas.get(position));
                return true;
            }
        });

        // Botão Apagar Tudo
        btnLimparTudo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ConsultarTrilhasActivity.this)
                        .setTitle("Apagar Histórico")
                        .setMessage("Tem certeza de que deseja deletar TODAS as trilhas do aparelho?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                trilhaDB.apagaTrilha();
                                atualizarLista();
                                Toast.makeText(ConsultarTrilhasActivity.this, "Todo o histórico foi limpo!", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Não", null).show();
            }
        });

        // Botão Apagar por Intervalo de Data
        btnLimparIntervalo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogoFiltroData();
            }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void atualizarLista() {
        listaTrilhas = trilhaDB.recuperarTodasAsTrilhas();
        // Usamos um layout nativo simples do Android para textos brancos na lista escura
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaTrilhas);
        listViewTrilhas.setAdapter(adapter);
    }

    private void abrirMenuOpcoesTrilha(final TrilhaModel trilha) {
        String[] opcoes = {"Editar Nome", "Apagar esta Trilha"};
        new AlertDialog.Builder(this)
                .setTitle(trilha.getNome())
                .setItems(opcoes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            abrirDialogoEditarNome(trilha);
                        } else if (which == 1) {
                            trilhaDB.apagarTrilhaEspecifica(trilha.getId());
                            atualizarLista();
                            Toast.makeText(ConsultarTrilhasActivity.this, "Trilha removida!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    private void abrirDialogoEditarNome(final TrilhaModel trilha) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alterar Nome");
        final EditText input = new EditText(this);
        input.setText(trilha.getNome());
        builder.setView(input);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String novoNome = input.getText().toString().trim();
                if (!novoNome.isEmpty()) {
                    trilhaDB.editarNomeTrilha(trilha.getId(), novoNome);
                    atualizarLista();
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void abrirDialogoFiltroData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Apagar por Intervalo");
        builder.setMessage("Digite as datas limite no formato DD/MM/AAAA:");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText inputInicio = new EditText(this);
        inputInicio.setHint("Data Inicial (Ex: 01/06/2026)");
        layout.addView(inputInicio);

        final EditText inputFim = new EditText(this);
        inputFim.setHint("Data Final (Ex: 07/06/2026)");
        layout.addView(inputFim);

        builder.setView(layout);

        builder.setPositiveButton("Excluir Período", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String d1 = inputInicio.getText().toString().trim();
                String d2 = inputFim.getText().toString().trim();
                if (d1.length() == 10 && d2.length() == 10) {
                    trilhaDB.apagarTrilhasPorIntervaloData(d1, d2);
                    atualizarLista();
                    Toast.makeText(ConsultarTrilhasActivity.this, "Trilhas do período excluídas!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ConsultarTrilhasActivity.this, "Formato de data inválido!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

}