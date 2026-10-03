package com.example.supplymanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.supplymanager.Adapter.MenuAdapter;
import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.Models.Notes;
import com.example.supplymanager.NotesTaker.NotesTakerActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class FoodMenuList {
    // Храним ссылку на активность и адаптер
    private final MainActivity activity;
    private final MenuAdapter menuAdapter;
    DrawerLayout main_menu;
    private RoomDB database;
    private List<Notes> notes = new ArrayList<>();
    ImageButton btn_back_header;
    RecyclerView recyclerHeader;
    Notes selectedNotes;

    // Конструктор принимает MainActivity
    public FoodMenuList(MainActivity activity) {
        this.activity = activity;
        this.database = RoomDB.getInstance(activity);
        this.notes = database.mainDAO().getAll();

        // Инициализируем адаптер с нужным контекстом (activity)
        this.menuAdapter = new MenuAdapter(this.notes, notesClickListener, activity);

        // САМОЕ ВАЖНОЕ: Находим NavigationView, а затем его шапку
        NavigationView navView = activity.findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);


        // 1. Находим элементы, но если их нет — просто пропускаем
        btn_back_header = headerView.findViewById(R.id.btn_back_header);
        main_menu = activity.findViewById(R.id.main_menu);

        // 2. Проверяем, существует ли кнопка
        if (btn_back_header != null) {
            btn_back_header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (main_menu != null) {
                        main_menu.closeDrawer(GravityCompat.START);
                    }
                }
            });
        }

//        // 3. Инициализируем RecyclerView (также через headerView)
        recyclerHeader = headerView.findViewById(R.id.recycler_header);
        recyclerHeader.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        recyclerHeader.setAdapter(this.menuAdapter);
    }
    public void refreshData() {
        // Обновляем данные в самом FoodMenuList
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        menuAdapter.notifyDataSetChanged();
    }

    // Метод для обновления списка (аналог updateRecycle)
    public void updateRecycle() {
        RecyclerView recyclerView = activity.findViewById(R.id.recycler_header);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(menuAdapter);
    }

    // Обработка результатов (старт из активности)
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("Запись");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                menuAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("Запись");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes(), new_notes.getText_title(), new_notes.getCount_title());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                menuAdapter.notifyDataSetChanged();
            }
        }
    }

    // Обработчик кликов (передает управление в NotesTakerActivity)
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(activity, NotesTakerActivity.class);
            intent.putExtra("Старая заметка", notes);
            // Используем activity.startActivityForResult(...)
            activity.startActivityForResult(intent, 102);

        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            // Здесь можно вызвать PopupMenu или что-то еще
            selectedNotes = notes;
            showPopupMenu(cardView, notes);
        }
    };

    // Менюшка закрепить, открепить, удалить
    void showPopupMenu(View view, Notes note) {
        selectedNotes = note;

        Context wrapper = new ContextThemeWrapper(activity, R.style.CustomPopupTheme);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        MenuItem pinItem = popupMenu.getMenu().findItem(R.id.pin);

        if (note.isPinned()) {
            pinItem.setTitle("📌 Открепить");
        } else {
            pinItem.setTitle("📌 Закрепить");
        }

        // Создаем обработчик для PopupMenu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.pin) {
                    if (selectedNotes.isPinned()) {
                        database.mainDAO().pin(selectedNotes.getID(), false);
                    } else {
                        database.mainDAO().pin(selectedNotes.getID(), true);
                    }

                    // Обновляем оба списка сразу (и главный, и в меню)
                    activity.refreshData();
                    refreshData();
                    return true;
                } else if (itemId == R.id.delete) {
                    database.mainDAO().delete(selectedNotes);

                    // Обновляем оба списка сразу (и главный, и в меню)
                    activity.refreshData();
                    refreshData();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}