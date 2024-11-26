@Service
public class EventService {
    private final TrainingEventRepository eventRepository;
    private final TrainingService trainingService;

    @Autowired
    public EventService(TrainingEventRepository eventRepository, TrainingService trainingService) {
        this.eventRepository = eventRepository;
        this.trainingService = trainingService;
    }


    public TrainingEvent saveEvent(TrainingEvent event) {
        Training training = event.getTraining();
        training.setEvent(event);
        training.getExercises().forEach(exr -> exr.setTraining(training));
        TrainingEvent savedEvent = eventRepository.save(event);
        trainingService.saveTraining(training);
        return savedEvent;
    }


    public Optional<Training> findTrainingById(long eventId) {
        return eventRepository.findById(eventId).map(TrainingEvent::getTraining);
    }
}