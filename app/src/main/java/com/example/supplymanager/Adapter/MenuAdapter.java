package com.example.supplymanager.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
    /**
     * Этот метод — обязательная часть адаптера для списка (RecyclerView.Adapter).
     * Он сообщает системе, сколько всего элементов (заметок) находится в вашем списке прямо сейчас.
     */
    @Override
    public int getItemCount() {
        // Берем наш список данных (list) и возвращаем его текущий размер (количество элементов).
        // Опираясь на это точное число, RecyclerView понимает, сколько всего карточек
        // ему нужно подготовить и отрисовать на экране, и когда нужно остановить прокрутку.
        return list.size();
    }

    /**
     * Этот метод — сердце адаптера. Он вызывается каждый раз, когда карточка
     * собирается появиться на экране. Его задача — взять "пустую" заготовку (holder)
     * из onCreateViewHolder и заполнить её реальными данными (текстом, цветом, картинками) конкретной заметки.
     */
    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        // Берем из списка (list) заметку под текущим номером (position)
        // и вставляем её заголовок в текстовое поле карточки
        holder.menu_list_item.setText(list.get(position).getTitle());
        // Эта хитрость (setSelected) часто используется для того, чтобы длинный текст,
        // который не помещается в одну строчку, начал плавно прокручиваться (эффект "бегущей строки")
        holder.menu_list_item .setSelected(true);

        // Проверяем, закреплена ли эта конкретная заметка
        if (list.get(position).isPinned()) {
            // Если да — показываем на карточке иконку булавки (из папки drawable)
            holder.pin_image.setImageResource(R.drawable.pinned);
        } else {
            // Если нет — передаем 0, что очищает ImageView (убирает картинку, чтобы её не было видно)
            holder.pin_image.setImageResource(0);
        }

        // Применяем этот цвет к фону самой карточки (CardView)
        holder.menu_list_container.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.white, null));

        // Настраиваем слушатель обычного (короткого) нажатия на всю карточку целиком
        holder.menu_list_container.setOnClickListener(new View.OnClickListener() {
            // Мы используем holder.getAdapterPosition() вместо переменной position.
            // Это спасает от ошибок рассинхронизации, когда элементы удалялись или добавлялись "на лету".
            // Передаем правильную заметку в интерфейс listener (он передаст команду в MainActivity)
            @Override
            public void onClick(View v) {
                listener.onClick(list.get(holder.getAdapterPosition()));
            }
        });

        // Настраиваем слушатель долгого удержания пальца на карточке
        holder.menu_list_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Передаем в слушатель саму заметку и физическую карточку (container),
                // чтобы MainActivity знала, поверх какого элемента рисовать всплывающее меню (PopupMenu)
                listener.onLongClick(list.get(holder.getAdapterPosition()), holder.menu_list_container);
                // Возвращаем false, хотя обычно в onLongClick возвращают true,
                // чтобы сказать системе: "Я обработал долгое нажатие, не нужно дополнительно вызывать обычный клик".
                // (Если при долгом нажатии у вас случайно открывается заметка, поменяйте здесь false на true)
                return false;
            }
        });
    }

    /**
     * Этот метод — обязательная часть адаптера (Adapter) для списка (RecyclerView).
     * Он отвечает за создание новых "пустых" карточек для заметок (View),
     * когда список только появляется на экране или когда вы скроллите и нужны новые элементы.
     */
    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1. LayoutInflater.from(context) берет специальный системный инструмент (условно "надуватель"),
        // который умеет превращать невидимый XML-код дизайна в реальные видимые объекты на экране.

        // 2. .inflate(R.layout.menu_list, parent, false) берет ваш файл дизайна карточки (menu_list.xml)
        // и "надувает" его.
        // 'parent' — это сам контейнер списка, чтобы карточка знала свои рамки.
        // 'false' означает: "создай карточку, но пока не прикрепляй её к списку намертво,
        // RecyclerView сам решит, когда её показать".

        // 3. new MenuItemViewHolder(...) берет получившуюся карточку-пустышку, заворачивает её
        // в класс-холдер (держатель) и отдает системе. Чуть позже система заполнит эту
        // пустую карточку реальным текстом в другом методе (onBindViewHolder).
        return new MenuItemViewHolder(LayoutInflater.from(context).inflate(R.layout.menu_list, parent, false));
    }
}

/**
 * Этот класс — "держатель" (ViewHolder) для элементов списка.
 * Его главная задача — один раз найти все элементы дизайна (тексты, картинки, контейнеры)
 * внутри одной карточки и сохранить их в памяти. Это нужно для высокой производительности,
 * чтобы Android не искал их заново (через долгий findViewById) каждый раз при прокрутке списка.
 */
class MenuItemViewHolder extends RecyclerView.ViewHolder {

    CardView menu_list_container;
    TextView menu_list_item;
    ImageView pin_image;

    // Конструктор: вызывается в тот момент, когда метод onCreateViewHolder
    // только-только создал "пустую" карточку (itemView).
    public MenuItemViewHolder(@NonNull View itemView) {
        // Обязательный вызов родительского конструктора, передаем ему саму карточку
        super(itemView);
        // Находим нужные элементы по их ID внутри созданной карточки и привязываем их к переменным.
        // Теперь в методе onBindViewHolder мы можем мгновенно обращаться к этим элементам,
        // например: holder.menu_list_item.setText("Моя заметка").
        menu_list_container = itemView.findViewById(R.id.menu_list_container);
        menu_list_item = itemView.findViewById(R.id.menu_list_item);
        pin_image = itemView.findViewById(R.id.pin_image);

    }
}

