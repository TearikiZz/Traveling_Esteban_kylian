package com.kcorteel.travel_esteban_kylian.travelshare.annotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationSuggestion {

    private final String summary;
    private final List<String> tags;

    public AnnotationSuggestion(String summary, List<String> tags) {
        this.summary = summary == null ? "" : summary.trim();
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public boolean isEmpty() {
        return summary.isEmpty() && tags.isEmpty();
    }
}
