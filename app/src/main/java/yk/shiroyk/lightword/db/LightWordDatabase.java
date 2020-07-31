package yk.shiroyk.lightword.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import yk.shiroyk.lightword.db.converter.DateConverter;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.dao.VocabDataDao;
import yk.shiroyk.lightword.db.dao.VocabTypeDao;
import yk.shiroyk.lightword.db.dao.VocabularyDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.db.entity.Vocabulary;

@Database(entities = {ExerciseData.class, VocabData.class, VocabType.class, Vocabulary.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class LightWordDatabase extends RoomDatabase {
    @VisibleForTesting
    public static final String DATABASE_NAME = "lightword";
    private static LightWordDatabase INSTANCE;

    public static LightWordDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LightWordDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            LightWordDatabase.class,
                            DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ExerciseDao exerciseDao();

    public abstract VocabDataDao vocabDataDao();

    public abstract VocabTypeDao vocabTypeDao();

    public abstract VocabularyDao vocabularyDao();
}