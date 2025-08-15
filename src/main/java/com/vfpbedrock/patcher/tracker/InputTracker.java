package com.vfpbedrock.patcher.tracker;

import com.vfpbedrock.patcher.protocol.enums.ClientInputLocksFlag;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InputTracker extends StoredObject {
    private Set<ClientInputLocksFlag> flags = new HashSet<>();

    public InputTracker(UserConnection user) {
        super(user);
    }

    public boolean has(ClientInputLocksFlag flag) {
        return this.flags.contains(flag);
    }

    public void flags(Set<ClientInputLocksFlag> flags) {
        System.out.println(Arrays.toString(flags.toArray()));
        this.flags = flags;
    }
}
