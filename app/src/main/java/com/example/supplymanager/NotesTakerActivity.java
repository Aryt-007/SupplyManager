package com.example.supplymanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.supplymanager.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesTakerActivity extends AppCompatActivity {

    // Дата создания заметки
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    EditText editText_tittle, editText_notes;
    ImageView imageView_save, imageView_cancellation;
    Notes notes;
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

        imageView_save = findViewById(R.id.imageView_save);
        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editText_tittle.getText().toString();
                String description = editText_notes.getText().toString();

                if (description.isEmpty()) {
                    Toast.makeText(NotesTakerActivity.this, "Добавтье текст", Toast.LENGTH_SHORT).show(); // Выводит текст "Добавтье текст", если человек ничего не ввел
                    return;
                } else if (title.isEmpty()) {
                    Toast.makeText(NotesTakerActivity.this, "Добавтье тему", Toast.LENGTH_SHORT).show(); // Выводит текст "Добавтье тему", если человек ничего не ввел
                    return;
                }

                Date date = new Date();

                if (!isOldNote) {
                    notes = new Notes();
                }
                notes.setTitle(title);
                notes.setNotes(description);
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
        editText_tittle = findViewById(R.id.editText_tittle);
        editText_notes = findViewById(R.id.editText_notes);

        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("Старая заметка");
            editText_notes.setText(notes.getNotes());
            editText_tittle.setText(notes.getTitle());
            isOldNote = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}