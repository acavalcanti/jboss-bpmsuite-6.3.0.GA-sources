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

package org.optaplanner.core.impl.heuristic.move;

import java.util.Collection;
import java.util.Collections;

import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Makes no changes.
 */
public class NoChangeMove extends AbstractMove {

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return true;
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new NoChangeMove();
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector scoreDirector) {
        // do nothing
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    public Collection<? extends Object> getPlanningEntities() {
        return Collections.<Object>emptyList();
    }

    public Collection<? extends Object> getPlanningValues() {
        return Collections.<Object>emptyList();
    }

    @Override
    public String toString() {
        return "No change";
    }

}
