package com.vasimvahabov.stockmarketsimulator.constant;

import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum Role {

    ADMIN(1),

    TRADER(2);

    int id;

    Role(int id) {
        this.id = id;
    }

    public static Role roleById(int id) {
        return Stream.of(Role.values())
                .filter(role -> role.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Role not found with id %s", id)));
    }

}
