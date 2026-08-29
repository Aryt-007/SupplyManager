package com.example.supplymanager.DataBase;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.supplymanager.Models.Notes;

import java.util.List;

@Dao
public interface mainDAO {
    @Insert (onConflict = REPLACE)
    void insert (Notes notes);

    // Измененный запрос: сначала закрепленные (pinned = 1), потом по дате
    @Query("SELECT * FROM notes ORDER BY pinned DESC, title ASC")
    List<Notes> getAll();

    @Query("UPDATE notes SET title = :title, notes = :notes WHERE ID = :ID")
    void update (int ID, String title, String notes);

    @Delete
    void delete (Notes notes);

    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id")
    void pin (int id, boolean pin);
}

//package com.example.supplymanager.DataBase;
//
//import static androidx.room.OnConflictStrategy.REPLACE;
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//
//import com.example.supplymanager.Models.Notes;
//
//import java.util.List;
//
//@Dao // (Data Access Object — Объект доступа к данным) Интерфейс для работы с локальной базой данных
//public interface mainDAO {
//    @Insert (onConflict = REPLACE)
//    void insert (Notes notes); // Метод чтобы вставлять данные в таблицу
//
//    // Query - это SQL запрос
//    @Query("SELECT * FROM notes ORDER BY id DESC") // Вывести таблицу от Я до А
//    List<Notes> getAll();
//
//    @Query("UPDATE notes SET title = :title, notes = :notes WHERE ID = :ID") // Если при обновлении таблицы notes совпадают ID, то обновляется title и notes
//    void update (int ID, String title, String notes);
//
//    @Delete
//    void delete (Notes notes); // Удаление записей или заметок
//    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id")
//    void pin (int id, boolean pin);
//
//}
