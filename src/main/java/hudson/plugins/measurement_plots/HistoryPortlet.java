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

    private String tsdbBaseURL;

    private boolean tsdbRateCounter;

    private String tsdbTagName;

    private String tsdbCounterName;

    private static Map<String,String>  metricTsdbBaseURLMap = new HashMap<String, String>();

    private static Map<String,String>  metricTsdbTagMap = new HashMap<String, String>();

    private static Map<String,String>  metricTsdbCounterMap = new HashMap<String, String>();

    private static Map<String,Boolean> metricIsRateMap = new HashMap<String, Boolean>();


    @DataBoundConstructor
    public HistoryPortlet(String name, String metricName,String tsdbBaseURL,Boolean tsdbRateCounter,String tsdbTagName, String tsdbCounterName) {
        super(name);
        this.metricName = metricName;
        this.tsdbBaseURL = tsdbBaseURL;
        if(tsdbRateCounter != null) {
            this.tsdbRateCounter = tsdbRateCounter;
        }
        if(tsdbBaseURL != null && metricName != null) {
            metricTsdbBaseURLMap.put(metricName, tsdbBaseURL);
        }
        if(tsdbTagName != null && metricName != null) {
            metricTsdbTagMap.put(metricName, tsdbTagName);
        }
        if(tsdbCounterName != null && metricName != null) {
            metricTsdbCounterMap.put(metricName, tsdbCounterName);
        }
        if(metricName != null) {
            metricIsRateMap.put(metricName, this.tsdbRateCounter);
        }
    }

    public String getMetricName() {
        return metricName;
    }

    public String getTsdbBaseURL() {
        if(tsdbBaseURL != null && metricName != null){
            metricTsdbBaseURLMap.put(metricName,tsdbBaseURL);
        }
        return tsdbBaseURL;
    }

    public void setTsdbBaseURL(String tsdbBaseURL) {
        this.tsdbBaseURL = tsdbBaseURL;
        if(tsdbBaseURL != null && metricName != null) {
            metricTsdbBaseURLMap.put(metricName, tsdbBaseURL);
        }
    }

    public String getTsdbTagName() {
        if(tsdbTagName != null && metricName != null){
            metricTsdbTagMap.put(metricName,tsdbTagName);
        }
        return tsdbTagName;
    }

    public void setTsdbTagName(String tsdbTagName) {
        this.tsdbTagName = tsdbTagName;
        if(tsdbTagName != null && metricName != null){
            metricTsdbTagMap.put(metricName,tsdbTagName);
        }
    }

    public String getTsdbCounterName() {
        if(tsdbCounterName != null && metricName != null){
            metricTsdbCounterMap.put(metricName,tsdbCounterName);
        }
        return tsdbCounterName;
    }

    public void setTsdbCounterName(String tsdbCounterName) {
        this.tsdbCounterName = tsdbCounterName;
        if(tsdbCounterName != null && metricName != null){
            metricTsdbCounterMap.put(metricName,tsdbCounterName);
        }
    }

    public boolean isTsdbRateCounter() {
        if(metricName != null){
            metricIsRateMap.put(metricName,tsdbRateCounter);
        }
        return tsdbRateCounter;
    }

    public void setTsdbRateCounter(boolean tsdbRateCounter) {
        if(metricName != null){
            metricIsRateMap.put(metricName,tsdbRateCounter);
        }
        this.tsdbRateCounter = tsdbRateCounter;
    }

    public static String getTsdbBaseUrlForMetirc(String metricName){
        return metricTsdbBaseURLMap.get(metricName);
    }

    public static String getTsdbTagNameForMetirc(String metricName){
        return metricTsdbTagMap.get(metricName);
    }

    public static String getTsdbCounterNameForMetirc(String metricName){
        return metricTsdbCounterMap.get(metricName);
    }

    public static boolean getTsdbRateForMetirc(String metricName){
        return metricIsRateMap.get(metricName);
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
                                measurement.setTsdbBaseURL(getTsdbBaseURL());
                                measurement.setTsdbRateCounter(isTsdbRateCounter());
                                measurement.setTsdbTagName(getTsdbTagName());
                                measurement.setTsdbCounterName(getTsdbCounterName());
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
