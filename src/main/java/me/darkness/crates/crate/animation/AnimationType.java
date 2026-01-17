package me.darkness.crates.crate.animation;

import java.util.Arrays;
import java.util.Optional;

public enum AnimationType {

    ROULETTE("roulette"),
    WITHOUT_ANIMATION("without_animation");

    private final String id;

    AnimationType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static Optional<AnimationType> fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.id.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value))
            .findFirst();
    }
}
