package fr.orion.api.event;

import net.dv8tion.jda.api.hooks.EventListener;

public interface EventRegistry {
    /**
     * Register a listener to receive events.
     *
     * @param listener The listener to register
     */
    void registerListener(EventListener listener);
    /**
     * Unregister a listener to stop receiving events.
     *
     * @param listener The listener to unregister
     */
    void unregisterListener(EventListener listener);
    /**
     * Register multiple listeners to receive events.
     *
     * @param listeners The listeners to register
     */
    void registerListeners(EventListener... listeners);
    /**
     * Unregister multiple listeners to stop receiving events.
     *
     * @param listeners The listeners to unregister
     */
    void unregisterListeners(EventListener... listeners);
}
