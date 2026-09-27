package com.example.supplymanager;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotesTakerActivity extends AppCompatActivity {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    EditText editText_tittle;
    ImageView imageView_save, imageView_cancellation, imageView_cancellation_taker, imageView_save_taker;
    ImageButton btn_add;
    Notes notes;
    RelativeLayout relative_layout_in_toolbar;
    NestedScrollView scrollView;
    List<Notes> listNotes;
    LinearLayout container, activity_notes_taker_btn_add;
    FrameLayout activity_notes_taker;
    LayoutInflater inflater;
    boolean isOldNote = false;
    RoomDB database;

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

        container = findViewById(R.id.rows_container);
        editText_tittle = findViewById(R.id.editText_tittle);
        btn_add = findViewById(R.id.btn_add);
        inflater = LayoutInflater.from(this);
        listNotes = database.mainDAO().getAll();
        scrollView = findViewById(R.id.scrollView);
        relative_layout_in_toolbar = findViewById(R.id.relative_layout_in_toolbar);

        imageView_cancellation_taker = findViewById(R.id.imageView_cancellation_taker);
        imageView_save_taker = findViewById(R.id.imageView_save_taker);

        activity_notes_taker_btn_add = findViewById(R.id.activity_notes_taker_btn_add);
        activity_notes_taker = findViewById(R.id.activity_notes_taker);

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

                // Собираем данные из всех строк таблицы
                StringBuilder textTitles = new StringBuilder();
                StringBuilder countTitles = new StringBuilder();

                for (int i = 0; i < container.getChildCount(); i++) {
                    View row = container.getChildAt(i);

                    if (row != null) {
                        EditText textTitle = row.findViewById(R.id.text_title);
                        EditText countTitle = row.findViewById(R.id.count_title);

                        if (textTitle != null && countTitle != null) {
                            String textTitleStr = textTitle.getText().toString().trim();
                            String countTitleStr = countTitle.getText().toString().trim();

                            // Пропускаем пустые строки
                            if (textTitleStr.isEmpty() && countTitleStr.isEmpty()) {
                                continue;
                            }

                            if (textTitles.length() > 0) {
                                textTitles.append("|");
                                countTitles.append("|");
                            }
                            textTitles.append(textTitleStr);
                            countTitles.append(countTitleStr);
                        }
                    }
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
    }

    private void showAddRowsDialog() {
        // Создаем AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Загружаем разметку
        LayoutInflater dialogInflater = getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.activity_notes_taker_btn_add, null);

        builder.setView(dialogView);
        // Настройка внешнего вида
        AlertDialog dialog = builder.create();

        // Настройка внешнего вида диалога
        if (dialog.getWindow() != null) {
            // Прозрачный фон
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Устанавливаем размеры из XML (200dp x 100dp)
            float density = getResources().getDisplayMetrics().density;
            int width = (int) (150 * density);
            int height = (int) (100 * density);
            dialog.getWindow().setLayout(width, height);
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

                // Добавляем строки
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

        dialog.show();
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
        View row = inflater.inflate(R.layout.row_item, container, false);
        setupRowButtons(row);
        container.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void loadExistingRows() {
        // Очищаем контейнер
        container.removeAllViews();

        EditText text_title;
        EditText count_title;
        String textTitles = notes.getText_title();
        String countTitles = notes.getCount_title();

        if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
            String[] textTitleArray = textTitles.split("\\|");
            String[] countTitleArray = countTitles.split("\\|");

            int maxLength = Math.min(textTitleArray.length, countTitleArray.length);

            for (int i = 0; i < maxLength; i++) {
                View row_item = inflater.inflate(R.layout.row_item, container, false);

                text_title = row_item.findViewById(R.id.text_title);
                count_title = row_item.findViewById(R.id.count_title);

                if (text_title != null && count_title != null) {
                    text_title.setText(textTitleArray[i]);
                    count_title.setText(countTitleArray[i]);
                }

                setupRowButtons(row_item);

                container.addView(row_item, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
            }
        }

        // Если нет строк или все пустые, добавляем одну
        if (container.getChildCount() == 0) {
            addNewRow();
        }
    }

    private void setupRowButtons(View row_item) {
        ImageButton btn_delete = row_item.findViewById(R.id.btn_delete);
        EditText count_title = row_item.findViewById(R.id.count_title);
        ImageButton btn_up = row_item.findViewById(R.id.btn_up);
        ImageButton btn_down = row_item.findViewById(R.id.btn_down);

        if (btn_delete != null) {
            btn_delete.setOnClickListener(v -> {
                container.removeView(row_item);
                if (container.getChildCount() == 0) addNewRow();
            });
        }

        if (btn_up != null && count_title != null) {
            btn_up.setOnClickListener(v -> {
                String text = count_title.getText().toString().trim();
                if (text.isEmpty()) text = "0";
                try {
                    if (text.contains(".")) {
                        float result = Float.parseFloat(text) + 1;
                        count_title.setText(result == Math.floor(result)
                                ? String.valueOf((int) result) : String.valueOf(result));
                    } else {
                        count_title.setText(String.valueOf(Integer.parseInt(text) + 1));
                    }
                } catch (NumberFormatException e) {
                    count_title.setText("0");
                }
            });
        }

        if (btn_down != null && count_title != null) {
            btn_down.setOnClickListener(v -> {
                String text = count_title.getText().toString().trim();
                if (text.isEmpty()) text = "0";
                try {
                    if (text.contains(".")) {
                        float result = Float.parseFloat(text) - 1;
                        count_title.setText(result == Math.floor(result)
                                ? String.valueOf((int) result) : String.valueOf(result));
                    } else {
                        count_title.setText(String.valueOf(Integer.parseInt(text) - 1));
                    }
                } catch (NumberFormatException e) {
                    count_title.setText("0");
                }
            });
        }
    }
    private void scrollToBottom() {
        scrollView.post(() -> {
            View child = scrollView.getChildAt(0);
            if (child == null) return;

            // Прокрутка в самый низ
            int target = child.getHeight() - scrollView.getHeight()
                    + scrollView.getPaddingBottom();
            scrollView.smoothScrollTo(0, Math.max(0, target));

            // Фокус на поле "Название" последней добавленной строки
            if (container.getChildCount() > 0) {
                View lastRow = container.getChildAt(container.getChildCount() - 1);
                EditText textTitle = lastRow.findViewById(R.id.text_title);
                if (textTitle != null) {
                    textTitle.requestFocus();
                }
            }
        });
    }

}