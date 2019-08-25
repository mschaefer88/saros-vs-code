package saros.intellij.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.intellij.project.filesystem.IntelliJPathImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ModuleUtil.class, ModuleRootManager.class, ProjectFileIndex.class})
public class IntelliJReferencePointManagerTest {

  IntelliJReferencePointManager intelliJReferencePointManager;
  IReferencePoint referencePoint;

  @Before
  public void prepare() {
    referencePoint = EasyMock.createMock(IReferencePoint.class);
    EasyMock.replay(referencePoint);

    intelliJReferencePointManager = new IntelliJReferencePointManager();
  }

  @Test
  public void testCreateReferencePoint() {
    IReferencePoint referencePoint = IntelliJReferencePointManager.create(createModule("Module1"));
    Assert.assertNotNull(referencePoint);
  }

  @Test
  public void testModulePutIfAbsent() {
    Module module = createModule("Module1");
    IReferencePoint referencePoint = IntelliJReferencePointManager.create(module);
    intelliJReferencePointManager.putIfAbsent(module);
    Module module2 = intelliJReferencePointManager.getModule(referencePoint);

    Assert.assertNotNull(module2);
    Assert.assertEquals(module, module2);
  }

  @Test
  public void testPairPutIfAbsent() {
    Module module = createModule("Module1");
    intelliJReferencePointManager.putIfAbsent(referencePoint, module);
    Module module2 = intelliJReferencePointManager.getModule(referencePoint);

    Assert.assertNotNull(module2);
    Assert.assertEquals(module, module2);
  }

  @Test
  public void testGetResource() {
    VirtualFile resource = EasyMock.createMock(VirtualFile.class);
    EasyMock.replay(resource);

    IPath relativePath = createReferencePointPath("./path/to/file");

    Module module = createModule("Module1", resource, relativePath.toString());

    VirtualFile moduleRoot = createModuleRoot(relativePath.toString(), resource);

    // This is needed for mocking static stuff
    createModuleRootManager(moduleRoot, module, resource);

    IReferencePoint referencePoint = IntelliJReferencePointManager.create(module);

    intelliJReferencePointManager.putIfAbsent(referencePoint, module);

    VirtualFile file = intelliJReferencePointManager.getResource(referencePoint, relativePath);
    Assert.assertNotNull(file);
    Assert.assertEquals(resource, file);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetResourceFromEmptyIntelliJReferencePointManager() {
    VirtualFile resource = EasyMock.createMock(VirtualFile.class);
    EasyMock.replay(resource);

    IPath relativePath = createReferencePointPath("./path/to/file");

    intelliJReferencePointManager.getResource(referencePoint, relativePath);
  }

  @Test
  public void testGetModuleRoot() {
    VirtualFile moduleRootCopy = EasyMock.createMock(VirtualFile.class);
    EasyMock.replay(moduleRootCopy);

    VirtualFile moduleRoot =
        createModuleRoot(createReferencePointPath("Module1").toString(), moduleRootCopy);

    Module module =
        createModule("Module1", moduleRoot, createReferencePointPath("Module1").toString());

    // This is needed for mocking static stuff
    createModuleRootManager(moduleRoot, module, moduleRootCopy);

    IReferencePoint referencePoint = IntelliJReferencePointManager.create(module);

    intelliJReferencePointManager.putIfAbsent(referencePoint, module);

    VirtualFile file =
        intelliJReferencePointManager.getResource(
            referencePoint, createReferencePointPath("Module1"));
    Assert.assertNotNull(file);
    Assert.assertEquals(moduleRootCopy, file);
  }

  private IPath createReferencePointPath(String path) {
    return IntelliJPathImpl.fromString(path);
  }

  private VirtualFile createModuleRoot(String pathToFile, VirtualFile resource) {
    VirtualFile moduleRoot = EasyMock.createMock(VirtualFile.class);
    EasyMock.expect(moduleRoot.findFileByRelativePath(pathToFile)).andStubReturn(resource);

    EasyMock.replay(moduleRoot);

    return moduleRoot;
  }

  @PrepareForTest(ModuleRootManager.class)
  private ModuleRootManager createModuleRootManager(
      VirtualFile moduleRoot, Module module, VirtualFile resource) {
    ModuleFileIndex moduleFileIndex = createModuleFileIndex(resource);

    ModuleRootManager moduleRootManager = EasyMock.createMock(ModuleRootManager.class);
    EasyMock.expect(moduleRootManager.getContentRoots())
        .andStubReturn(new VirtualFile[] {moduleRoot});
    EasyMock.expect(moduleRootManager.getFileIndex()).andStubReturn(moduleFileIndex);
    EasyMock.replay(moduleRootManager);

    PowerMock.mockStatic(ModuleRootManager.class);
    EasyMock.expect(ModuleRootManager.getInstance(module)).andStubReturn(moduleRootManager);
    PowerMock.replay(ModuleRootManager.class);

    return moduleRootManager;
  }

  private ModuleFileIndex createModuleFileIndex(VirtualFile resource) {
    ModuleFileIndex moduleFileIndex = EasyMock.createMock(ModuleFileIndex.class);
    EasyMock.expect(moduleFileIndex.isInContent(resource)).andStubReturn(true);
    EasyMock.replay(moduleFileIndex);

    return moduleFileIndex;
  }

  private Module createModule(String name) {
    VirtualFile file = EasyMock.createMock(VirtualFile.class);
    EasyMock.replay(file);

    String pathToFile = "foo/bar";

    return createModule(name, file, pathToFile);
  }

  private Module createModule(String name, VirtualFile resource, String pathToFile) {
    ModuleFileIndex o = EasyMock.createMock(ModuleFileIndex.class);
    EasyMock.replay(o);

    ModuleRootManager moduleRootManager = EasyMock.createMock(ModuleRootManager.class);
    EasyMock.expect(moduleRootManager.getContentRoots())
        .andStubReturn(new VirtualFile[] {createModuleRoot(pathToFile, resource)});
    EasyMock.expect(moduleRootManager.getFileIndex()).andStubReturn(o);
    EasyMock.replay(moduleRootManager);

    Module module = EasyMock.createMock(Module.class);
    EasyMock.expect(module.getComponent(ModuleRootManager.class)).andStubReturn(moduleRootManager);
    EasyMock.expect(module.getName()).andStubReturn(name);
    EasyMock.replay(module);

    return module;
  }
}
