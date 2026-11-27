package test.java.io.github.albert200000.dlangdt.common.tooling;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.github.albert200000.dlangdt.common.preference.DEnvPreferenceConstants;
import io.github.albert200000.dlangdt.common.preference.DEnvPreferencePlugin;
import io.github.albert200000.dlangdt.common.tooling.DToolChain;

class DToolChainTest {

    private IPreferenceStore prefStore;
    private MockedStatic<DEnvPreferencePlugin> pluginStaticMock;
    private MockedStatic<Display> displayStaticMock;

    @BeforeEach
    void setUp() throws Exception {
        DEnvPreferencePlugin mockPlugin = mock(DEnvPreferencePlugin.class);
        prefStore = mock(IPreferenceStore.class);
        when(mockPlugin.getPreferenceStore()).thenReturn(prefStore);

        pluginStaticMock = mockStatic(DEnvPreferencePlugin.class);
        pluginStaticMock.when(DEnvPreferencePlugin::getPlugin).thenReturn(mockPlugin);

        displayStaticMock = mockStatic(Display.class);
        Display mockDisplay = mock(Display.class);
        displayStaticMock.when(Display::getDefault).thenReturn(mockDisplay);

        clearQueueAndLocks();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearQueueAndLocks();
        if (displayStaticMock != null) {
            displayStaticMock.close();
        }
        if (pluginStaticMock != null) {
            pluginStaticMock.close();
        }
    }

    @Test
    @DisplayName("getDubBinary returns default when preference is blank")
    void testGetDubBinaryDefault() {
        when(prefStore.getString(DEnvPreferenceConstants.DUB_BINARY_PATH)).thenReturn("");

        String dubBinary = DToolChain.getDubBinary();

        String osName = System.getProperty("os.name").toLowerCase();
        String expected = osName.startsWith("win") ? "dub.exe" : "dub";
        assertEquals(expected, dubBinary);
    }

    @Test
    @DisplayName("getDubBinary returns preference value when set")
    void testGetDubBinaryConfigured() {
        when(prefStore.getString(DEnvPreferenceConstants.DUB_BINARY_PATH)).thenReturn("/path/to/dub");

        assertEquals("/path/to/dub", DToolChain.getDubBinary());
    }

    @Test
    @DisplayName("isDUBAvailable returns false when dub binary does not exist")
    void testIsDUBAvailableMissingBinary() {
        when(prefStore.getString(DEnvPreferenceConstants.DUB_BINARY_PATH))
                .thenReturn("/nonexistent/path/to/dub-that-does-not-exist");

        assertFalse(DToolChain.isDUBAvailable());
    }

    @Test
    @DisplayName("asyncExecute queues a job when runningLock is already locked")
    void testAsyncExecuteQueuesJobWhenLocked() throws Exception {
        ReentrantLock runningLock = (ReentrantLock) getStaticField(DToolChain.class, "runningLock");
        IProject mockProject = mock(IProject.class);

        runningLock.lock();
        try {
            DToolChain.asyncExecute(mockProject, List.of("test"));

            BlockingQueue<?> queue = (BlockingQueue<?>) getStaticField(DToolChain.class, "jobQueue");
            assertEquals(1, queue.size());
        } finally {
            if (runningLock.isHeldByCurrentThread()) {
                runningLock.unlock();
            }
        }
    }

    private static void clearQueueAndLocks() throws Exception {
        BlockingQueue<?> queue = (BlockingQueue<?>) getStaticField(DToolChain.class, "jobQueue");
        queue.clear();

        ReentrantLock runningLock = (ReentrantLock) getStaticField(DToolChain.class, "runningLock");
        while (runningLock.isHeldByCurrentThread()) {
            runningLock.unlock();
        }

        ReentrantLock preemptiveLock = (ReentrantLock) getStaticField(DToolChain.class, "preemptiveLock");
        while (preemptiveLock.isHeldByCurrentThread()) {
            preemptiveLock.unlock();
        }
    }

    private static Object getStaticField(Class<?> targetClass, String fieldName) throws Exception {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }
}