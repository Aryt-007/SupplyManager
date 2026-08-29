package com.example.supplymanager;

import androidx.cardview.widget.CardView;

import com.example.supplymanager.Models.Notes;

public interface NotesClickListener {

    void onClick (Notes notes);
    void onLongClick (Notes notes, CardView cardView);

}
