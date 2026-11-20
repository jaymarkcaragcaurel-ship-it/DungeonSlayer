package Systems.Dungeon;

import Data.DungeonData;
import Systems.Dungeon.DungeonRun.Outcome;
import Systems.Dungeon.DungeonRun.State;
import Systems.Dungeon.MonsterSpec.Kind;

import java.util.*;
import java.util.function.Function;

/** Assertion-based tests for the dungeon system. Run: java Systems.Dungeon.DungeonDemo [seed] */
public final class DungeonDemo {

    private DungeonDemo() {}

    /** Records listener events so tests can count them. */
    private static final class Log implements DungeonListener {
        final List<String> cleared = new ArrayList<>();
        final List<Outcome> ended = new ArrayList<>();
        @Override public void floorCleared(String d, int f) { cleared.add(d + "#" + f); }
        @Override public void dungeonEnded(String d, Outcome o) { ended.add(o); }
    }

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.nanoTime();
        System.out.println("Seed: " + seed);

        sampleDungeons(seed);
        generationTest();
        independenceTest(seed);
        wrongStateTest(seed);
        fleeRulesTest(seed);
        floorClearedTest(seed);
        declineAndRetreatTest(seed);
        scriptedRunsTest(seed);
        reproducibilityTest();
        System.out.println("\nAll dungeon checks passed.");
    }

    // ---- tests ----------------------------------------------------------

    private static void sampleDungeons(long seed) {
        System.out.println("\n== Sample generation ==");
        for (DungeonBlueprint d : DungeonSystem.generate(new Random(seed), new DataMonsterSource())) {
            System.out.println(d.name() + " (" + d.floorCount() + " floors)");
            for (FloorBlueprint f : d.floors()) {
                System.out.println("  Floor " + f.number() + ": " + f.monsters().size() + " monsters + "
                        + f.boss().displayName() + " [" + f.boss().kind() + ", HP " + f.boss().hp()
                        + ", ATK " + f.boss().attack() + "]");
            }
        }
    }

    private static void generationTest() {
        Set<String> allNames = new HashSet<>(Arrays.asList(DungeonData.NAMES));
        for (int s = 0; s < 1000; s++) {
            List<DungeonBlueprint> ds = DungeonSystem.generate(new Random(s), new DataMonsterSource());
            check(ds.size() == DungeonData.DUNGEON_COUNT, "dungeon count");
            Set<String> names = new HashSet<>();
            Set<String> ids = new HashSet<>();
            for (DungeonBlueprint d : ds) {
                check(names.add(d.name()), "unique name, seed " + s);
                check(allNames.contains(d.name()), "name comes from the list");
                check(d.floorCount() >= 2 && d.floorCount() <= 5, "floors 2..5");
                for (FloorBlueprint f : d.floors()) {
                    check(f.monsters().size() >= 1 && f.monsters().size() <= 5, "monsters 1..5");
                    check(f.last() == (f.number() == d.floorCount()), "last flag");
                    check(f.boss().kind() == (f.last() ? Kind.DUNGEON_BOSS : Kind.FLOOR_BOSS), "boss kind by floor");
                    ids.add(f.boss().id());
                    for (MonsterSpec m : f.monsters()) { check(m.kind() == Kind.NORMAL, "regular kind"); ids.add(m.id()); }
                }
            }
            int specCount = ds.stream().flatMap(d -> d.floors().stream()).mapToInt(FloorBlueprint::encounterCount).sum();
            check(ids.size() == specCount, "every monster spec has a unique id");
        }
        pass("generation: 1,000 seeds, names unique, floors/monsters in range, boss kinds correct");
    }

    private static void independenceTest(long seed) {
        DungeonBlueprint bp = blueprint(seed);
        String before = bp.toString();
        DungeonRun a = DungeonSystem.start(bp);
        DungeonRun b = DungeonSystem.start(bp);
        play(a, e -> EncounterResult.WON);
        check(a.outcome() == Outcome.VICTORY, "run A cleared");
        check(b.state() == State.AWAITING_ENCOUNTER && b.floorNumber() == 1, "run B untouched");
        check(b.currentEncounter().position() == 1, "run B still at first encounter");
        check(bp.toString().equals(before), "blueprint unchanged");
        pass("independent runs: clearing run 1 changes nothing in run 2");
    }

    private static void wrongStateTest(long seed) {
        DungeonBlueprint bp = blueprint(seed);
        DungeonRun r = DungeonSystem.start(bp);
        expectIllegalState(r::advanceFloor, "advanceFloor while an encounter is pending");
        expectIllegalState(r::exit, "exit before the rest area");
        expectIllegalState(r::fightAnyway, "fightAnyway without declining");
        r.decline();
        expectIllegalState(r::decline, "decline twice");
        expectIllegalState(r::advanceFloor, "advanceFloor while declined");

        DungeonRun rest = toRestArea(bp);
        expectIllegalState(() -> rest.reportResult(EncounterResult.WON), "reportResult in rest area");
        expectIllegalState(rest::currentEncounter, "currentEncounter in rest area");
        expectIllegalState(rest::decline, "decline in rest area");
        expectIllegalState(rest::retreat, "retreat in rest area (use exit)");

        rest.exit();
        expectIllegalState(rest::advanceFloor, "advanceFloor after the run ended");
        expectIllegalState(rest::exit, "exit twice");
        pass("wrong-state calls are rejected with IllegalStateException");
    }

    private static void fleeRulesTest(long seed) {
        DungeonBlueprint bp = blueprint(seed);
        int cannotFlee = 0;
        for (FloorBlueprint f : bp.floors()) {
            List<MonsterSpec> all = new ArrayList<>(f.monsters());
            all.add(f.boss());
            for (MonsterSpec m : all) {
                boolean expected = m.kind() != Kind.DUNGEON_BOSS;
                Encounter e = new Encounter(m, 1, 1);
                check(e.canFlee() == expected, "canFlee only false for the dungeon boss");
                if (!e.canFlee()) cannotFlee++;
            }
        }
        check(cannotFlee == 1, "exactly one unfleeable encounter per dungeon");

        DungeonRun r = DungeonSystem.start(bp);
        while (!(r.state() == State.AWAITING_ENCOUNTER && r.currentEncounter().isDungeonBoss())) {
            if (r.state() == State.REST_AREA) r.advanceFloor();
            else r.reportResult(EncounterResult.WON);
        }
        DungeonRun boss = r;
        expectIllegalState(() -> boss.reportResult(EncounterResult.FLED), "fleeing the dungeon boss");
        check(boss.state() == State.AWAITING_ENCOUNTER, "failed flee changes nothing");
        pass("only the dungeon boss reports that fleeing is not allowed");
    }

    private static void floorClearedTest(long seed) {
        DungeonBlueprint bp = blueprint(seed);
        // Flee the floor boss on floor 1 (it is not the dungeon boss: every dungeon has 2+ floors)
        Log log = new Log();
        DungeonRun r = DungeonSystem.start(bp, log);
        while (!r.currentEncounter().isBoss()) r.reportResult(EncounterResult.WON);
        check(r.currentEncounter().isFloorBoss(), "floor 1 has a floor boss");
        r.reportResult(EncounterResult.FLED);
        check(r.state() == State.REST_AREA, "fled boss floor is passable");
        check(log.cleared.isEmpty(), "fleeing the floor boss gives no Floor cleared event");

        // Now win the next floor's boss: event fires for floor 2
        r.advanceFloor();
        while (!r.currentEncounter().isBoss()) r.reportResult(EncounterResult.WON);
        r.reportResult(EncounterResult.WON);
        check(log.cleared.size() == 1 && log.cleared.get(0).endsWith("#2"), "winning the boss fires Floor cleared");
        pass("Floor cleared only when the floor boss is won");
    }

    private static void declineAndRetreatTest(long seed) {
        DungeonBlueprint bp = blueprint(seed);

        Log a = new Log();
        DungeonRun r = DungeonSystem.start(bp, a);
        r.decline();
        check(r.state() == State.DECLINED, "declined state");
        check(r.currentEncounter().position() == 1, "same encounter still pending");
        r.retreat();
        check(r.outcome() == Outcome.EXITED && r.isEnded(), "retreat gives EXITED");
        check(a.ended.equals(List.of(Outcome.EXITED)), "one Dungeon ended event");

        Log b = new Log();
        DungeonRun r2 = DungeonSystem.start(bp, b);
        r2.decline();
        r2.fightAnyway();
        check(r2.state() == State.AWAITING_ENCOUNTER, "fightAnyway returns to the fight");
        r2.reportResult(EncounterResult.WON);
        check(b.ended.isEmpty() && r2.outcome() == Outcome.IN_PROGRESS, "run continues");

        // Reporting a result straight from DECLINED also works (player fights after the prompt)
        r2.decline();
        r2.reportResult(EncounterResult.FLED);
        pass("declining never loops; retreat ends the run as EXITED with one event");
    }

    private static void scriptedRunsTest(long seed) {
        DungeonBlueprint bp = blueprint(seed);

        Log win = new Log();
        DungeonRun w = DungeonSystem.start(bp, win);
        int fights = play(w, e -> EncounterResult.WON);
        int expected = bp.floors().stream().mapToInt(FloorBlueprint::encounterCount).sum();
        check(fights == expected, "always-win fights every encounter");
        check(w.outcome() == Outcome.VICTORY, "always-win ends in VICTORY");
        check(win.cleared.size() == bp.floorCount(), "Floor cleared once per floor");
        check(win.ended.equals(List.of(Outcome.VICTORY)), "exactly one Dungeon ended (victory)");

        Log flee = new Log();
        DungeonRun f = DungeonSystem.start(bp, flee);
        play(f, e -> e.canFlee() ? EncounterResult.FLED : EncounterResult.WON);
        check(f.outcome() == Outcome.VICTORY, "flee-everything run still reaches the end");
        check(flee.cleared.size() == 1, "only the dungeon boss floor counts as cleared when everything else is fled");
        check(flee.ended.size() == 1, "one Dungeon ended event");

        Log lose = new Log();
        DungeonRun l = DungeonSystem.start(bp, lose);
        int lost = play(l, e -> EncounterResult.DEFEATED);
        check(lost == 1 && l.outcome() == Outcome.DEFEATED, "loss ends the run as DEFEATED");
        check(lose.ended.equals(List.of(Outcome.DEFEATED)), "one Dungeon ended (defeated)");

        Log quit = new Log();
        DungeonRun q = toRestArea(bp, quit);
        q.exit();
        check(q.outcome() == Outcome.EXITED && quit.ended.equals(List.of(Outcome.EXITED)), "exit from rest area");
        pass("scripted runs: always win, always flee, always lose, exit from rest area");
    }

    private static void reproducibilityTest() {
        String one = DungeonSystem.generate(new Random(42), new DataMonsterSource()).toString();
        String two = DungeonSystem.generate(new Random(42), new DataMonsterSource()).toString();
        check(one.equals(two), "same seed gives identical dungeons");
        pass("same seed gives identical dungeons");
    }

    // ---- helpers --------------------------------------------------------

    private static DungeonBlueprint blueprint(long seed) {
        return DungeonSystem.generate(new Random(seed), new DataMonsterSource()).get(0);
    }

    /** Plays a run to its end; returns the number of fights reported. */
    private static int play(DungeonRun run, Function<Encounter, EncounterResult> policy) {
        int fights = 0;
        while (!run.isEnded()) {
            if (run.state() == State.REST_AREA) run.advanceFloor();
            else { run.reportResult(policy.apply(run.currentEncounter())); fights++; }
        }
        return fights;
    }

    private static DungeonRun toRestArea(DungeonBlueprint bp) { return toRestArea(bp, DungeonListener.NONE); }

    private static DungeonRun toRestArea(DungeonBlueprint bp, DungeonListener l) {
        DungeonRun r = DungeonSystem.start(bp, l);
        while (r.state() != State.REST_AREA) r.reportResult(EncounterResult.WON);
        return r;
    }

    private static void expectIllegalState(Runnable r, String what) {
        try { r.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("Expected IllegalStateException: " + what);
    }

    private static void check(boolean ok, String msg) {
        if (!ok) throw new AssertionError("FAILED: " + msg);
    }

    private static void pass(String msg) { System.out.println("  ok  " + msg); }
}