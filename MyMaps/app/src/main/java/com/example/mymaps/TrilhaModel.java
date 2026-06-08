package com.example.mymaps;

public class TrilhaModel {
    private long id;
    private String nome;
    private String dataInicio;
    private String dataFim;

    public TrilhaModel() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    // Representação textual para aparecer bonito na ListView padrão
    @Override
    public String toString() {
        return nome + "\nInício: " + dataInicio;
    }
}