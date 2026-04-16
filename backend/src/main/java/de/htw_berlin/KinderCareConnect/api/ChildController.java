package de.htw_berlin.KinderCareConnect.api;

import de.htw_berlin.KinderCareConnect.model.Child;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for managing children health information.
 * Provides endpoints to retrieve children data.
 */
@RestController
@RequestMapping("/api/children")
public class ChildController {

    /**
     * GET endpoint - Returns a list of children with sample health data
     * @return List of Child entities with sample data
     */
    @GetMapping
    public List<Child> getAllChildren() {
        return createSampleChildren();
    }

    /**
     * Creates sample data for testing
     * @return List of sample Child objects
     */
    private List<Child> createSampleChildren() {
        List<Child> children = new ArrayList<>();

        children.add(new Child(
            1L,
            "Anna Schmidt",
            "2020-03-15",
            "Peanuts, Milk",
            "Mild Asthma",
            "None",
            "Parents: +49-123-456789"
        ));

        children.add(new Child(
            2L,
            "Max Weber",
            "2019-07-22",
            "Nuts",
            "Diabetes Type 1",
            "ADHD",
            "Parents: +49-987-654321"
        ));

        children.add(new Child(
            3L,
            "Emma Fischer",
            "2021-01-10",
            "Lactose",
            "None",
            "Autism",
            "Parents: +49-555-888444"
        ));

        return children;
    }
}

