package com.example.a04.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a04.FilmeDetalhes;
import com.example.a04.R;
import com.example.a04.api.Filme;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterFavorito extends RecyclerView.Adapter<AdapterFavorito.FilmeViewHolder> {

    private ArrayList<Filme> listaFilmes;
    private Context context;
    private TextView text_empty_favorites;

    public AdapterFavorito(ArrayList<Filme> listaFilmes, Context context, TextView text_empty_favorites) {
        this.listaFilmes = listaFilmes;
        this.context = context;
        this.text_empty_favorites = text_empty_favorites;
    }

    @NonNull
    @Override
    public AdapterFavorito.FilmeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_historico, parent, false);
        return new FilmeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFavorito.FilmeViewHolder holder, int position) {
        Filme filme = listaFilmes.get(position);

        holder.text_title.setText(filme.getTitulo());
        holder.text_overview.setText(filme.getDescricao());
        holder.text_info.setText("Avaliação " + String.format("%.1f", filme.getAvaliacao()) + " | " + filme.getAnoLancamento());

        holder.button_delete.setOnClickListener(v -> {
            int posicao = holder.getBindingAdapterPosition();
            if (posicao != RecyclerView.NO_POSITION) {
                Filme filmeDeletado = listaFilmes.get(posicao);

                Map<String, Object> filmeMap = new HashMap<>();
                filmeMap.put("id", (long) filmeDeletado.getFilme_id());
                filmeMap.put("titulo", filmeDeletado.getTitulo());
                filmeMap.put("data", filmeDeletado.getData_lancamento());
                filmeMap.put("descricao", filmeDeletado.getDescricao());
                filmeMap.put("poster", filmeDeletado.getPoster());
                filmeMap.put("nota", filmeDeletado.getAvaliacao());

                Map<String, Object> deletar = new HashMap<>();
                deletar.put("lista", FieldValue.arrayRemove(filmeMap));

                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Usuários")
                            .document(usuario.getUid())
                            .collection("favoritos")
                            .document("filmes")
                            .update(deletar);
                }
                listaFilmes.remove(posicao);
                notifyItemRemoved(posicao);
                notifyItemRangeChanged(posicao, listaFilmes.size());

                if (listaFilmes.isEmpty()) {
                    text_empty_favorites.setVisibility(View.VISIBLE);
                }


            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FilmeDetalhes.class);

            intent.putExtra("titulo", filme.getTitulo());
            intent.putExtra("descricao", filme.getDescricao());
            intent.putExtra("poster", filme.getPoster());
            intent.putExtra("nota", filme.getAvaliacao());
            intent.putExtra("data", filme.getData_lancamento());

            v.getContext().startActivity(intent);
        });

        String image_url = "https://image.tmdb.org/t/p/w500" + filme.getPoster();
        Glide.with(context).load(image_url).into(holder.image_poster);
    }

    @Override
    public int getItemCount() {
        return listaFilmes.size();
    }

    class FilmeViewHolder extends RecyclerView.ViewHolder {

        ImageButton button_delete;
        ImageView image_poster;
        TextView text_title;
        TextView text_info;
        TextView text_overview;

        FilmeViewHolder(@NonNull View itemView) {
            super(itemView);

            image_poster = itemView.findViewById(R.id.image_poster);
            text_title = itemView.findViewById(R.id.text_title);
            text_info = itemView.findViewById(R.id.text_info);
            text_overview = itemView.findViewById(R.id.text_overview);
            button_delete = itemView.findViewById(R.id.button_delete);
        }
    }
}
