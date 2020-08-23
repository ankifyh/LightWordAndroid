package yk.shiroyk.lightword.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "exercise_data",
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
public class ExerciseData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;
    @ColumnInfo(name = "word_id", index = true)
    private Long wordId;
    @ColumnInfo(name = "vtype_id", index = true)
    private Long vtypeId;
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    private Date timestamp;
    @ColumnInfo(name = "last_practice", defaultValue = "CURRENT_TIMESTAMP")
    private Date lastPractice;
    @ColumnInfo(name = "stage", defaultValue = "1")
    private Integer stage;
    @ColumnInfo(name = "correct", defaultValue = "1")
    private Integer correct;
    @ColumnInfo(name = "wrong", defaultValue = "0")
    private Integer wrong;

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getLastPractice() {
        return lastPractice;
    }

    public void setLastPractice(Date lastPractice) {
        this.lastPractice = lastPractice;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public Integer getWrong() {
        return wrong;
    }

    public void setWrong(Integer wrong) {
        this.wrong = wrong;
    }

    public String toMasterString() {
        return "单词ID: " + wordId +
                ", 种类ID: " + vtypeId +
                "\n复习阶段: " + "已掌握" +
                "\n正确次数: " + correct +
                ", 错误次数: " + wrong;
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd  HH:mm");
        return "单词ID: " + wordId +
                ", 种类ID: " + vtypeId +
                "\n下次复习时间: " + df.format(timestamp) +
                "\n最后复习时间: " + df.format(lastPractice) +
                "\n复习阶段: " + stage + "/10" +
                "\n正确次数: " + correct +
                ", 错误次数: " + wrong;
    }
}
