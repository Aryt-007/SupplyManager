package com.example.supplymanager.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.supplymanager.DataBase.RoomDB;
import com.example.supplymanager.MainActivity;
import com.example.supplymanager.Models.Notes;
import com.example.supplymanager.R;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainSettings extends AppCompatActivity {
    ImageButton btn_back, import_xlsx, export_xlsx;
    RoomDB database;
    private boolean isDataChanged = false;
    MainActivity mainActivity;
    List <Notes> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_system), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = RoomDB.getInstance(this);

        btn_back = findViewById(R.id.btn_back);
        import_xlsx = findViewById(R.id.import_xlsx);
        export_xlsx = findViewById(R.id.export_xlsx);

        btn_back.setOnClickListener(v -> {
            if (isDataChanged) {
                setResult(RESULT_OK);
            }
            finish();
        });

        export_xlsx.setOnClickListener(v -> {
            List<Notes> allNotes = database.mainDAO().getAll();
            exportNotesToExcel(this, allNotes);
        });

        import_xlsx.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            excelImportLauncher.launch(intent);
        });
    }

    // Современный лаунчер для выбора Excel-файла при импорте
    private final ActivityResultLauncher<Intent> excelImportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importNotesFromExcel(uri);
                    }
                }
            }
    );

    // --- ЭКСПОРТ: Каждый тайтл = отдельный лист с таблицей ID | НАЗВАНИЕ | КОЛИЧЕСТВО ---
    public void exportNotesToExcel(Context context, List<Notes> notesList) {
        Workbook workbook = new XSSFWorkbook();

        if (notesList.isEmpty()) {
            Toast.makeText(context, "Нет данных для экспорта", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Notes note : notesList) {
            // Берем название тайтла для имени листа. Очищаем от недопустимых в Excel символов и ограничиваем 31 символом
            String rawTitle = note.getTitle() != null && !note.getTitle().trim().isEmpty() ? note.getTitle() : "Заметка " + note.getID();
            String sheetName = rawTitle.replaceAll("[\\\\/?*\\[\\]:]", "_");
            if (sheetName.length() > 31) {
                sheetName = sheetName.substring(0, 31);
            }

            // Защита от дублирования имен листов в Excel
            String uniqueSheetName = sheetName;
            int counter = 1;
            while (workbook.getSheet(uniqueSheetName) != null) {
                uniqueSheetName = sheetName.length() > 28 ? sheetName.substring(0, 28) + "_" + counter : sheetName + "_" + counter;
                counter++;
            }

            Sheet sheet = workbook.createSheet(uniqueSheetName);

            // Создаем шапку таблицы
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("НАЗВАНИЕ");
            headerRow.createCell(2).setCellValue("КОЛИЧЕСТВО");

            String textTitles = note.getText_title();
            String countTitles = note.getCount_title();

            int rowIndex = 1;

            if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
                String[] textArray = textTitles.split("\\|");
                String[] countArray = countTitles.split("\\|");

                int maxLength = Math.min(textArray.length, countArray.length);

                for (int i = 0; i < maxLength; i++) {
                    Row row = sheet.createRow(rowIndex++);
                    // ID строки (или можно ставить индекс / ID элемента)
                    row.createCell(0).setCellValue(i + 1);
                    row.createCell(1).setCellValue(textArray[i]);
                    row.createCell(2).setCellValue(countArray[i]);
                }
            }
        }

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "Список из ЗАВХОЗА.xlsx");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            workbook.close();
            Toast.makeText(context, "Экспорт успешен! Файл сохранен в Загрузки", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- ИМПОРТ: Читает все листы книги и автоматически воссоздает тайтлы и строки ---
    @SuppressLint("NotifyDataSetChanged")
    private void importNotesFromExcel(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            Workbook workbook = WorkbookFactory.create(inputStream);

            int numberOfSheets = workbook.getNumberOfSheets();
            int importedCount = 0; // Счeтчик новых импортированных заметок

            // 1. Сразу получаем текущий полный список всех заметок из базы данных, чтобы с ними сравнивать
            List<Notes> existingNotes = database.mainDAO().getAll();

            for (int s = 0; s < numberOfSheets; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                String noteTitle = workbook.getSheetName(s);

                StringBuilder textTitlesBuilder = new StringBuilder();
                StringBuilder countTitlesBuilder = new StringBuilder();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String itemName = row.getCell(1) != null ? row.getCell(1).toString().trim() : "";
                    String itemAmount = row.getCell(2) != null ? row.getCell(2).toString().trim() : "";

                    if (itemName.isEmpty() && itemAmount.isEmpty()) continue;

                    if (textTitlesBuilder.length() > 0) {
                        textTitlesBuilder.append("|");
                        countTitlesBuilder.append("|");
                    }
                    textTitlesBuilder.append(itemName);
                    countTitlesBuilder.append(itemAmount);
                }

                String newTextTitle = textTitlesBuilder.toString();
                String newCountTitle = countTitlesBuilder.toString();

                // 2. Проверяем, есть ли уже точно такая же заметка в базе
                boolean isDuplicate = false;
                for (Notes existing : existingNotes) {
                    // Сравниваем заголовок, элементы и количество точь-в-точь
                    boolean titleEquals = (existing.getTitle() != null && existing.getTitle().equals(noteTitle));
                    boolean textEquals = (existing.getText_title() != null && existing.getText_title().equals(newTextTitle));
                    boolean countEquals = (existing.getCount_title() != null && existing.getCount_title().equals(newCountTitle));

                    if (titleEquals && textEquals && countEquals) {
                        isDuplicate = true; // Нашли полный дубликат!
                        break;
                    }
                }

                // 3. Если это не дубликат (такой записи еще нет), то сохраняем
                if (!isDuplicate) {
                    Notes newNote = new Notes();
                    newNote.setTitle(noteTitle);
                    newNote.setDate(new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date()));
                    newNote.setText_title(newTextTitle);
                    newNote.setCount_title(newCountTitle);

                    database.mainDAO().insert(newNote);

                    // Также добавляем в локальный список existingNotes, чтобы защититься от дубликатов
                    // внутри самого же импортируемого Excel-файла (если там повторяются листы)
                    existingNotes.add(newNote);

                    importedCount++;
                }
            }

            workbook.close();
            Toast.makeText(this, "Импорт завершен! Добавлено новых заметок: " + importedCount, Toast.LENGTH_LONG).show();
            isDataChanged = true;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка чтения файла: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isDataChanged) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}