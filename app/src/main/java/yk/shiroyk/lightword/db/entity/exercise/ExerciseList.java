package yk.shiroyk.lightword.db.entity.exercise;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExerciseList {
    @SerializedName("pronounce")
    @Expose
    private List<String> pronounce;
    @SerializedName("inflection")
    @Expose
    private List<String> inflection;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("collocation")
    @Expose
    private List<Collocation> collocation;

    public List<String> getPronounce() {
        return pronounce;
    }

    public String getPronounceString() {
        return pronounce.toString().replaceAll("[\\[\\]]", "");
    }

    public void setPronounce(List<String> pronounce) {
        this.pronounce = pronounce;
    }

    public List<String> getInflection() {
        return inflection;
    }

    public void setInflection(List<String> inflection) {
        this.inflection = inflection;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Collocation> getCollocation() {
        return collocation;
    }

    public void setCollocation(List<Collocation> collocation) {
        this.collocation = collocation;
    }

}
