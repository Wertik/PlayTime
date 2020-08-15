package space.devport.wertik.playtime;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;

public class PlayTimeCommons {
    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }
}