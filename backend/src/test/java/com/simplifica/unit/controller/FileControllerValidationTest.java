package com.simplifica.unit.controller;

import com.simplifica.presentation.controller.FileController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for FileController path validation.
 */
class FileControllerValidationTest {

    private FileController fileController;
    private Method isValidPathSegmentMethod;

    @BeforeEach
    void setUp() throws Exception {
        fileController = new FileController();
        ReflectionTestUtils.setField(fileController, "basePath", "/tmp/test");

        // Access private method via reflection for testing
        isValidPathSegmentMethod = FileController.class.getDeclaredMethod("isValidPathSegment", String.class);
        isValidPathSegmentMethod.setAccessible(true);
    }

    @Test
    void testIsValidPathSegment_ValidFilenames() throws Exception {
        assertTrue((Boolean) isValidPathSegmentMethod.invoke(fileController, "test.png"));
        assertTrue((Boolean) isValidPathSegmentMethod.invoke(fileController, "file-name.jpg"));
        assertTrue((Boolean) isValidPathSegmentMethod.invoke(fileController, "image_123.jpeg"));
        assertTrue((Boolean) isValidPathSegmentMethod.invoke(fileController, "institutions"));
        assertTrue((Boolean) isValidPathSegmentMethod.invoke(fileController, "users"));
    }

    @Test
    void testIsValidPathSegment_PathTraversal() throws Exception {
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "../../../etc/passwd"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, ".."));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "../file.txt"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "file/../other.txt"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "./file.txt"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, ".\\file.txt"));
    }

    @Test
    void testIsValidPathSegment_AbsolutePaths() throws Exception {
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "/etc/passwd"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "\\windows\\system32"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "C:\\windows"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "D:/data"));
    }

    @Test
    void testIsValidPathSegment_NullByte() throws Exception {
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "file.png\0.txt"));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, "test\0"));
    }

    @Test
    void testIsValidPathSegment_EmptyOrNull() throws Exception {
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, (String) null));
        assertFalse((Boolean) isValidPathSegmentMethod.invoke(fileController, ""));
    }
}
