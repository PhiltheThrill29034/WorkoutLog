
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.EnumSet;

enum ExerciseType {
    
    

    BENCH_PRESS_BARBELL("Bench Press",
    """
    Bench Press: A foundational compound movement where a barbell is 
    lowered to the mid-sternum and driven upward, requiring total upper 
    body stability and significant pectoral power. 

    """
    ,EnumSet.of(Muscles.CHEST,Muscles.SHOULDERS,Muscles.TRICEPS)),
    CHEST_PRESS_MACHINE("Chest Press (Machine)",
    """
            Chest Press (Machine): A guided pressing motion that eliminates the need for 
            stabilization, allowing for intense, concentrated focus on the 
            contraction of the chest muscles through a fixed horizontal path.
    """
    ,EnumSet.of(Muscles.CHEST,Muscles.SHOULDERS,Muscles.TRICEPS)),
    PECK_DECK_MACHINE("Peck Deck",
    """
        Peck Deck: An isolation movement that uses a semi-circular 
        path to bring the arms together, emphasizing the "squeeze" at the center of the 
        chest and providing a deep stretch in the outer pectorals.
    """,
    EnumSet.of(Muscles.CHEST)),
    LOW_FLY_CABLE("Low Chest Flies",
    """
        Low Chest Flies: A cable-based movement where the resistance is pulled 
        from above the torso toward the belly, 
        specifically targeting the lower pectoral.
    """
    ,EnumSet.of(Muscles.CHEST)),
    INCLINE_CHEST_PRESS_SMITH("Incline Smith Machine Press",
    """
      Incline Smith Machine Press: A vertical-dominant press performed on an 
      upward angle, utilizing a fixed track to safely 
      load the upper chest and front deltoids with heavy resistance.    
   """,
    EnumSet.of(Muscles.CHEST,Muscles.SHOULDERS,Muscles.TRICEPS)),
    LATERAL_RAISE_DUMBBELL("Shoulder Lateral Raises (Dumbells)",
    """
       Shoulder Lateral Raises (Dumbbells): A leverage-based movement where weights are 
       swept out to the sides, creating a peak burn 
       in the lateral deltoids and contributing to shoulder width.     
    """,
    EnumSet.of(Muscles.SHOULDERS)),
    LATERAL_RAISES_MACHINE("Shoulder Lateral Raises (Machine)",
    """
      Shoulder Lateral Raises (Machine): A mechanical isolation of the side delts that 
      maintains constant tension throughout the entire 
      range of motion, from the bottom of the lift to the very top.      
    """,
    EnumSet.of(Muscles.SHOULDERS)),
    SHOULDER_PRESS_SMITH("Smith Machine Shoulder Press",
    """
       Smith Machine Shoulder Press: A stabilized overhead press that allows the lifter to 
       focus purely on the vertical drive of the shoulders without worrying 
       about the bar drifting forward or backward.     
    """,
    EnumSet.of(Muscles.SHOULDERS)),
    REAR_DELT_FLY_CABLE("Rear Delt Fly (Cable",
    """
        Rear Delt Fly (Cable): A pulling motion that targets the often-neglected
        posterior deltoids, using the constant 
        tension of cables to improve shoulder posture and thickness.   
    """,
    EnumSet.of(Muscles.SHOULDERS)),
    PULL_UP("Pullups",
    """
        Pullups: A classic bodyweight vertical pull that emphasizes the "V-taper" 
        of the back by engaging the lats, 
        rhomboids, and biceps to lift the body's entire mass.    
    """,
    EnumSet.of(Muscles.BACK,Muscles.BICEPS)),
    T_BAR("T-Bar Rows",
    """
        T-Bar Rows: A heavy rowing variation where the weight is 
        pulled toward the hips, focusing on the 
        thickness of the mid-back and the strength of the spinal erectors.       
    """,
    EnumSet.of(Muscles.BACK,Muscles.BICEPS)),
    LAT_PULLDOWN_MACHINE("Lat Pulldown (Machine)",
    """
      Lat Pulldown (Machine): A vertical pulling exercise that mimics 
      the pullup but allows for adjustable weight, focusing on the 
      downward rotation of the shoulder blades to engage the lats.      
    """,
    EnumSet.of(Muscles.BACK)),
    PENDULUM_SQUAT("Pendulum Squat",
    """
          Pendulum Squat: A specialized squat that follows a curved, 
          swinging path, allowing for extreme knee flexion and massive 
          quad loading while minimizing stress on the lower back.  
    """,
    EnumSet.of(Muscles.QUADS,Muscles.HAMSTRINGS)),
    HACK_SQUAT("Hack Squat",
    """
        Hack Squat: A machine-based squat where the back is braced 
        against a sled, allowing the lifter to place their feet forward and 
        drive through the heels to isolate the quadriceps.       
    """,
    EnumSet.of(Muscles.QUADS,Muscles.HAMSTRINGS)),
    RDL("Romanian Deadlift",
    """     
    Romanian Deadlift: A posterior chain movement centered on a 
    hip hinge, creating an intense stretch and 
    strengthening effect throughout the hamstrings and glutes.
    """,
    EnumSet.of(Muscles.HAMSTRINGS)),
    PREACHER_CURLS_MACHINE("Preacher Curls (Machine)",
    """
    Preacher Curls (Machine): A bicep isolation where the arms are 
    locked onto a pad, preventing any momentum and forcing the biceps 
    to work from a fully lengthened position.
    """,
    EnumSet.of(Muscles.BICEPS)),
    HAMMER_CURLS_DUMBBELL("Hammer Curls",
    """
    Hammer Curls: A neutral-grip curl that targets the brachialis 
    and the forearms, adding "thickness" to the 
    arm when viewed from the side.
    """,EnumSet.of(Muscles.BICEPS)),
    BICEP_CURLS_CABLE("Cable Curls",
    """
    Cable Curls: A fluid curling motion that provides constant 
    esistance against the biceps, preventing any "dead spots" 
    in the movement where the muscle can rest.
    """,EnumSet.of(Muscles.BICEPS)),
    TRICEP_EXTENSIONS_CABLE("Tricep Extensions (Cable)",
    """
    Tricep Extensions (Cable): A focused pushdown movement that 
    isolates the three heads of the triceps, emphasizing the lockout
     at the bottom for maximum muscle definition.
    """,EnumSet.of(Muscles.TRICEPS)),
    JM_PRESS_SMITH("JM Press (Smith Machine)",
    """
    JM Press (Smith Machine): A unique hybrid 
    movement that blends a tricep 
    extension with a close-grip press, allowing 
    for very heavy loading of the triceps.
    """,EnumSet.of(Muscles.TRICEPS,Muscles.SHOULDERS));

    private final String name;
    private final EnumSet<Muscles> musclesTrained;
    String desc;

    ExerciseType (String name,String desc,EnumSet<Muscles> muscles){
        this.name=name;
        this.musclesTrained=muscles;
        this.desc=desc;
    }

    public String getName(){
        return this.name;
    }

    public String getDesc(){
        return this.desc;
    }
    
    public Set<Muscles> getMuscles(){
        return Set.copyOf(musclesTrained);
    }

    public static ExerciseType fromIndex(int index){
        if (index<0||index>=values().length) throw new IllegalArgumentException("Invalid exercise index");
        return values()[index];
    }



}
