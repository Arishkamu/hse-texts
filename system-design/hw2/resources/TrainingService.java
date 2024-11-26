package org.sportApp.services;

import org.sportApp.entities.Exercise;
import org.sportApp.entities.Training;
import org.sportApp.repo.ExerciseRepository;
import org.sportApp.repo.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;

    @Autowired
    public TrainingService(TrainingRepository trainingRepository, ExerciseRepository exerciseRepository) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
    }


    public Training saveTraining(Training training) {
        if (training.getExercises() == null) {
            return trainingRepository.save(training);
        }
        training.getExercises().forEach(exr -> exr.setTraining(training));
        Training savedTraining = trainingRepository.save(training);
        exerciseRepository.saveAll(training.getExercises());
        return savedTraining;
    }


    public Optional<Training> findById(long trainId) {
        return trainingRepository.findById(trainId);
    }
}