package com.example.supplymanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import com.example.supplymanager.Adapter.NotesListAdapterMain;
import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.Models.Notes;
import com.example.supplymanager.NotesTaker.NotesTakerActivity;
import com.example.supplymanager.Settings.MainSettings;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    RecyclerView recyclerView;
    ImageButton fab_add;
    NotesListAdapterMain notesListAdapterMain;
    RoomDB database;
    List <Notes> notes = new ArrayList<>();
    SearchView searchView_home;
    Notes selectedNotes;
    ImageButton food_list, settings, cancel_button, pin_button, delete_button;
    DrawerLayout main_menu;
    FoodMenuList foodMenuList;
    RelativeLayout selection_toolbar, default_toolbar;
    boolean isSelectionMode = false;
    List<Notes> selectedNotesList = new ArrayList<>();



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

        selection_toolbar = findViewById(R.id.selection_toolbar);
        default_toolbar = findViewById(R.id.default_toolbar);

        food_list = findViewById(R.id.food_list);
        settings = findViewById(R.id.settings);
        cancel_button = findViewById(R.id.cancel_button);
        pin_button = findViewById(R.id.pin_button);
        delete_button = findViewById(R.id.delete_button);

        foodMenuList = new FoodMenuList(this);
        main_menu = findViewById(R.id.main_menu);

        // Кнопка настроек
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_settings = new Intent(MainActivity.this, MainSettings.class);
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
                // Убираем фокус с поиска перед открытием заметки
                searchView_home.clearFocus();

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

        // Обработка отмены выделения
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelectionMode();
            }
        });

        pin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedNotesList.isEmpty()) {
                    // Проходимся по всем выделенным заметкам
                    for (Notes note : selectedNotesList) {
                        boolean isPinned = note.isPinned();
                        // Инвертируем состояние: если была закреплена - открепляем, и наоборот
                        database.mainDAO().pin(note.getID(), !isPinned);
                    }
                    stopSelectionMode();
                    refreshData();
                    foodMenuList.refreshData();
                }
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedNotesList.isEmpty()) {
                    // Проходимся по всем выделенным заметкам и удаляем
                    for (Notes note : selectedNotesList) {
                        database.mainDAO().delete(note);
                    }
                    stopSelectionMode();
                    refreshData();
                    foodMenuList.refreshData();
                }
            }
        });

        // Слушатель скролла для скрытия/показа fab_add
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Если мы в режиме выделения - кнопка и так скрыта, ничего не делаем
                if (isSelectionMode) return;

                if (dy > 0 && fab_add.getVisibility() == View.VISIBLE) {
                    // Пользователь скроллит вниз - прячем кнопку
                    fab_add.setVisibility(View.GONE);
                } else if (dy < 0 && fab_add.getVisibility() != View.VISIBLE) {
                    // Пользователь скроллит вверх - показываем кнопку
                    fab_add.setVisibility(View.VISIBLE);
                }
            }
        });

        // Обновление списка
        updateRecycle(notes);
    }

    private void startSelectionMode(Notes firstNote) {
        isSelectionMode = true;
        selectedNotesList.clear();
        selectedNotesList.add(firstNote);
        notesListAdapterMain.setSelectionMode(true, selectedNotesList, recyclerView);

        // Прячем обычный тулбар и кнопку добавления, показываем тулбар выделения
        default_toolbar.setVisibility(View.GONE);
        selection_toolbar.setVisibility(View.VISIBLE);
        fab_add.setVisibility(View.GONE);
    }

    private void stopSelectionMode() {
        isSelectionMode = false;
        selectedNotesList.clear();
        notesListAdapterMain.setSelectionMode(false, selectedNotesList, recyclerView);

        // Возвращаем всё обратно
        selection_toolbar.setVisibility(View.GONE);
        default_toolbar.setVisibility(View.VISIBLE);
        fab_add.setVisibility(View.VISIBLE);
    }

    // Поиск
    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) // Переводит в поиске текст в маленькие буквы
                    || singleNote.getText_title().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getCount_title().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        notesListAdapterMain.filterList(filteredList);
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapterMain.notifyDataSetChanged();
                foodMenuList.refreshData();
            }
        } else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapterMain.notifyDataSetChanged();
                foodMenuList.refreshData();
            }
        }
    }

    // Обновление заметок
    private void updateRecycle(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        // 1. ЗАПРЕЩАЕМ СЕТКЕ ПЕРЕСЧИТЫВАТЬ ПОЗИЦИИ И ПРЫГАТЬ
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);

        notesListAdapterMain = new NotesListAdapterMain(notes, notesClickListener, MainActivity.this);

        // 2. Включаем поддержку стабильных ID, которые мы прописали в адаптере
        notesListAdapterMain.setHasStableIds(true);

        recyclerView.setAdapter(notesListAdapterMain);

        // 3. Отключаем анимацию (чтобы галочка появлялась мгновенно)
        recyclerView.setItemAnimator(null);
    }
    public void refreshData() { // В остальных местах
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        notesListAdapterMain.notifyDataSetChanged();
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            if (isSelectionMode) {
                // Если мы в режиме выделения, то клик ставит/снимает галочку
                toggleSelection();
            } else {
                // Убираем фокус с поиска перед открытием заметки
                searchView_home.clearFocus();

                // Обычный режим - открываем заметку
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                intent.putExtra("Старая заметка", notes);
                startActivityForResult(intent, 102);
            }
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            if (!isSelectionMode) {
                // Включаем режим выделения и сразу выделяем ту карточку, на которую долго нажали
                startSelectionMode(notes);
            }
        }
    };

    private void toggleSelection() {
        if (selectedNotesList.isEmpty()) {
            stopSelectionMode();
        }
    }

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
}