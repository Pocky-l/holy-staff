package com.pockyl.holy_staff.skill;

/**
 * Per-player cooldowns of the skills, as game times (of the side that owns this copy) when each skill becomes ready
 * again. The server copy is authoritative; the client copy is filled from {@code CooldownPayload} for the HUD.
 */
public final class SkillCooldowns {
    private final long[] readyAt = new long[Skill.values().length];
    private final int[] total = new int[Skill.values().length];

    public boolean isReady(Skill skill, long gameTime) {
        return gameTime >= readyAt[skill.ordinal()];
    }

    /** Game time when the skill became (or becomes) ready. */
    public long readyAt(Skill skill) {
        return readyAt[skill.ordinal()];
    }

    /** Length of the last started cooldown, for drawing its progress. */
    public int total(Skill skill) {
        return total[skill.ordinal()];
    }

    public void start(Skill skill, long gameTime, int ticks) {
        readyAt[skill.ordinal()] = gameTime + ticks;
        total[skill.ordinal()] = ticks;
    }
}
