/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.projecteditor.client.forms.dependencies;

import org.guvnor.common.services.project.model.Dependency;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.shared.dependencies.DependencyService;
import org.kie.workbench.common.services.shared.dependencies.EnhancedDependencies;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.mocks.CallerMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class DependencyLoaderTest {

    @Mock
    private EnhancedDependenciesManager manager;

    @Mock
    private DependencyService dependencyService;

    private DependencyLoader dependencyLoader;

    @Before
    public void setUp() throws Exception {

        dependencyLoader = new DependencyLoader( new CallerMock<DependencyService>( dependencyService ) );
        dependencyLoader.init( manager );
    }

    @Test
    public void testLoadEmptyQueue() throws Exception {
        dependencyLoader.load();

        final EnhancedDependencies enhancedDependencies = getUpdatedEnhancedDependencies();

        assertTrue( enhancedDependencies.isEmpty() );
    }

    @Test
    public void testAdd() throws Exception {

        final EnhancedDependencies enhancedDependencies = new EnhancedDependencies();
        when( dependencyService.loadEnhancedDependencies( anyCollection() ) ).thenReturn( enhancedDependencies );

        dependencyLoader.addToQueue( makeDependency( "artifactId", "groupId", "1.0" ) );

        dependencyLoader.load();

        final EnhancedDependencies updatedEnhancedDependencies = getUpdatedEnhancedDependencies();
        assertEquals( enhancedDependencies, updatedEnhancedDependencies );
    }

    private Dependency makeDependency( final String artifactId,
                                       final String groupId,
                                       final String version ) {
        final Dependency dependency = new Dependency();
        dependency.setArtifactId( artifactId );
        dependency.setGroupId( groupId );
        dependency.setVersion( version );
        return dependency;
    }

    private EnhancedDependencies getUpdatedEnhancedDependencies() {
        ArgumentCaptor<EnhancedDependencies> argumentCaptor = ArgumentCaptor.forClass( EnhancedDependencies.class );

        verify( manager ).onEnhancedDependenciesUpdated( argumentCaptor.capture() );

        return argumentCaptor.getValue();
    }

}