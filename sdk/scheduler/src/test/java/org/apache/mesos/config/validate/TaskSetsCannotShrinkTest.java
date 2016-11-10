package org.apache.mesos.config.validate;

import org.apache.mesos.offer.InvalidRequirementException;
import org.apache.mesos.specification.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

public class TaskSetsCannotShrinkTest {
    private static final ConfigurationValidator<ServiceSpec> VALIDATOR = new PodSpecsCannotShrink();

    @Mock
    private TaskSpecification mockTaskSpec;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMatchingSize() throws InvalidRequirementException {
        ServiceSpec serviceSpec1 = new DefaultServiceSpec(
                "svc1",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec, mockTaskSpec))));
        ServiceSpec serviceSpec2 = new DefaultServiceSpec(
                "svc2",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec, mockTaskSpec))));

        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec2).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec2).size());
    }

    @Test
    public void testSetGrowth() throws InvalidRequirementException {
        ServiceSpec serviceSpec1 = new DefaultServiceSpec(
                "svc1",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec))));
        ServiceSpec serviceSpec2 = new DefaultServiceSpec(
                "svc2",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec, mockTaskSpec))));

        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec2).size());
        Assert.assertEquals(1, VALIDATOR.validate(serviceSpec2, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec2).size());
    }

    @Test
    public void testTaskGrowth() throws InvalidRequirementException {
        ServiceSpec serviceSpec1 = new DefaultServiceSpec(
                "svc1",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec))));
        ServiceSpec serviceSpec2 = new DefaultServiceSpec(
                "svc2",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec, mockTaskSpec))));

        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec2).size());
        Assert.assertEquals(1, VALIDATOR.validate(serviceSpec2, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec2).size());
    }

    @Test
    public void testSetRemove() throws InvalidRequirementException {
        ServiceSpec serviceSpec1 = new DefaultServiceSpec(
                "svc1",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec, mockTaskSpec))));
        ServiceSpec serviceSpec2 = new DefaultServiceSpec(
                "svc2",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec))));

        Assert.assertEquals(1, VALIDATOR.validate(serviceSpec1, serviceSpec2).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec2).size());
    }

    @Test
    public void testSetRename() throws InvalidRequirementException {
        ServiceSpec serviceSpec1 = new DefaultServiceSpec(
                "svc1",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec, mockTaskSpec))));
        ServiceSpec serviceSpec2 = new DefaultServiceSpec(
                "svc2",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set3", Arrays.asList(mockTaskSpec, mockTaskSpec))));

        Assert.assertEquals(1, VALIDATOR.validate(serviceSpec1, serviceSpec2).size());
        Assert.assertEquals(1, VALIDATOR.validate(serviceSpec2, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec2).size());
    }

    @Test
    public void testDuplicateSet() throws InvalidRequirementException {
        ServiceSpec serviceSpec1 = new DefaultServiceSpec(
                "svc1",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec))));
        ServiceSpec serviceSpec2 = new DefaultServiceSpec(
                "svc2",
                Arrays.asList(
                        DefaultTaskSet.create("set1", Arrays.asList(mockTaskSpec)),
                        DefaultTaskSet.create("set2", Arrays.asList(mockTaskSpec))));

        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec1, serviceSpec2).size()); // only checked against new config
        Assert.assertEquals(2, VALIDATOR.validate(serviceSpec2, serviceSpec1).size());
        Assert.assertEquals(1, VALIDATOR.validate(serviceSpec1, serviceSpec1).size());
        Assert.assertEquals(0, VALIDATOR.validate(serviceSpec2, serviceSpec2).size());
    }
}
