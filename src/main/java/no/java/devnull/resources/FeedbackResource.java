package no.java.devnull.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by kenneth on 18.11.14.
 */

@Path("/feedback")
public class FeedbackResource {

    @GET
    public String getFeedback(){
        return "hei";
    }

}
