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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    boolean isSelectionMode = false;
    DrawerLayout main_menu;
    FoodMenuList foodMenuList;
    ImageButton food_list, settings, cancel_button, pin_button, delete_button, fab_add, all_highlight;
    public List <Notes> notes = new ArrayList<>();
    List<Notes> selectedNotesList = new ArrayList<>();
    Notes selectedNotes;
    NotesListAdapterMain notesListAdapterMain;
    RecyclerView recyclerView;
    RelativeLayout selection_toolbar, default_toolbar;
    RoomDB database;
    SearchView searchView_home;

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

        // Панель для вывода списка, поиск, настроек
        default_toolbar = findViewById(R.id.default_toolbar);
        // Список
        food_list = findViewById(R.id.food_list);
        // Поиск
        searchView_home = findViewById(R.id.searchView_home);
        // Настройки
        settings = findViewById(R.id.settings);

        // Скрытая панель для выхода из меню с выделением, закрепления заметки, удаление заметки
        selection_toolbar = findViewById(R.id.selection_toolbar);
        // Выход из меню
        cancel_button = findViewById(R.id.cancel_button);
        // Выделить все
        all_highlight = findViewById(R.id.all_highlight);
        // Закрепление заметки
        pin_button = findViewById(R.id.pin_button);
        // Удаление заметки
        delete_button = findViewById(R.id.delete_button);

        // Объект с выводом списка
        recyclerView = findViewById(R.id.recycler_home);
        // Java класс для вывода списка меню
        foodMenuList = new FoodMenuList(this);
        // id для DrawerLayout в activity_main.xml
        main_menu = findViewById(R.id.main_menu);

        // Кнопка добавления заметки
        fab_add = findViewById(R.id.fab_add);

        // База данных
        database = RoomDB.getInstance(this);
        // Записи из базы данных
        notes = database.mainDAO().getAll();

        // Кнопка вывода списка с тремя полосками
        all_highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Убеждаемся, что режим выделения еще не включен, либо принудительно переводим в него
                isSelectionMode = true;

                // 2. Очищаем текущий список выбранных, чтобы избежать дубликатов
                selectedNotesList.clear();

                // 3. Добавляем абсолютно все заметки из основного списка (notes) в список выбранных
                selectedNotesList.addAll(notes);

                // 4. Передаем данные адаптеру, чтобы он нарисовал галочки на всех карточках
                notesListAdapterMain.setSelectionMode(true, selectedNotesList, recyclerView);

                // 5. Прячем обычный тулбар и кнопку "плюс", показываем панель управления выделением
                default_toolbar.setVisibility(View.GONE);
                selection_toolbar.setVisibility(View.VISIBLE);
                fab_add.setVisibility(View.GONE);
            }
        });

        // Кнопка вывода списка с тремя полосками
        food_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Дает команду компоненту DrawerLayout выдвинуть боковое меню (шторку) с левого края экрана
                main_menu.openDrawer(GravityCompat.START);
                // Обновляем список через наш новый класс
                foodMenuList.updateRecycle();
            }
        });

        // Поиск
        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Метод срабатывает, когда пользователь нажимает кнопку "Найти" (Enter) на клавиатуре
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Возвращаем false, так как нам не нужно специальное действие при нажатии Enter.
                return false;
            }

            // Метод срабатывает мгновенно при ЛЮБОМ изменении текста (ввод символа, удаление)
            @Override
            public boolean onQueryTextChange(String newText) {
               // Этот метод отвечает за работу поиска (фильтрацию списка).
                // Он принимает текст, который ввел пользователь (newText), и ищет его во всех заметках.
                // Передаем новый введенный текст в ваш метод filter(),
                // который обновляет RecyclerView и оставляет только подходящие заметки
                // Создаем новый временный список, куда будем складывать только те заметки, которые подошли под запрос
                List<Notes> filteredList = new ArrayList<>();
                // Запускаем цикл: перебираем по очереди все заметки из нашего основного полного списка (notes)
                for (Notes singleNote : notes) {
                    // Проверяем, содержится ли введенный текст (newText) внутри текущей заметки.
                    // .toLowerCase() переводит и текст заметки, и запрос в строчные (маленькие) буквы,
                    // чтобы поиск не зависел от регистра (например, чтобы "Яблоко" и "яблоко" находились одинаково).
                    if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) // Переводит в поиске текст в маленькие буквы
                            || singleNote.getText_title().toLowerCase().contains(newText.toLowerCase()) // ИЛИ ищем в текстовых строках (названиях товаров)
                            || singleNote.getCount_title().toLowerCase().contains(newText.toLowerCase())) { // ИЛИ ищем в колонке с числами/количеством
                        // Если текст найден хотя бы в одном из этих трех полей, добавляем заметку в отфильтрованный список
                        filteredList.add(singleNote);
                    }
                }
                notesListAdapterMain.filterList(filteredList);
                // Возвращаем false, показывая, что стандартная обработка события системой не требуется
                return false;
            }
        });

        // Кнопка настроек
        settings.setOnClickListener(new View.OnClickListener() {
            // Метод срабатывает в момент клика по кнопке
            @Override
            public void onClick(View v) {
                // Создаем намерение (Intent), которое указывает системе:
                // "Откуда идем" (MainActivity.this) и "Куда идем" (MainSettings.class)
                Intent intent_settings = new Intent(MainActivity.this, MainSettings.class);
                // Даем команду Android запустить новую активность (открыть экран настроек)
                settingsLauncher.launch(intent_settings);
            }
        });

        // Обработка отмены выделения
        cancel_button.setOnClickListener(new View.OnClickListener() {
            // Метод срабатывает, когда пользователь нажимает на кнопку (например, крестик или стрелку назад)
            @Override
            public void onClick(View v) {
                // Вызывает ваш метод stopSelectionMode(), который очищает список выбранных заметок,
                // убирает галочки с карточек и возвращает на экран обычный тулбар
                stopSelectionMode();
            }
        });

        // Закрепление выделенных заметок
        pin_button.setOnClickListener(new View.OnClickListener() {
            // Срабатывает при нажатии на иконку булавки в режиме выделения
            @Override
            public void onClick(View v) {
                // Убеждаемся, что список выделенных заметок не пуст (защита от ошибок)
                if (!selectedNotesList.isEmpty()) {
                    // Проходимся по всем выделенным заметкам
                    for (Notes note : selectedNotesList) {
                        // Узнаем текущий статус заметки (закреплена она сейчас или нет)
                        boolean isPinned = note.isPinned();
                        // Инвертируем состояние: если была закреплена - открепляем, и наоборот
                        database.mainDAO().pin(note.getID(), !isPinned);
                    }
                    // Выключаем режим выделения (прячем галочки, возвращаем обычный тулбар)
                    stopSelectionMode();
                    // Запрашиваем свежие данные из БД для главного экрана, чтобы обновить иконки булавок
                    refreshData();
                    // Запрашиваем свежие данные для бокового меню, чтобы списки везде были синхронизированы
                     foodMenuList.refreshData();
                }
            }
        });

        // Удаление выделенных заметок
        delete_button.setOnClickListener(new View.OnClickListener() {
            // Метод срабатывает при клике на иконку корзины в режиме выделения
            @Override
            public void onClick(View v) {
                // Убеждаемся, что список выделенных элементов не пуст, прежде чем начинать удаление
                if (!selectedNotesList.isEmpty()) {
                    // Проходимся по всем выделенным заметкам и удаляем
                    for (Notes note : selectedNotesList) {
                        // Обращаемся к базе данных и удаляем эту конкретную заметку
                        database.mainDAO().delete(note);
                    }
                    // Выходим из режима выделения (убираем галочки, возвращаем обычную верхнюю панель)
                    stopSelectionMode();
                    // Запрашиваем свежие данные из базы данных, чтобы удаленные заметки исчезли с главного экрана
                    refreshData();
                    // Обновляем список заметок в боковой шторке, чтобы данные не рассинхронизировались
                    foodMenuList.refreshData();
                }
            }
        });

        // Слушатель скролла для скрытия/показа fab_add
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // Метод постоянно срабатывает во время прокрутки списка.
            // Переменная dy показывает направление и скорость скролла по вертикали.
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Если мы в режиме выделения - кнопка и так скрыта, ничего не делаем
                if (isSelectionMode) return;

                // Если dy > 0 (пользователь листает список вниз) и кнопка сейчас видима:
                if (dy > 0 && fab_add.getVisibility() == View.VISIBLE) {
                    // Пользователь скроллит вниз - прячем кнопку
                    fab_add.setVisibility(View.GONE);
                } else if (dy < 0 && fab_add.getVisibility() != View.VISIBLE) {
                    // Пользователь скроллит вверх - показываем кнопку
                    fab_add.setVisibility(View.VISIBLE);
                }
            }
        });

        // Кнопка добавления новой заметки
        fab_add.setOnClickListener(new View.OnClickListener() {
            // Метод срабатывает, когда пользователь нажимает на кнопку "+"
            @Override
            public void onClick(View v) {
                // Убираем фокус с поиска перед открытием заметки
                searchView_home.clearFocus();

                // Создаем намерение (Intent) для перехода с текущего главного экрана (MainActivity)
                // на экран создания/редактирования заметки (NotesTakerActivity).
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                // Запускаем новый экран с ожиданием результата (с уникальным кодом 101).
                // Это означает: "Открой экран создания заметки, а когда он закроется,
                // верни мне данные (новую заметку) под кодом 101, чтобы я знал, что с ними делать".
                startActivityForResult(intent, 101);
            }
        });

        // Обновление списка
        updateRecycle(notes);
    }

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Когда пользователь нажмет "Назад" в настройках и если был импорт (RESULT_OK)
                if (result.getResultCode() == RESULT_OK) {
                    // Достаем свежий список из базы данных
                    List<Notes> updatedList = database.mainDAO().getAll();
                    // Обновляем RecyclerView (вызовите ваш актуальный метод обновления)
                    refreshData();
                    foodMenuList.updateRecycle();
                }
            }
    );

    /**
     * Обрабатывает нажатие системной кнопки отмены назад
     * при открытой шторке
     */
    @Override
    public void onBackPressed() {
        main_menu = findViewById(R.id.main_menu);
        // Проверяем, выдвинута ли шторка слева (GravityCompat.START)
        if (main_menu != null && main_menu.isDrawerOpen(GravityCompat.START)) {
            // Плавно задвигаем её обратно
            main_menu.closeDrawer(GravityCompat.START);
        } else {
            // Если шторка закрыта — закрываем приложение (или возвращаемся на предыдущий экран)
            super.onBackPressed();
        }
    }

    /**
     * Обновление списка данных для выхода или создания новых заметок
     */
    private void changeNotes() {
        // Полностью очищаем текущий список заметок в памяти телефона
        notes.clear();
        // Загружаем все заметки из базы данных заново (теперь там появилась и наша новая запись)
        notes.addAll(database.mainDAO().getAll());
        // Говорим адаптеру главного списка: "Данные полностью поменялись, перерисуй карточки"
        notesListAdapterMain.notifyDataSetChanged();
        // Даем команду вспомогательному классу бокового меню тоже обновить свои данные,
        // чтобы списки не рассинхронизировались
        foodMenuList.refreshData();
    }

    /**
     * Этот блок кода создает слушатель обычных (коротких) нажатий на карточку заметки в списке.
     * Он определяет, что должно произойти при клике: поставить галочку (если мы что-то массово выделяем)
     * или открыть заметку для редактирования (в обычном режиме).
     */
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        // Метод автоматически срабатывает при коротком тапе по любой заметке.
        // Переменная 'notes' содержит данные именно той заметки, на которую нажал пользователь.
        @Override
        public void onClick(Notes notes) {
            // Проверяем: включен ли сейчас режим множественного выбора (когда сверху другая панель)
            if (isSelectionMode) {
                // Если мы в режиме выделения, то клик ставит/снимает галочку
                toggleSelection();
            } else {
                // Убираем фокус с поиска перед открытием заметки
                searchView_home.clearFocus();

                // Обычный режим - открываем заметку
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                // Упаковываем данные выбранной заметки в Intent под именем "Старая заметка".
                // Так новый экран поймет, что нужно не создавать пустой лист, а заполнить поля уже существующим текстом.
                intent.putExtra("Старая заметка", notes);
                // Открываем экран редактора и ожидаем от него результат с кодом 102.
                // Код 102 будет означать для нас: "Пользователь вернулся после редактирования СТАРОЙ заметки, нужно обновить список".
                startActivityForResult(intent, 102);
            }
        }

        // Метод автоматически срабатывает при долгом тапе по любой заметке.
        // Переменная 'notes' содержит данные именно той заметки, на которую нажал пользователь.
        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            if (!isSelectionMode) {
                // Включаем режим выделения и сразу выделяем ту карточку, на которую долго нажали
                startSelectionMode(notes);
            }
        }
    };

    /**
     * Этот метод ловит "ответы" от других экранов, когда они закрываются.
     * Он автоматически вызывается системой каждый раз, когда вы возвращаетесь
     * из экрана создания или редактирования заметки обратно на главный экран.
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Стандартный вызов родительского метода, чтобы Android правильно обработал возвращение
        super.onActivityResult(requestCode, resultCode, data);

        // Проверяем requestCode: откуда именно мы только что вернулись?
        // 101 — это код, который мы задавали при нажатии на кнопку "Плюс" (создание новой заметки)
        if (requestCode == 101) {
            // Проверяем resultCode: как именно пользователь покинул тот экран?
            // RESULT_OK означает, что всё прошло успешно (пользователь нажал кнопку "Сохранить", а не "Отмена")
            if (resultCode == Activity.RESULT_OK) {
                changeNotes();
            }
        } else if (requestCode == 102) { // 102 — это код, который мы задавали при клике на существующую карточку (редактирование старой заметки)
            if (resultCode == Activity.RESULT_OK) {
                changeNotes();
            }
        }
    }
//
    /**
     * Этот метод обрабатывает нажатия на пункты всплывающего меню (например, если у вас есть меню по клику на три точки).
     * Он определяет, какое именно действие выбрал пользователь, и применяет его к одной конкретной заметке.
     */
    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        // Узнаем уникальный ID того пункта меню, на который только что нажал пользователь
        int itemId = item.getItemId();

        // Если пользователь выбрал пункт "Закрепить/Открепить"
        if (itemId == R.id.pin) {
            // Проверяем, закреплена ли эта заметка прямо сейчас
            if (selectedNotes.isPinned()) {
                // Если да — обращаемся к базе данных и открепляем её (передаем статус false)
                database.mainDAO().pin(selectedNotes.getID(), false);
            } else {
                // Если нет — закрепляем её (передаем статус true)
                database.mainDAO().pin(selectedNotes.getID(), true);
            }

            // Обновляем главный список сразу
            refreshData();
            // Обновляем список в боковой шторке для синхронизации интерфейса
            foodMenuList.refreshData();

            // Возвращаем true — это сигнал для Android: "Мы сами обработали этот клик, системе больше ничего делать не нужно"
            return true;
        } else if (itemId == R.id.delete) { // Если пользователь выбрал пункт "Удалить"
            // Обращаемся к базе данных и безвозвратно удаляем эту заметку
            database.mainDAO().delete(selectedNotes);

            // Обновляем оба списка сразу (и главный, и в меню)
            refreshData();
            foodMenuList.refreshData();

            // Подтверждаем успешную обработку клика
            return true;
        } else {
            // Если пользователь нажал на какой-то неизвестный пункт меню, возвращаем false,
            // позволяя системе обработать это событие по умолчанию
            return false;
        }
    }

    /**
     * Этот метод — универсальный помощник для обновления главного экрана.
     * Он используется в разных местах приложения, когда нужно убедиться,
     * что на экране отображаются самые свежие данные из базы данных.
     */
    public void refreshData() { // В остальных местах
        // Шаг 1: Полностью очищаем текущий список заметок в памяти (чтобы не было дубликатов)
        notes.clear();
        // Шаг 2: Обращаемся к базе данных, берем оттуда все актуальные заметки
        // (с учетом последних изменений, удалений или закреплений) и добавляем их в наш пустой список
        notes.addAll(database.mainDAO().getAll());
        // Шаг 3: Даем команду адаптеру главного списка: "Данные полностью обновились,
        // перерисуй все карточки на экране с новыми значениями"
        notesListAdapterMain.notifyDataSetChanged();
    }

    /**
     * Этот метод отвечает за активацию режима множественного выбора (выделения).
     * Он срабатывает, когда пользователь совершает долгое нажатие на любую заметку в списке.
     */
    private void startSelectionMode(Notes firstNote) {
        // Включаем глобальный "рубильник": теперь все приложение знает,
        // что мы находимся в режиме выделения (изменятся клики по карточкам и поведение скролла)
        isSelectionMode = true;
        // Очищаем список выбранных элементов, чтобы там случайно не осталось старых данных
        selectedNotesList.clear();
        // Сразу добавляем в список ту заметку, на которую пользователь сделал долгое нажатие
        selectedNotesList.add(firstNote);
        // Передаем команду нашему адаптеру:
        // "Включи режим выделения, вот текущий список выбранных заметок,
        // и вот сам recyclerView, чтобы ты мог аккуратно показать галочки без дергания экрана"
        notesListAdapterMain.setSelectionMode(true, selectedNotesList, recyclerView);

        // Прячем обычный тулбар и кнопку добавления, показываем тулбар выделения
        default_toolbar.setVisibility(View.GONE);
        // Показываем специальную панель выделения (где находятся кнопки корзины, булавки и отмены)
        selection_toolbar.setVisibility(View.VISIBLE);
        // Убираем с экрана плавающую кнопку "Плюс", чтобы она не мешала и пользователь
        // случайно не начал создавать новую заметку во время выделения
        fab_add.setVisibility(View.GONE);
    }

    /**
     * Этот метод отвечает за выход из режима множественного выбора (выделения).
     * Он возвращает приложение в обычное состояние, снимая все выделения и восстанавливая стандартный интерфейс.
     */
    private void stopSelectionMode() {
        // Выключаем глобальный "рубильник": теперь приложение знает, что мы вернулись в обычный режим
        isSelectionMode = false;
        // Очищаем список выбранных заметок, чтобы при следующем долгом нажатии начать с чистого листа
        selectedNotesList.clear();
        // Передаем команду адаптеру: "Выключи режим выделения".
        // Адаптер пробежится по видимым карточкам, спрячет галочки и вернет на место иконки булавок.
        notesListAdapterMain.setSelectionMode(false, selectedNotesList, recyclerView);

        // Возвращаем всё обратно
        selection_toolbar.setVisibility(View.GONE);
        // Возвращаем на экран стандартную верхнюю панель (с кнопкой меню, поиском и настройками)
        default_toolbar.setVisibility(View.VISIBLE);
        // Снова показываем плавающую кнопку "Плюс", чтобы пользователь мог создавать новые заметки
        fab_add.setVisibility(View.VISIBLE);
    }

    /**
     * Этот метод — своеобразный предохранитель для режима выделения.
     * Он проверяет, остались ли еще выделенные заметки, и если нет — выключает этот режим.
     * (Обычно этот метод вызывается после того, как пользователь снял галочку с какой-то карточки).
     */
    private void toggleSelection() {
        // Проверяем: пуст ли сейчас список выделенных заметок?
        // (То есть, снял ли пользователь галочку с самой последней выделенной карточки?)
        if (selectedNotesList.isEmpty()) {
            // Если список оказался пустым, нам больше не нужно показывать панель с корзиной и булавкой.
            // Поэтому мы вызываем метод stopSelectionMode(), который возвращает обычный интерфейс
            // с кнопкой добавления и панелью поиска.
            stopSelectionMode();
        }
    }

    /**
     * Этот метод отвечает за самую первую настройку и подготовку вашего списка (RecyclerView).
     * Именно здесь мы применили все те важные оптимизации, чтобы сетка карточек
     * работала плавно, не дергалась и не перестраивалась при долгом нажатии.
     */
    public void updateRecycle(List<Notes> notes) {
        // Сообщаем Android, что размер самого списка (RecyclerView) не будет
        // постоянно меняться из-за добавления новых элементов. Это небольшая оптимизация производительности.
        recyclerView.setHasFixedSize(true);

        // Создаем "шахматную" сетку (как в Pinterest) в 2 колонки по вертикали
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        // 1. ЗАПРЕЩАЕМ СЕТКЕ ПЕРЕСЧИТЫВАТЬ ПОЗИЦИИ И ПРЫГАТЬ
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        // Применяем нашу настроенную сетку к списку
        recyclerView.setLayoutManager(layoutManager);

        // Создаем наш адаптер, передавая ему список заметок, слушатель кликов и контекст экрана
        notesListAdapterMain = new NotesListAdapterMain(notes, notesClickListener, MainActivity.this);

        // 2. Включаем поддержку стабильных ID, которые мы прописали в адаптере
        notesListAdapterMain.setHasStableIds(true);

        // Подключаем готовый адаптер к нашему RecyclerView
        recyclerView.setAdapter(notesListAdapterMain);

        // 3. Отключаем анимацию (чтобы галочка появлялась мгновенно)
        recyclerView.setItemAnimator(null);
    }
}