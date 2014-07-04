package org.greengin.nquireit.dao;

import lombok.Getter;
import org.greengin.nquireit.entities.activities.senseit.SenseItActivity;
import org.greengin.nquireit.entities.activities.senseit.SenseItProfile;
import org.greengin.nquireit.entities.activities.senseit.SenseItSeries;
import org.greengin.nquireit.entities.activities.senseit.SensorInput;
import org.greengin.nquireit.logic.project.senseit.SenseItProfileRequest;
import org.greengin.nquireit.logic.project.senseit.SensorInputRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Component
public class SenseItDao {


    @PersistenceContext
    @Getter
    EntityManager em;

    public SenseItSeries getSeries(Long dataId) {
        return em.find(SenseItSeries.class, dataId);
    }


    private void persistProfile(SenseItActivity activity) {
        if (activity.getProfile() == null) {
            activity.setProfile(new SenseItProfile());
        }

        em.persist(activity.getProfile());
    }


    @Transactional
    public void updateProfile(SenseItProfileRequest profileData, SenseItActivity activity) {
        persistProfile(activity);
        profileData.updateProfile(activity.getProfile());
    }

    @Transactional
    public void createSensor(SensorInputRequest inputData, SenseItActivity activity) {
        persistProfile(activity);
        SensorInput input = new SensorInput();
        activity.getProfile().getSensorInputs().add(input);
        inputData.updateInput(input);
        em.persist(input);
    }

    @Transactional
    public void updateSensor(SensorInputRequest inputData, Long inputId) {
        SensorInput input = em.find(SensorInput.class, inputId);
        inputData.updateInput(input);
        em.persist(input);
    }

    @Transactional
    public void deleteSensor(SenseItActivity activity, Long inputId) {
        SensorInput input = em.find(SensorInput.class, inputId);
        persistProfile(activity);
        activity.getProfile().getSensorInputs().remove(input);
    }
}
