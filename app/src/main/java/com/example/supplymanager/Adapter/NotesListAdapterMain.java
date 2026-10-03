package com.example.supplymanager.Adapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class NotesListAdapterMain extends RecyclerView.Adapter <NotesViewHolderMain>{

    Context context;
    List <Notes> list;
    NotesClickListener listener;
    private boolean isSelectionMode = false;
    private List<Notes> selectedNotes = new ArrayList<>();

    public NotesListAdapterMain(List<Notes> list, NotesClickListener listener, Context context) {
        this.list = list;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public NotesViewHolderMain onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolderMain(LayoutInflater.from(context).inflate(R.layout.main_notes_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolderMain holder, int position) {
        Notes notes = list.get(position);
        holder.textView_title.setText(list.get(position).getTitle());
        holder.textView_title.setSelected(true);

        displayTableData(holder.textView_notes, list.get(position));

        holder.textView_date.setText(list.get(position).getDate());
        holder.textView_date.setSelected(true);

        if (list.get(position).isPinned()) {
            holder.imageView_pin.setImageResource(R.drawable.pinned);
        } else {
            holder.imageView_pin.setImageResource(0);
        }

        // Убираем лишнее пространство - устанавливаем высоту по содержимому
        holder.textView_notes.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textView_notes.getLayoutParams();
        if (params != null) {
            params.height = holder.textView_notes.getMeasuredHeight();
            holder.textView_notes.setLayoutParams(params);
        }

        // Смена цвета заднего фона в заметках
        int color_code = getRandomColor();
        holder.notes_container.setCardBackgroundColor(holder.itemView.getResources().getColor(color_code, null));

        holder.notes_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // МГНОВЕННО меняем картинку прямо во View, без перерисовки адаптера!
                    if (selectedNotes.contains(notes)) {
                        selectedNotes.remove(notes); // Удаляем из списка
                        holder.check_mark_on.setVisibility(View.GONE);
                        holder.check_mark_off.setVisibility(View.VISIBLE);
                    } else {
                        selectedNotes.add(notes); // Добавляем в список
                        holder.check_mark_on.setVisibility(View.VISIBLE);
                        holder.check_mark_off.setVisibility(View.GONE);
                    }
                }
                // Передаем сигнал в MainActivity (вдруг список стал пустым и надо выйти)
                listener.onClick(notes);
            }
        });

        holder.notes_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongClick(list.get(holder.getAdapterPosition()), holder.notes_container);
                return true;
            }
        });

        // Логика отображения иконок в зависимости от режима
        if (isSelectionMode) {
            // Прячем иконку закрепления в режиме выделения
            holder.imageView_pin.setVisibility(View.GONE);

            // Если заметка есть в списке выделенных — ставим зеленую галочку (on)
            if (selectedNotes.contains(notes)) {
                holder.check_mark_on.setVisibility(View.VISIBLE);
                holder.check_mark_off.setVisibility(View.GONE);
            } else {
                // Если нет — ставим пустой кружок (off)
                holder.check_mark_on.setVisibility(View.GONE);
                holder.check_mark_off.setVisibility(View.VISIBLE);
            }
        } else {
            // Обычный режим
            holder.check_mark_on.setVisibility(View.GONE);
            holder.check_mark_off.setVisibility(View.GONE);

            // Показываем пин только если заметка закреплена
            if (notes.isPinned()) {
                holder.imageView_pin.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_pin.setVisibility(View.GONE);
            }
        }

        if (isSelectionMode) {
            holder.imageView_pin.setVisibility(View.GONE);

            if (selectedNotes.contains(notes)) {
                holder.check_mark_on.setVisibility(View.VISIBLE);
                holder.check_mark_off.setVisibility(View.GONE);
            } else {
                holder.check_mark_on.setVisibility(View.GONE);
                holder.check_mark_off.setVisibility(View.VISIBLE);
            }
        } else {
            // Если выходим из режима выделения
            holder.check_mark_on.setVisibility(View.GONE);
            holder.check_mark_off.setVisibility(View.GONE);
            if (notes.isPinned()) {
                holder.imageView_pin.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_pin.setVisibility(View.GONE);
            }
        }
    }

    // Метод для отображения данных таблицы
    private void displayTableData(LinearLayout container, Notes note) {
        // Очищаем контейнер перед добавлением
        container.removeAllViews();

        String textTitles = note.getText_title();
        String countTitles = note.getCount_title();

        // Если есть данные таблицы
        if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
            String[] textTitleArray = textTitles.split("\\|");
            String[] countTitleArray = countTitles.split("\\|");

            int maxLength = Math.min(textTitleArray.length, countTitleArray.length);

            // Ограничиваем до 3 строк
            int maxDisplayRows = Math.min(maxLength, 3);

            // Добавляем заголовок таблицы
            LinearLayout headerRow = new LinearLayout(context);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            headerRow.setLayoutParams(headerParams);

            TextView headerName = new TextView(context);
            headerName.setText("Название");
            headerName.setTextColor(Color.BLACK);
            headerName.setTextSize(14);
            headerName.setTypeface(null, android.graphics.Typeface.BOLD);
            headerName.setGravity(Gravity.START);
            headerName.setPadding(8, 4, 8, 4);
            headerName.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            TextView headerCount = new TextView(context);
            headerCount.setText("Кол-во");
            headerCount.setTextColor(Color.BLACK);
            headerCount.setTextSize(14);
            headerCount.setTypeface(null, android.graphics.Typeface.BOLD);
            headerCount.setGravity(Gravity.CENTER);
            headerCount.setPadding(8, 4, 8, 4);
            headerCount.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
            ));

            headerRow.addView(headerName);
            headerRow.addView(headerCount);
            container.addView(headerRow);

            // Добавляем разделитель
            View divider = new View(context);
            divider.setBackgroundColor(Color.LTGRAY);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            );
            divider.setLayoutParams(dividerParams);
            container.addView(divider);

            // Добавляем строки данных (максимум 3)
            for (int i = 0; i < maxDisplayRows; i++) {
                LinearLayout dataRow = new LinearLayout(context);
                dataRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                dataRow.setLayoutParams(rowParams);

                TextView nameTextView = new TextView(context);
                nameTextView.setText(textTitleArray[i]);
                nameTextView.setTextColor(Color.BLACK);
                nameTextView.setTextSize(16);
                nameTextView.setGravity(Gravity.START);
                nameTextView.setPadding(8, 4, 8, 4);
                nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                ));

                TextView countTextView = new TextView(context);
                countTextView.setText(countTitleArray[i]);
                countTextView.setTextColor(Color.BLACK);
                countTextView.setTextSize(16);
                countTextView.setGravity(Gravity.CENTER);
                countTextView.setPadding(8, 4, 8, 4);
                countTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.5f
                ));

                dataRow.addView(nameTextView);
                dataRow.addView(countTextView);
                container.addView(dataRow);
            }

            // Если строк больше 3, показываем индикатор
            if (maxLength > 3) {
                TextView moreIndicator = new TextView(context);
                moreIndicator.setText("Eщe " + (maxLength - 3));
                moreIndicator.setTextColor(Color.GRAY);
                moreIndicator.setTextSize(12);
                moreIndicator.setGravity(Gravity.CENTER);
                moreIndicator.setPadding(8, 2, 8, 2);
                moreIndicator.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                container.addView(moreIndicator);
            }

            // В конце метода - принудительно устанавливаем высоту
            container.post(new Runnable() {
                @Override
                public void run() {
                    // Измеряем содержимое
                    container.measure(
                            View.MeasureSpec.makeMeasureSpec(container.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    );

                    // Устанавливаем точную высоту
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
                    if (params != null) {
                        params.height = container.getMeasuredHeight();
                        container.setLayoutParams(params);
                    }
                }
            });
        }
    }

    public void updateSingleNoteSelection(List<Notes> selectedNotes, Notes toggledNote) {
        this.selectedNotes = selectedNotes;
        int position = list.indexOf(toggledNote);

        if (position != -1) {
            notifyItemChanged(position);
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        // У каждой заметки в базе данных есть свой уникальный ID.
        // Мы говорим адаптеру использовать его, чтобы он не путал карточки.
        return list.get(position).getID();
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

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectionMode(boolean isSelectionMode, List<Notes> selectedNotes, RecyclerView recyclerView) {
        this.isSelectionMode = isSelectionMode;
        this.selectedNotes = selectedNotes;

        // ВАЖНО: Мы удалили notifyDataSetChanged()!
        // Вместо этого мы вручную обновляем только те карточки, которые сейчас на экране.

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);

            // Замените NotesViewHolder на ваше название (например, NotesViewHolderMain), если оно отличается
            NotesViewHolderMain holder = (NotesViewHolderMain) recyclerView.getChildViewHolder(child);

            if (holder != null) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Notes note = list.get(position);

                    if (isSelectionMode) {
                        holder.imageView_pin.setVisibility(View.GONE);
                        if (selectedNotes.contains(note)) {
                            holder.check_mark_on.setVisibility(View.VISIBLE);
                            holder.check_mark_off.setVisibility(View.GONE);
                        } else {
                            holder.check_mark_on.setVisibility(View.GONE);
                            holder.check_mark_off.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Если выходим из режима
                        holder.check_mark_on.setVisibility(View.GONE);
                        holder.check_mark_off.setVisibility(View.GONE);
                        if (note.isPinned()) {
                            holder.imageView_pin.setVisibility(View.VISIBLE);
                        } else {
                            holder.imageView_pin.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }
}

class NotesViewHolderMain extends RecyclerView.ViewHolder {

    // Ваши текущие переменные (возможно, они у вас уже есть)
    CardView notes_container;
    TextView textView_title, textView_date;
    LinearLayout textView_notes;
    ImageView imageView_pin, check_mark_on, check_mark_off;

    public NotesViewHolderMain(@NonNull View itemView) {
        super(itemView);

        notes_container = itemView.findViewById(R.id.menu_list_container); // Или как он у вас называется
        textView_title = itemView.findViewById(R.id.textView_title);
        textView_notes = itemView.findViewById(R.id.textView_notes);
        textView_date = itemView.findViewById(R.id.textView_date);
        imageView_pin = itemView.findViewById(R.id.imageView_pin);

        check_mark_on = itemView.findViewById(R.id.check_mark_on);
        check_mark_off = itemView.findViewById(R.id.check_mark_off);
    }
}
