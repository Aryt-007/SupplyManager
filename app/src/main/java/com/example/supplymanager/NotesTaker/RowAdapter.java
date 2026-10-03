package com.example.supplymanager.NotesTaker;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supplymanager.R;

import java.util.List;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowViewHolder> {

    private final List<NoteRow> items;
    private int focusPosition = -1; // Позиция, на которую нужно поставить фокус

    public RowAdapter(List<NoteRow> items) {
        this.items = items;
    }

    // Метод для установки фокуса на новую строку
    public void requestFocusOn(int position) {
        this.focusPosition = position;
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new RowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        NoteRow item = items.get(position);

        // Отключаем старые слушатели, чтобы не затереть данные при переиспользовании ячейки
        if (holder.titleWatcher != null) holder.textTitle.removeTextChangedListener(holder.titleWatcher);
        if (holder.countWatcher != null) holder.countTitle.removeTextChangedListener(holder.countWatcher);

        // Устанавливаем данные
        holder.textTitle.setText(item.getTitle());
        holder.countTitle.setText(item.getCount());

        // Устанавливаем фокус, если это новая строка
        if (position == focusPosition) {
            holder.textTitle.requestFocus();
            focusPosition = -1; // Сбрасываем после установки
        }

        // Сохраняем введенный текст обратно в объект
        holder.titleWatcher = new CustomTextWatcher() {
            @Override
            public void onTextChanged(String text) { item.setTitle(text); }
        };
        holder.textTitle.addTextChangedListener(holder.titleWatcher);

        holder.countWatcher = new CustomTextWatcher() {
            @Override
            public void onTextChanged(String text) { item.setCount(text); }
        };
        holder.countTitle.addTextChangedListener(holder.countWatcher);

        // Логика кнопок
        holder.btnUp.setOnClickListener(v -> updateCount(item, holder, 1));
        holder.btnDown.setOnClickListener(v -> updateCount(item, holder, -1));

        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                // Если нужно добавить пустую строку, когда удалили последнюю:
                if (items.isEmpty()) {
                    items.add(new NoteRow("", ""));
                    notifyItemInserted(0);
                }
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    private void updateCount(NoteRow item, RowViewHolder holder, int delta) {
        String text = item.getCount().trim();
        if (text.isEmpty()) text = "0";
        try {
            if (text.contains(".")) {
                float result = Float.parseFloat(text) + delta;
                item.setCount(result == Math.floor(result) ? String.valueOf((int) result) : String.valueOf(result));
            } else {
                item.setCount(String.valueOf(Integer.parseInt(text) + delta));
            }
            holder.countTitle.setText(item.getCount()); // Обновляем UI
        } catch (NumberFormatException e) {
            item.setCount("0");
            holder.countTitle.setText("0");
        }
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        EditText textTitle, countTitle;
        ImageButton btnUp, btnDown, btnDelete;
        TextWatcher titleWatcher, countWatcher;

        public RowViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            countTitle = itemView.findViewById(R.id.count_title);
            btnUp = itemView.findViewById(R.id.btn_up);
            btnDown = itemView.findViewById(R.id.btn_down);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    // Вспомогательный класс, чтобы не писать 3 метода TextWatcher каждый раз
    abstract static class CustomTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChanged(s.toString()); }
        @Override public void afterTextChanged(Editable s) {}
        public abstract void onTextChanged(String text);
    }
}