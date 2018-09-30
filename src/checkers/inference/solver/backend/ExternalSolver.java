package checkers.inference.solver.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;

import org.checkerframework.javacutil.BugInCF;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.FileUtils;
import checkers.inference.solver.util.SolverEnvironment;

/**
 * Abstract solver which extends {@link Solver} with helper methods to invoke a custom external
 * solver program.
 *
 * @param <T>
 *            type of FormatTranslator required by this Solver
 *
 * @see Solver
 */
public abstract class ExternalSolver<T extends FormatTranslator<?, ?, ?>> extends Solver<T> {

    public ExternalSolver(SolverEnvironment solverEnvironment, Collection<Slot> slots,
            Collection<Constraint> constraints, T formatTranslator, Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    /**
     * Functional interface for lambdas that process the stdOut or stdErr of the external solver
     * process. Each lambda is given a {@link BufferedReader} as an input wrapping the stdOut or
     * stdErr {@link InputStream}s.
     */
    @FunctionalInterface
    protected interface StdOutputHandler {
        public void handle(BufferedReader input);
    }

    /**
     * Runs the external solver as given by command and uses the given stdOutHandler and
     * stdErrHandler lambdas to process stdOut and stdErr.
     *
     * @param command
     *            an external solver command to be executed, each string in the array is
     *            space-concatenated to form the final command
     *
     * @param stdOutHandler
     *            a lambda which takes a {@link BufferedReader} providing the stdOut of the external
     *            solver and handles the stdOut.
     * @param stdErrHandler
     *            a lambda which takes a {@link BufferedReader} providing the stdErr of the external
     *            solver and handles the stdErr.
     * @return the exit status code of the external command
     */
    protected int runExternalSolver(String[] command, StdOutputHandler stdOutHandler,
            StdOutputHandler stdErrHandler) {

        System.out.println("Running external solver command \"" + String.join(" ", command) + "\"");

        // Start the external solver process
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new BugInCF("Could not run external solver");
        }

        // Create threads to handle stdOut and stdErr
        StdOutputHandlerThread stdOutHandlerThread = new StdOutputHandlerThread(
                process.getInputStream(), stdOutHandler);
        StdOutputHandlerThread stdErrHandlerThread = new StdOutputHandlerThread(
                process.getErrorStream(), stdErrHandler);
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

        System.out.println("External solver process finished");

        return exitStatus;
    }

    /**
     * A thread which wraps the InputStream in a BufferedReader and tasks the lambda function to
     * handle the outputs.
     */
    private class StdOutputHandlerThread extends Thread {
        private InputStream stream;
        private StdOutputHandler handler;

        public StdOutputHandlerThread(final InputStream stream, final StdOutputHandler handler) {
            this.stream = stream;
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.handle(new BufferedReader(new InputStreamReader(stream)));
        }
    }

    /**
     * Writes the given content to the given file. This method overwrites the given file if it
     * already exists.
     *
     * @param file
     *            a file to be written to.
     * @param content
     *            the content to be written to the file.
     *
     * @see #writeFile(File, String, boolean)
     */
    protected void writeFile(File file, String content) {
        try (PrintStream stream = FileUtils.getFilePrintStream(file, false)) {
            stream.println(content);
        }
    }

    /**
     * Writes the given content to the given file. This method appends to the given file if it
     * already exists.
     *
     * @param file
     *            a file to be written to.
     * @param content
     *            the content to be written to the file.
     *
     * @see #writeFile(File, String, boolean)
     */
    protected void writeFileInAppendMode(File file, String content) {
        try (PrintStream stream = FileUtils.getFilePrintStream(file, true)) {
            stream.println(content);
        }
    }
}
