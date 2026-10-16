package com.example.supplymanager.NotesTaker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.Models.Notes;
import com.example.supplymanager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotesTakerActivity extends AppCompatActivity {

    FrameLayout activity_notes_taker;
    LinearLayout activity_notes_taker_btn_add;
    ImageButton btn_add;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    EditText editText_tittle;
    ImageView imageView_cancellation, imageView_cancellation_taker, imageView_save, imageView_save_taker;
    // Флаг для защиты кнопки от скрытия при автоматическом скролле
    private boolean isAutoScrolling = false;
    boolean isOldNote = false;
    List<Notes> listNotes;
    Notes notes;
    RelativeLayout relative_layout_in_toolbar;
    private RowAdapter adapter;
    private RecyclerView recyclerView;
    private List<NoteRow> rowList = new ArrayList<>();
    RoomDB database;
    View rootView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes_taker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_notes_taker), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        // Инициализация базы данных
        database = RoomDB.getInstance(this);

        activity_notes_taker = findViewById(R.id.activity_notes_taker); // Создание списка
        rootView = findViewById(android.R.id.content);

        editText_tittle = findViewById(R.id.editText_tittle); // Название темы списка
        btn_add = findViewById(R.id.btn_add); // Кнопка добавления строки списка
        listNotes = database.mainDAO().getAll(); // Список для хранения данных

        relative_layout_in_toolbar = findViewById(R.id.relative_layout_in_toolbar); // Группа для кнопок отмены и сохранения
        imageView_save = findViewById(R.id.imageView_save); // Кнопка сохранения
        imageView_cancellation = findViewById(R.id.imageView_cancellation); // Кнопка отмены

        activity_notes_taker_btn_add = findViewById(R.id.activity_notes_taker_btn_add); // Поле для ввода числа для создания RecyclerView
        imageView_cancellation_taker = findViewById(R.id.imageView_cancellation_taker); // Кнопка отмены для создания RecyclerView
        imageView_save_taker = findViewById(R.id.imageView_save_taker); // Кнопка сохранения для создания RecyclerView

        recyclerView = findViewById(R.id.scrollView); // Это ваш RecyclerView в XML
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RowAdapter(rowList);
        recyclerView.setAdapter(adapter);


        // Устанавливаем слушатель нажатий на корневой layout
        // Если пользователь кликает в любое пустое место на фоне или на верхней панели,
        // мы вызываем метод clearFocus(), который убирает курсор из полей ввода и прячет клавиатуру.
        activity_notes_taker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocus();
            }
        });

        // Также на другие элементы, кроме ScrollView
        editText_tittle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                clearFocus();
            }
        });
        relative_layout_in_toolbar.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                clearFocus();
            }
        });

        // Проверяем, есть ли старая заметка
        notes = null;
        try {
            // Пытаемся "выловить" данные, которые нам передал MainActivity при открытии этого экрана
            Notes oldNote = (Notes) getIntent().getSerializableExtra("Старая заметка");
            if (oldNote != null) {
                // Если данные есть, значит мы открыли старую заметку для редактирования!
                notes = oldNote;
                editText_tittle.setText(notes.getTitle()); // Вставляем старый заголовок
                isOldNote = true; // Ставим флаг, что это редактирование

                // Загружаем строки таблицы из старой заметки
                // Восстанавливаем старые строки списка из базы данных (парсим строки с разделителем "|")
                loadExistingRows();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Добавляем первую строку только для новой заметки
        // Если это создание абсолютно новой заметки, то список пуст.
        // Добавляем одну стартовую пустую строку, чтобы пользователю было куда писать.
        if (!isOldNote) {
            addNewRow();
        }

        //Настройка кнопки "+"
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRow(); // Обычный клик: добавляем одну строку
                scrollToBottom(); // И сразу прокручиваем список в самый низ
            }
        });
        btn_add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAddRowsDialog(); // Долгое удержание: показываем окошко, чтобы спросить "Сколько строк добавить?"
                return true;
            }
        });

        // Логика сохранения заметки
        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_tittle.getText().toString(); // Берем заголовок

                StringBuilder textTitles = new StringBuilder();
                StringBuilder countTitles = new StringBuilder();

                // Берем данные прямо из нашего списка
                // Проходимся циклом по всем визуальным строкам из нашего адаптера
                for (NoteRow row : rowList) {
                    String textTitleStr = row.getTitle().trim();
                    String countTitleStr = row.getCount().trim();

                    // Если строка абсолютно пустая, просто игнорируем её (не сохраняем мусор в БД)
                    if (textTitleStr.isEmpty() && countTitleStr.isEmpty()) continue;

                    // Если это не первая строка, добавляем наш специальный разделитель "|"
                    if (textTitles.length() > 0) {
                        textTitles.append("|");
                        countTitles.append("|");
                    }
                    // "Склеиваем" все названия в одну длинную строку и все количества в другую
                    textTitles.append(textTitleStr);
                    countTitles.append(countTitleStr);
                }

                Date date = new Date(); // Берем текущую дату и время

                if (isOldNote && notes != null) {
                    // Обновляем существующую заметку
                    // Записываем новые данные поверх старых в объект
                    notes.setTitle(title);
                    notes.setText_title(textTitles.toString());
                    notes.setCount_title(countTitles.toString());
                    notes.setDate(DATE_FORMAT.format(date));

                    // Обновляем в базе данных
                    // Отправляем команду в Room обновить строку в базе данных по её уникальному ID
                    database.mainDAO().update(
                            notes.getID(),
                            notes.getTitle(),
                            notes.getDate(),
                            notes.getText_title(),
                            notes.getCount_title()
                    );

                } else {
                    // Создаем новую заметку
                    // Создаем новый объект Notes с нуля
                    notes = new Notes();
                    notes.setTitle(title);
                    notes.setText_title(textTitles.toString());
                    notes.setCount_title(countTitles.toString());
                    notes.setDate(DATE_FORMAT.format(date));

                    // Сохраняем в базу данных
                    // Отправляем команду в Room вставить (insert) новую запись
                    database.mainDAO().insert(notes);

                }

                // Отправляем команду в Room вставить (insert) новую запись
                Intent intent = new Intent();
                intent.putExtra("Запись", notes);
                // Говорим системе "Всё прошло успешно" (RESULT_OK) и закрываем этот экран
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        // Логика кнопки отмены
        imageView_cancellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Говорим системе "Пользователь передумал" (RESULT_CANCELED) и закрываем экран без сохранения
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


        // Автоматическое скрытие "+" при открытии клавиатуры
        // Это нужно, чтобы кнопка не перекрывала текстовые поля, когда вы печатаете
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());

            if (isKeyboardVisible) {
                btn_add.setVisibility(View.GONE);
            } else {
                btn_add.setVisibility(View.VISIBLE);
            }

            return insets;
        });

        // Слушатель скролла для скрытия/показа кнопки btn_add
        // Автоматическое скрытие "+" при скроллинге вниз
        // Классическое поведение плавающей кнопки (FAB) в Android
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            // Этот метод срабатывает, когда меняется СОСТОЯНИЕ скролла (начался, идет по инерции, остановился)
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // SCROLL_STATE_IDLE означает, что список полностью остановился.
                // Как только автоматический скролл доехал до низа, мы выключаем защиту.
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isAutoScrolling = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Если прямо сейчас идет автоматический скролл (из-за новой строки) —
                // просто прерываем метод (return). Никакого скрытия кнопки не произойдет!
                if (isAutoScrolling) {
                    return;
                }

                // Если защита выключена, значит пользователь листает сам.
                // Выполняем вашу классическую логику скрытия/показа кнопки.
                if (dy > 0 && btn_add.getVisibility() == View.VISIBLE) {
                    // Пользователь скроллит вниз пальцем — прячем кнопку
                    btn_add.setVisibility(View.GONE);
                } else if (dy < 0 && btn_add.getVisibility() != View.VISIBLE) {
                    // Пользователь скроллит вверх — показываем кнопку
                    btn_add.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Этот метод отвечает за добавление новой, пустой строки (позиции) в ваш список покупок/задач.
     * Он вызывается при нажатии на кнопку добавления, чтобы дать пользователю новое поле для ввода.
     */
    public void addNewRow() {
        // Создаем новый объект NoteRow с пустыми полями (нет ни названия, ни количества)
        // и добавляем его в конец нашего массива данных (rowList).
        rowList.add(new NoteRow("", ""));
        // Вычисляем индекс (позицию) этой только что добавленной строки.
        // Так как индексация начинается с нуля, последний элемент имеет индекс (размер_списка - 1).
        int newPosition = rowList.size() - 1;
        // Сообщаем адаптеру: "Эй, я добавил один новый элемент вот на эту конкретную позицию!".
        // Благодаря notifyItemInserted, строка появится на экране с красивой анимацией выезда,
        // а не просто резко "прыгнет", как было бы при notifyDataSetChanged().
        adapter.notifyItemInserted(newPosition);

        // Ставим фокус на новую строку
        // Вызываем ваш кастомный метод из адаптера, который мы разбирали ранее.
        // Он заставит курсор (фокус) автоматически прыгнуть в только что созданное поле.
        adapter.requestFocusOn(newPosition);
    }

    /**
     * Это вспомогательный метод для улучшения пользовательского опыта (UX).
     * Он убирает мигающий курсор из текстовых полей и прячет виртуальную клавиатуру на телефоне.
     * Метод срабатывает, когда пользователь кликает в пустое место экрана (вне полей ввода).
     */
    // Метод для снятия фокуса со всех полей
    private void clearFocus() {
        // Снимаем фокус с текущего активного поля
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus(); // Если такой элемент есть, принудительно забираем у него фокус
        }

        // Скрываем клавиатуру
        // Обращаемся к системной службе Android, которая управляет клавиатурой (InputMethodManager)
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // Даем команду жестко скрыть клавиатуру с экрана
            imm.hideSoftInputFromWindow(activity_notes_taker.getWindowToken(), 0);
        }
    }

    /**
     * Этот метод нужен для открытия старых (ранее сохраненных) заметок.
     * Он извлекает данные из базы (где они хранятся одной длинной строкой с разделителями "|")
     * и превращает их обратно в удобный список карточек на экране.
     */
    private void loadExistingRows() {
        // Очищаем текущий список, чтобы старые данные случайно не смешались с новыми
        rowList.clear(); // Очищаем список

        // Достаем "склеенные" строки из объекта заметки
        String textTitles = notes.getText_title();
        String countTitles = notes.getCount_title();

        // Защита от ошибок: проверяем, что строки вообще существуют и не пустые
        if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
            // Разрезаем длинную строку на массив отдельных слов.
            // Символ "|" в программировании часто означает логическое "ИЛИ",
            // поэтому мы экранируем его двумя слешами ("\\|"), чтобы система поняла его как обычный текст.
            String[] textTitleArray = textTitles.split("\\|");
            String[] countTitleArray = countTitles.split("\\|");

            // Находим минимальную длину, чтобы избежать вылета программы,
            // если массивы вдруг оказались разного размера
            int maxLength = Math.min(textTitleArray.length, countTitleArray.length);

            // Проходимся по всем элементам и воссоздаем объекты NoteRow
            for (int i = 0; i < maxLength; i++) {
                rowList.add(new NoteRow(textTitleArray[i], countTitleArray[i]));
            }
        }

        // Если заметка оказалась совсем пустой, мы все равно добавляем хотя бы
        // одну пустую строчку, чтобы пользователю было куда начать печатать.
        if (rowList.isEmpty()) {
            rowList.add(new NoteRow("", ""));
        }

        // Говорим адаптеру полностью перерисовать список с новыми восстановленными данными
        adapter.notifyDataSetChanged();
    }

    /**
     * Крошечный, но очень полезный метод.
     * Он автоматически прокручивает экран в самый низ списка.
     */
    private void scrollToBottom() {
        // Защита: проверяем, что в списке вообще есть хотя бы один элемент
        if (rowList.size() > 0) {
            // Включаем "защиту", перед тем как запустить скролл
            isAutoScrolling = true;
            // Даем команду RecyclerView плавно (smooth) прокрутиться до последнего элемента.
            // Это очень удобно, когда список уже длинный, и вы добавляете новую позицию:
            // экран сам едет вниз, избавляя вас от необходимости скроллить пальцем.
            recyclerView.smoothScrollToPosition(rowList.size() - 1);
        }
    }

    /**
     * Этот метод отвечает за создание и показ всплывающего окна (диалога),
     * которое появляется при ДОЛГОМ нажатии на кнопку "+".
     * В нем пользователь может ввести число (например, "5"), чтобы добавить
     * 5 пустых строк за один раз, вместо того чтобы 5 раз кликать на плюсик.
     */
    private void showAddRowsDialog() {
        // Создаем "строителя" для всплывающего окна
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Загружаем разметку
        // Берем ваш отдельный файл дизайна (activity_notes_taker_btn_add.xml)
        // и превращаем его в реальный View-объект для диалога
        LayoutInflater dialogInflater = getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.activity_notes_taker_btn_add, null);

        // Получаем элементы
        // Находим кнопки внутри этого нового диалога
        imageView_cancellation_taker = dialogView.findViewById(R.id.imageView_cancellation_taker);
        imageView_save_taker = dialogView.findViewById(R.id.imageView_save_taker);

        // Вставляем ваш дизайн в диалог и создаем его
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Прозрачный фон
        // Делаем стандартный фон диалога полностью прозрачным.
        // Это позволяет показать скругленные углы, которые вы, скорее всего, нарисовали в своем XML.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // лучше сделать прозрачным, а не белым, чтобы углы не конфликтовали
        }

        // Настройка кнопки сохранения
        // Настройка кнопки "Принять/Добавить"
        imageView_save_taker.setOnClickListener(v -> {
            EditText number_add = dialogView.findViewById(R.id.number_add);
            String countText = number_add.getText().toString().trim();

            // Проверка: не оставил ли пользователь поле пустым?
            if (countText.isEmpty()) {
                return;
            }

            try {
                // Пытаемся превратить введенный текст в число
                int count = Integer.parseInt(countText);

                // Проверка на отрицательные числа и ноль
                if (count <= 0) {
                    return;
                }

                // Запускаем цикл и добавляем нужное количество строк
                for (int i = 0; i < count; i++) {
                    addNewRow();
                }
                // Прокручиваем список в самый низ, чтобы показать новые строки
                scrollToBottom();

                // Закрываем всплывающее окно
                dialog.dismiss();

            } catch (NumberFormatException e) {
                // Если пользователь умудрился ввести текст вместо цифр
            }
        });

        // Настройка кнопки отмены
        imageView_cancellation_taker.setOnClickListener(v -> {
            dialog.dismiss(); // Просто закрываем окно без изменений
        });

        // 1. Сначала показываем диалог
        dialog.show();

        // 2. И ТОЛЬКО ПОСЛЕ ЭТОГО жестко фиксируем размеры через Window!
        // Если сделать это до .show(), Android может проигнорировать настройки.
        // Мы переводим 250dp и 100dp в реальные пиксели экрана (умножая на плотность пикселей - density),
        // чтобы на любых телефонах окошко выглядело одинаково.
        if (dialog.getWindow() != null) {
            float density = getResources().getDisplayMetrics().density;
            int width = (int) (250 * density);
            int height = (int) (100 * density);

            dialog.getWindow().setLayout(width, height);
        }
    }

}