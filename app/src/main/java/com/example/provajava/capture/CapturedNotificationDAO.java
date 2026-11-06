package com.example.provajava.capture;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CapturedNotificationDAO {

    @Insert
    long insert(TCapturedNotification n);

    @Query("SELECT * FROM T_CapturedNotification ORDER BY PostedAt DESC")
    List<TCapturedNotification> getAll();

    @Query("SELECT COUNT(*) FROM T_CapturedNotification")
    int count();

    // Apps often re-post the same notification (updates, re-alerts): used to skip copies
    @Query("SELECT COUNT(*) FROM T_CapturedNotification WHERE PackageName = :pkg "
            + "AND IFNULL(Title, '') = IFNULL(:title, '') AND IFNULL(Text, '') = IFNULL(:text, '') "
            + "AND PostedAt >= :since")
    int countSame(String pkg, String title, String text, long since);

    @Query("DELETE FROM T_CapturedNotification")
    void deleteAll();
}
