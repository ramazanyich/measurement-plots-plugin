package hudson.plugins.measurement_plots;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by risrafil on 3/13/14.
 */
public class MeasurementsPortlet extends DashboardPortlet {



    @DataBoundConstructor
    public MeasurementsPortlet(String name) {
        super(name);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.Measurements();
        }
    }

    public BuildMeasurements getMeasurements(){
        BuildMeasurements rv=null;
        for(Job job:getDashboard().getJobs()){
            AbstractBuild<?,?> lastBuild = (AbstractBuild<?, ?>) job.getLastSuccessfulBuild();
            BuildMeasurements measuruments = MeasurementsHelper.getBuildMeasurements(lastBuild);
            if(measuruments != null && measuruments.size()>0){
                rv=measuruments;
                break;
            }

        }
        return rv;
    }







}
