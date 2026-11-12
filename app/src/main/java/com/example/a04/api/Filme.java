package com.example.a04.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Filme {

    @SerializedName("id")
    private int filme_id;

    @SerializedName("title")
    private String titulo;

    @SerializedName("overview")
    private String descricao;

    @SerializedName("poster_path")
    private String poster;

    @SerializedName("genre_ids")
    private ArrayList<Integer> generos_id;

    @SerializedName("release_date")
    private String data_lancamento;

    @SerializedName("vote_average")
    private double avaliacao;

    public String getAnoLancamento() {
        if (data_lancamento != null && data_lancamento.length() >= 4) {
            return data_lancamento.substring(0, 4);
        }
        return "N/D";
    }

    public static String getGeneros(int id) {
        switch (id) {
            case 28:
                return "Ação";
            case 12:
                return "Aventura";
            case 16:
                return "Animação";
            case 35:
                return "Comédia";
            case 80:
                return "Crime";
            case 99:
                return "Documentário";
            case 18:
                return "Drama";
            case 10751:
                return "Família";
            case 14:
                return "Fantasia";
            case 36:
                return "História";
            case 27:
                return "Terror";
            case 10402:
                return "Música";
            case 9648:
                return "Mistério";
            case 10749:
                return "Romance";
            case 878:
                return "Ficção Científica";
            case 10770:
                return "Cinema TV";
            case 53:
                return "Suspense";
            case 10752:
                return "Guerra";
            case 37:
                return "Faroeste";
            default:
                return "Filme";
        }
    }

    public int getFilme_id() {
        return filme_id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPoster() {
        return poster;
    }

    public ArrayList<Integer> getGeneros_id() {
        return generos_id;
    }

    public String getData_lancamento() {
        return data_lancamento;
    }

    public double getAvaliacao() {
        return avaliacao;
    }

    public void setFilme_id(int filme_id) {
        this.filme_id = filme_id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setData_lancamento(String data_lancamento) {
        this.data_lancamento = data_lancamento;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }
}
