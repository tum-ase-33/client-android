package de.tum.tumattendancechecker.stores;

import android.net.Uri;

import de.tum.tumattendancechecker.models.Lesson;

public class LessonsStore extends AsyncListItemsStore<Lesson> {
    public LessonsStore(String jwtToken) {
        super(jwtToken);
    }

    @Override
    Uri.Builder getApiURI() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("tum-attendance-checker.appspot.com")
                .appendPath("lessons");
        return builder;
    }
}
