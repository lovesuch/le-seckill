// Example Controller Implementation
@RestController
public class ExampleController {
    @Autowired
    private ExampleService exampleService;

    @GetMapping("/example")
    public String getExample() {
        return exampleService.getExample();
    }
}