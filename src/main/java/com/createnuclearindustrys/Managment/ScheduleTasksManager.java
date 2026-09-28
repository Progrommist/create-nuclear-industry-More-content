package com.createnuclearindustrys.Managment;

import com.createnuclearindustrys.Config;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Utils.Tasks.ConfigTaskCache;
import com.createnuclearindustrys.Utils.Tasks.DelayedTask;
import com.createnuclearindustrys.Utils.Tasks.Task;
import com.createnuclearindustrys.Utils.Tasks.TaskCreator;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class ScheduleTasksManager {
    public static final List<ScheduleTasksManager> instances = new ArrayList<>();

    private final ArrayList<Task> tasks = new ArrayList<>();
    private ArrayList<DelayedTask> delayedTasks = new ArrayList<>();
    private boolean isDebugging;

    public ScheduleTasksManager() {
    }

    public void init(List<TaskCreator> newTasks) {
        instances.add(this);

        tasks.clear();

        for (int i = 0; i < newTasks.size(); i++) {
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
                    CreateNuclearIndustrys.LOGGER.info(level.dimension().location() + ": delayed task id executed: {}", task.taskIndex);
            } else {
                if (isDebugging)
                    CreateNuclearIndustrys.LOGGER.info(level.dimension().location() + ": delayed task id delayed: {}", task.taskIndex);
            }
        }
        delayedTasks = (ArrayList<DelayedTask>) newDelayedTasks.clone();

        for (Task task : tasks) {
            if (task.ticks >= task.configTaskCache.currentDelay && task.configTaskCache.currentDelay != -1) {
                task.ticks = 0;

                if (!hadWorked || task.configTaskCache.currentPriority) {
                    task.method.execute(level);

                    hadWorked = !task.configTaskCache.currentPriority;
                    if (isDebugging)
                        CreateNuclearIndustrys.LOGGER.info(level.dimension().location() + ": task id executed: {}", tasks.indexOf(task));
                } else {

                    delayedTasks.add(new DelayedTask(
                            task.configTaskCache.currentDelay <= tasks.size(), // if task delay less than tasks count, in 1 tick we sometimes do several tasks
                            tasks.indexOf(task)
                    ));
                    if (isDebugging)
                        CreateNuclearIndustrys.LOGGER.info(level.dimension().location() + ": task id delayed: {}", tasks.indexOf(task));
                }
            }
        }

        for (Task task : tasks) {
            task.ticks++;

        }
    }

    void configRefresh() {
        for (Task i : tasks) {
            i.configTaskCache.refresh();
        }
        isDebugging = Config.CONSOLE_DEBUG.get();

        RadiationManager.configRefresh();
    }

    public void doConfigRefreshNextTime() {
        delayedTasks.add(new DelayedTask(
                true,
                0
        ));
    }
}