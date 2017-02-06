package de.tum.tumattendancechecker.stores;

import android.net.Uri;

import java.util.Map;

import de.tum.tumattendancechecker.models.Lesson;

public class LessonGroupsStore extends AsyncListItemsStore<Lesson> {
    private long lessonId;

    public LessonGroupsStore(String jwtToken) {
        super(jwtToken);
    }

    @Override
    Uri.Builder getApiURI() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("tum-attendance-checker.appspot.com")
                .appendPath("lesson-groups");
        return builder;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    @Override
    Map<String, String> getQuery() {
        Map<String, String> queryData = super.getQuery();
        queryData.put("lessonId", Long.toString(lessonId));
        return queryData;
    }
}
