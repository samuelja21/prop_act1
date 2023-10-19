package Exercici2;

import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * @author Oriol
 */
public class Leader extends TeamRobot {

    private int enemics;
    private int teamMates;
    private String objectiu;
    private List<String> robotsMorts = new ArrayList<>();
    private double posCano;
    private int tiks;
    
    @Override
    public void run() {
        //Radar independent
        setBodyColor(Color.white);
        setGunColor( Color.white);
        setRadarColor(Color.blue);
        setScanColor(Color.white);
        setBulletColor(Color.blue);
        setAdjustRadarForRobotTurn(false);
        setAdjustGunForRobotTurn(true);

        //Inicialitzem valors
        enemics = getOthers() - getTeammates().length;
        teamMates = getTeammates().length + 1;
        posCano=10;
        tiks=0;
        objectiu=null;
       
        //Per defecte
        while (true) {
                turnGunRight(posCano);
                tiks++;
                if(tiks>2)posCano=-10;
                if(tiks>5)posCano=10;
                //if(tiks>11)objectiu=null;
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if(objectiu!=null && !e.getName().equals(objectiu))return;
        if (isTeammate(e.getName()))return;
        if(objectiu != null && !e.getName().equals(objectiu) && this.robotsMorts.isEmpty() && !(e.getEnergy()>199.0))return;
        objectiu=e.getName();
        tiks=0;
        posCano=normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        double enemyX = (this.getX() + Math.sin(posCano) * e.getDistance());
        double enemyY = (this.getY() + Math.cos(posCano) * e.getDistance());
        //broadcastMessage(e.getName(),enemyX,enemyY,e.getBearing());
        if(e.getDistance()>150){
            turnGunRight(posCano); 
            turnRight(e.getBearing());
            ahead(e.getDistance() - 140);
        }
        else{
            turnGunRight(posCano);
            fire(3);
            if (e.getDistance() < 100) {
                            if (e.getBearing() > -90 && e.getBearing() <= 90) {
                                    back(40);
                            } else {
                                    ahead(40);
                            }
                    }
                    scan();
            }
        }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        String nom = e.getName();
        if (isTeammate(nom)) {
            teamMates--;
        } else {
            enemics--;
            this.robotsMorts.add(nom);
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        ahead(-100);
    }
    
    @Override
    public void onHitRobot(HitRobotEvent e){
        
    }
}
