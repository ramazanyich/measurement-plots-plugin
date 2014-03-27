package hudson.plugins.measurement_plots;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.RunList;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.*;

/**
 * Created by risrafil on 3/17/14.
 */
public class HistoryPortlet extends DashboardPortlet {

    private String metricName;

    private String jobName;

    private String  testAction;

    private int lastBuildNumber;


    @DataBoundConstructor
    public HistoryPortlet(String name, String metricName) {
        super(name);
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTestAction() {
        return testAction;
    }

    public int getLastBuildNumber() {
        return lastBuildNumber;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.History();
        }
    }

    public Set<String> getMeasurementKeys(){
        SortedSet<String> rv = new TreeSet<String>();

        for (Job job : getDashboard().getJobs()) {

            RunList builds = job.getBuilds();
            for (Iterator<Run> iterator1 = builds.iterator(); iterator1.hasNext(); ) {
                AbstractBuild<?, ?> nextbuild = (AbstractBuild<?, ?>) iterator1.next();
                BuildMeasurements measuruments = MeasurementsHelper.load(nextbuild);
                if (measuruments != null && measuruments.size() > 0) {
                    for (Iterator<TestObjectMeasurements> iterator = measuruments.values().iterator(); iterator.hasNext(); ) {
                        TestObjectMeasurements next = iterator.next();
                        for (Iterator<Measurement> measurementIterator = next.iterator(); measurementIterator.hasNext(); ) {
                            Measurement measurement = measurementIterator.next();
                            if(!rv.contains(measurement.getName())){
                                rv.add(measurement.getName());
                            }

                        }
                    }
                }
            }
        }
        return rv;
       /* ArrayList<String> res = new ArrayList<String>();
        for (Iterator<String> iterator = rv.iterator(); iterator.hasNext(); ) {
            res.add(iterator.next());

        }
        return res;*/
    }

    public BuildMeasurements getMeasurements() {
        BuildMeasurements rv = new BuildMeasurements();
        for (Job job : getDashboard().getJobs()) {

            RunList builds = job.getBuilds();
            for (Iterator<Run> iterator1 = builds.iterator(); iterator1.hasNext(); ) {
                AbstractBuild<?, ?> nextbuild = (AbstractBuild<?, ?>) iterator1.next();


                BuildMeasurements measuruments = MeasurementsHelper.load(nextbuild);
                if (measuruments != null && measuruments.size() > 0) {
                    testAction = measuruments.keySet().iterator().next().toString();
                    for (Iterator<TestObjectMeasurements> iterator = measuruments.values().iterator(); iterator.hasNext(); ) {
                        TestObjectMeasurements next = iterator.next();
                        for (Iterator<Measurement> measurementIterator = next.iterator(); measurementIterator.hasNext(); ) {
                            Measurement measurement = measurementIterator.next();


                            if (measurement.getName().trim().equalsIgnoreCase(metricName.trim())) {
                                TestObjectMeasurements test = new TestObjectMeasurements();
                                test.add(measurement);
                                rv.put(TestObjectId.fromString(nextbuild.getDisplayName()), test);
                                jobName = job.getName();
                                if(nextbuild.getNumber() > lastBuildNumber){
                                    lastBuildNumber = nextbuild.getNumber();
                                }
                                break;
                            }
                        }
                    }

                }
            }

        }
        return rv;
    }

    public Graph getGraph() {
        return new Graph(metricName, Calendar.getInstance()) {
            @Override
            protected hudson.util.DataSetBuilder<String, GraphLabel> getDataSetBuilder() {
                hudson.util.DataSetBuilder<String, GraphLabel> data =
                        new hudson.util.DataSetBuilder<String, GraphLabel>();
                for (final Measurement measurement : getMeasurementsForAllbuilds()) {
                    //data.add(value, rowKey, columnKey);
                    Double value = null;
                    try {
                        value = Double.valueOf(measurement.getValue());
                    } catch (NumberFormatException exception) {
                        value = null;
                    }
                    data.add(value, "", new GraphLabel(measurement));
                }
                return data;
            }

            private List<Measurement> getMeasurementsForAllbuilds() {
                List<Measurement> rv = new ArrayList<Measurement>();
                BuildMeasurements m = getMeasurements();
                for(TestObjectMeasurements t:m.values()){
                    for (Iterator<Measurement> iterator = t.iterator(); iterator.hasNext(); ) {
                        Measurement next = iterator.next();
                        rv.add(next);
                    }
                }
                return rv;
            }
        };
    }
}
