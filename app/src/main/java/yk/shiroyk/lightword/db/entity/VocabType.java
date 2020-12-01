package yk.shiroyk.lightword.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocab_type")
public class VocabType {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;
    @ColumnInfo(name = "vocabtype", index = true)
    private String vocabtype;
    @ColumnInfo(name = "alias")
    private String alias;
    @ColumnInfo(name = "amount", defaultValue = "0")
    private Integer amount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVocabtype() {
        return vocabtype;
    }

    public void setVocabtype(String vocabtype) {
        this.vocabtype = vocabtype;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void upAmount(Integer amount) {
        this.amount += amount;
    }

    @Override
    public String toString() {
        return vocabtype.isEmpty() ? alias : vocabtype + " (" + amount + ")";
    }
}
