package yk.shiroyk.lightword.db.entity.exercise;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Collocation {
    @SerializedName("meaning")
    @Expose
    private String meaning;
    @SerializedName("part of speech")
    @Expose
    private String partOfSpeech;
    @SerializedName("examplelist")
    @Expose
    private List<Example> example;

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public List<Example> getExample() {
        return example;
    }

    public void setExample(List<Example> example) {
        this.example = example;
    }

}
