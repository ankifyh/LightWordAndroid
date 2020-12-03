/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import yk.shiroyk.lightword.db.converter.DateConverter;
import yk.shiroyk.lightword.db.converter.OrderConverter;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.dao.UserStatisticDao;
import yk.shiroyk.lightword.db.dao.VocabTypeDao;
import yk.shiroyk.lightword.db.dao.VocabularyDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.UserStatistic;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.db.entity.Vocabulary;

@Database(entities = {ExerciseData.class,
        VocabType.class,
        Vocabulary.class,
        UserStatistic.class}, version = 4)
@TypeConverters({DateConverter.class, OrderConverter.class})
public abstract class LightWordDatabase extends RoomDatabase {
    @VisibleForTesting
    public static final String DATABASE_NAME = "lightword";
    private static LightWordDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE user_statistic "
                    + " (timestamp INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + " correct INTEGER,"
                    + " wrong INTEGER,"
                    + " count INTEGER,"
                    + " PRIMARY KEY(timestamp))");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // merge vocabulary and vocab_data tables
            // create new table
            database.execSQL("CREATE TABLE new_vocabulary "
                    + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " word TEXT, "
                    + " vtype_id INTEGER, "
                    + " frequency INTEGER DEFAULT 99999, "
                    + " total_correct INTEGER NOT NULL DEFAULT 0, "
                    + " total_error INTEGER NOT NULL DEFAULT 0, "
                    + " FOREIGN KEY(vtype_id) REFERENCES vocab_type(id)"
                    + " ON UPDATE CASCADE ON DELETE CASCADE )");

            database.execSQL("CREATE INDEX index_vocabulary_vtype_id ON new_vocabulary (vtype_id)");

            // copy old table data
            database.execSQL("INSERT INTO new_vocabulary (word, vtype_id, frequency) "
                    + " SELECT vocabulary.word, vocab_data.vtype_id, vocabulary.frequency"
                    + " FROM vocabulary, vocab_data"
                    + " WHERE vocabulary.id = vocab_data.word_id");

            // drop old table or can rename to *_bk
            database.execSQL("DROP TABLE vocab_data");

            database.execSQL("DROP TABLE vocabulary");

            // rename new table
            database.execSQL("ALTER TABLE new_vocabulary RENAME TO vocabulary");

        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // update all mastered vocabulary exercise data
            database.execSQL("UPDATE exercise_data SET stage = 99, timestamp = 0 WHERE stage = 11");
        }
    };

    public static LightWordDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LightWordDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            LightWordDatabase.class,
                            DATABASE_NAME
                    )
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ExerciseDao exerciseDao();

    public abstract VocabTypeDao vocabTypeDao();

    public abstract VocabularyDao vocabularyDao();

    public abstract UserStatisticDao userStatisticDao();
}