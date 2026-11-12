package com.example.a04.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a04.Adapters.AdapterFavorito;
import com.example.a04.R;
import com.example.a04.api.Filme;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class Favorito_fragment extends Fragment {

    private TextView text_empty_favorites;
    private RecyclerView recycler_favorites;
    private AdapterFavorito adapter;
    private ArrayList<Filme> filmes;

    //API
    public Favorito_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorito, container, false);

        text_empty_favorites = view.findViewById(R.id.text_empty_favorites);
        recycler_favorites = view.findViewById(R.id.recycler_favorites);

        filmes = new ArrayList<>();
        adapter = new AdapterFavorito(filmes, getContext(), text_empty_favorites);
        recycler_favorites.setAdapter(adapter);

        checarFavoritos();

        return view;
    }

    private void checarFavoritos() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Usuários")
                .document(usuario.getUid())
                .collection("favoritos")
                .document("filmes")
                .get().addOnCompleteListener(resultado -> {
                    if (resultado.isSuccessful()) {
                        DocumentSnapshot documento = resultado.getResult();
                        if (documento.exists() && documento != null) {
                            ArrayList<Map<String, Object>> favoritos =
                                    (ArrayList<Map<String, Object>>) documento.get("lista");

                            if (favoritos != null) {
                                filmes.clear();
                                for (var filme : favoritos) {
                                    Filme f = new Filme();

                                    Long id = (Long) filme.get("id");
                                    f.setFilme_id(id.intValue());
                                    f.setTitulo((String) filme.get("titulo"));
                                    f.setData_lancamento((String) filme.get("data"));
                                    f.setDescricao((String) filme.get("descricao"));
                                    f.setPoster((String) filme.get("poster"));
                                    if (filme.get("nota") != null) {
                                        f.setAvaliacao(((Number) filme.get("nota")).doubleValue());
                                    }

                                    filmes.add(f);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (filmes.isEmpty()) {
                            text_empty_favorites.setVisibility(View.VISIBLE);
                        } else {
                            text_empty_favorites.setVisibility(View.GONE);
                        }
                    }
                });
    }

}