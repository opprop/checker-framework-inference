package checkers.inference.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;

@RunWith(MockitoJUnitRunner.class)
public class ConstraintsHashcodeEqualsTest {
    Set<Constraint> constraintSet;
    Map<Constraint, Integer> constraintMap;

    AnnotationLocation dummyLocation;
    VariableSlot one;
    VariableSlot two;
    VariableSlot three;
    ArithmeticVariableSlot arithRes;
    CombVariableSlot combRes;
    // use Mockito to build a constant slot as AnnotationBuilder requires a ProcessingEnvironment,
    // this CS has id 0
    @Mock ConstantSlot goal;

    AlwaysTrueConstraint atCon;
    AlwaysFalseConstraint afCon;
    ArithmeticConstraint addCon;
    ArithmeticConstraint mulCon;
    CombineConstraint combCon;
    Constraint compCon;
    Constraint compCon2;
    Constraint eqCon;
    Constraint eqCon2;
    Constraint neqCon;
    Constraint neqCon2;
    Constraint exCon;
    Constraint impCon;
    PreferenceConstraint prefCon;
    Constraint stCon;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        constraintSet = new HashSet<>();
        constraintMap = new HashMap<>();

        dummyLocation = new AnnotationLocation.ClassDeclLocation("java.lang.Number");
        one = new VariableSlot(5);
        two = new VariableSlot(6);
        three = new VariableSlot(7);
        arithRes = new ArithmeticVariableSlot(dummyLocation, 8);
        combRes = new CombVariableSlot(dummyLocation, 9, one, two);

        atCon = AlwaysTrueConstraint.create();
        afCon = AlwaysFalseConstraint.create();
        addCon = ArithmeticConstraint.create(ArithmeticOperationKind.PLUS, one, two, arithRes,
                dummyLocation);
        mulCon = ArithmeticConstraint.create(ArithmeticOperationKind.MULTIPLY, one, two, arithRes,
                dummyLocation);
        combCon = CombineConstraint.create(one, two, combRes, dummyLocation);
        compCon = ComparableConstraint.create(one, two, dummyLocation, null);
        compCon2 = ComparableConstraint.create(two, one, dummyLocation, null);
        eqCon = EqualityConstraint.create(one, two, dummyLocation);
        eqCon2 = EqualityConstraint.create(two, one, dummyLocation);
        neqCon = InequalityConstraint.create(one, two, dummyLocation);
        neqCon2 = InequalityConstraint.create(two, one, dummyLocation);
        exCon = ExistentialConstraint.create(one, new HashSet<>(Arrays.asList(atCon)),
                new HashSet<>(Arrays.asList(atCon)), dummyLocation);
        impCon = ImplicationConstraint.create(Arrays.asList(addCon), mulCon, dummyLocation);
        prefCon = PreferenceConstraint.create(three, goal, 100, dummyLocation);
        stCon = SubtypeConstraint.create(one, two, dummyLocation, null);
    }

    /**
     * Tests the hashcode methods.
     */
    @Test
    public void testConstraintHashcodeEqualsMethods() {
        isDistinctHashcodes(atCon, afCon, addCon, mulCon, combCon, compCon, compCon2, eqCon, eqCon2,
                neqCon, neqCon2, exCon, impCon, prefCon, stCon);
    }

    /**
     * checks and ensures every constraint is distinct from each other
     */
    private void isDistinctHashcodes(Constraint... constraints) {
        for (Constraint c : constraints) {
            for (Constraint d : constraints) {
                // if equals then hashcode must be same
                if (c.equals(d)) {
                    assertTrue("hashcode of " + c + " and " + d + " must be same.",
                            c.hashCode() == d.hashCode());
                }
                // if not equals then hashcode must be different
                else {
                    assertFalse("hashcode of " + c + " and " + d + " must be different.",
                            c.hashCode() == d.hashCode());
                }
            }
        }
    }

    @Test
    public void testConstraintSet() {
        constraintSet.add(atCon);
        constraintSet.add(afCon);
        constraintSet.add(addCon);
        constraintSet.add(mulCon);
        constraintSet.add(combCon);
        constraintSet.add(compCon);
        constraintSet.add(compCon2);
        constraintSet.add(eqCon);
        constraintSet.add(eqCon2);
        constraintSet.add(neqCon);
        constraintSet.add(neqCon2);
        constraintSet.add(exCon);
        constraintSet.add(impCon);
        constraintSet.add(prefCon);
        constraintSet.add(stCon);

        assertTrue(constraintSet.contains(atCon));
        assertTrue(constraintSet.contains(afCon));
        assertTrue(constraintSet.contains(addCon));
        assertTrue(constraintSet.contains(mulCon));
        assertTrue(constraintSet.contains(combCon));
        assertTrue(constraintSet.contains(compCon));
        assertTrue(constraintSet.contains(compCon2));
        assertTrue(constraintSet.contains(eqCon));
        assertTrue(constraintSet.contains(eqCon2));
        assertTrue(constraintSet.contains(neqCon));
        assertTrue(constraintSet.contains(neqCon2));
        assertTrue(constraintSet.contains(exCon));
        assertTrue(constraintSet.contains(impCon));
        assertTrue(constraintSet.contains(prefCon));
        assertTrue(constraintSet.contains(stCon));
    }

    @Test
    public void testConstraintMap() {
        constraintMap.put(atCon, 1);
        constraintMap.put(afCon, 2);
        constraintMap.put(addCon, 3);
        constraintMap.put(mulCon, 4);
        constraintMap.put(combCon, 5);
        constraintMap.put(compCon, 6);
        constraintMap.put(compCon2, 66); // compCon2 has the same hashcode as compCon
        constraintMap.put(eqCon, 7);
        constraintMap.put(eqCon2, 77); // eqCon2 has the same hashcode as eqCon
        constraintMap.put(neqCon, 8);
        constraintMap.put(neqCon2, 88); // neqCon2 has the same hashcode as neqCon
        constraintMap.put(exCon, 9);
        constraintMap.put(impCon, 10);
        constraintMap.put(prefCon, 11);
        constraintMap.put(stCon, 12);

        assertTrue(constraintMap.containsKey(atCon) && constraintMap.get(atCon) == 1);
        assertTrue(constraintMap.containsKey(afCon) && constraintMap.get(afCon) == 2);
        assertTrue(constraintMap.containsKey(addCon) && constraintMap.get(addCon) == 3);
        assertTrue(constraintMap.containsKey(mulCon) && constraintMap.get(mulCon) == 4);
        assertTrue(constraintMap.containsKey(combCon) && constraintMap.get(combCon) == 5);
        assertTrue(constraintMap.containsKey(compCon) && constraintMap.get(compCon) == 66);
        assertTrue(constraintMap.containsKey(eqCon) && constraintMap.get(eqCon) == 77);
        assertTrue(constraintMap.containsKey(neqCon) && constraintMap.get(neqCon) == 88);
        assertTrue(constraintMap.containsKey(exCon) && constraintMap.get(exCon) == 9);
        assertTrue(constraintMap.containsKey(impCon) && constraintMap.get(impCon) == 10);
        assertTrue(constraintMap.containsKey(prefCon) && constraintMap.get(prefCon) == 11);
        assertTrue(constraintMap.containsKey(stCon) && constraintMap.get(stCon) == 12);
    }

    /**
     * Ensure that a preference constraint expressed over the same slot and goal can only have 1
     * weight.
     */
    @Test
    public void testDuplicatePreferenceConstraint() {
        constraintSet.add(atCon);
        constraintSet.add(afCon);
        constraintSet.add(addCon);
        constraintSet.add(mulCon);

        constraintSet.add(prefCon);
        PreferenceConstraint prefCon2 = PreferenceConstraint.create(three, goal, 200,
                dummyLocation);

        assertTrue(constraintSet.contains(prefCon2));

        Optional<Constraint> matchingPrefCon = constraintSet.stream()
                .filter(c -> c.hashCode() == prefCon2.hashCode()).findFirst();
        assertTrue(matchingPrefCon.isPresent());

        PreferenceConstraint pcInSet = (PreferenceConstraint) matchingPrefCon.get();
        assertEquals(prefCon.getWeight(), pcInSet.getWeight());
    }
}
