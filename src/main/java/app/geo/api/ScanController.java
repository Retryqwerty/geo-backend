package app.geo.api;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static java.lang.Math.*;

/** Детермінована "шумова" сітка 20м, стабільна для кожної клітини */
@RestController
@RequestMapping("/v1")
public class ScanController {

  record ScanCell(double lat, double lon, double density){}
  record ScanResult(double quality, double rate, double buildCost, List<ScanCell> cells){}

  @GetMapping("/scan")
  public ScanResult scan(@RequestParam double lat, @RequestParam double lon, @RequestParam(defaultValue="CLAY") String resource){
    // ключ клітини 20м (≈0.00018°) — для стабільності heat
    double step = 0.00018; 
    int cx = (int) floor(lat/step);
    int cy = (int) floor(lon/step);

    Random rnd = new Random(hash(resource+":"+cx+":"+cy));
    double base = 0.2 + rnd.nextDouble()*0.6; // 0.2..0.8
    double quality = round(base*100.0)/100.0;

    double rate = round((0.5 + quality)*100.0)/100.0;         // т/год (спрощено)
    double buildCost = round((80000 + quality*300000)/10.0)*10.0; // грн (спрощено)

    // заповнюємо кружок 300м (~0.0027°) клітинами 20м
    double radius = 0.0027;
    List<ScanCell> cells = new ArrayList<>();
    for(double a = -radius; a <= radius; a+=step){
      for(double b = -radius; b <= radius; b+=step){
        if(a*a + b*b <= radius*radius){
          double clat = lat + a;
          double clon = lon + b;
          double density = cellNoise(resource, clat, clon); // 0..1 детерміновано
          cells.add(new ScanCell(clat, clon, round(density*100.0)/100.0));
        }
      }
    }
    return new ScanResult(quality, rate, buildCost, cells);
  }

  private static long hash(String s){
    long h=1125899906842597L;
    for(int i=0;i<s.length();i++) h=31*h + s.charAt(i);
    return h;
  }
  /** стабільний "noise" на клітинку */
  private static double cellNoise(String res, double lat, double lon){
    long h = hash(res + ":" + floor(lat*5555) + ":" + floor(lon*5555));
    // xorshift
    h ^= (h<<13); h ^= (h>>7); h ^= (h<<17);
    double v = (h >>> 1) / (double)(Long.MAX_VALUE);
    return max(0, min(1, v));
  }
}
