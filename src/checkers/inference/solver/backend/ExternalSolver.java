package checkers.inference.solver.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.UserError;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.FileUtils;
import checkers.inference.solver.util.SolverEnvironment;

/**
 * Abstract solver which extends {@link Solver} with helper methods to invoke a
 * custom external solver program.
 *
 * @param <T>
 *            type of FormatTranslator required by this Solver
 * @see Solver
 * @see FileUtils
 */
public abstract class ExternalSolver<T extends FormatTranslator<?, ?, ?>> extends Solver<T> {

    public final Logger logger = Logger.getLogger(ExternalSolver.class.getName());

    public ExternalSolver(SolverEnvironment solverEnvironment, Collection<Slot> slots,
            Collection<Constraint> constraints, T formatTranslator, Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    /**
     * Runs the external solver as given by command and uses the given
     * stdOutHandler and stdErrHandler lambdas to process stdOut and stdErr.
     *
     * @param command
     *            an external solver command to be executed, each string in the
     *            array is space-concatenated to form the final command
     * @param stdOutHandler
     *            a lambda which takes a {@link BufferedReader} providing the
     *            stdOut of the external solver and handles the stdOut.
     * @param stdErrHandler
     *            a lambda which takes a {@link BufferedReader} providing the
     *            stdErr of the external solver and handles the stdErr.
     * @return the exit status code of the external command
     */
    protected int runExternalSolver(String[] command, Consumer<BufferedReader> stdOutHandler,
            Consumer<BufferedReader> stdErrHandler) {

        logger.info("Running external solver command \"" + String.join(" ", command) + "\".");

        // Start the external solver process
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new UserError("Could not run external solver.");
        }

        // Create threads to handle stdOut and stdErr
        StdHandlerThread stdOutHandlerThread = new StdHandlerThread(process.getInputStream(),
                stdOutHandler);
        StdHandlerThread stdErrHandlerThread = new StdHandlerThread(process.getErrorStream(),
                stdErrHandler);
        stdOutHandlerThread.start();
        stdErrHandlerThread.start();

        // Wait for external solver threads to finish
        try {
            stdOutHandlerThread.join();
        } catch (InterruptedException e) {
            throw new BugInCF(
                    "The threads for handling stdOut of the external solver was interrupted.");
        }

        try {
            stdErrHandlerThread.join();
        } catch (InterruptedException e) {
            throw new BugInCF(
                    "The threads for handling stdErr of the external solver was interrupted.");
        }

        int exitStatus;
        try {
            exitStatus = process.waitFor();
        } catch (InterruptedException e) {
            throw new BugInCF("The threads for the external solver was interrupted.");
        }

        logger.info("External solver process finished");

        return exitStatus;
    }

    /**
     * A thread which wraps an InputStream in a BufferedReader and tasks the
     * lambda function to handle the outputs.
     */
    private class StdHandlerThread extends Thread {
        private InputStream stream;
        private Consumer<BufferedReader> handler;

        public StdHandlerThread(final InputStream stream, final Consumer<BufferedReader> handler) {
            this.stream = stream;
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.accept(new BufferedReader(new InputStreamReader(stream)));
        }
    }

    /**
     * Prints any content from the given {@link BufferedReader} to Checker
     * Framework Inference's StdErr.
     *
     * @param stdErr
     *            a BufferedReader containing the contents of an external
     *            process's std err output.
     */
    protected void printStdErr(BufferedReader stdErr) {
        String line;
        try {
            while ((line = stdErr.readLine()) != null) {
                System.err.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
