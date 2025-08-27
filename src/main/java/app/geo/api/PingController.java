package app.geo.api;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class PingController {
  @GetMapping("/ping")
  public Map<String,String> ping(){ return Map.of("pong","ok"); }
}
