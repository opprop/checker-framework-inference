package checkers.inference.solver.backend;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;

import org.checkerframework.framework.util.ExecUtil;
import org.checkerframework.javacutil.BugInCF;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.FileUtils;
import checkers.inference.solver.util.SolverEnvironment;

/**
 * Abstract solver which extends {@link Solver} with helper methods to invoke a custom external
 * solver process.
 *
 * @param <T>
 *            type of FormatTranslator required by this Solver
 *
 * @see Solver
 */
public abstract class ExternalProcessSolver<T extends FormatTranslator<?, ?, ?>> extends Solver<T> {

    // initial buffer size for the ByteArrayOutputStreams used to capture stdout and stderr for the
    // external solver process
    private static final int BUFFER_INITIAL_SIZE = 1 << 16; // 2^16 = 65536

    /**
     * Stream Reader for stdout of the external solver process
     */
    BufferedReader stdOutReader;

    /**
     * Stream Reader for stderr of the external solver process
     */
    BufferedReader stdErrReader;

    public ExternalProcessSolver(SolverEnvironment solverEnvironment, Collection<Slot> slots,
            Collection<Constraint> constraints, T formatTranslator, Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    /**
     * Runs the external solver command as given by command and captures the stdout and stderr into
     * {@link BufferedReader}s
     *
     * @param command
     *            an external solver command to be executed, each string in the array is
     *            space-concatenated to form the final command
     * @return the exit status code of the external command
     *
     * @see #getStdOut()
     * @see #getStdErr()
     * @see #reset()
     */
    protected int runExternalSolver(String[] command) {
        // use ByteArrayOutputStream to store stdout and stderr
        ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream(BUFFER_INITIAL_SIZE);
        ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream(BUFFER_INITIAL_SIZE);

        int exitStatus = ExecUtil.execute(command, stdOutStream, stdErrStream);

        // extract byte array from ByteArrayOutputStreams and wrap in a buffered reader for post
        // processing
        stdOutReader = createBufferedReader(stdOutStream);
        stdErrReader = createBufferedReader(stdErrStream);

        return exitStatus;
    }

    /**
     * Extracts the byte array from the given stream, wraps the array in a {@link BufferedReader},
     * and returns the BufferedReader.
     *
     * @param stream
     * @return the contents of the stream wrapped in a {@link BufferedReader}
     */
    private BufferedReader createBufferedReader(ByteArrayOutputStream stream) {
        return new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(stream.toByteArray())));
    }

    /**
     * Returns a {@link BufferedReader} containing the contents of the stdout of the external solver
     * process if the output is available. If not available, this method throws an exception.
     *
     * @return the {@link BufferedReader}.
     */
    protected BufferedReader getStdOut() {
        if (stdOutReader == null) {
            throw new BugInCF("getStdOut() called when there are no outputs, "
                    + "check that runExternalSolver() has been executed.");
        }
        return stdOutReader;
    }

    /**
     * Returns a {@link BufferedReader} containing the contents of the stderr of the external solver
     * process if the output is available. If not available, this method throws an exception.
     *
     * @return the {@link BufferedReader}.
     */
    protected BufferedReader getStdErr() {
        if (stdErrReader == null) {
            throw new BugInCF("getStdErr() called when there are no outputs, "
                    + "check that runExternalSolver() has been executed.");
        }
        return stdErrReader;
    }

    /**
     * Clears the BufferedReaders to allow for another execution of an external solver.
     */
    protected void reset() {
        // Close the two existing readers so that any old references to it will
        // no longer work
        try {
            stdOutReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            stdErrReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the two fields so that calls to get the readers result in a crash
        stdOutReader = null;
        stdErrReader = null;
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
