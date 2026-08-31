package com.createnuclearindustrys.Manament;

import com.createnuclearindustrys.Config;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Utills.Tasks.ConfigTaskCache;
import com.createnuclearindustrys.Utills.Tasks.DelayedTask;
import com.createnuclearindustrys.Utills.Tasks.Task;
import com.createnuclearindustrys.Utills.Tasks.TaskCreator;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTasksManager {
    private final ArrayList<Task> tasks = new ArrayList<>();
    private ArrayList<DelayedTask> delayedTasks = new ArrayList<>();
    private boolean isDebugging;

    public void init(List<TaskCreator> newTasks) {
        tasks.clear();

        tasks.addLast(new Task(level -> configRefresh(), 0,
                new ConfigTaskCache(new TaskCreator(null, () -> 100, () -> true))));

        for (int i = 0; i < newTasks.size(); i++){
            tasks.addLast(new Task(newTasks.get(i).method, i, new ConfigTaskCache(newTasks.get(i))));
        }
    }

    public void tick(ServerLevel level) {
        boolean hadWorked = false;

        ArrayList<DelayedTask> newDelayedTasks = (ArrayList<DelayedTask>) delayedTasks.clone();
        for (DelayedTask task : delayedTasks) {
            if (!hadWorked || task.currentPriority) {
                tasks.get(task.taskIndex).method.execute(level);

                newDelayedTasks.remove(task);

                hadWorked = !task.currentPriority;

                if (isDebugging)
                    CreateNuclearIndustrys.LOGGER.info(level.dimension().location().toString() + ": delayed task id executed: {}", task.taskIndex);
            }
            else {
                if (isDebugging)
                    CreateNuclearIndustrys.LOGGER.info(level.dimension().location().toString() + ": delayed task id delayed: {}", task.taskIndex);
            }
        }
        delayedTasks = (ArrayList<DelayedTask>) newDelayedTasks.clone();

        for (Task task : tasks) {
            if (task.ticks >= task.configTaskCache.currentDelay) {
                task.ticks = 0;

                if (!hadWorked || task.configTaskCache.currentPriority) {
                    task.method.execute(level);

                    hadWorked = !task.configTaskCache.currentPriority;
                    if (isDebugging)
                        CreateNuclearIndustrys.LOGGER.info(level.dimension().location().toString() + ": task id executed: {}", tasks.indexOf(task));
                }
                else {

                    delayedTasks.add(new DelayedTask(
                            task.configTaskCache.currentDelay <= tasks.size(),
                            tasks.indexOf(task)
                    ));
                    if (isDebugging)
                        CreateNuclearIndustrys.LOGGER.info(level.dimension().location().toString() + ": task id delayed: {}", tasks.indexOf(task));
                }
            }
        }

        for (Task task : tasks) {
            task.ticks++;

        }
    }
    private void configRefresh() {
        for (Task i : tasks) {
            i.configTaskCache.refresh();
        }
        isDebugging = Config.CONSOLE_DEBUG.get();
    }
}