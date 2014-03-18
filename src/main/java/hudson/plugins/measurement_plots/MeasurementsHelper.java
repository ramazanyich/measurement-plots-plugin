package hudson.plugins.measurement_plots;

/**
 * Created by risrafil on 3/17/14.
 */
public class MeasurementsHelper {
    private static transient java.lang.ref.WeakReference<BuildMeasurements> weakBuildMeasurements;

    private static hudson.XmlFile getDataFile(hudson.model.AbstractBuild<?, ?> build) {
        return new hudson.XmlFile(TestActionResolver.XSTREAM, new java.io.File(build.getRootDir(), "measurement-plots.xml"));
    }

    /**
     * Loads a {@link BuildMeasurements} from disk.
     */
    public static BuildMeasurements load(hudson.model.AbstractBuild<?, ?> build) {
        BuildMeasurements loadedMeasurements;
        try {
            loadedMeasurements = (BuildMeasurements)getDataFile(build).read();
        } catch (java.io.IOException exception) {

            loadedMeasurements = new BuildMeasurements();
        }
        return loadedMeasurements;
    }

    public static synchronized BuildMeasurements getBuildMeasurements(hudson.model.AbstractBuild<?, ?> build) {
        BuildMeasurements buildMeasurements;

        if(weakBuildMeasurements == null) {
            buildMeasurements = MeasurementsHelper.load(build);
            weakBuildMeasurements = new java.lang.ref.WeakReference<BuildMeasurements>(buildMeasurements);
        } else {
            buildMeasurements = weakBuildMeasurements.get();
        }

        if(buildMeasurements == null) {
            buildMeasurements = MeasurementsHelper.load(build);
            weakBuildMeasurements = new java.lang.ref.WeakReference<BuildMeasurements>(buildMeasurements);
        }
        return buildMeasurements;
    }


}
