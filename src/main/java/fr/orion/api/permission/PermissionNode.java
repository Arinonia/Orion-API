package fr.orion.api.permission;

/**
 * Represents a permission node with utility methods for wildcard matching.
 */
public record PermissionNode(String permission) {
    public PermissionNode(String permission) {
        this.permission = permission != null ? permission.toLowerCase() : "";
    }

    /**
     * Check if this permission matches the given permission, considering wildcards.
     *
     * @param requiredPermission The permission to check against
     * @return true if this permission grants access to the required permission
     */
    public boolean matches(String requiredPermission) {
        if (requiredPermission == null || requiredPermission.isEmpty()) {
            return false;
        }

        String required = requiredPermission.toLowerCase();
        String current = this.permission;

        if (current.equals(required)) {
            return true;
        }

        if (current.equals("*")) {
            return true;
        }

        if (current.endsWith(".*")) {
            String module = current.substring(0, current.length() - 2);
            return required.startsWith(module + ".");
        }

        return false;
    }

    /**
     * Get the permission string.
     *
     * @return The permission string
     */
    @Override
    public String permission() {
        return this.permission;
    }

    /**
     * Check if this is a wildcard permission.
     *
     * @return true if this is a wildcard permission
     */
    public boolean isWildcard() {
        return this.permission.equals("*") || this.permission.endsWith(".*");
    }

    /**
     * Get the module name from this permission.
     *
     * @return The module name, or null if not a module permission
     */
    public String getModule() {
        if (this.permission.contains(".")) {
            return this.permission.substring(0, this.permission.indexOf("."));
        }
        return null;
    }

    @Override
    public String toString() {
        return this.permission;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PermissionNode permNode = (PermissionNode) obj;
        return this.permission.equals(permNode.permission);
    }

}
