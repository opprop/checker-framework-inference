package checkers.inference.solver.backend.geneticmaxsat;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.backend.maxsat.MaxSatSolver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.FileUtils;
import checkers.inference.solver.util.SolverEnvironment;

import javax.lang.model.element.AnnotationMirror;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * GeneticMaxSatSolver adds support to use Genetic Algorithm to optimize the {@link checkers.inference.model.PreferenceConstraint} weights
 *
 */
public abstract class GeneticMaxSatSolver extends MaxSatSolver {

    public int allSoftWeightsCount = 0;
    public String wcnfFileContent;

    public GeneticMaxSatSolver(SolverEnvironment solverEnvironment, Collection<Slot> slots,
                               Collection<Constraint> constraints, MaxSatFormatTranslator formatTranslator,
                               Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {
        Map<Integer, AnnotationMirror> superSolutions = super.solve(); // to create initial set of constraints
        List<String> allLines = null;

        try {
            allLines = Files.readAllLines(Paths.get("cnfData/wcnfdata.wcnf")); // read from the wcnf file created by super
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        assert allLines != null;
        wcnfFileContent = String.join("\n", allLines.toArray(new String[0]));

        softWeightCounter();
        fit();

        return superSolutions;
    }

    public String changeSoftWeights(int[] newSoftWeights, String wcnfFileContent, boolean writeToFile){
        int oldTop = 0;
        int wtIndex = 0;

        String[] wcnfContentSplit = wcnfFileContent.split("\n");

        StringBuilder WCNFModInput = new StringBuilder();

        for (String line : wcnfContentSplit) {

            String[] trimAndSplit = line.trim().split(" ");

            if (trimAndSplit[0].equals("p")) {
                oldTop = Integer.parseInt(trimAndSplit[4]);
                trimAndSplit[4] = String.valueOf(Arrays.stream(newSoftWeights).sum()); // replacing the top value with current sum of soft weights
            } else if (oldTop != 0 && Integer.parseInt(trimAndSplit[0]) < oldTop) {
                trimAndSplit[0] = String.valueOf(newSoftWeights[wtIndex]);
                wtIndex++;
            }

            WCNFModInput.append(String.join(" ", trimAndSplit));
            WCNFModInput.append("\n");
        }

        WCNFModInput.setLength(WCNFModInput.length() - 1); // to prevent unwanted character at the end of file

        if (writeToFile){
            File WCNFData = new File(new File("").getAbsolutePath() + "/cnfData");
            FileUtils.writeFile(new File(WCNFData.getAbsolutePath() + "/" + "wcnfdata_modified.wcnf"), WCNFModInput.toString());
        }

        return WCNFModInput.toString();
    }

    public void softWeightCounter(){
        allSoftWeightsCount = 0;
        int top = 0;
        String[] wcnfContentSplit = wcnfFileContent.split("\n");

        for (String line : wcnfContentSplit) {

            String[] trimAndSplit = line.trim().split(" ");

            if (trimAndSplit[0].equals("p")) {
                top = Integer.parseInt(trimAndSplit[4]);
            } else if (top != 0 && Integer.parseInt(trimAndSplit[0]) < top) {
                allSoftWeightsCount++;
            }
        }
    }

    /**
     * Override this method to declare an {@link io.jenetics.engine.Engine} builder and create a fitness function.
     * For reference, please look at Universe type system.
     */
    public abstract void fit();

}
