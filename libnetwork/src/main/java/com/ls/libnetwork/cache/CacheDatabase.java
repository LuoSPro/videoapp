package com.ls.libnetwork.cache;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ls.libcommon.global.AppGlobals;

/**
 * Room数据库是通过注解的方式来实现相关的功能，在编译时通过annotationProcessor来生成一个他的实现类。
 * 这里将CacheDatabase写成一个抽象类，在运行的时候会生成一个CacheDatabase的实现类，这样我们就不用覆写RoomDatabase的相关方法了
 */
@Database(entities = {Cache.class},version = 1,exportSchema = true)//exportSchema:导出一个json文件，里面存放数据库的数据、sql语句等信息，如果设置为true，要给其指定json文件存储的位置
public abstract class CacheDatabase extends RoomDatabase {

    static {//初始化
        //创建一个内存数据库
        //但是这种数据库的数据只存在于内存中，也就是说进程被杀了之后，数据随之丢失
//        Room.inMemoryDatabaseBuilder()
        //所以我们选择这一种实现方式
        //构建出cacheDatebase
        sDatabase = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "videoapp_cache")
                .allowMainThreadQueries()//是否允许在主线程进行查询
//                .addCallback()//数据库被创建或者被打开的时候回调
//                .setQueryExecutor()//设置查询的线程池，默认会使用ArchTaskExecutor.getIOThreadExecutor();
//                .openHelperFactory()//SupportSQLiteOpenHelper的一个工厂类
//                .setJournalMode()//Room数据库的日志模式
//                .fallbackToDestructiveMigration()//数据库版本升级之后发生异常，然后回滚
//                .fallbackToDestructiveMigration()//数据库升级发生异常，根据传进去饿指定版本进行回滚
//                .addMigrations(CacheDatabase.sMigration)//传进去要升级的版本和操作
                .build();
    }

//    static Migration sMigration = new Migration(1,3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table teacher rename to student");
//            database.execSQL("alter table teacher add column teacher_age INTEGER NOT NULL default 0");
//        }
//    };

    private static CacheDatabase sDatabase;

    public static CacheDatabase get(){
        return sDatabase;
    }

    public abstract CacheDao getCacheDao();
}
