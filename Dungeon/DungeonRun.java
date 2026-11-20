package Systems.Dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A live playthrough of one blueprint. Silent: no printing, no input, no battles.
 * The app asks for the current encounter, runs the fight, then reports the result.
 *
 *   AWAITING_ENCOUNTER --decline()--> DECLINED --fightAnyway()--> AWAITING_ENCOUNTER
 *   AWAITING_ENCOUNTER / DECLINED --reportResult()/retreat()--> ...
 *   (last encounter resolved) --> REST_AREA --advanceFloor()--> next floor, or VICTORY after the last
 *   REST_AREA --exit()--> EXITED
 *
 * Calling an operation in the wrong state throws IllegalStateException.
 */
public final class DungeonRun {

    public enum State { AWAITING_ENCOUNTER, DECLINED, REST_AREA, ENDED }
    public enum Outcome { IN_PROGRESS, VICTORY, EXITED, DEFEATED }

    private final DungeonBlueprint blueprint;
    private final DungeonListener listener;

    private State state = State.AWAITING_ENCOUNTER;
    private Outcome outcome = Outcome.IN_PROGRESS;
    private int floorIndex = 0;
    private List<Encounter> encounters;
    private int encounterIndex = 0;
    private boolean bossWonOnFloor = false;

    DungeonRun(DungeonBlueprint blueprint, DungeonListener listener) {
        this.blueprint = Objects.requireNonNull(blueprint, "blueprint");
        this.listener = listener == null ? DungeonListener.NONE : listener;
        loadFloor();
    }

    // ---- queries --------------------------------------------------------

    public DungeonBlueprint blueprint() { return blueprint; }
    public String dungeonName()         { return blueprint.name(); }
    public State state()                { return state; }
    public Outcome outcome()            { return outcome; }
    public boolean isEnded()            { return state == State.ENDED; }
    public int floorCount()             { return blueprint.floorCount(); }
    public boolean isLastFloor()        { return floorIndex == blueprint.floorCount() - 1; }

    /** Current floor number, 1-based. Stays on the final floor after the run ends. */
    public int floorNumber()            { return floorIndex + 1; }

    /** The pending encounter. Only valid while awaiting or after declining. */
    public Encounter currentEncounter() {
        requireState("currentEncounter", State.AWAITING_ENCOUNTER, State.DECLINED);
        return encounters.get(encounterIndex);
    }

    // ---- operations -----------------------------------------------------

    /** Player said no to the fight. The app then offers fightAnyway() or retreat(); the run never re-asks by itself. */
    public void decline() {
        requireState("decline", State.AWAITING_ENCOUNTER);
        state = State.DECLINED;
    }

    /** After declining, the player chose to fight after all. */
    public void fightAnyway() {
        requireState("fightAnyway", State.DECLINED);
        state = State.AWAITING_ENCOUNTER;
    }

    /** Leave the dungeon instead of fighting. Ends the run as EXITED. */
    public void retreat() {
        requireState("retreat", State.AWAITING_ENCOUNTER, State.DECLINED);
        end(Outcome.EXITED);
    }

    /**
     * Report how the current fight went.
     * WON: rewards are the app's job; the run moves on.
     * FLED: no rewards, encounter skipped; throws if the encounter cannot be fled (dungeon boss).
     * DEFEATED: the run ends as DEFEATED.
     */
    public void reportResult(EncounterResult result) {
        Objects.requireNonNull(result, "result");
        requireState("reportResult", State.AWAITING_ENCOUNTER, State.DECLINED);
        Encounter e = encounters.get(encounterIndex);

        switch (result) {
            case DEFEATED -> { end(Outcome.DEFEATED); return; }
            case FLED -> {
                if (!e.canFlee())
                    throw new IllegalStateException("Cannot flee from the dungeon boss: " + e.spec().name());
            }
            case WON -> { if (e.isBoss()) bossWonOnFloor = true; }
        }

        encounterIndex++;
        if (encounterIndex < encounters.size()) {
            state = State.AWAITING_ENCOUNTER;
        } else {
            state = State.REST_AREA;
            if (bossWonOnFloor) listener.floorCleared(blueprint.name(), floorNumber());
        }
    }

    /** Rest area only. After the last floor this ends the run as VICTORY. */
    public void advanceFloor() {
        requireState("advanceFloor", State.REST_AREA);
        if (isLastFloor()) {
            end(Outcome.VICTORY);
        } else {
            floorIndex++;
            loadFloor();
        }
    }

    /** Rest area only: leave the dungeon. Ends the run as EXITED. */
    public void exit() {
        requireState("exit", State.REST_AREA);
        end(Outcome.EXITED);
    }

    // ---- internals ------------------------------------------------------

    private void loadFloor() {
        FloorBlueprint f = blueprint.floors().get(floorIndex);
        List<Encounter> list = new ArrayList<>();
        int total = f.encounterCount();
        int pos = 1;
        for (MonsterSpec m : f.monsters()) list.add(new Encounter(m, pos++, total));
        list.add(new Encounter(f.boss(), pos, total));
        encounters = List.copyOf(list);
        encounterIndex = 0;
        bossWonOnFloor = false;
        state = State.AWAITING_ENCOUNTER;
    }

    private void end(Outcome result) {
        state = State.ENDED;
        outcome = result;
        listener.dungeonEnded(blueprint.name(), result);
    }

    private void requireState(String op, State... allowed) {
        for (State s : allowed) if (state == s) return;
        throw new IllegalStateException(op + "() is not allowed while the run is " + state
                + " (floor " + floorNumber() + " of " + blueprint.name() + ")");
    }
}