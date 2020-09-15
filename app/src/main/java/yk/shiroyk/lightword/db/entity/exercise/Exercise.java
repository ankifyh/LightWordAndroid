package yk.shiroyk.lightword.db.entity.exercise;

import java.util.List;

public class Exercise {
    private Long id;
    private Long vtypeId;
    private boolean status;
    private String word;
    private String meaning;
    private String pronounce;
    private String partOfSpeech;
    private List<String> inflection;
    private String sentence;
    private String translation;
    private String answer;
    private Integer answerIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVtypeId() {
        return vtypeId;
    }

    public void setVtypeId(Long vtypeId) {
        this.vtypeId = vtypeId;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getPronounce() {
        return pronounce;
    }

    public Boolean hasPronounce() {
        return pronounce != null;
    }

    public void setPronounce(String pronounce) {
        this.pronounce = pronounce;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public List<String> getInflection() {
        return inflection;
    }

    public void setInflection(List<String> inflection) {
        this.inflection = inflection;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(Integer answerIndex) {
        this.answerIndex = answerIndex;
    }
}
