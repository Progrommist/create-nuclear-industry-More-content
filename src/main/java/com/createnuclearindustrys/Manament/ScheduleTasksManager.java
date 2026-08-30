package com.createnuclearindustrys.Manament;

import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Utills.*;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTasksManager {
    private final ArrayList<Task> tasks = new ArrayList<>();
    private ArrayList<DelayedTask> delayedTasks = new ArrayList<>();

    public void init(List<TaskCreator> newTasks) {
        tasks.clear();

        tasks.addLast(new Task(level -> configRefresh(), 0,
                new ConfigTaskCache(new TaskCreator(null, () -> 40, () -> true))));

        for (int i = 0; i < newTasks.size(); i++){
            tasks.addLast(new Task(newTasks.get(i).method, i, new ConfigTaskCache(newTasks.get(i))));
        }
    }

    public void tick(ServerLevel level) {
        for (Task task : tasks) {
            task.ticks++;

        }

        boolean hadWorked = false;
        for (Task task : tasks) {
            if (task.ticks >= task.configTaskCache.currentDelay) {
                task.ticks = 0;

                if (!hadWorked || task.configTaskCache.currentPriority) {
                    CreateNuclearIndustrys.LOGGER.info("task executed: {} {}", task.configTaskCache.currentPriority);
                    task.method.execute(level);

                    hadWorked = true;
                }
                else {

                    delayedTasks.add(new DelayedTask(
                            task.configTaskCache.currentDelay <= tasks.size(),
                            tasks.indexOf(task)
                    ));

                    CreateNuclearIndustrys.LOGGER.info("task delayed: {}", tasks.indexOf(task));
                }
            }
        }
        ArrayList<DelayedTask> newDelayedTasks = (ArrayList<DelayedTask>) delayedTasks.clone();
        for (DelayedTask task : delayedTasks) {
            if (!hadWorked || task.currentPriority) {
                tasks.get(task.taskIndex).method.execute(level);

                newDelayedTasks.remove(task);
            }
        }
        delayedTasks = (ArrayList<DelayedTask>) newDelayedTasks.clone();
    }
    private void configRefresh() {
        for (Task i : tasks) {
            i.configTaskCache.refresh();
        }
    }
}