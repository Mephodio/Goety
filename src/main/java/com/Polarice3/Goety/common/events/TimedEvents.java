package com.Polarice3.Goety.common.events;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.utils.EventTask;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * Timed Task events based on @Shadows-of-Fire Placebo codes: <a href="https://github.com/Shadows-of-Fire/Placebo/blob/1.20/src/main/java/dev/shadowsoffire/placebo/util/PlaceboTaskQueue.java">...</a>
 */
@Mod.EventBusSubscriber(modid = Goety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimedEvents {

    private static final Queue<Pair<String, EventTask>> TASKS = new ArrayDeque<>();

    @SubscribeEvent
    public static void ServerTickEvents(TickEvent.ServerTickEvent event){
        if (event.phase == TickEvent.Phase.END) {
            Iterator<Pair<String, EventTask>> it = TASKS.iterator();
            Pair<String, EventTask> current;
            while (it.hasNext()) {
                current = it.next();
                try {
                    if (current.getRight().getAsBoolean()) {
                        current.getRight().endTask();
                        it.remove();
                    } else {
                        current.getRight().tickTask();
                    }
                } catch (Exception ex) {
                    Goety.LOGGER.error("An exception occurred while running a ticking task with ID {}.  It will be terminated.", current.getLeft());
                    it.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        for (Pair<String, EventTask> pair : TASKS){
            if (pair.getRight() != null) {
                pair.getRight().endTask();
            }
        }
        TASKS.clear();
    }

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        TASKS.clear();
    }

    public static void submitTask(String id, EventTask task) {
        TASKS.add(Pair.of(id, task));
        task.startTask();
    }
}
