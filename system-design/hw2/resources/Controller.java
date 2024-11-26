@RestController
@RequestMapping("/sport_app")
public class Controller {
    private final UserService userService;
    private final TrainingService trainingService;
    private final EventService eventService;
    private final PlanService planService;


    @Autowired
    public Controller(UserService userService, TrainingService trainingService, EventService eventService, PlanService planService) {
        this.userService = userService;
        this.trainingService = trainingService;
        this.eventService = eventService;
        this.planService = planService;
    }



    // USER

    @PostMapping("register")
    public @ResponseBody
    CompletableFuture<ResponseEntity<?>> registerUser(@RequestBody UserRegistrationDto userDto) {
        User user = this.mapper.map(userDto, User.class);
        if (userService.existsByLogin(user.getLogin())) {
            return CompletableFuture.supplyAsync(() -> ResponseEntity.status(HttpStatus.CONFLICT).body("User with this login already exists"));
        }

        User registeredUser = userService.registerUser(user);
        CompletableFuture<User> future = CompletableFuture.supplyAsync(() -> registeredUser);
        if (!registeredUser.equals(user)) {
            return future.thenApply(result -> ResponseEntity.status(HttpStatus.VARIANT_ALSO_NEGOTIATES).body("Registered user is different from required"));
        }
        return future.thenApply(result -> ResponseEntity.status(HttpStatus.CREATED).body(registeredUser.getId()));
    }


    @GetMapping("getUserById/{userId}")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> getUser(@PathVariable(value = "userId") long userId) {
        return userService.getUser(userId)
                .<CompletableFuture<ResponseEntity<?>>>map(user -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.OK).body(user)))
                .orElseGet(() -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Required userId doesn't found")));
    }


    // TRAINING
    @PostMapping("createTraining")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> createTraining(@RequestBody TrainingDto trainingDto) {
        Training training = this.mapper.map(trainingDto, Training.class);

        Training savedTraining = trainingService.saveTraining(training);
        CompletableFuture<Training> future = CompletableFuture.supplyAsync(() -> savedTraining);
        if (!savedTraining.equals(training)) {
            return future.thenApply(result -> ResponseEntity.status(HttpStatus.VARIANT_ALSO_NEGOTIATES).body("Saved training is different from required"));
        }
        return future.thenApply(result -> ResponseEntity.status(HttpStatus.CREATED).body(savedTraining.getTrainId()));
    }


    @GetMapping("getAllExerciseByTrain/{trainId}")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> getAllExerciseByTrainId(@PathVariable(value = "trainId") long trainId) {
        return trainingService.findById(trainId)
                .<CompletableFuture<ResponseEntity<?>>>map(training -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.OK).body(
                                training.getExercises().stream()
                                        .map(exr -> this.mapper.map(exr, ExerciseDto.class))
                                        .toList())))
                .orElseGet(() -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Required training doesn't found")));
    }



    // EVENT
    @GetMapping("getTrainingByEvent/{eventId}")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> getTrainingByEventId(@PathVariable(value = "eventId") long eventId) {
        return eventService.findTrainingById(eventId)
                .<CompletableFuture<ResponseEntity<?>>>map(training -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.OK).body(mapper.map(training, TrainingDto.class))))
                .orElseGet(() -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Required training doesn't found")));
    }


    // PLAN
    @PostMapping("createPlan")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> createPlan(@RequestBody PlanDto planDto) {
        Plan plan = this.mapper.map(planDto, Plan.class);

        Plan savedPlan = planService.savePlan(plan);
        CompletableFuture<Plan> future = CompletableFuture.supplyAsync(() -> savedPlan);
        if (!savedPlan.equals(plan)) {
            return future.thenApply(result -> ResponseEntity.status(HttpStatus.VARIANT_ALSO_NEGOTIATES).body("Saved plan is different from required"));
        }
        return future.thenApply(result -> ResponseEntity.status(HttpStatus.CREATED).body(savedPlan.getPlanId()));
    }


    @GetMapping("getPlan/{planId}")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> getPlanByPlanId(@PathVariable(value = "planId") long planId) {
        return planService.findPlanByPlanId(planId)
                .<CompletableFuture<ResponseEntity<?>>>map(plan -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.OK).body(mapper.map(plan, PlanDto.class))))
                .orElseGet(() -> CompletableFuture.supplyAsync(
                        () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan with this ID doesn't exist")));

    }
}