package com.enderio.machines.common.init;

import com.enderio.machines.common.events.AnvilUpdateEvent;

public class MachineEvents {
    public static void register() {
        AnvilUpdateEvent.initAnvilRecipes();
    }
}
