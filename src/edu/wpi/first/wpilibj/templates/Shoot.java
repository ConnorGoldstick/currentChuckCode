/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;
import java.util.Timer;

public class Shoot extends Thread {

    Victor victor;
    Solenoid sol4, sol5, sol7, sol8;
    int count;
    Unload Unload;
    int catapaultExtensionTime = 100;

    public Shoot(Solenoid s4, Solenoid s5, Solenoid s7, Solenoid s8) {
        sol4 = s4;
        sol5 = s5;
        sol7 = s7;
        sol8 = s8;
        
        count = 0;
    }
    
    public void setShotTime(int length){
        catapaultExtensionTime = length;
    }

    public void setCountToZero() {
        count = 0;
    }

    public void Shoot() {
        if (count < catapaultExtensionTime) {
            count++;
            catapaultShoot();
        }
        if (count >= catapaultExtensionTime) {
            catapaultUnshoot();
        }
    }

    public void catapaultShoot() {
        sol7.set(false);
        sol8.set(true);
    }

    public void catapaultUnshoot() {
        sol7.set(true);
        sol8.set(false);
    }
}
