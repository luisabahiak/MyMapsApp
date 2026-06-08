package com.example.mymaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracaoActivity extends AppCompatActivity {

    private RadioGroup radioGroupMapa, radioGroupNavegacao;
    private RadioButton radioVetorial, radioSatelite, radioNorthUp, radioCourseUp;
    private Button btnSalvar;

    // Constantes de chaves de armazenamento das SharedPreferences
    public static final String PREFS_NAME = "ConfigsAppTrilhas";
    public static final String KEY_TIPO_MAPA = "tipo_mapa";          // "vetorial" ou "satelite"
    public static final String KEY_FORMA_NAVEGACAO = "forma_nav";    // "north_up" ou "course_up"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        // Vinculação dos componentes de interface
        radioGroupMapa = findViewById(R.id.radioGroup_mapa);
        radioGroupNavegacao = findViewById(R.id.radioGroup_navegacao);

        radioVetorial = findViewById(R.id.radio_vetorial);
        radioSatelite = findViewById(R.id.radio_satelite);
        radioNorthUp = findViewById(R.id.radio_north_up);
        radioCourseUp = findViewById(R.id.radio_course_up);
        btnSalvar = findViewById(R.id.button_salvar_config);

        // Carrega as configurações atualmente gravadas para atualizar os botões de seleção
        carregarPreferenciasSalvas();

        // Salva as novas escolhas nas SharedPreferences
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarPreferencias();
            }
        });
    }

    private void carregarPreferenciasSalvas() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Define "vetorial" e "north_up" como strings default caso o app esteja abrindo pela primeira vez
        String tipoMapa = sharedPref.getString(KEY_TIPO_MAPA, "vetorial");
        String formaNav = sharedPref.getString(KEY_FORMA_NAVEGACAO, "north_up");

        if (tipoMapa.equals("satelite")) {
            radioSatelite.setChecked(true);
        } else {
            radioVetorial.setChecked(true);
        }

        if (formaNav.equals("course_up")) {
            radioCourseUp.setChecked(true);
        } else {
            radioNorthUp.setChecked(true);
        }
    }

    private void salvarPreferencias() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Avalia qual RadioButton de Tipo de Mapa foi selecionado
        if (radioGroupMapa.getCheckedRadioButtonId() == R.id.radio_satelite) {
            editor.putString(KEY_TIPO_MAPA, "satelite");
        } else {
            editor.putString(KEY_TIPO_MAPA, "vetorial");
        }

        // Avalia qual RadioButton de Orientação foi selecionado
        if (radioGroupNavegacao.getCheckedRadioButtonId() == R.id.radio_course_up) {
            editor.putString(KEY_FORMA_NAVEGACAO, "course_up");
        } else {
            editor.putString(KEY_FORMA_NAVEGACAO, "north_up");
        }

        // Aplica as alterações de forma assíncrona
        editor.apply();

        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();

        // Encerra a activity e retorna para a tela inicial (MainActivity)
        finish();
    }
}