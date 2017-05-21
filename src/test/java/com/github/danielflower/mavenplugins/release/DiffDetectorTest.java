package com.github.danielflower.mavenplugins.release;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import scaffolding.TestProject;

import java.io.IOException;
import java.util.ArrayList;

import static com.github.danielflower.mavenplugins.release.AnnotatedTagFinderTest.saveFileInModule;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DiffDetectorTest {

    @Test
    public void canDetectIfFilesHaveBeenChangedForAModuleSinceSomeSpecificTag() throws Exception {
        TestProject project = TestProject.independentVersionsProject();

        AnnotatedTag tag1 = saveFileInModule(project, "console-app", "1.2", 3);
        AnnotatedTag tag2 = saveFileInModule(project, "core-utils", "2", 0);
        AnnotatedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());

        assertThat(detector.hasChangedSince("core-utils", noChildModules(), asList(tag2)).hasChanged(), is(false));
        assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag2)).hasChanged(), is(true));
        assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag3)).hasChanged(), is(false));
    }

    @Test
    public void canDetectThingsInTheRoot() throws IOException, GitAPIException {
        TestProject simple = TestProject.singleModuleProject();
        AnnotatedTag tag1 = saveFileInModule(simple, ".", "1.0", 1);
        simple.commitRandomFile(".");
        DiffDetector detector = new TreeWalkingDiffDetector(simple.local.getRepository());
        assertThat(detector.hasChangedSince(".", noChildModules(), asList(tag1)).hasChanged(), is(true));

        AnnotatedTag tag2 = saveFileInModule(simple, ".", "1.0", 2);
        assertThat(detector.hasChangedSince(".", noChildModules(), asList(tag2)).hasChanged(), is(false));
    }

    @Test
    public void canDetectChangesAfterTheLastTag() throws IOException, GitAPIException {
        TestProject project = TestProject.independentVersionsProject();

        saveFileInModule(project, "console-app", "1.2", 3);
        saveFileInModule(project, "core-utils", "2", 0);
        AnnotatedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);
        project.commitRandomFile("console-app");

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());
        assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag3)).hasChanged(), is(true));
    }

    @Test
    public void canIgnoreModuleFolders() throws IOException, GitAPIException {
        TestProject project = TestProject.independentVersionsProject();

        saveFileInModule(project, "console-app", "1.2", 3);
        saveFileInModule(project, "core-utils", "2", 0);
        AnnotatedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);
        project.commitRandomFile("console-app");

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());
        assertThat(detector.hasChangedSince("console-app", asList("console-app"), asList(tag3)).hasChanged(), is(false));
    }


    private static java.util.List<String> noChildModules() {
        return new ArrayList<String>();
    }
}
