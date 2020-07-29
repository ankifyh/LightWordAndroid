package yk.shiroyk.lightword.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocab_data",
        foreignKeys = {
                @ForeignKey(
                        entity = Vocabulary.class,
                        parentColumns = "id",
                        childColumns = "word_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = VocabType.class,
                        parentColumns = "id",
                        childColumns = "vtype_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        })
public class VocabData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;
    @ColumnInfo(name = "word_id", index = true)
    private Long wordId;
    @ColumnInfo(name = "vtype_id", index = true)
    private Long vtypeId;
    @ColumnInfo(name = "frequency", defaultValue = "99999")
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

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
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

}
