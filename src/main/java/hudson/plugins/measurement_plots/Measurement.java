/*
 * The MIT License
 *
 * Copyright (c) 2010, Stellar Science Ltd Co, K. R. Walker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.measurement_plots;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * A measurement. Measurements have names and values.
 * @author krwalker
 */
public class Measurement {

    transient private TestAction testAction;
    private String name;
    private String value;




    private static transient HashMap<String,String> nameToTSDBCounter = new HashMap<String, String>();

    private static transient HashMap<String,String> tags = new HashMap<String, String>();

    private static transient HashMap<String,String> rate = new HashMap<String, String>();

    static {
        nameToTSDBCounter.put("bulk_rate_report:success","mdm.jobstatistics.successcount");
        nameToTSDBCounter.put("bulk_rate_report:started","mdm.jobstatistics.startedcount");
        nameToTSDBCounter.put("bulk_rate_report:failed ","mdm.jobstatistics.failedcount");

        nameToTSDBCounter.put("DB_server_cpu_usage:Idle","");
        nameToTSDBCounter.put("DB_server_cpu_usage:Nice","");
        nameToTSDBCounter.put("DB_server_cpu_usage:System","");
        nameToTSDBCounter.put("DB_server_cpu_usage:User","");
        nameToTSDBCounter.put("DB_server_cpu_usage:Wait","");

        nameToTSDBCounter.put("MDM_servers_cpu_usage:System","");
        nameToTSDBCounter.put("MDM_servers_cpu_usage:Wait","");
        nameToTSDBCounter.put("MDM_servers_cpu_usage:User","");
        nameToTSDBCounter.put("MDM_servers_cpu_usage:Nice","");
        nameToTSDBCounter.put("MDM_servers_cpu_usage:Idle","");

        nameToTSDBCounter.put("queue_paging_report:kick  ","hornetq.paging.numberofpages");
        nameToTSDBCounter.put("queue_paging_report:result","hornetq.paging.numberofpages");
        nameToTSDBCounter.put("queue_paging_report:notify","hornetq.paging.numberofpages");

        nameToTSDBCounter.put("queue_depth_report:result  ","hornetq.queue.messagecount");
        nameToTSDBCounter.put("queue_depth_report:notify  ","hornetq.queue.messagecount");
        nameToTSDBCounter.put("queue_depth_report:kick    ","hornetq.queue.messagecount");
        nameToTSDBCounter.put("queue_depth_report:subbatch","hornetq.queue.messagecount");

        nameToTSDBCounter.put("logs_report:error  ","mdm.logtracker.errorcount");
        nameToTSDBCounter.put("logs_report:warning","mdm.logtracker.warncount");

        nameToTSDBCounter.put("SESSION_pool_report:jobengine","jboss.ejb3.poolavailablecount");
        nameToTSDBCounter.put("SESSION_pool_report:oracle   ","jboss.oracle.availablecount");
        nameToTSDBCounter.put("SESSION_pool_report:OMADM    ","jboss.ejb3.poolavailablecount");
        nameToTSDBCounter.put("SESSION_pool_report:dsm      ","jboss.ejb3.poolavailablecount");

        nameToTSDBCounter.put("oracle_connections_blocking_report:duration","jboss.oracle.totalblockingtime");

        tags.put("queue_paging_report:kick  ","queue=KickQueue");
        tags.put("queue_paging_report:result","queue=ResultQueue");
        tags.put("queue_paging_report:notify","queue=NotifierQueue");

        tags.put("queue_depth_report:result  ","queue=ResultQueue");
        tags.put("queue_depth_report:notify  ","queue=NotifierQueue");
        tags.put("queue_depth_report:kick    ","queue=KickQueue");
        tags.put("queue_depth_report:subbatch","queue=SubBatchQueue");

        tags.put("SESSION_pool_report:jobengine","name=JobEngineService");
        tags.put("SESSION_pool_report:oracle   ","name=");
        tags.put("SESSION_pool_report:OMADM    ","name=Southbound-OMDM");
        tags.put("SESSION_pool_report:dsm      ","name=DSMService");

        rate.put("bulk_rate_report:success","rate:");
        rate.put("bulk_rate_report:started","rate:");
        rate.put("bulk_rate_report:failed ","rate:");
        rate.put("mdm.logtracker.errorcount","rate:");
        rate.put("mdm.logtracker.errorcount","rate:");
        rate.put("oracle_connections_blocking_report:duration","rate:");


    }
    Measurement(String name, String value) {
        this.name = name;
        this.value = value;

    }

    /**
     * Gets the actual name (as opposed to the URL-safe component name).
     */
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets a URL-safe component name.
     */
    public String getUrlName() {
        try {
            return java.net.URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
        } catch (java.io.UnsupportedEncodingException exception) {
            // This shouldn't happen?
            return getName();
        }
    }

    public StringBuffer getAbsoluteUrl() {
        return getTestAction().getAbsoluteUrl().append(getUrlName() + '/');
    }

    public String getTsdbMetricUrl(){
        StringBuilder rv = new StringBuilder();
        String baseURL = "http://172.31.110.9:4242/q";
        SimpleDateFormat df = new SimpleDateFormat("yyyyy/dd/MM-HH:mm:ss");
        Date startDate = new Date(getTestAction().getBuild().getStartTimeInMillis());
        Date endDate = new Date(getTestAction().getBuild().getStartTimeInMillis()+getTestAction().getBuild().getDuration());
        rv.append(baseURL);
        rv.append("?");

        rv.append("start=");rv.append(df.format(startDate));
        rv.append("&end=");rv.append(df.format(endDate));
        rv.append("&m=avg:");
        rv.append(rate.get(name) == null?"":rate.get(name));
        rv.append(nameToTSDBCounter.get(name));
        rv.append("&o=&yrange=%5B0:%5D&wxh=900x600&smooth=csplines&png&key=top%20center&");
        rv.append(tags.get(name)==null?"":tags.get(name));
        return rv.toString();
    }



    public String getBuildName() {
        return getBuild().getDisplayName();
    }

    public int getBuildNumber() {
        return getBuild().number;
    }

    /**
     * Returns the node name on which this measurement was taken.
     */
    public String getNodeName() {
        return getBuild().getBuiltOnStr();
    }

    public java.util.Calendar getBuildTimestamp() {
        return getBuild().getTimestamp();
    }

    public hudson.model.AbstractBuild<?, ?> getBuild() {
        return getTestAction().getBuild();
    }

    public hudson.tasks.test.TestObject getTestObject() {
        return getTestAction().getTestObject();
    }

    /** Measurements need access to their TestAction. */
    void setTestAction(TestAction testAction) {
        this.testAction = testAction;
    }

    public TestAction getTestAction() {
        return testAction;
    }

    /**
     * @param build The build in which to find another measurement.
     * @return The measurement for the build or null if no measurement
     * exists in the build.
     */
    Measurement getMeasurementInBuild(hudson.model.AbstractBuild<?, ?> build) {
        hudson.tasks.test.TestObject otherTestObject = getTestObject().getResultInBuild(build);
        if (otherTestObject != null) {
            hudson.tasks.test.AbstractTestResultAction otherAbstractTestResultAction =
                    otherTestObject.getTestResultAction();
            if (otherAbstractTestResultAction != null) {
                hudson.tasks.junit.TestResultAction otherJunitTestResultAction =
                        (hudson.tasks.junit.TestResultAction)otherAbstractTestResultAction;
                if (otherJunitTestResultAction != null ) {
                    for (hudson.tasks.junit.TestAction otherJunitTestAction :
                            otherJunitTestResultAction.getActions(otherTestObject)) {
                        TestAction measurementAction = (TestAction)otherJunitTestAction;
                        if (measurementAction != null) {
                            Measurement otherMeasurement =
                                    measurementAction.getMeasurement(getName());
                            if (otherMeasurement != null) {
                                return otherMeasurement;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public History getHistory() {
        return new History(this);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Measurement)) {
            return false;
        }
        Measurement measurement = (Measurement)other;
        return this.getName().equals(measurement.getName());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
