package com.example.a04.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a04.Adapters.AdapterRecomendados;
import com.example.a04.BuildConfig;
import com.example.a04.R;
import com.example.a04.api.Filme;
import com.example.a04.api.FilmeApi;
import com.example.a04.api.Results;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Recomendados_fragment extends Fragment {

    private ArrayList<Filme> filmes;
    private AdapterRecomendados adapter;
    private RecyclerView recycler_recommendations;

    private boolean carregando = false;
    private Set<Integer> filmesLista;
    private ArrayList<Integer> generosSalvos = new ArrayList<>();
    private boolean usuarioTemGeneros = false;

    //VARIÁVEIS API

    private int paginaAtual = 1;
    private String idioma = "pt-BR";
    FilmeApi api;
    private Retrofit retrofit;
    private String chave_api = BuildConfig.TMDB_API_KEY;

    public Recomendados_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recomendados, container, false);

        recycler_recommendations = view.findViewById(R.id.recycler_recommendations);

        retrofit = new Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/").addConverterFactory(GsonConverterFactory.create()).build();

        filmesLista = new HashSet<>();
        filmes = new ArrayList<>();
        api = retrofit.create(FilmeApi.class);

        adapter = new AdapterRecomendados(filmes, getContext());
        recycler_recommendations.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = recycler_recommendations.getLayoutManager();
        recycler_recommendations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && layoutManager instanceof GridLayoutManager) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;

                    int visiveis = gridLayoutManager.getChildCount();
                    int totais = gridLayoutManager.getItemCount();
                    int primeiroVisivel = gridLayoutManager.findFirstVisibleItemPosition();

                    if (!carregando && (visiveis + primeiroVisivel) >= totais - 4 && usuarioTemGeneros && paginaAtual < 500) {
                        carregando = true;
                        paginaAtual++;
                        procurarFilmes(generosSalvos);
                    }
                }
            }
        });


        checarGeneros();

        return view;
    }

    private void checarGeneros() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Usuários").document(usuario.getUid()).collection("generos").orderBy("peso", Query.Direction.DESCENDING).limit(3).get().addOnSuccessListener(resultado -> {
            if (resultado.isEmpty()) {
                procurarFilmes();
            } else {
                usuarioTemGeneros = true;

                for (QueryDocumentSnapshot documento : resultado) {
                    Long idGenero = documento.getLong("id");
                    if (idGenero != null) {
                        generosSalvos.add(idGenero.intValue());
                    }
                }
                procurarFilmes(generosSalvos);
            }
        });
    }

    private void procurarFilmes() {

        Call<Results> chamada = api.descobrirFilmes(chave_api, idioma);
        chamada.enqueue(new Callback<Results>() {
            @Override
            public void onResponse(Call<Results> call, Response<Results> response) {
                carregando = false;
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Filme> filmesTotais = response.body().getResultados();
                    ArrayList<Filme> filmesFiltrados = new ArrayList<>();

                    for (Filme f : filmesTotais) {
                        if (!filmesLista.contains(f.getFilme_id())) {
                            filmesFiltrados.add(f);
                            filmesLista.add(f.getFilme_id());
                        }
                    }

                    int posicaoInserir = filmes.size();
                    filmes.addAll(filmesFiltrados);
                    adapter.notifyItemRangeInserted(posicaoInserir, filmesFiltrados.size());
                }
            }

            @Override
            public void onFailure(Call<Results> call, Throwable t) {
                carregando = false;
            }
        });
    }

    private void procurarFilmes(ArrayList<Integer> ids) {
        String queryGenero;
        if (ids.size() == 1) {
            queryGenero = String.valueOf(ids.get(0));
        } else if (ids.size() == 2) {
            queryGenero = ids.get(0) + "|" + ids.get(1);
        } else {
            queryGenero = ids.get(0) + "|" + ids.get(1) + "|" + ids.get(2);
        }

        Call<Results> chamada = api.buscarPorGenero(chave_api, idioma, queryGenero, paginaAtual);
        chamada.enqueue(new Callback<Results>() {
            @Override
            public void onResponse(Call<Results> call, Response<Results> response) {
                carregando = false;
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Filme> filmesTotais = response.body().getResultados();
                    ArrayList<Filme> filmesFiltrados = new ArrayList<>();

                    for (Filme f : filmesTotais) {
                        if (!filmesLista.contains(f.getFilme_id())) {
                            filmesFiltrados.add(f);
                            filmesLista.add(f.getFilme_id());
                        }
                    }

                    int posicaoInserir = filmes.size();
                    filmes.addAll(filmesFiltrados);
                    adapter.notifyItemRangeInserted(posicaoInserir, filmesFiltrados.size());
                }
            }

            @Override
            public void onFailure(Call<Results> call, Throwable t) {
                carregando = false;
            }
        });
    }
}