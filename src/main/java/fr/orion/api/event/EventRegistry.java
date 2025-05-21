package fr.orion.api.event;

import net.dv8tion.jda.api.hooks.EventListener;

public interface EventRegistry {
    void registerListener(EventListener listener);
    void unregisterListener(EventListener listener);
    void registerListeners(EventListener... listeners);
    void unregisterListeners(EventListener... listeners);
}
