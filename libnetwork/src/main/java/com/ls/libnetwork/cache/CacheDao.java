package com.ls.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CacheDao {

    //插入数据库的时候，发生了冲突，应该怎么办
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Cache cache);


    @Query("select * from cache where `key` =: key")
    Cache getCache(String key);

    @Delete
    int delete(Cache cache);

    //发生冲突时的解决策略
    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Cache cache);
}
