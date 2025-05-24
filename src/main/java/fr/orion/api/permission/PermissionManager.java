package fr.orion.api.permission;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.Set;

public interface PermissionManager {

    /**
     * Check if a member has a specific permission.
     * This checks both user-specific permissions and role-based permissions.
     *
     * @param member The Discord member to check
     * @param permission The permission string (example: "moderation.kick")
     * @return true if the member has the permission, false otherwise
     */
    boolean hasPermission(Member member, String permission);

    /**
     * Check if a user has a specific permission (without role context).
     *
     * @param user The Discord user to check
     * @param permission The permission string (example: "moderation.kick")
     * @return true if the user has the permission, false otherwise
     */
    boolean hasPermission(User user, String permission);

    /**
     * Check if a role has a specific permission.
     *
     * @param role The Discord role to check
     * @param permission The permission string (example: "moderation.kick")
     * @return true if the role has the permission, false otherwise
     */
    boolean hasPermission(Role role, String permission);

    /**
     * Add a permission to a specific user.
     *
     * @param userId The Discord user ID
     * @param permission The permission to add
     */
    void addUserPermission(String userId, String permission);

    /**
     * Remove a permission from a specific user.
     *
     * @param userId The Discord user ID
     * @param permission The permission to remove
     */
    void removeUserPermission(String userId, String permission);

    /**
     * Add a permission to a specific role.
     *
     * @param roleId The Discord role ID
     * @param permission The permission to add
     */
    void addRolePermission(String roleId, String permission);

    /**
     * Remove a permission from a specific role.
     *
     * @param roleId The Discord role ID
     * @param permission The permission to remove
     */
    void removeRolePermission(String roleId, String permission);

    /**
     * Get all permissions for a specific user.
     *
     * @param userId The Discord user ID
     * @return Set of permissions for the user
     */
    Set<String> getUserPermissions(String userId);

    /**
     * Get all permissions for a specific role.
     *
     * @param roleId The Discord role ID
     * @return Set of permissions for the role
     */
    Set<String> getRolePermissions(String roleId);

    /**
     * Get all effective permissions for a member (combining user and role permissions).
     *
     * @param member The Discord member
     * @return Set of all effective permissions
     */
    Set<String> getEffectivePermissions(Member member);

    /**
     * Clear all permissions for a user.
     *
     * @param userId The Discord user ID
     */
    void clearUserPermissions(String userId);

    /**
     * Clear all permissions for a role.
     *
     * @param roleId The Discord role ID
     */
    void clearRolePermissions(String roleId);

    /**
     * Get all users with permissions.
     *
     * @return Set of user IDs that have permissions
     */
    Set<String> getAllUsersWithPermissions();

    /**
     * Get all roles with permissions.
     *
     * @return Set of role IDs that have permissions
     */
    Set<String> getAllRolesWithPermissions();

    /**
     * Reload permissions from storage.
     */
    void reload();

    /**
     * Save permissions to storage.
     */
    void save();
}
