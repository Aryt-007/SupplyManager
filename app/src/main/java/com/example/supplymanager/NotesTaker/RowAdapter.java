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

    /**
     * Это конструктор адаптера RowAdapter.
     * Он вызывается в тот самый момент, когда вы создаете этот адаптер (через new RowAdapter(...)).
     * Его главная задача — принять исходные данные и сохранить их внутри себя, чтобы
     * в дальнейшем отрисовать их в списке (RecyclerView).
     */
    public RowAdapter(List<NoteRow> items) {
        // Принимаем список элементов (NoteRow) извне и сохраняем его во внутреннюю
        // переменную класса (this.items).
        // Ключевое слово 'this' означает "переменная именно этого класса", что помогает
        // не запутаться, так как входящий параметр называется точно так же (items).
        // Теперь методы вроде getItemCount() или onBindViewHolder() смогут брать данные из этого списка.
        this.items = items;
    }

    /**
     * Этот метод используется для программного управления фокусом (курсором ввода) внутри списка.
     * Скорее всего, он применяется в самом редакторе заметок, когда вы динамически добавляете
     * новую строку (например, новый пункт списка покупок). Вызывая этот метод, вы заставляете
     * курсор автоматически "прыгнуть" в только что созданное поле ввода, чтобы пользователь
     * мог сразу начать печатать.
     */
    // Метод для установки фокуса на новую строку
    public void requestFocusOn(int position) {
        // 1. Сохраняем переданный номер строки в специальную переменную адаптера (focusPosition).
        // Чуть позже, когда система начнет рисовать эту строку (в методе onBindViewHolder),
        // она проверит эту переменную. Если текущая позиция совпадает с focusPosition,
        // код внутри onBindViewHolder вызовет у нужного EditText команду .requestFocus().
        this.focusPosition = position;
        // 2. Даем точечную команду RecyclerView: "Строка под номером 'position' изменилась!".
        // Система не будет трогать весь список, она лишь перерисует эту конкретную строку.
        // При перерисовке сработает логика установки фокуса, и курсор замигает в нужном поле.
        notifyItemChanged(position);
    }

    /**
     * Метод создает новую "пустую" визуальную строку (ячейку списка),
     * когда на экране появляется место для нового элемента.
     */
    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Берем файл дизайна одной строчки (row_item.xml) и "надуваем" его,
        // превращая в реальный объект View.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        // Отдаем эту пустую строку в держатель (ViewHolder), чтобы он нашел все поля ввода и кнопки
        return new RowViewHolder(view);
    }

    /**
     * Главный метод адаптера. Берет данные конкретной строки (NoteRow) и заполняет ими
     * визуальные элементы (текстовые поля), а также настраивает кнопки +, - и удаление.
     */
    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        // Получаем объект с данными для текущей строки
        NoteRow item = items.get(position);

        // Отключаем старые слушатели, чтобы не затереть данные при переиспользовании ячейки
        // ВАЖНЕЙШИЙ МОМЕНТ ДЛЯ СПИСКОВ С ПОЛЯМИ ВВОДА (EditText):
        // Так как RecyclerView переиспользует старые ячейки при скролле, на них могут висеть
        // старые слушатели текста (TextWatcher) от других строк. Если их не удалить,
        // ввод текста в одной строке начнет случайно перезаписывать данные в других строках.
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
        // Создаем новые слушатели. Как только пользователь напечатает хоть одну букву,
        // она тут же сохранится в объект NoteRow. Это гарантирует, что данные не потеряются при прокрутке.
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
        holder.btnUp.setOnClickListener(v -> updateCount(item, holder, 1)); // Кнопка "+" (увеличить количество на 1)
        holder.btnDown.setOnClickListener(v -> updateCount(item, holder, -1)); // Кнопка "-" (уменьшить количество на 1)

        // Кнопка удаления строки (корзина)
        holder.btnDelete.setOnClickListener(v -> {
            // Обязательно запрашиваем актуальную позицию прямо перед удалением,
            // так как после добавлений/удалений других строк переменная 'position' может устареть.
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos); // Удаляем элемент из массива данных
                notifyItemRemoved(currentPos); // Говорим списку анимированно убрать строку с экрана
                // Удобная фича: если пользователь удалил самую последнюю строку,
                // мы автоматически создаем новую пустую, чтобы экран не стал совсем пустым.
                // Если нужно добавить пустую строку, когда удалили последнюю:
                if (items.isEmpty()) {
                    items.add(new NoteRow("", ""));
                    notifyItemInserted(0);
                }
            }
        });
    }

    /**
     * Сообщает системе, сколько всего строк у нас сейчас в списке.
     */
    @Override
    public int getItemCount() { return items.size(); }

    private void updateCount(NoteRow item, RowViewHolder holder, int delta) {
        String text = item.getCount().trim();
        // Если поле было пустым, считаем, что там был 0
        if (text.isEmpty()) text = "0";
        try {
            // Проверяем, есть ли точка (например, пользователь ввел 1.5 кг)
            if (text.contains(".")) {
                // Работаем с дробными числами (Float)
                float result = Float.parseFloat(text) + delta;
                // Проверяем, не стало ли число целым после прибавления (например, 1.0)
                // Если стало, отбрасываем ноль после запятой, чтобы было просто "1"
                item.setCount(result == Math.floor(result) ? String.valueOf((int) result) : String.valueOf(result));
            } else {
                // Если точки нет, просто прибавляем как обычные целые числа
                item.setCount(String.valueOf(Integer.parseInt(text) + delta));
            }
            // Мгновенно обновляем текст на экране
            holder.countTitle.setText(item.getCount()); // Обновляем UI
        } catch (NumberFormatException e) {
            // Если пользователь ввел вместо цифр буквы (например "два"),
            // программа не вылетит с ошибкой, а просто сбросит значение в "0".
            item.setCount("0");
            holder.countTitle.setText("0");
        }
    }

    /**
     * Класс-держатель. Один раз находит все View-элементы внутри одной строки списка
     * и хранит ссылки на них, чтобы мы могли быстро менять текст и настраивать кнопки.
     */
    static class RowViewHolder extends RecyclerView.ViewHolder {
        EditText textTitle, countTitle; // Поля для ввода названия и количества
        ImageButton btnUp, btnDown, btnDelete; // Кнопки +, - и корзина
        // Важно: мы храним ссылки на TextWatcher-ы здесь, чтобы в методе onBindViewHolder
        // можно было отвязать старые слушатели перед привязкой новых.
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

    /**
     * Это очень удобный вспомогательный класс (обертка).
     * Стандартный TextWatcher заставляет нас прописывать 3 метода каждый раз,
     * хотя в 99% случаев нам нужен только один (когда текст уже изменился).
     * Этот класс прячет ненужные методы, оставляя нам только один чистый onTextChanged(String text).
     */
    // Вспомогательный класс, чтобы не писать 3 метода TextWatcher каждый раз
    abstract static class CustomTextWatcher implements TextWatcher {
        // Игнорируем метод "до изменения"
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        // Перехватываем стандартный метод и передаем уже готовую строку (String) в наш собственный метод
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChanged(s.toString()); }
        // Игнорируем метод "после изменения"
        @Override public void afterTextChanged(Editable s) {}
        // Тот самый единственный метод, который нам придется реализовывать при создании CustomTextWatcher
        public abstract void onTextChanged(String text);
    }
}