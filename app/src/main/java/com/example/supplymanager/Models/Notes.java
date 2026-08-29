package com.example.supplymanager.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Notes implements Serializable { // Таблица для хранения данных
    @PrimaryKey(autoGenerate = true) // Чтобы ключ автоматически присваивался PrimaryKey
    int ID = 0; // Первичный ключ в таблице

    @ColumnInfo(name = "title")
    String title = ""; // Название записи

    @ColumnInfo(name = "notes")
    String notes = ""; // Сами заметка

    @ColumnInfo(name = "date")
    String date = ""; // Дата записи заметки

    @ColumnInfo(name = "pinned")
    boolean pinned = false; // Возможность закреплять самые важные заметки

    // Getter and Setter всех колонок таблицы
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
