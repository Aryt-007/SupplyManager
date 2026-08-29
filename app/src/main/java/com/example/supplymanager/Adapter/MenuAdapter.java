package com.example.supplymanager.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supplymanager.Models.Notes;
import com.example.supplymanager.NotesClickListener;
import com.example.supplymanager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuAdapter extends RecyclerView.Adapter <MenuItemViewHolder>{

    Context context;
    List<Notes> list;
    NotesClickListener listener;

    public MenuAdapter(List<Notes> list, NotesClickListener listener, Context context) {
        this.list = list;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuItemViewHolder(LayoutInflater.from(context).inflate(R.layout.menu_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        holder.menu_list_item.setText(list.get(position).getTitle());
        holder.menu_list_item .setSelected(true);

        if (list.get(position).isPinned()) {
            holder.pin_image.setImageResource(R.drawable.pinned);
        } else {
            holder.pin_image.setImageResource(0);
        }

        // Смена цвета заднего фона в заметках
        int color_code = getRandomColor();
        holder.menu_list_container.setCardBackgroundColor(holder.itemView.getResources().getColor(color_code, null));

        holder.menu_list_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(list.get(holder.getAdapterPosition()));
            }
        });

        holder.menu_list_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongClick(list.get(holder.getAdapterPosition()), holder.menu_list_container);
                return false;
            }
        });
    }

    private int getRandomColor () { // Цвет заднего фона заметок
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.white);

        Random random = new Random();
        int random_color = random.nextInt(colorCode.size());
        return colorCode.get(random_color);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList (List <Notes> filterAddList) {
        list = filterAddList;
        notifyDataSetChanged();
    }
}

class MenuItemViewHolder extends RecyclerView.ViewHolder {

    CardView menu_list_container;
    TextView menu_list_item;
    ImageView pin_image;


    public MenuItemViewHolder(@NonNull View itemView) {
        super(itemView);
        menu_list_container = itemView.findViewById(R.id.menu_list_container);
        menu_list_item = itemView.findViewById(R.id.menu_list_item);
        pin_image = itemView.findViewById(R.id.pin_image);

    }
}

