package univ.lecture.riotapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tchi on 2017. 4. 1..
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
//public class Summoner {
//    private String name;
//    private int summonerLevel;
//}
public class Calculate {
    private String teamId;
    private int now;
    private double result;
    //private String msg;
}
