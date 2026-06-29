package com.scholario.identity.service;

import com.scholario.identity.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakRoleSyncService {

    @Value("${scholario.security.keycloak.enabled:true}")
    private boolean keycloakEnabled;

    @Value("${scholario.keycloak.admin.server-url:}")
    private String serverUrl;

    @Value("${scholario.keycloak.admin.realm:scholario}")
    private String realm;

    @Value("${scholario.keycloak.admin.client-id:scholario-backend}")
    private String clientId;

    @Value("${scholario.keycloak.admin.client-secret:}")
    private String clientSecret;

    @Value("${scholario.keycloak.admin.target-client-id:}")
    private String targetClientId;

    @Value("${scholario.keycloak.admin.force-logout:true}")
    private boolean forceLogout;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        if (!keycloakEnabled) {
            log.info("Keycloak role synchronization is disabled via configuration.");
            return;
        }
        
        if (serverUrl == null || serverUrl.isEmpty()) {
            log.warn("Keycloak server URL is not configured. Role sync will not work.");
            return;
        }

        try {
            log.info("Initializing Keycloak Admin Client for realm: {}", realm);
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak Admin Client", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (keycloak != null) {
            keycloak.close();
        }
    }

    public void syncRoles(String username, Set<Role> newRoles) {
        if (!keycloakEnabled || keycloak == null) {
            log.debug("Skipping Keycloak role sync for user {} (disabled or not initialized)", username);
            return;
        }

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);
            if (users == null || users.isEmpty()) {
                log.warn("User {} not found in Keycloak. Cannot sync roles.", username);
                return;
            }

            UserRepresentation userRep = users.get(0);
            UserResource userResource = usersResource.get(userRep.getId());

            boolean realmChanged = syncRealmRoles(realmResource, userResource, newRoles);
            boolean clientChanged = false;

            if (targetClientId != null && !targetClientId.isEmpty()) {
                clientChanged = syncClientRoles(realmResource, userResource, newRoles);
            }

            if (realmChanged || clientChanged) {
                if (forceLogout) {
                    log.info("Forcing logout for user {} to apply role changes immediately.", username);
                    userResource.logout();
                }
                log.info("Successfully synchronized roles for user {} in Keycloak.", username);
            } else {
                log.debug("No role changes detected for user {} in Keycloak.", username);
            }

        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized"))) {
                log.warn("Failed to synchronize roles to Keycloak for user {} due to unauthorized admin credentials (check client-secret): {}", username, e.getMessage());
            } else {
                log.error("Failed to synchronize roles to Keycloak for user {}: {}", username, e.getMessage(), e);
            }
        }
    }

    private boolean syncRealmRoles(RealmResource realmResource, UserResource userResource, Set<Role> newRoles) {
        List<RoleRepresentation> currentRealmRoles = userResource.roles().realmLevel().listAll();
        boolean changed = false;

        List<RoleRepresentation> rolesToRemove = currentRealmRoles.stream()
                .filter(r -> isAppRole(r.getName()))
                .filter(r -> newRoles.stream().noneMatch(nr -> nr.name().equalsIgnoreCase(r.getName())))
                .collect(Collectors.toList());

        if (!rolesToRemove.isEmpty()) {
            userResource.roles().realmLevel().remove(rolesToRemove);
            log.debug("Removed old realm roles: {}", rolesToRemove.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            changed = true;
        }

        List<RoleRepresentation> rolesToAdd = newRoles.stream()
                .filter(nr -> currentRealmRoles.stream().noneMatch(cr -> cr.getName().equalsIgnoreCase(nr.name())))
                .map(role -> {
                    try {
                        return realmResource.roles().get(role.name()).toRepresentation();
                    } catch (Exception e) {
                        log.warn("Realm role {} does not exist in Keycloak", role.name());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (!rolesToAdd.isEmpty()) {
            userResource.roles().realmLevel().add(rolesToAdd);
            log.debug("Added new realm roles: {}", rolesToAdd.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            changed = true;
        }

        return changed;
    }

    private boolean syncClientRoles(RealmResource realmResource, UserResource userResource, Set<Role> newRoles) {
        List<ClientRepresentation> clients = realmResource.clients().findByClientId(targetClientId);
        if (clients == null || clients.isEmpty()) {
            log.warn("Target client {} not found in Keycloak. Skipping client role sync.", targetClientId);
            return false;
        }

        String clientUuid = clients.get(0).getId();
        List<RoleRepresentation> currentClientRoles = userResource.roles().clientLevel(clientUuid).listAll();
        boolean changed = false;

        List<RoleRepresentation> rolesToRemove = currentClientRoles.stream()
                .filter(r -> isAppRole(r.getName()))
                .filter(r -> newRoles.stream().noneMatch(nr -> nr.name().equalsIgnoreCase(r.getName())))
                .collect(Collectors.toList());

        if (!rolesToRemove.isEmpty()) {
            userResource.roles().clientLevel(clientUuid).remove(rolesToRemove);
            log.debug("Removed old client roles: {}", rolesToRemove.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            changed = true;
        }

        List<RoleRepresentation> rolesToAdd = newRoles.stream()
                .filter(nr -> currentClientRoles.stream().noneMatch(cc -> cc.getName().equalsIgnoreCase(nr.name())))
                .map(role -> {
                    try {
                        return realmResource.clients().get(clientUuid).roles().get(role.name()).toRepresentation();
                    } catch (Exception e) {
                        log.warn("Client role {} does not exist in Keycloak client {}", role.name(), targetClientId);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (!rolesToAdd.isEmpty()) {
            userResource.roles().clientLevel(clientUuid).add(rolesToAdd);
            log.debug("Added new client roles: {}", rolesToAdd.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            changed = true;
        }

        return changed;
    }

    private boolean isAppRole(String roleName) {
        try {
            Role.valueOf(roleName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public UserRepresentation getUserFromKeycloak(String username) {
        if (!keycloakEnabled || keycloak == null) {
            return null;
        }
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.search(username, true);
            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }
        } catch (Exception e) {
            log.error("Failed to query user {} from Keycloak: {}", username, e.getMessage());
        }
        return null;
    }

    public List<String> getUserRolesFromKeycloak(String username, String keycloakUserId) {
        if (!keycloakEnabled || keycloak == null) {
            return Collections.emptyList();
        }
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);
            
            java.util.Set<String> allRoles = new java.util.HashSet<>();
            
            List<RoleRepresentation> realmRoles = userResource.roles().realmLevel().listAll();
            if (realmRoles != null) {
                realmRoles.forEach(r -> allRoles.add(r.getName()));
            }
            
            if (targetClientId != null && !targetClientId.isEmpty()) {
                List<ClientRepresentation> clients = realmResource.clients().findByClientId(targetClientId);
                if (clients != null && !clients.isEmpty()) {
                    String clientUuid = clients.get(0).getId();
                    List<RoleRepresentation> clientRoles = userResource.roles().clientLevel(clientUuid).listAll();
                    if (clientRoles != null) {
                        clientRoles.forEach(r -> allRoles.add(r.getName()));
                    }
                }
            }
            return allRoles.stream().toList();
        } catch (Exception e) {
            log.error("Failed to query roles for user {} from Keycloak: {}", username, e.getMessage());
        }
        return Collections.emptyList();
    }
}
