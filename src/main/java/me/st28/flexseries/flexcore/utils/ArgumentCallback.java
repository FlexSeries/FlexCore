package me.st28.flexseries.flexcore.utils;

import me.st28.flexseries.flexcore.utils.TaskChain.GenericTask;
import org.bukkit.Bukkit;

/**
 * A future task to be run once something finishes executing.
 *
 * @param <T> The type of argument the callback requires for the {@link #callback(Object)} method.
 */
public abstract class ArgumentCallback<T> {

    public static <T> void callCallback(ArgumentCallback<T> callback,  T argument) {
        if (callback != null) {
            callback.run(argument);
        }
    }

    /**
     * Runs the callback.
     */
    public void run(final T argument) {
        if (isSynchronous() && !Bukkit.isPrimaryThread()) {
            new TaskChain().add(new GenericTask() {
                @Override
                protected void run() {
                    callback(argument);
                }
            }).execute();
        } else {
            callback(argument);
        }
    }

    /**
     * Specifies whether or not the callback should be run synchronously.
     *
     * @return True to run the callback synchronously.<br />
     *         False to run the callback in whatever thread the executor is in.
     */
    public abstract boolean isSynchronous();

    /**
     * Runs the callback.
     *
     * @param argument An argument from the executor that the callback requires.
     */
    public abstract void callback(T argument);

}