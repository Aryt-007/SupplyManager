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

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    EditText editText_tittle;
    ImageView imageView_save, imageView_cancellation, imageView_cancellation_taker, imageView_save_taker;
    ImageButton btn_add;
    Notes notes;
    RelativeLayout relative_layout_in_toolbar;
    RecyclerView scrollView;
    List<Notes> listNotes;
    LinearLayout activity_notes_taker_btn_add;
    View rootView;
    FrameLayout activity_notes_taker;
    boolean isOldNote = false;
    RoomDB database;
    private RecyclerView recyclerView;
    private RowAdapter adapter;
    private List<NoteRow> rowList = new ArrayList<>();

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

        editText_tittle = findViewById(R.id.editText_tittle);
        btn_add = findViewById(R.id.btn_add);
        listNotes = database.mainDAO().getAll();
        relative_layout_in_toolbar = findViewById(R.id.relative_layout_in_toolbar);

        imageView_cancellation_taker = findViewById(R.id.imageView_cancellation_taker);
        imageView_save_taker = findViewById(R.id.imageView_save_taker);

        activity_notes_taker_btn_add = findViewById(R.id.activity_notes_taker_btn_add);
        activity_notes_taker = findViewById(R.id.activity_notes_taker);
        rootView = findViewById(android.R.id.content);

        recyclerView = findViewById(R.id.scrollView); // Это ваш RecyclerView в XML
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RowAdapter(rowList);
        recyclerView.setAdapter(adapter);


        // Устанавливаем слушатель нажатий на корневой layout
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
            Notes oldNote = (Notes) getIntent().getSerializableExtra("Старая заметка");
            if (oldNote != null) {
                notes = oldNote;
                editText_tittle.setText(notes.getTitle());
                isOldNote = true;

                // Загружаем строки таблицы из старой заметки
                loadExistingRows();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Добавляем первую строку только для новой заметки
        if (!isOldNote) {
            addNewRow();
        }

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRow();
                scrollToBottom();
            }
        });
        btn_add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAddRowsDialog();
                return true;
            }
        });

        imageView_save = findViewById(R.id.imageView_save);
        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_tittle.getText().toString();

                StringBuilder textTitles = new StringBuilder();
                StringBuilder countTitles = new StringBuilder();

                // Берем данные прямо из нашего списка
                for (NoteRow row : rowList) {
                    String textTitleStr = row.getTitle().trim();
                    String countTitleStr = row.getCount().trim();

                    if (textTitleStr.isEmpty() && countTitleStr.isEmpty()) continue;

                    if (textTitles.length() > 0) {
                        textTitles.append("|");
                        countTitles.append("|");
                    }
                    textTitles.append(textTitleStr);
                    countTitles.append(countTitleStr);
                }

                Date date = new Date();

                if (isOldNote && notes != null) {
                    // Обновляем существующую заметку
                    notes.setTitle(title);
                    notes.setText_title(textTitles.toString());
                    notes.setCount_title(countTitles.toString());
                    notes.setDate(DATE_FORMAT.format(date));

                    // Обновляем в базе данных
                    database.mainDAO().update(
                            notes.getID(),
                            notes.getTitle(),
                            notes.getNotes(),
                            notes.getText_title(),
                            notes.getCount_title()
                    );

                } else {
                    // Создаем новую заметку
                    notes = new Notes();
                    notes.setTitle(title);
                    notes.setText_title(textTitles.toString());
                    notes.setCount_title(countTitles.toString());
                    notes.setDate(DATE_FORMAT.format(date));

                    // Сохраняем в базу данных
                    database.mainDAO().insert(notes);

                }

                Intent intent = new Intent();
                intent.putExtra("Запись", notes);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        imageView_cancellation = findViewById(R.id.imageView_cancellation);
        imageView_cancellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && btn_add.getVisibility() == View.VISIBLE) {
                    // Пользователь скроллит вниз — прячем кнопку
                    btn_add.setVisibility(View.GONE);
                } else if (dy < 0 && btn_add.getVisibility() != View.VISIBLE) {
                    // Пользователь скроллит вверх — показываем кнопку
                    btn_add.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showAddRowsDialog() {
        // Создаем AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Загружаем разметку
        LayoutInflater dialogInflater = getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.activity_notes_taker_btn_add, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Прозрачный фон
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // лучше сделать прозрачным, а не белым, чтобы углы не конфликтовали
        }

        // Получаем элементы
        imageView_cancellation_taker = dialogView.findViewById(R.id.imageView_cancellation_taker);
        imageView_save_taker = dialogView.findViewById(R.id.imageView_save_taker);

        // Настройка кнопки сохранения
        imageView_save_taker.setOnClickListener(v -> {
            EditText number_add = dialogView.findViewById(R.id.number_add);
            String countText = number_add.getText().toString().trim();

            if (countText.isEmpty()) {
                Toast.makeText(this, "Введите количество", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int count = Integer.parseInt(countText);

                if (count <= 0) {
                    Toast.makeText(this, "Введите число больше 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int i = 0; i < count; i++) {
                    addNewRow();
                }
                scrollToBottom();

                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
            }
        });

        // Настройка кнопки отмены
        imageView_cancellation_taker.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // 1. Сначала показываем диалог
        dialog.show();

        // 2. И ТОЛЬКО ПОСЛЕ ЭТОГО жестко фиксируем размеры через Window!
        if (dialog.getWindow() != null) {
            float density = getResources().getDisplayMetrics().density;
            int width = (int) (250 * density);
            int height = (int) (100 * density);

            dialog.getWindow().setLayout(width, height);
        }
    }

    // Метод для снятия фокуса со всех полей
    private void clearFocus() {
        // Снимаем фокус с текущего активного поля
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        // Скрываем клавиатуру
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity_notes_taker.getWindowToken(), 0);
        }
    }


    public void addNewRow() {
        rowList.add(new NoteRow("", ""));
        int newPosition = rowList.size() - 1;
        adapter.notifyItemInserted(newPosition);

        // Ставим фокус на новую строку
        adapter.requestFocusOn(newPosition);
    }

    private void loadExistingRows() {
        rowList.clear(); // Очищаем список

        String textTitles = notes.getText_title();
        String countTitles = notes.getCount_title();

        if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
            String[] textTitleArray = textTitles.split("\\|");
            String[] countTitleArray = countTitles.split("\\|");

            int maxLength = Math.min(textTitleArray.length, countTitleArray.length);

            for (int i = 0; i < maxLength; i++) {
                rowList.add(new NoteRow(textTitleArray[i], countTitleArray[i]));
            }
        }

        if (rowList.isEmpty()) {
            rowList.add(new NoteRow("", ""));
        }

        adapter.notifyDataSetChanged();
    }

    private void scrollToBottom() {
        if (rowList.size() > 0) {
            recyclerView.smoothScrollToPosition(rowList.size() - 1);
        }
    }

}