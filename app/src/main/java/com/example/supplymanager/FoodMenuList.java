package com.example.supplymanager;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

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
    ImageButton btn_back_header;
    DrawerLayout main_menu;
    private final MenuAdapter menuAdapter;
    private final MainActivity activity;
    private List<Notes> notes = new ArrayList<>();
    RecyclerView recyclerHeader;
    NavigationView navView;
    Notes selectedNotes;
    private RoomDB database;
    View headerView;

    /**
     * Это конструктор класса FoodMenuList. Он вызывается один раз при запуске приложения
     * и отвечает за полную настройку бокового меню (шторки) и загрузку в него данных.
     */
    // Конструктор принимает MainActivity
    public FoodMenuList(MainActivity activity) {
        this.activity = activity;
        // Подключаемся к базе данных и сразу выгружаем оттуда все существующие заметки
        this.database = RoomDB.getInstance(activity);
        this.notes = database.mainDAO().getFoodList();

        // Инициализируем адаптер с нужным контекстом (activity)
        this.menuAdapter = new MenuAdapter(this.notes, notesClickListener, activity);

        // САМОЕ ВАЖНОЕ: Находим NavigationView, а затем его шапку
        navView = activity.findViewById(R.id.nav_view);
        // Получаем доступ к "шапке" (Header) этого меню, так как все наши кнопки
        // и списки физически лежат внутри разметки header_layout
        headerView = navView.getHeaderView(0);

        // 1. Находим элементы, но если их нет — просто пропускаем
        main_menu = activity.findViewById(R.id.main_menu);

        // 2. Инициализируем RecyclerView (также через headerView)
        recyclerHeader = headerView.findViewById(R.id.recycler_header);
        // Настраиваем его так, чтобы элементы пролистывались ГОРИЗОНТАЛЬНО (влево-вправо)
        recyclerHeader.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        // Подключаем к списку наш адаптер, чтобы на экране появились карточки с данными
        recyclerHeader.setAdapter(this.menuAdapter);
    }
    /**
     * Этот метод предназначен для обновления списка заметок конкретно в боковом меню (шторке).
     * Он вызывается каждый раз, когда данные в приложении меняются (добавление, удаление, закрепление),
     * чтобы карточки в скрытом боковом меню всегда синхронизировались с главным экраном.
     */
    public void refreshData() {
        // Шаг 1: Обновляем данные в самом FoodMenuList
        notes.clear();
        // Шаг 2: Обращаемся к базе данных и забираем оттуда самую свежую версию всех заметок.
        // Сразу добавляем их в наш только что очищенный список.
        notes.addAll(database.mainDAO().getFoodList());
        // Шаг 3: Даем команду адаптеру боковой шторки (menuAdapter):
        // "Данные обновились, пожалуйста, перерисуй горизонтальный список карточек на экране"
        menuAdapter.notifyDataSetChanged();
    }

    /**
     * Этот метод отвечает за перенастройку списка (RecyclerView) внутри бокового меню.
     * Он используется, когда нужно заново привязать адаптер и задать правила отображения карточек.
     */
    // Метод для обновления списка (аналог updateRecycle)
    public void updateRecycle() {
        // Находим наш список по его ID через главную активность (MainActivity)
        RecyclerView recyclerView = activity.findViewById(R.id.recycler_header);
        // Сообщаем системе, что физический размер самого списка меняться не будет
        // (это небольшая оптимизация для повышения производительности)
        recyclerView.setHasFixedSize(true);
        // Настраиваем внешний вид списка: создаем сетку (StaggeredGrid)
        // ровно в 1 колонку с вертикальной прокруткой.
        // По сути, элементы будут выстроены в обычный столбик сверху вниз.
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        // Подключаем наш адаптер боковой шторки (menuAdapter) к этому списку,
        // чтобы загруженные заметки появились на экране
        recyclerView.setAdapter(menuAdapter);
        // Сразу подтягиваем свежие данные из базы
        refreshData();
    }

    /**
     * Этот блок кода создает слушатель нажатий (кликов) для карточек заметок.
     * Он определяет два разных сценария поведения: при обычном коротком клике
     * и при долгом удержании пальца на карточке.
     */
    // Обработчик кликов (передает управление в NotesTakerActivity)
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        // Метод срабатывает при обычном (коротком) нажатии на заметку
        @Override
        public void onClick(Notes notes) {
            // Создаем намерение (Intent) для перехода на экран редактора (NotesTakerActivity)
            Intent intent = new Intent(activity, NotesTakerActivity.class);
            // Передаем данные выбранной заметки на новый экран под ключом "Старая заметка".
            // Благодаря этому редактор откроется не пустым, а заполнит поля нужным текстом.
            intent.putExtra("Старая заметка", notes);
            // Запускаем экран редактора и ждем результат с кодом 102
            // (102 означает, что мы пошли редактировать уже существующую запись).
            activity.startActivityForResult(intent, 102);

        }

        // Метод срабатывает при долгом нажатии на карточку заметки
        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            // Здесь можно вызвать PopupMenu или что-то еще
            selectedNotes = notes;
            // Вызываем метод показа всплывающего меню (PopupMenu).
            // Передаем ему саму карточку (cardView), чтобы меню появилось прямо поверх нее,
            // и данные заметки (notes), чтобы меню знало, с чем работает.
            showPopupMenu(cardView, notes);
        }
    };

    /**
     * Этот метод отвечает за создание и показ небольшого всплывающего меню.
     * Оно появляется поверх карточки заметки при долгом нажатии и предлагает
     * дополнительные действия: закрепить/открепить или удалить заметку.
     */
    // Менюшка закрепить, открепить, удалить
    void showPopupMenu(View view, Notes note) {
        // Сохраняем текущую заметку, чтобы внутри обработчиков кликов знать, с чем работать
        selectedNotes = note;

        // Создаем специальную "обертку" для контекста (ContextThemeWrapper).
        // Это нужно для того, чтобы применить к стандартному меню ваш собственный
        // красивый дизайн (цвета, скругления), описанный в стиле CustomPopupTheme.
        Context wrapper = new ContextThemeWrapper(activity, R.style.CustomPopupTheme);
        // Создаем само всплывающее меню и привязываем его к view (к карточке, на которую нажали)
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        // "Надуваем" (inflate) меню: берем заготовку из файла R.menu.popup_menu
        // и превращаем её в реальные пункты на экране
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        // Находим пункт меню, отвечающий за закрепление, чтобы менять его текст
        MenuItem pinItem = popupMenu.getMenu().findItem(R.id.pin);

        // Умная логика текста: проверяем текущий статус заметки.
        if (note.isPinned()) {
            // Если заметка уже закреплена, предлагаем её открепить
            pinItem.setTitle("📌 Открепить");
        } else {
            // Если обычная — предлагаем закрепить
            pinItem.setTitle("📌 Закрепить");
        }

        // Создаем обработчик для PopupMenu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Узнаем, на какой конкретно пункт нажал пользователь
                int itemId = item.getItemId();
                if (itemId == R.id.pin) {
                    // Если нажали "Закрепить/Открепить", меняем статус в базе данных на противоположный
                    if (selectedNotes.isPinned()) {
                        database.mainDAO().pin(selectedNotes.getID(), false);
                    } else {
                        database.mainDAO().pin(selectedNotes.getID(), true);
                    }

                    // Обновляем списки заметок и на главном экране (activity.refreshData()),
                    // и в боковом меню (refreshData()), чтобы интерфейс везде обновился
                    activity.refreshData();
                    refreshData();
                    return true; // Говорим системе, что мы успешно обработали клик
                } else if (itemId == R.id.delete) {
                    // Если нажали "Удалить", безвозвратно стираем заметку из базы данных
                    database.mainDAO().delete(selectedNotes);

                    // Обновляем оба списка сразу (и главный, и в меню)
                    activity.refreshData();
                    refreshData();
                    return true;
                }
                return false; // Если нажали что-то неизвестное, возвращаем false
            }
        });
        // После того как меню настроено и кнопки запрограммированы, показываем его на экране!
        popupMenu.show();
    }
}