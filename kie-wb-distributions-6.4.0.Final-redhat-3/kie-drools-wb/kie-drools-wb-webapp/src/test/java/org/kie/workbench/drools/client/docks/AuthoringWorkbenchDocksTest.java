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

package org.kie.workbench.drools.client.docks;

import java.util.HashSet;
import java.util.Set;

import org.guvnor.common.services.shared.security.KieWorkbenchACL;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchContext;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchContextChangeEvent;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchFocusEvent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.workbench.docks.UberfireDock;
import org.uberfire.client.workbench.docks.UberfireDockPosition;
import org.uberfire.client.workbench.docks.UberfireDockReadyEvent;
import org.uberfire.client.workbench.docks.UberfireDocks;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.rpc.SessionInfo;

import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class AuthoringWorkbenchDocksTest {

    @Mock
    private UberfireDocks uberfireDocks;

    @Mock
    private DataModelerWorkbenchContext dataModelerWBContext;

    @Mock
    private KieWorkbenchACL kieACL;

    @Mock
    private SessionInfo sessionInfo;

    @InjectMocks
    private AuthoringWorkbenchDocks authoringDocks;

    @Mock
    private PlaceRequest placeRequest;

    @Mock
    User user;

    UberfireDock plannerDock;

    private static final String PLANNER_ROLE = "plannermgmt";

    private Set<String> featureRoles = new HashSet<String>();

    @Before
    public void initTest() {
        MockitoAnnotations.initMocks( this );
        authoringDocks.setup( "authoring", placeRequest );
        featureRoles.add( PLANNER_ROLE );
        plannerDock = new UberfireDock( UberfireDockPosition.EAST,
                "CALCULATOR",
                new DefaultPlaceRequest( "PlannerDomainScreen" ),
                "authoring" ).withSize( 450 ).withLabel( "OptaPlanner" );

    }

    @Test
    public void plannerRoleGrantedTest() {
        Set<Role> userRoles = new HashSet<Role>();
        userRoles.add( new Role() {
                @Override public String getName() {
                    return PLANNER_ROLE;
                }
            } );

        when( kieACL.getGrantedRoles( "wb_optaplanner_domain" ) ).thenReturn( featureRoles );

        when( sessionInfo.getId() ).thenReturn( "logged_user" );
        when( sessionInfo.getIdentity() ).thenReturn( user );
        when( user.getRoles() ).thenReturn( userRoles );

        UberfireDockReadyEvent event = new UberfireDockReadyEvent( "authoring" );
        authoringDocks.perspectiveChangeEvent( event );

        verify( uberfireDocks, times( 1 ) ).add( plannerDock );
    }

    @Test
    public void plannerRoleNotGrantedNeverVisitedTest() {
        testPlannerNotGranted( false );
    }

    @Test
    public void plannerRoleNotGrantedVisitedTest() {
        testPlannerNotGranted( true );
    }

    private void testPlannerNotGranted( boolean visited ) {

        if ( visited ) {
            //make that a user with the grants visits the authoring perspective
            plannerRoleGrantedTest();

        }
        //user hasn't the planner role in this case
        Set<Role> userRoles = new HashSet<Role>();

        when( kieACL.getGrantedRoles( "wb_optaplanner_domain" ) ).thenReturn( featureRoles );

        when( sessionInfo.getId() ).thenReturn( "logged_user" );
        when( sessionInfo.getIdentity() ).thenReturn( user );
        when( user.getRoles() ).thenReturn( userRoles );

        UberfireDockReadyEvent event = new UberfireDockReadyEvent( "authoring" );
        authoringDocks.perspectiveChangeEvent( event );

        if ( visited ) {
            //if the authoring was visited at least once by a user with the planner role
            //ensure the dock is removed
            verify( uberfireDocks, times( 1 ) ).remove( plannerDock );
        }
        //if not, do nothing
    }

    /**
     * This test checks that docks operations resulting from the event processing only occurs on the docks belonging
     * to the given active perspective.
     */
    @Test
    public void avoidDocksManipulationInNonActivePerspective() {

        //authoringDocks docks was previously configured to manage the "authoring" perspective docks.

        //emulates current perspective has now changed.
        authoringDocks.perspectiveChangeEvent( new UberfireDockReadyEvent( "some_other_authoring" ) );

        //emulate the different events that can modify the docks
        DataModelerContext context1 = mock( DataModelerContext.class );
        when( context1.getEditionMode() ).thenReturn( DataModelerContext.EditionMode.GRAPHICAL_MODE );
        DataModelerContext context2 = mock( DataModelerContext.class );
        when( context2.getEditionMode() ).thenReturn( DataModelerContext.EditionMode.GRAPHICAL_MODE );

        when ( dataModelerWBContext.getActiveContext() ).thenReturn( context1 );
        authoringDocks.onDataModelerWorkbenchFocusEvent( new DataModelerWorkbenchFocusEvent() ) ;
        authoringDocks.onContextChange( new DataModelerWorkbenchContextChangeEvent() );

        when ( dataModelerWBContext.getActiveContext() ).thenReturn( context2 );
        authoringDocks.onDataModelerWorkbenchFocusEvent( new DataModelerWorkbenchFocusEvent().lostFocus() );
        authoringDocks.onContextChange( new DataModelerWorkbenchContextChangeEvent() );

        //disable operation should have been invoked only one time as part of the setup process, but never again.
        verify( uberfireDocks, times( 1 ) ).disable( any( UberfireDockPosition.class ), anyString() );
        //no other docks operations should have been invoked.
        verify( uberfireDocks, times( 0 ) ).enable( any( UberfireDockPosition.class ), anyString() );
    }

    /**
     * This test checks that unnecessary operations on the docks are performed. e.g. if the docks are already enabled
     * then subsequent enabling operation will be skipped, and the same for disabling operations.
     */
    @Test
    public void avoidDuplicatedStateChangeOnDocksStatusTest() {

        //authoringDocks docks was previously configured to manage the "authoring" perspective docks.

        //at this point the docks were disabled as part of the initialization procedure.
        verify( uberfireDocks, times( 1 ) ).disable( UberfireDockPosition.EAST, "authoring" );

        //emulates that "authoring" perspective was selected.
        authoringDocks.perspectiveChangeEvent( new UberfireDockReadyEvent( "authoring" ) );

        //emulates the different events that typically may cause the docks to be set on "enabled"
        DataModelerContext context1 = mock( DataModelerContext.class );
        when( context1.getEditionMode() ).thenReturn( DataModelerContext.EditionMode.GRAPHICAL_MODE );
        DataModelerContext context2 = mock( DataModelerContext.class );
        when( context2.getEditionMode() ).thenReturn( DataModelerContext.EditionMode.GRAPHICAL_MODE );

        when ( dataModelerWBContext.getActiveContext() ).thenReturn( context1 );
        authoringDocks.onDataModelerWorkbenchFocusEvent( new DataModelerWorkbenchFocusEvent() );
        authoringDocks.onContextChange( new DataModelerWorkbenchContextChangeEvent() );

        when ( dataModelerWBContext.getActiveContext() ).thenReturn( context2 );
        authoringDocks.onDataModelerWorkbenchFocusEvent( new DataModelerWorkbenchFocusEvent() );
        authoringDocks.onContextChange( new DataModelerWorkbenchContextChangeEvent() );

        //the docks should have been enabled only one time.
        verify( uberfireDocks, times( 1 ) ).enable( UberfireDockPosition.EAST, "authoring" );

        //now let's the dock to be disabled multiple times
        when ( dataModelerWBContext.getActiveContext() ).thenReturn( context1 );
        authoringDocks.onDataModelerWorkbenchFocusEvent( new DataModelerWorkbenchFocusEvent().lostFocus() );
        authoringDocks.onContextChange( new DataModelerWorkbenchContextChangeEvent() );

        when ( dataModelerWBContext.getActiveContext() ).thenReturn( context2 );
        authoringDocks.onDataModelerWorkbenchFocusEvent( new DataModelerWorkbenchFocusEvent().lostFocus() );
        authoringDocks.onContextChange( new DataModelerWorkbenchContextChangeEvent() );

        //the docks should have been disabled only one two times (the initial disabling that was part of the setup
        // procedure) + only one additional that derives from the multiple context changes.

        verify( uberfireDocks, times( 2 ) ).disable( UberfireDockPosition.EAST, "authoring" );
    }
}
