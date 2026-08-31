package com.example.supplymanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.supplymanager.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotesTakerActivity extends AppCompatActivity {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    EditText editText_tittle, editText_notes;
    ImageView imageView_save, imageView_cancellation;
    ImageButton btn_add;
    Notes notes;
    LinearLayout container;
    LayoutInflater inflater;
    boolean isOldNote = false;

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

        container = findViewById(R.id.rows_container);
        editText_tittle = findViewById(R.id.editText_tittle);
        editText_notes = findViewById(R.id.editText_notes);
        btn_add = findViewById(R.id.btn_add);
        inflater = LayoutInflater.from(this);

        addNewRow();

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRow();
            }
        });


        imageView_save = findViewById(R.id.imageView_save);
        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_tittle.getText().toString();
                String description = editText_notes.getText().toString();
//                String overallTextTitle = "";
//                float overallCount = 0;
//                for (int i = 0; i < container.getChildCount(); i++) {
//                    View row = container.getChildAt(i);
//                    EditText rowTextTitle = row.findViewById(R.id.text_title);
//                    EditText rowCount = row.findViewById(R.id.count_title);
//
//                    // Получаем текст
//                    String name = rowTextTitle.getText().toString();
//                    String countStr = rowCount.getText().toString();
//
//                    // Преобразуем количество в число
//                    float count = 0;
//                    if (countStr.isEmpty()) {
//                        count = 0;
//                    } else {
//                        try {
//                            count = Float.parseFloat(countStr);
//                        } catch (NumberFormatException e) {
//                            count = 0;
//                        }
//                    }
//
//                    // Добавляем в общий строковый список
//                    overallTextTitle += name + "|";
//                    overallCount += count;
//                }

                if (description.isEmpty()) {
                    Toast.makeText(NotesTakerActivity.this, "Добавьте текст", Toast.LENGTH_SHORT).show();
                    return;
                } else if (title.isEmpty()) {
                    Toast.makeText(NotesTakerActivity.this, "Добавьте тему", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date date = new Date();
                if (!isOldNote) {
                    notes = new Notes();
                }
                notes.setTitle(title);
                notes.setNotes(description);
//                notes.setText_title(overallTextTitle);
//                notes.setCount_title(String.valueOf(overallCount));
                notes.setDate(DATE_FORMAT.format(date));

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
                Intent intent = new Intent();
                intent.putExtra("Отмена", notes);
                setResult(Activity.RESULT_CANCELED, intent);

                finish();
            }
        });


        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("Старая заметка");
            editText_notes.setText(notes.getNotes());
            editText_tittle.setText(notes.getTitle());

//            notes.setText_title(names.toString());
//            notes.setCount_title(counts.toString());
//            if (notes.getText_title() != null && !notes.getText_title().isEmpty()) {
//                String[] rows = notes.getText_title().split("\\|");
//                if (rows.length > 0) {
//                    addNewRow();
//                    EditText firstRowText = container.getChildAt(0).findViewById(R.id.text_title);
//                    EditText firstRowCount = container.getChildAt(0).findViewById(R.id.count_title);
//                    firstRowText.setText(rows[0]);
//                    // Если есть общее количество, копируем его в первую строку
//                    firstRowCount.setText(String.valueOf(notes.getCount_title()));
//                }
//            }
            isOldNote = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addNewRow() {
        View row_item = inflater.inflate(R.layout.row_item, container, false);

        ImageButton btn_delete = row_item.findViewById(R.id.btn_delete);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeView(row_item);
            }
        });

        EditText count_title = row_item.findViewById(R.id.count_title);
        ImageButton btn_up = row_item.findViewById(R.id.btn_up);
        ImageButton btn_down = row_item.findViewById(R.id.btn_down);

        btn_up.setOnClickListener(v -> {
            String text = count_title.getText().toString().trim();
            if (text.isEmpty()) text = "0";
            try {
                float val = Float.parseFloat(text);
                count_title.setText(String.valueOf(val + 1));
            } catch (NumberFormatException e) {
                count_title.setText("0");
            }
        });

        btn_down.setOnClickListener(v -> {
            String text = count_title.getText().toString().trim();
            if (text.isEmpty()) text = "0";
            try {
                float val = Float.parseFloat(text);
                count_title.setText(String.valueOf(val - 1));
            } catch (NumberFormatException e) {
                count_title.setText("0");
            }
        });

        container.addView(row_item, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }
}