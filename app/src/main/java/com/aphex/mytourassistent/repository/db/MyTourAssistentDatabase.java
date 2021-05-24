package com.aphex.mytourassistent.repository.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.aphex.mytourassistent.repository.db.dao.GeoPointsActualDAO;
import com.aphex.mytourassistent.repository.db.dao.GeoPointsPlannedDAO;
import com.aphex.mytourassistent.repository.db.dao.PhotoDAO;
import com.aphex.mytourassistent.repository.db.dao.ToursDAO;
import com.aphex.mytourassistent.repository.db.entities.GeoPointActual;
import com.aphex.mytourassistent.repository.db.entities.GeoPointPlanned;
import com.aphex.mytourassistent.repository.db.entities.Photo;
import com.aphex.mytourassistent.repository.db.entities.Tour;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Tour.class, GeoPointPlanned.class, GeoPointActual.class, Photo.class}, version = 1, exportSchema = false )
public abstract class MyTourAssistentDatabase extends RoomDatabase {

    public abstract ToursDAO toursDAO();

    public abstract GeoPointsPlannedDAO geoPointsPlannedDAO();

    public abstract GeoPointsActualDAO geoPointsActualDAO();

    public abstract PhotoDAO photoDAO();

    // volatile: har sammenheng med multithreading. Sikrer at alle tråder ser samme kopi av INSTANCE.
    private static volatile MyTourAssistentDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MyTourAssistentDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MyTourAssistentDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        MyTourAssistentDatabase.class, "tours_database")
                        .allowMainThreadQueries()
                        .addCallback(sRoomDatabaseCallback)
                        .build();
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        /**
         * Called when the database is created for the first time.
         * This is called after all the tables are created.
         * @param db
         */
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

    // MIGRERING/ENDRING I DATABASEN.
    /*
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user "
                    + " ADD COLUMN birth_year INTEGER");
        }
    };
    */

}
