package fr.orion.api;

import fr.orion.api.permission.PermissionNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class BasicApiTest {

    @Test
    @DisplayName("PermissionNode should be created correctly")
    public void testPermissionNodeCreation() {
        PermissionNode node = new PermissionNode("test.permission");

        assertNotNull(node, "PermissionNode should not be null");
        assertEquals("test.permission", node.permission(), "Permission should match input");
        assertFalse(node.isWildcard(), "Basic permission should not be wildcard");
    }

    @Test
    @DisplayName("PermissionNode should handle wildcard permissions")
    public void testWildcardPermissions() {
        PermissionNode wildcardNode = new PermissionNode("module.*");
        PermissionNode globalNode = new PermissionNode("*");

        assertTrue(wildcardNode.isWildcard(), "Module wildcard should be detected");
        assertTrue(globalNode.isWildcard(), "Global wildcard should be detected");

        assertTrue(wildcardNode.matches("module.action"), "Wildcard should match module actions");
        assertTrue(globalNode.matches("any.permission"), "Global wildcard should match anything");
    }

    @Test
    @DisplayName("PermissionNode should extract module name correctly")
    public void testModuleExtraction() {
        PermissionNode modulePermission = new PermissionNode("moderation.kick");
        PermissionNode wildcardPermission = new PermissionNode("music.*");
        PermissionNode globalPermission = new PermissionNode("*");

        assertEquals("moderation", modulePermission.getModule(), "Should extract module name");
        assertEquals("music", wildcardPermission.getModule(), "Should extract module from wildcard");
        assertNull(globalPermission.getModule(), "Global permission has no module");
    }

    @Test
    @DisplayName("PermissionNode should handle null/empty permissions safely")
    public void testNullSafetyPermissions() {
        PermissionNode nullNode = new PermissionNode(null);
        PermissionNode emptyNode = new PermissionNode("");

        assertEquals("", nullNode.permission(), "Null should become empty string");
        assertEquals("", emptyNode.permission(), "Empty should stay empty");

        assertFalse(nullNode.matches("test"), "Null permission should not match anything");
        assertFalse(emptyNode.matches("test"), "Empty permission should not match anything");
    }

    @Test
    @DisplayName("Basic API constants should be accessible")
    public void testApiConstants() {
        assertDoesNotThrow(() -> {
            Class.forName("fr.orion.api.Bot");
            Class.forName("fr.orion.api.permission.PermissionManager");
            Class.forName("fr.orion.api.module.Module");
            Class.forName("fr.orion.api.command.Command");
        }, "All main API interfaces should be loadable");
    }
}