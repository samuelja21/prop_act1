/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exercisi1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

/**
 *
 * @author samuel
 */
public class Predator extends TeamRobot{
    
    private int fase;
    private int enemics;
    private int teamMates;
    private int direccioOrbita;
    private double enemicX;
    private double enemicY;
    private double distanciaEnemic;
    private double enemicBearing;
    private double ultimBearing;
    private double vidaEnemic;
    private double tempsOrbita;
    private String objectiu;
    private List<String> robotsEnemics = new ArrayList<>();
    private List<String> robotsEnemicsLocal = new ArrayList<>();
    private List<Double> distanciesEnemics = new ArrayList<>();
    private List<String> robotsMorts = new ArrayList<>();
    
    private String obtenirObjectiu() {
        double min = this.distanciesEnemics.get(0);
        for (int i = 1; i < this.distanciesEnemics.size(); ++i){
            if (min > this.distanciesEnemics.get(i)) min = this.distanciesEnemics.get(i);
        }
        return this.robotsEnemics.get(this.distanciesEnemics.indexOf(min));
    }
    
    private void actualitzarFase(){
        switch (fase){
            case 0:
                if (this.getTime() >= 60){
                    fase = 1;
                    objectiu = obtenirObjectiu();
                }
                break;
            case 1:
                if (distanciaEnemic <= this.getBattleFieldWidth()*0.3){
                    setAdjustGunForRobotTurn(false);
                    turnGunRight(90);
                    turnLeft(90);
                    if (tempsOrbita == 0){
                        double time = this.getTime();
                        tempsOrbita = time;
                        String msg = "RobotOrbita," + time;
                        try {
                            this.broadcastMessage(msg);
                        } catch (IOException ex) {
                        } 
                    }
                    fase = 2;
                } 
                break;
            case 2:
                if (vidaEnemic <= 50){
                    turnGunLeft(90);
                    turnRight(90);
                    fase = 3;
                }
                break;
            case 3:
                if (robotsMorts.contains(objectiu)){
                    fase = 0;
                    this.distanciesEnemics.clear();
                    this.robotsEnemics.clear();
                    this.robotsEnemicsLocal.clear();
                    enemicX = -1;
                    enemicY = -1;
                    enemicBearing = -1;
                    tempsOrbita = 0;
                    back(200);
                }
                break;
        }
    }
    
    private void dirigirseARobot(){
        double costat_x = enemicX-this.getX();
        double costat_y = enemicY-this.getY();
        double angle = Math.toDegrees(Math.atan2(costat_x, costat_y));
        double gir = angle - this.getHeading();
        if (gir > 180) gir = gir - 360;
        if (gir < -180) gir = 360 + gir;
        if (fase == 1) setFire(1);
        setTurnRight(gir);
        if (fase == 1) setFire(2);
        setAhead(recorrerDistancia());
        if (fase == 1) setFire(3);
        execute();
    }
    
    private double recorrerDistancia(){
        double distanciaMax = this.getBattleFieldWidth()*0.3;
        double recorregut = 0;
        if (fase == 1){
            if (distanciaEnemic > distanciaMax){
                if ((distanciaEnemic - 100) <= distanciaMax) recorregut = 100;
                else recorregut = distanciaEnemic - distanciaMax;
            }
        } else recorregut = distanciaEnemic;
        return recorregut;
    }
    
    private void orbitarRobot(){
        double angle = 0;
        angle = enemicBearing - 90;
        if (angle > 180) angle = angle - 360;
        else if (angle < -180) angle = 360 - angle;
        setTurnRight(angle);
        setFire(1);
        setAhead(25 * direccioOrbita);
        execute();
        fire(4);
    }
    
    private void gestionarCanviDireccio(){
        double time = this.getTime();
        if ((time - tempsOrbita) >= 120){
            tempsOrbita = time;
            direccioOrbita *= -1;
            System.out.println("Cambio en " + time);
            String msg = "CanviDeSentit," + time;
            try {
                this.broadcastMessage(msg);
            } catch (IOException ex) {
            } 
        }
    }
    
    private void gestionarMoviment(){
        switch (fase){
            case 0:
                turnRadarRight(360);
                break;
            case 1:
                turnRadarRight(360);
                if (enemicX != -1 && enemicY != -1) dirigirseARobot();
                break;
            case 2:
                setTurnRadarRight(360);
                orbitarRobot();
                gestionarCanviDireccio();
                break;
            case 3:
                turnRadarRight(360);
                dirigirseARobot();
                break;
        }
    }
    
    @Override
    public void run(){
        setAdjustRadarForRobotTurn(false);
        fase = 0;
        enemics = getOthers() - getTeammates().length;
        teamMates = getTeammates().length + 1;
        enemicX = enemicY = -1;
        enemicBearing = -1;
        direccioOrbita = 1;
        tempsOrbita = 0;
        while(true){
            gestionarMoviment();
            actualitzarFase();
        }
        
    }
    
    @Override
    public void onScannedRobot (ScannedRobotEvent e){
        switch (fase){
            case 0:
                if (!isTeammate(e.getName())){
                    boolean enviarMissatge = false;
                    if (!robotsEnemicsLocal.contains(e.getName())){
                        robotsEnemicsLocal.add(e.getName());
                        if (!robotsEnemics.contains(e.getName())){
                            robotsEnemics.add(e.getName());
                            distanciesEnemics.add(e.getDistance());
                            enviarMissatge = true;
                        } else {
                            int i = this.robotsEnemics.indexOf(e.getName());
                            if (e.getDistance() > distanciesEnemics.get(i)) {
                                distanciesEnemics.set(i, e.getDistance());
                                enviarMissatge = true;
                            }
                            }
                        }
                        if (enviarMissatge){
                            String msg = "EnemicDetectat," + e.getName() + "," + e.getDistance();
                            try {
                                this.broadcastMessage(msg);
                            } catch (IOException ex) {
                            }    
                        }
      
                    } 
                
                break;
            
            default:
                if (e.getName().equals(this.objectiu)){
                    double angle = this.getHeading() + e.getBearing();
                    distanciaEnemic = e.getDistance();
                    enemicX = this.getX() + e.getDistance() * Math.sin(Math.toRadians(angle));
                    enemicY = this.getY() + e.getDistance() * Math.cos(Math.toRadians(angle));
                    enemicBearing = e.getBearing();
                    vidaEnemic = e.getLife();
                }
        }
        
    }
    
    @Override
    public void onMessageReceived(MessageEvent e){
        String missatge = (String)e.getMessage();
        String[] parts = missatge.split(",");
        String tipus = parts[0];
        if (tipus.equals("EnemicDetectat")){
            String nom = parts[1];
            double distancia = Double.parseDouble(parts[2]);
            if (!robotsEnemics.contains(nom)){
                robotsEnemics.add(nom);
                distanciesEnemics.add(distancia);
            }
            else if (distanciesEnemics.get(robotsEnemics.indexOf(nom)) < distancia){
                distanciesEnemics.set(robotsEnemics.indexOf(nom), distancia);
            }
        }
        else if (tipus.equals("RobotOrbita")){
            tempsOrbita = Double.parseDouble(parts[1]);
        }
        else if (tipus.equals("CanviDeSentit")){
            direccioOrbita *= -1;
            tempsOrbita = Double.parseDouble(parts[1]);
        }
    }
    
    @Override
    public void onRobotDeath(RobotDeathEvent e){
        String nom = e.getName();
        if (isTeammate(nom)){
            teamMates --;
        } else {
            enemics --;
            this.robotsMorts.add(nom);
        }
    }
    
    @Override
    public void onHitWall(HitWallEvent e){
        if (fase == 2){
            double temps = this.getTime();
            String msg = "CanviDeSentit," + temps;
            try {
                this.broadcastMessage(msg);
            } catch (IOException ex) {
            } 
            direccioOrbita *= -1;
            tempsOrbita = temps;
        }
    }
    
}
