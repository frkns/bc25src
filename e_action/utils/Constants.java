package e_action.utils;

public class Constants {
    public static final int SMALL_PHASE1 = 200;
    public static final int SMALL_PHASE2 = 500;
    public static final int MEDIUM_PHASE1 = 300;
    public static final int MEDIUM_PHASE2 = 600;
    public static final int LARGE_PHASE1 = 400;
    public static final int LARGE_PHASE2 = 800;
    public static final int HUGE_PHASE1 = 500;
    public static final int HUGE_PHASE2 = 800;
    // -------------- Action/Interest score related variables -------------
    // NAME CONSTANTS AFTER THE FILE
    // "Score" suffix indicates a fixed scores for an action
    // No suffix indicates a variable used to help calculate the scores of an action

    // Unit Interest variables
    public static final int ExploreScore = 2;
    // Unit Action variables
    public static final int CompleteTowerPatternScore = 20;
    public static final int CompleteSrpScore = 10;
    public static final int RefillPaintScore = 10;
    public static final int FindRuinScore = 10;
    // Tower Action variables
    public static final int SpawnUnitsScore = 10;
}