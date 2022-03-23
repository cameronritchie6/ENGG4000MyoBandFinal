package com.cmorrell.myobandcompanionapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);

    @Insert
    void insertUsers(User... users);

    @Update
    void updateUser(User user);

    @Update
    void updateUsers(User... users);

    @Delete
    void deleteUser(User user);

    @Delete
    void deleteUsers(User... users);

    @Query("SELECT * FROM user")
    List<User> getAll();
}
