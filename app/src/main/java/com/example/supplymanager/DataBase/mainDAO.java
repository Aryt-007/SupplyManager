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
    @Insert(onConflict = REPLACE)
    void insert(Notes notes);

    @Query("SELECT * FROM notes ORDER BY pinned DESC, date DESC")
    List<Notes> getAll();

    @Query("UPDATE notes SET title = :title, notes = :notes, text_title = :text_title, count_title = :count_title WHERE ID = :ID")
    void update(int ID, String title, String notes, String text_title, String count_title);

    @Delete
    void delete(Notes notes);

    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id")
    void pin(int id, boolean pin);

    @Query("SELECT * FROM notes WHERE ID = :id")
    Notes getById(int id);
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
//@Dao
//public interface mainDAO {
//    @Insert (onConflict = REPLACE)
//    void insert (Notes notes);
//
//    // Измененный запрос: сначала закрепленные (pinned = 1), потом по дате
//    @Query("SELECT * FROM notes ORDER BY pinned DESC, title ASC")
//    List<Notes> getAll();
//
//    @Query("UPDATE notes SET title = :title, notes = :notes, text_title = :text_title, count_title = :count_title WHERE ID = :ID")
//    void update (int ID, String title, String notes, String text_title, String count_title);
//
//    @Delete
//    void delete (Notes notes);
//
//    @Query("UPDATE notes SET pinned = :pin WHERE ID = :id")
//    void pin (int id, boolean pin);
//}
