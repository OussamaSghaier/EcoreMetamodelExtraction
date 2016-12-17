package eme.generator;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.ecore.EPackage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eme.generator.saving.CustomPathSavingStrategy;
import eme.model.ExtractedPackage;
import eme.model.IntermediateModel;
import eme.properties.ExtractionProperties;
import eme.properties.TestProperties;

public class EcoreMetamodelGeneratorTest {
    ExtractionProperties properties;
    EcoreMetamodelGenerator generator;
    IntermediateModel model;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        properties = new TestProperties(); // don't use real properties
        generator = new EcoreMetamodelGenerator(properties);
        model = new IntermediateModel("UnitTestProject");
    }

    @Test
    public void testPackageStructure() {
        buildMVCPackages();
        EPackage metamodel = generator.generateMetamodelFrom(model);
        assertEquals(properties.getDefaultPackageName(), metamodel.getName());
        assertEquals(properties.getDefaultPackageName(), metamodel.getNsPrefix());
        assertEquals(model.getProjectName() + "/", metamodel.getNsURI());
        assertEquals(1, metamodel.getESubpackages().size());
        EPackage main = metamodel.getESubpackages().get(0);
        assertEquals(3, main.getESubpackages().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testNullSaving() {
        generator.saveMetamodel(); // no metamodel
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullStrategy() {
        generator.changeSavingStrategy(new CustomPathSavingStrategy()); // has to pass
        generator.changeSavingStrategy(null); // throws exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalModel() {
        generator.generateMetamodelFrom(model); // empty model
    }

    private void buildMVCPackages() {
        ExtractedPackage extractedPackage = new ExtractedPackage("");
        extractedPackage.setAsRoot();
        model.add(extractedPackage);
        model.add(new ExtractedPackage("main"));
        model.add(new ExtractedPackage("main.model"));
        model.add(new ExtractedPackage("main.view"));
        model.add(new ExtractedPackage("main.controller"));
    }
}
