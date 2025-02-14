package com.owlmaddie.goals;

/**
 * The {@code GoalPriority} enum sets the priorities of each type of custom Goal used in this mod.
 * For example, talking to a player is higher priority than following a player.
 */
public enum GoalPriority {
    // Enum constants (Goal Types) with their corresponding priority values
    TALK_PLAYER(2),
    PROTECT_PLAYER(2),
    LEAD_PLAYER(3),
    FOLLOW_PLAYER(3),
    FLEE_PLAYER(3),
    ATTACK_PLAYER(3);

    private final int priority;

    // Constructor for the enum to set the priority value
    GoalPriority(int priority) {
        this.priority = priority;
    }

    // Getter method to access the priority value
    public int getPriority() {
        return this.priority;
    }
}
