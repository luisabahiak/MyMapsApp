package com.example.mymaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class TrilhaDB extends SQLiteOpenHelper {

    private static final String DATABASE = "trilha_database";
    private static final int VERSION = 2;

    public TrilhaDB(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_trilhas_table = "CREATE TABLE trilhas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nome TEXT NOT NULL, " +
                "data_inicio TEXT NOT NULL, " +
                "data_fim TEXT);";
        db.execSQL(create_trilhas_table);

        String create_waypoints_table = "CREATE TABLE waypoints (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "trilha_id INTEGER NOT NULL, " +
                "latitude NUMERIC NOT NULL, " +
                "longitude NUMERIC NOT NULL, " +
                "altitude NUMERIC NOT NULL, " +
                "FOREIGN KEY(trilha_id) REFERENCES trilhas(id) ON DELETE CASCADE);";
        db.execSQL(create_waypoints_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS waypoints");
        db.execSQL("DROP TABLE IF EXISTS trilhas");
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Habilita o suporte a chaves estrangeiras para que o ON DELETE CASCADE funcione
        // Isso garante que ao apagar uma trilha, todos os waypoints dela sumam automaticamente
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    public long criarNovaTrilha(String nome, String dataInicio) {
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("data_inicio", dataInicio);
        return getWritableDatabase().insert("trilhas", null, values);
    }

    public void finalizarTrilha(long trilhaId, String dataFim) {
        ContentValues values = new ContentValues();
        values.put("data_fim", dataFim);
        getWritableDatabase().update("trilhas", values, "id = ?", new String[]{String.valueOf(trilhaId)});
    }

    public void registrarWaypoint(Waypoint waypoint) {
        ContentValues values = new ContentValues();
        values.put("trilha_id", waypoint.getTrilhaId());
        values.put("latitude", waypoint.getLatitude());
        values.put("longitude", waypoint.getLongitude());
        values.put("altitude", waypoint.getAltitude());
        getWritableDatabase().insert("waypoints", null, values);
    }

    public ArrayList<Waypoint> recuperarWaypoints(long trilhaId) {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        String[] columns = {"id", "trilha_id", "latitude", "longitude", "altitude"};

        Cursor cursor = getWritableDatabase().query("waypoints", columns,
                "trilha_id = ?", new String[]{String.valueOf(trilhaId)}, null, null, null);

        while (cursor.moveToNext()) {
            Waypoint waypoint = new Waypoint();
            waypoint.setId(cursor.getInt(0));
            waypoint.setTrilhaId(cursor.getLong(1));
            waypoint.setLatitude(cursor.getDouble(2));
            waypoint.setLongitude(cursor.getDouble(3));
            waypoint.setAltitude(cursor.getDouble(4));
            waypoints.add(waypoint);
        }
        cursor.close();
        return waypoints;
    }

    // --- MÉTODOS REQUISITADOS PARA A TELA DE CONSULTA ---

    // 1. Recupera a lista de todas as trilhas cadastradas para exibir no histórico
    public ArrayList<TrilhaModel> recuperarTodasAsTrilhas() {
        ArrayList<TrilhaModel> lista = new ArrayList<>();
        Cursor cursor = getWritableDatabase().rawQuery("SELECT id, nome, data_inicio, data_fim FROM trilhas ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            TrilhaModel t = new TrilhaModel();
            t.setId(cursor.getLong(0));
            t.setNome(cursor.getString(1));
            t.setDataInicio(cursor.getString(2));
            t.setDataFim(cursor.getString(3));
            lista.add(t);
        }
        cursor.close();
        return lista;
    }

    // 2. Permite alterar o nome de uma trilha selecionada
    public void editarNomeTrilha(long id, String novoNome) {
        ContentValues values = new ContentValues();
        values.put("nome", novoNome);
        getWritableDatabase().update("trilhas", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // 3. Apaga uma trilha específica por ID (o CASCADE limpa os waypoints)
    public void apagarTrilhaEspecifica(long id) {
        getWritableDatabase().delete("trilhas", "id = ?", new String[]{String.valueOf(id)});
    }

    // 4. Apaga trilhas contidas em um determinado intervalo de data
    // Formato esperado das datas recebidas no parâmetro: "dd/MM/yyyy"
    public void apagarTrilhasPorIntervaloData(String dataInicio, String dataFim) {
        // Convertemos as strings brasileiras "dd/MM/yyyy..." para ordenação comparável do SQLite
        // Usamos substr para ordenar ano-mes-dia na query
        String query = "DELETE FROM trilhas WHERE " +
                "substr(data_inicio,7,4)||substr(data_inicio,4,2)||substr(data_inicio,1,2) " +
                "BETWEEN ? AND ?";

        String dtStartFormat = dataInicio.substring(6,10) + dataInicio.substring(3,5) + dataInicio.substring(0,2);
        String dtEndFormat = dataFim.substring(6,10) + dataFim.substring(3,5) + dataFim.substring(0,2);

        getWritableDatabase().execSQL(query, new String[]{dtStartFormat, dtEndFormat});
    }

    // 5. Apaga todas as trilhas percorridas
    public void apagaTrilha() {
        getWritableDatabase().execSQL("DELETE FROM waypoints");
        getWritableDatabase().execSQL("DELETE FROM trilhas");
    }
}