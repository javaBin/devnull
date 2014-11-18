package no.java.devnull;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import no.java.devnull.resources.FeedbackResource;

/**
 * Created by kenneth on 18.11.14.
 */
public class DevnullApplication extends Application<DevnullConfiguration> {

    @Override
    public void run(DevnullConfiguration devnullConfiguration, Environment environment) throws Exception {
        final FeedbackResource feedback = new FeedbackResource();
        environment.jersey().register(feedback);
    }

    public static void main(String[] args) throws Exception{
        new DevnullApplication().run(args);
    }

    @Override
    public String getName(){
        return "Devnull";
    }

    @Override
    public void initialize(Bootstrap<DevnullConfiguration> bootstrap){

    }


}
