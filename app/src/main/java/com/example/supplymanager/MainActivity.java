package com.example.supplymanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.supplymanager.Adapter.NotesListAdapter;
import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.Models.Notes;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    RecyclerView recyclerView;
    ImageButton fab_add;
    NotesListAdapter notesListAdapter;
    RoomDB database;
    List <Notes> notes = new ArrayList<>();
    SearchView searchView_home;
    Notes selectedNotes;
    ImageButton food_list, settings;
    DrawerLayout main_menu;
    FoodMenuList foodMenuList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.relative_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);
        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();

        food_list = findViewById(R.id.food_list);
        settings = findViewById(R.id.settings);

        main_menu = findViewById(R.id.main_menu);

        foodMenuList = new FoodMenuList(this);

        // Кнопка настроек
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_settings = new Intent(MainActivity.this, Settings.class);
                startActivity(intent_settings);
            }
        });

        // Кнопка вывода списка с тремя полосками
        food_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main_menu.openDrawer(GravityCompat.START);
                // Обновляем список через наш новый класс
                foodMenuList.updateRecycle();
            }
        });

        // Кнопка добавления новой заметки
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                startActivityForResult(intent, 101);
            }
        });

        // Поиск
        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });

        // Обновление списка
        updateRecycle(notes);
    }

    // Поиск
    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) // Переводит в поиске текст в маленькие буквы
            || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("Запись");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                foodMenuList.refreshData();
            }
        } else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_notes = (Notes) data.getSerializableExtra("Запись");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                foodMenuList.refreshData();
            }
        }

    }

    // Обновление заметок
    private void updateRecycle(List<Notes> notes) { // В onCreate
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(notes, notesClickListener, MainActivity.this);
        recyclerView.setAdapter(notesListAdapter);
    }
    public void refreshData() { // В остальных местах
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        notesListAdapter.notifyDataSetChanged();
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("Старая заметка", notes);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNotes = notes;
            showPopupMenu(cardView, notes);
        }
    };

    // Закрепление
    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
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
            refreshData();
            foodMenuList.refreshData();

            return true;
        } else if (itemId == R.id.delete) {
            database.mainDAO().delete(selectedNotes);

            // Обновляем оба списка сразу (и главный, и в меню)
            refreshData();
            foodMenuList.refreshData();

            return true;
        } else {
            return false;
        }
    }

    // Менюшка закрепить, открепить, удалить
    void showPopupMenu(View view, Notes note) {
        selectedNotes = note;

        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupTheme);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        MenuItem pinItem = popupMenu.getMenu().findItem(R.id.pin);

        if (note.isPinned()) {
            pinItem.setTitle("📌 Открепить");
        } else {
            pinItem.setTitle("📌 Закрепить");
        }

        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }
}