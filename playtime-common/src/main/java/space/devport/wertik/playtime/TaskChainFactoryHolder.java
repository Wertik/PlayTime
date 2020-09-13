package space.devport.wertik.playtime;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

public abstract class TaskChainFactoryHolder {

    @Getter
    @Setter
    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        Validate.notNull(taskChainFactory, "No task chain factory");
        return taskChainFactory.newChain();
    }
}