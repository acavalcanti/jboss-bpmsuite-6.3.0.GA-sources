/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.constructionheuristic.placer.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.optaplanner.core.impl.constructionheuristic.placer.Placement;
import org.optaplanner.core.impl.constructionheuristic.placer.QueuedValuePlacer;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicRecordingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicReplayingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.composite.CartesianProductMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicRecordingValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;

public class QueuedValuePlacerTest {

    @Test
    public void oneMoveSelector() {
        GenuineVariableDescriptor variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        EntitySelector entitySelector = SelectorTestUtils.mockEntitySelector(variableDescriptor.getEntityDescriptor(),
                new TestdataEntity("a"), new TestdataEntity("b"), new TestdataEntity("c"));
        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                new TestdataValue("1"), new TestdataValue("2"));
        MimicRecordingValueSelector recordingValueSelector = new MimicRecordingValueSelector(
                valueSelector);

        MoveSelector moveSelector = new ChangeMoveSelector(
                entitySelector,
                new MimicReplayingValueSelector(recordingValueSelector),
                false);
        QueuedValuePlacer placer = new QueuedValuePlacer(recordingValueSelector, moveSelector);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        placer.solvingStarted(solverScope);

        AbstractPhaseScope phaseScopeA = mock(AbstractPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        placer.phaseStarted(phaseScopeA);
        Iterator<Placement> placementIterator = placer.iterator();

        assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        placer.stepStarted(stepScopeA1);
        assertPlacement(placementIterator.next(), "1", "a", "b", "c");
        placer.stepEnded(stepScopeA1);

        assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        placer.stepStarted(stepScopeA2);
        assertPlacement(placementIterator.next(), "2", "a", "b", "c");
        placer.stepEnded(stepScopeA2);

        assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeA3 = mock(AbstractStepScope.class);
        when(stepScopeA3.getPhaseScope()).thenReturn(phaseScopeA);
        placer.stepStarted(stepScopeA3);
        assertPlacement(placementIterator.next(), "1", "a", "b", "c");
        placer.stepEnded(stepScopeA3);

        // Requires adding ReinitializeVariableValueSelector complexity to work
        // assertFalse(placementIterator.hasNext());
        placer.phaseEnded(phaseScopeA);

        AbstractPhaseScope phaseScopeB = mock(AbstractPhaseScope.class);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        placer.phaseStarted(phaseScopeB);
        placementIterator = placer.iterator();

        assertTrue(placementIterator.hasNext());
        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        placer.stepStarted(stepScopeB1);
        assertPlacement(placementIterator.next(), "1", "a", "b", "c");
        placer.stepEnded(stepScopeB1);

        placer.phaseEnded(phaseScopeB);

        placer.solvingEnded(solverScope);

        verifyPhaseLifecycle(entitySelector, 1, 2, 4);
        verifyPhaseLifecycle(valueSelector, 1, 2, 4);
    }

    private void assertPlacement(Placement placement, String valueCode, String... entityCodes) {
        Iterator<Move> iterator = placement.iterator();
        assertNotNull(iterator);
        for (String entityCode : entityCodes) {
            assertTrue(iterator.hasNext());
            ChangeMove move = (ChangeMove) iterator.next();
            assertCode(entityCode, move.getEntity());
            assertCode(valueCode, move.getToPlanningValue());
        }
        assertFalse(iterator.hasNext());
    }

}
