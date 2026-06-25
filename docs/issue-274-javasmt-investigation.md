# Issue 274: JavaSMT solver interface investigation

Issue: https://github.com/opprop/checker-framework-inference/issues/274

## Summary

JavaSMT is a reasonable fit for a new solver backend, but it should be added as a
new backend rather than by replacing the current solver framework. The existing
`SolverEngine` already loads backends by convention:

- package: `checkers.inference.solver.backend.<solver-name-lowercase>`
- factory: `<SolverName>SolverFactory`
- selected by solver arg, for example `solver=MaxSat` or `solver=Z3`

That means a JavaSMT backend can be introduced as
`checkers.inference.solver.backend.javasmt.JavaSmtSolverFactory` and selected
with `solver=JavaSmt`, without changing the frontend solver dispatch.

This branch implements that first step:

- JavaSMT dependencies are added only on Java 11+.
- JavaSMT sources live under `srcJava11`, which is only added to the main source
  set on Java 11+.
- `solver=JavaSmt` loads a JavaSMT backend via the existing solver factory
  convention.
- The initial JavaSMT backend supports generic bit-vector encodings for subtype
  and equality constraints.

## Current solver shape

The current general solver framework has three layers:

- `SolverEngine`: builds the qualifier lattice, chooses a solving strategy, and
  reflectively loads a backend factory.
- `SolverFactory` / `Solver`: adapts one concrete backend.
- `FormatTranslator` and constraint encoders: serialize slots and constraints
  to backend-specific terms and decode model values back to annotations.

The Z3 Java backend uses Z3's Java API directly, with bit-vector terms and
`Optimize` for soft constraints. The Z3 SMT backend serializes SMT-LIB, writes a
file, and shells out to `z3`.

## JavaSMT fit

JavaSMT provides a common Java API over multiple SMT solvers and supports
bit-vector formulas, model generation, unsat cores, and optimization features
depending on the selected underlying solver.

Useful references:

- JavaSMT repository: https://github.com/sosy-lab/java-smt
- JavaSMT Maven artifact: `org.sosy-lab:java-smt`
- Latest Maven Central metadata at investigation time reported
  `6.0.0-499-gf35a21a8e`, with stable release `6.0.0` also available.

Important compatibility point: JavaSMT requires Java 11 or newer. This project
still has Java 8-specific build paths. The preferred approach is to isolate the
JavaSMT backend in a Java 11+ source set, rather than just adding the dependency
inside an `if (isJava11plus)` block. That keeps Java 8 builds from compiling
sources that import JavaSMT classes at all.

## Implemented path

1. Add a JavaSMT backend.
   - `JavaSmtSolverFactory`
   - `JavaSmtSolver`
   - `JavaSmtFormatTranslator`
   - `JavaSmtConstraintEncoderFactory`
   - Place these classes in a Java 11+ source set, for example
     `srcJava11/checkers/inference/solver/backend/javasmt`, and only wire that
     source set into the build when `isJava11plus` is true.

2. Add JavaSMT dependency and packaging only for the Java 11+ source set.
   - Use a JavaSMT stable release such as `org.sosy-lab:java-smt:6.0.0`.
   - Keep the default source set free of JavaSMT imports so Java 8 compilation
     remains possible.

3. Start with the bit-vector encoding already used by the Z3 Java backend.
   - This keeps model values close to the current `Z3BitVectorCodec`.
   - It should minimize semantic drift for existing type systems.

4. Initially support the constraint kinds already implemented by the direct Z3
   backend.
   - subtype
   - equality
   - preference / soft constraints remain follow-up work because they need
     JavaSMT optimization support and solver-specific capability checks.

5. Add solver selection options.
   - CFI backend option: `solver=JavaSmt`
   - JavaSMT concrete solver option: for example `javasmtSolver=Z3`,
     `SMTINTERPOL`, or `PRINCESS`

6. Add small backend tests that compare JavaSMT results with the existing Z3 or
   MaxSat backend on a two-qualifier lattice.

## Solver smoke-test results

The current dependency set includes `org.sosy-lab:java-smt:6.0.0` and
`org.sosy-lab:javasmt-solver-z3:4.15.4`. A direct JavaSMT smoke test over a
4-bit bit-vector variable produced:

- `Z3`: works after preloading the existing `tools.aqua:z3-turnkey` native
  libraries and using a no-op JavaSMT loader.
- `PRINCESS`: not runnable with the current dependencies. JavaSMT reports a
  missing `ap/parser/IExpression` class.
- `SMTINTERPOL`: not runnable with the current dependencies. JavaSMT reports a
  missing `de/uni_freiburg/informatik/ultimate/logic/SMTLIBException` class.

This branch therefore keeps Z3 as the only enabled JavaSMT concrete solver in
practice. Enabling Princess or SMTInterpol should be a follow-up PR that adds
their optional runtime artifacts and verifies their theory support against this
backend's bit-vector encoding.

## many-smt fit

The issue comment also mentions https://github.com/Calvin-L/many-smt.

many-smt is not a Java API. It is a Python 3 SMT-LIB frontend that launches
multiple backend solvers in parallel and returns the first result. It would fit
better as an alternative executable for the existing SMT-LIB shell-out path than
as a replacement for the direct Java solver API.

If many-smt is pursued, the smaller path is to generalize `Z3SmtSolver` so the
SMT-LIB executable is configurable, then run the existing SMT-LIB output through
`many-smt` instead of hardcoding `z3`.

## Recommendation

Use JavaSMT for the new Java backend. Treat many-smt separately as a possible
improvement to the existing SMT-LIB external-process backend.

The lowest-risk first PR is this branch: JavaSMT source-set separation plus a
loadable backend for subtype/equality constraints, without changing the default
solver. Follow-up PRs should add soft constraints, broader constraint coverage,
and tests that compare JavaSMT results against existing backends.
