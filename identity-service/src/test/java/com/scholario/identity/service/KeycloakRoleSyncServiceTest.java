package com.scholario.identity.service;

import com.scholario.identity.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakRoleSyncServiceTest {

    @Mock
    private Keycloak keycloak;

    @InjectMocks
    private KeycloakRoleSyncService keycloakRoleSyncService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakRoleSyncService, "keycloakEnabled", true);
        ReflectionTestUtils.setField(keycloakRoleSyncService, "realm", "scholario");
        ReflectionTestUtils.setField(keycloakRoleSyncService, "targetClientId", "client");
        ReflectionTestUtils.setField(keycloakRoleSyncService, "forceLogout", true);
        ReflectionTestUtils.setField(keycloakRoleSyncService, "keycloak", keycloak);
    }

    @Test
    void testSyncRoles_Success() {
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource realmRoleScope = mock(RoleScopeResource.class);
        RoleScopeResource clientRoleScope = mock(RoleScopeResource.class);

        when(keycloak.realm("scholario")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        UserRepresentation userRep = new UserRepresentation();
        userRep.setId("user-uuid");
        when(usersResource.search("testuser", true)).thenReturn(List.of(userRep));
        when(usersResource.get("user-uuid")).thenReturn(userResource);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(realmRoleScope);

        // Realm roles setup
        RoleRepresentation currentRealmRole = new RoleRepresentation();
        currentRealmRole.setName("SUPER_ADMIN");
        when(realmRoleScope.listAll()).thenReturn(List.of(currentRealmRole));

        RolesResource rolesResource = mock(RolesResource.class);
        RoleResource rResource = mock(RoleResource.class);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(anyString())).thenReturn(rResource);
        
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName("MEMBER");
        when(rResource.toRepresentation()).thenReturn(roleRep);

        // Client roles setup
        ClientsResource clientsResource = mock(ClientsResource.class);
        ClientResource clientResource = mock(ClientResource.class);
        when(realmResource.clients()).thenReturn(clientsResource);
        
        ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setId("client-uuid");
        when(clientsResource.findByClientId("client")).thenReturn(List.of(clientRep));
        when(roleMappingResource.clientLevel("client-uuid")).thenReturn(clientRoleScope);

        RoleRepresentation currentClientRole = new RoleRepresentation();
        currentClientRole.setName("SUPER_ADMIN");
        when(clientRoleScope.listAll()).thenReturn(List.of(currentClientRole));

        when(clientsResource.get("client-uuid")).thenReturn(clientResource);
        when(clientResource.roles()).thenReturn(rolesResource);

        keycloakRoleSyncService.syncRoles("testuser", Set.of(Role.MEMBER));

        verify(realmRoleScope).remove(anyList());
        verify(realmRoleScope).add(anyList());
        verify(clientRoleScope).remove(anyList());
        verify(clientRoleScope).add(anyList());
        verify(userResource).logout();
    }

    @Test
    void testSyncRoles_Disabled() {
        ReflectionTestUtils.setField(keycloakRoleSyncService, "keycloakEnabled", false);
        keycloakRoleSyncService.syncRoles("testuser", Set.of(Role.MEMBER));
        verifyNoInteractions(keycloak);
    }

    @Test
    void testInitAndCleanup() {
        ReflectionTestUtils.setField(keycloakRoleSyncService, "keycloakEnabled", false);
        keycloakRoleSyncService.init();

        ReflectionTestUtils.setField(keycloakRoleSyncService, "keycloakEnabled", true);
        ReflectionTestUtils.setField(keycloakRoleSyncService, "serverUrl", "");
        keycloakRoleSyncService.init();

        keycloakRoleSyncService.cleanup();
        verify(keycloak).close();
    }
}
