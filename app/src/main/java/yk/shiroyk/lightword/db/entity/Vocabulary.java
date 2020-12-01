package yk.shiroyk.lightword.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary",
        foreignKeys = {
                @ForeignKey(
                        entity = VocabType.class,
                        parentColumns = "id",
                        childColumns = "vtype_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        })
public class Vocabulary {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;
    @ColumnInfo(name = "word")
    private String word;
    @ColumnInfo(name = "vtype_id", index = true)
    private Long vtypeId;
    @ColumnInfo(name = "frequency")
    private Long frequency;
    @ColumnInfo(name = "total_correct", defaultValue = "0")
    private short totalCorrect;
    @ColumnInfo(name = "total_error", defaultValue = "0")
    private short totalError;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getVtypeId() {
        return vtypeId;
    }

    public void setVtypeId(Long vtypeId) {
        this.vtypeId = vtypeId;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public short getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(short totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public short getTotalError() {
        return totalError;
    }

    public void setTotalError(short totalError) {
        this.totalError = totalError;
    }

    public Vocabulary(VocabExercise vocabExercise) {
        this.id = vocabExercise.id;
        this.word = vocabExercise.word;
        this.vtypeId = vocabExercise.vtypeId;
        this.frequency = vocabExercise.frequency;
    }

    public Vocabulary(String word, Long vtypeId, Long frequency) {
        this.word = word;
        this.vtypeId = vtypeId;
        this.frequency = frequency;
    }
}
