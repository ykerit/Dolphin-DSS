package example.distributedshell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PlacementSpec {

    private static final Logger LOG =
            LogManager.getLogger(PlacementSpec.class);

    public final String sourceTag;
    private int numContainers;

    public PlacementSpec(String sourceTag, int numContainers) {
        this.sourceTag = sourceTag;
        this.numContainers = numContainers;
    }

    /**
     * Get the number of container for this spec.
     * @return container count
     */
    public int getNumContainers() {
        return numContainers;
    }

    /**
     * Set number of containers for this spec.
     * @param numContainers number of containers.
     */
    public void setNumContainers(int numContainers) {
        this.numContainers = numContainers;
    }

    // Placement specification should be of the form:
    // PlacementSpec => ""|KeyVal;PlacementSpec
    // KeyVal => SourceTag=Constraint
    // SourceTag => String
    // Constraint => NumContainers|
    //               NumContainers,"in",Scope,TargetTag|
    //               NumContainers,"notin",Scope,TargetTag|
    //               NumContainers,"cardinality",Scope,TargetTag,MinCard,MaxCard
    // NumContainers => int (number of containers)
    // Scope => "NODE"|"RACK"
    // TargetTag => String (Target Tag)
    // MinCard => int (min cardinality - needed if ConstraintType == cardinality)
    // MaxCard => int (max cardinality - needed if ConstraintType == cardinality)

    /**
     * Parser to convert a string representation of a placement spec to mapping
     * from source tag to Placement Constraint.
     *
     * @param specs Placement spec.
     * @return Mapping from source tag to placement constraint.
     */
    public static Map<String, PlacementSpec> parse(String specs)
            throws IllegalArgumentException {
        LOG.info("Parsing Placement Specs: [{}]", specs);

        Map<String, PlacementSpec> pSpecs = new HashMap<>();
        return  pSpecs;
    }
}
