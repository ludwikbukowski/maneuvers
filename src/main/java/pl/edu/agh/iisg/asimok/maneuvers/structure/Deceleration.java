package pl.edu.agh.iisg.asimok.maneuvers.structure;

import java.util.Date;

/**
 * Created by ludwikbukowski on 21/06/18.
 */
public class Deceleration {
    public final double startAcc;
    public final double endAcc;
    public final double startSpeed;
    public final double endSpeed;

    public Date startTimestamp;
    public Date endTimestamp;

    public Deceleration(double sA, double eA, double sS, double eS, Date sT, Date eT){
        this.startAcc = sA;
        this.endAcc = eA;
        this.startSpeed = sS;
        this.endSpeed = eS;
        this.startTimestamp = sT;
        this.endTimestamp = eT;
    }
    public Deceleration merge(Deceleration other){
        Deceleration res = new Deceleration(this.startAcc, other.endAcc,
                this.startSpeed, other.endSpeed, this.startTimestamp, other.endTimestamp);
        return res;
    }

    public void log(String title){
        System.out.println("-----Logging Deceleration:[" + title + "}");
        System.out.println("startAcc: " + startAcc);
        System.out.println("startSpeed: " + startSpeed);
        System.out.println("endAcc: " + endAcc);
        System.out.println("endSpeed: " + endSpeed);
        System.out.println("startTimestamp: " + startTimestamp);
        System.out.println("endTimestamp: " + endTimestamp);
        System.out.println("-------------------------");
    }
}
