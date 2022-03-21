package com.cmorrell.myobandcompanionapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    @PrimaryKey
    public int id;

    @ColumnInfo(name = "electrode1")
    public double electrode1Value;

    @ColumnInfo(name = "electrode2")
    public double electrode2Value;
}
