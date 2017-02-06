package de.tum.tumattendancechecker.stores;

import java.util.ArrayList;
import java.util.List;

import de.tum.tumattendancechecker.models.EventListener;

public class EventsStore {

    private List<EventListener> listeners;

    public EventsStore() {
        this.listeners = new ArrayList<>();
    }

    public void attachListener(EventListener listener) {
        listeners.add(listener);
    }

    void notifyProcessing() {
        for (int i = 0; i < listeners.size(); i += 1) {
            listeners.get(i).onProcessing(null);
        }
    }

    void notifySuccess(String token) {
        for (int i = 0; i < listeners.size(); i += 1) {
            listeners.get(i).onFinished(token);
        }
    }

    void notifyError(Exception error) {
        for (int i = 0; i < listeners.size(); i += 1) {
            listeners.get(i).onError(error);
        }
    }

}
