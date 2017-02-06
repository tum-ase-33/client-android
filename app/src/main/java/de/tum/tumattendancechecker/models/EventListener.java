package de.tum.tumattendancechecker.models;

public interface EventListener {

    void onProcessing(Object data);

    void onFinished(Object result);

    void onError(Exception e);

}
