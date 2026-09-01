package com.example.supplymanager.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.supplymanager.Models.Notes;

@Database(entities = {Notes.class}, version = 1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;
    private static final String DATABASE_NAME = "NotesApp";

    public static synchronized RoomDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                            RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries() // Только для тестирования!
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract mainDAO mainDAO();
}