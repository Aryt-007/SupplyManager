package com.example.supplymanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotesTakerActivity extends AppCompatActivity {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    EditText editText_tittle;
    ImageView imageView_save, imageView_cancellation;
    ImageButton btn_add;
    Notes notes;
    ScrollView scrollView;
    List<Notes> listNotes;
    LinearLayout container;
    LayoutInflater inflater;
    boolean isOldNote = false;
    RoomDB database; // Добавьте поле для базы данных

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes_taker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_notes_taker), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
                scrollToBottom(); // Прокручиваем вниз
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

    private void addNewRow() {
        View row_item = inflater.inflate(R.layout.row_item, container, false);

        // Настраиваем кнопки для новой строки
        setupRowButtons(row_item);

        container.addView(row_item, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private void loadExistingRows() {
        // Очищаем контейнер
        container.removeAllViews();

        String textTitles = notes.getText_title();
        String countTitles = notes.getCount_title();

        if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
            String[] textTitleArray = textTitles.split("\\|");
            String[] countTitleArray = countTitles.split("\\|");

            int maxLength = Math.min(textTitleArray.length, countTitleArray.length);

            for (int i = 0; i < maxLength; i++) {
                View row_item = inflater.inflate(R.layout.row_item, container, false);

                EditText text_title = row_item.findViewById(R.id.text_title);
                EditText count_title = row_item.findViewById(R.id.count_title);

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
        if (btn_delete != null) {
            btn_delete.setOnClickListener(v -> {
                container.removeView(row_item);
                if (container.getChildCount() == 0) {
                    addNewRow();
                }
            });
        }

        EditText count_title = row_item.findViewById(R.id.count_title);
        ImageButton btn_up = row_item.findViewById(R.id.btn_up);
        ImageButton btn_down = row_item.findViewById(R.id.btn_down);

        if (btn_up != null && count_title != null) {
            btn_up.setOnClickListener(v -> {
                String text = count_title.getText().toString().trim();
                if (text.isEmpty()) text = "0";
                try {
                    // Проверяем, является ли число целым
                    if (text.contains(".")) {
                        // Если есть десятичная часть
                        float val = Float.parseFloat(text);
                        float result = val + 1;
                        // Если результат целый, показываем без ".0"
                        if (result == Math.floor(result)) {
                            count_title.setText(String.valueOf((int) result));
                        } else {
                            count_title.setText(String.valueOf(result));
                        }
                    } else {
                        // Если целое число
                        int val = Integer.parseInt(text);
                        count_title.setText(String.valueOf(val + 1));
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
                        float val = Float.parseFloat(text);
                        float result = val - 1;
                        if (result == Math.floor(result)) {
                            count_title.setText(String.valueOf((int) result));
                        } else {
                            count_title.setText(String.valueOf(result));
                        }
                    } else {
                        int val = Integer.parseInt(text);
                        count_title.setText(String.valueOf(val - 1));
                    }
                } catch (NumberFormatException e) {
                    count_title.setText("0");
                }
            });
        }
    }
    private void scrollToBottom() {
        if (scrollView != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }
}