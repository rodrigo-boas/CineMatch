package com.example.a04.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.a04.Adapters.AdapterDescobrir;
import com.example.a04.BuildConfig;
import com.example.a04.R;
import com.example.a04.api.Filme;
import com.example.a04.api.FilmeApi;
import com.example.a04.api.Results;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Descobrir_fragment extends Fragment implements CardStackListener {

    private ProgressBar progress;
    private CardStackView cardStackView;
    private CardStackLayoutManager layoutManager;
    private TextView textRatedCount;
    private int filmesAvaliados = 0;
    private ArrayList<Filme> filmes;
    private AdapterDescobrir adapter;

    //VARIÁVEIS API

    private Set<Integer> filmesVistos = new HashSet<>();
    private int paginaAtual = 1;
    private String idioma = "pt-BR";
    FilmeApi api;
    private Retrofit retrofit;
    private String chave_api = BuildConfig.TMDB_API_KEY;

    public Descobrir_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_descobrir, container, false);

        ArrayList<Direction> directions = new ArrayList<>(
                Arrays.asList(Direction.Left, Direction.Right, Direction.Top)
        );

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(FilmeApi.class);
        filmes = new ArrayList<>();

        atualizarContador();

        checarHistorico();

        progress = view.findViewById(R.id.progress);
        cardStackView = view.findViewById(R.id.card_stack_view);
        textRatedCount = view.findViewById(R.id.text_rated_count);
        ImageButton button_dislike = view.findViewById(R.id.button_dislike);
        ImageButton button_favorite = view.findViewById(R.id.button_favorite);
        ImageButton button_like = view.findViewById(R.id.button_like);

        layoutManager = new CardStackLayoutManager(getContext(), this);
        layoutManager.setCanScrollHorizontal(true);
        layoutManager.setCanScrollVertical(true);
        layoutManager.setDirections(directions);
        cardStackView.setLayoutManager(layoutManager);

        adapter = new AdapterDescobrir(filmes, getContext());
        cardStackView.setAdapter(adapter);

        button_dislike.setOnClickListener(v -> deslizarCard(Direction.Left));
        button_favorite.setOnClickListener(v -> deslizarCard(Direction.Top));
        button_like.setOnClickListener(v -> deslizarCard(Direction.Right));

        // Inflate the layout for this fragment
        return view;
    }

    private void atualizarContador() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Usuários")
                .document(usuario.getUid())
                .collection("historico")
                .document("filmes")
                .get().addOnCompleteListener(resultado -> {
                    if (resultado.isSuccessful() && resultado.getResult() != null) {
                        DocumentSnapshot documento = resultado.getResult();

                        if (documento.exists()) {
                            ArrayList<Object> vistos = (ArrayList<Object>) documento.get("vistos");

                            if (vistos != null) {
                                filmesAvaliados = vistos.size();

                                textRatedCount.setText(filmesAvaliados + " filmes avaliados");
                            } else {
                                textRatedCount.setText("0 filmes avaliados");
                            }
                        } else {
                            textRatedCount.setText("0 filmes avaliados");
                        }
                    }
                });
    }

    private void checarHistorico() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            checarGeneros();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Usuários")
                .document(usuario.getUid())
                .collection("historico")
                .document("filmes")
                .get().addOnSuccessListener(document -> {
                    filmesVistos.clear();
                    if (document.exists() && document.get("vistos") != null) {
                        ArrayList<Long> ids = (ArrayList<Long>) document.get("vistos");
                        for (Long id : ids) {
                            filmesVistos.add(id.intValue());
                        }
                    }
                    checarGeneros();
                }).addOnFailureListener(e -> checarGeneros());
    }

    private void checarGeneros() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            procurarFilmes();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Usuários")
                .document(usuario.getUid())
                .collection("generos")
                .orderBy("peso", Query.Direction.DESCENDING)
                .limit(3).get().addOnSuccessListener(resultado -> {
                    if (resultado.isEmpty()) {
                        procurarFilmes();
                    } else {
                        ArrayList<Integer> idsGostados = new ArrayList<>();

                        for (QueryDocumentSnapshot documento : resultado) {
                            Long idGenero = documento.getLong("id");
                            if (idGenero != null) {
                                idsGostados.add(idGenero.intValue());
                            }
                        }
                        procurarFilmes(idsGostados);
                    }
                }).addOnFailureListener(e -> {
                    procurarFilmes();
                });
    }

    private void procurarFilmes() {

        Call<Results> chamada = api.descobrirFilmes(chave_api, idioma);
        chamada.enqueue(new Callback<Results>() {
            @Override
            public void onResponse(Call<Results> call, Response<Results> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    filmes.addAll(response.body().getResultados());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Results> call, Throwable t) {
                progress.setVisibility(View.GONE);
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
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Filme> filmesTotais = response.body().getResultados();
                    ArrayList<Filme> filmesIneditos = new ArrayList<>();

                    for (Filme f : filmesTotais) {
                        if (!filmesVistos.contains(f.getFilme_id())) {
                            filmesIneditos.add(f);
                        }
                    }

                    int posicaoInserir = filmes.size();
                    filmes.addAll(filmesIneditos);
                    adapter.notifyItemRangeInserted(posicaoInserir, filmesIneditos.size());
                    progress.setVisibility(View.GONE);

                    if (filmes.size() < 10 && paginaAtual < 500) {
                        paginaAtual++;
                        procurarFilmes(ids);
                    }
                }
            }

            @Override
            public void onFailure(Call<Results> call, Throwable t) {

            }
        });
    }

    private void salvarHistorico(int idFilme) {
        filmesVistos.add(idFilme);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Usuários")
                .document(usuario.getUid())
                .collection("historico")
                .document("filmes")
                .update("vistos", FieldValue.arrayUnion(idFilme))
                .addOnFailureListener(e -> {
                    Map<String, Object> primeiroFilme = new HashMap<>();
                    primeiroFilme.put("vistos", Arrays.asList(idFilme));

                    db.collection("Usuários")
                            .document(usuario.getUid())
                            .collection("historico")
                            .document("filmes")
                            .set(primeiroFilme);
                });
    }

    private void salvarFavorito(Filme SalvarFilme) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> filmeMap = new HashMap<>();
        filmeMap.put("id", (long) SalvarFilme.getFilme_id());
        filmeMap.put("titulo", SalvarFilme.getTitulo());
        filmeMap.put("data", SalvarFilme.getData_lancamento());
        filmeMap.put("descricao", SalvarFilme.getDescricao());
        filmeMap.put("poster", SalvarFilme.getPoster());
        filmeMap.put("nota", SalvarFilme.getAvaliacao());

        Map<String, Object> dados = new HashMap<>();
        dados.put("lista", FieldValue.arrayUnion(filmeMap));

        db.collection("Usuários")
                .document(usuario.getUid())
                .collection("favoritos")
                .document("filmes")
                .set(dados, SetOptions.merge());
    }

    private void deslizarCard(Direction direction) {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                .setDirection(direction)
                .setDuration(200)
                .build();
        layoutManager.setSwipeAnimationSetting(setting);
        cardStackView.swipe();
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        filmesAvaliados++;
        textRatedCount.setText(filmesAvaliados + " filmes avaliados");

        int posicaoFilme = layoutManager.getTopPosition() - 1;

        if (posicaoFilme >= 0 && posicaoFilme < filmes.size()) {
            Filme filmeAtual = filmes.get(posicaoFilme);
            ArrayList<Integer> idsGeneros = filmeAtual.getGeneros_id();

            if (direction == Direction.Right) {
                atualizarGostos(idsGeneros, 2);
                salvarHistorico(filmeAtual.getFilme_id());
            } else if (direction == Direction.Left) {
                atualizarGostos(idsGeneros, -1);
                salvarHistorico(filmeAtual.getFilme_id());
            } else if (direction == Direction.Top) {
                atualizarGostos(idsGeneros, 5);
                salvarHistorico(filmeAtual.getFilme_id());
                salvarFavorito(filmeAtual);
            }
        }

        int cardsRestantes = filmes.size() - layoutManager.getTopPosition();

        if (cardsRestantes < 5 && paginaAtual < 500) {
            paginaAtual++;
            checarGeneros();
        }
    }

    private void atualizarGostos(ArrayList<Integer> idsGeneros, int valorAlteracao) {
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual == null) {
            return;
        }

        String uidUsuario = usuarioAtual.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Integer idGenero : idsGeneros) {
            String genero = Filme.getGeneros(idGenero);

            Map<String, Object> dadosGenero = new HashMap<>();
            dadosGenero.put("id", idGenero);
            dadosGenero.put("peso", FieldValue.increment(valorAlteracao));

            db.collection("Usuários")
                    .document(uidUsuario)
                    .collection("generos")
                    .document(genero)
                    .set(dadosGenero, SetOptions.merge());

        }
    }

    @Override
    public void onCardRewound() {

    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {

    }

    @Override
    public void onCardDisappeared(View view, int position) {

    }
}
