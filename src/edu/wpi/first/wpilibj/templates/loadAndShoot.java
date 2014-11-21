/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class loadAndShoot extends Thread {

    boolean running = false;
    Solenoid sol4, sol5, sol7, sol8;
    Jaguar victor;
    AnalogChannel encoder;
    DigitalInput digi14, digi13, digi3;
    Joystick xBox;
    Shoot shooter;
    //DigitalOutput readyShoot;
    boolean shooting = false; //starts the catapult
    int ShootCount = 0;
    Load Load;
    boolean loadingWithBall = false; //loads with more force, to compensate for the ball
    boolean loadingWithoutBall = false; //loads with less force
    int endLoadCount = 0;
    Unload Unload;
    boolean unloading = false; //extends the suction cup
    SuckUpBall SuckUpBall;
    boolean sucking = false; //begins the autosuction
    int suckingCount = 0;
    SmartDashboard smart;
    public final double LOADARM_REST_ANGLE = 3.65;
    public final double LOADARM_REST_ANGLE2 = 3.75;
    public final double LOADARM_REST_ADJUSTMENT_SPEED = 0.3;
    public final double LOADARM_UNLOADED_MIN_THESHOLD = 2.5;
    public final double LOADARM_UNLOADED_THESHOLD = 3.7;
    public final int SHOOT_COUNT_MAX = 105;
    public final int SUCTION_COUNT_MAX = 100;

    public loadAndShoot(AnalogChannel e, Jaguar v, Solenoid s4, Solenoid s5, Solenoid s7, Solenoid s8, Joystick x, DigitalInput d14, DigitalInput d13, DigitalInput d3, SmartDashboard sd/*
             * , DigitalOutput rs
             */) {
        victor = v;
        encoder = e;

        sol4 = s4;
        sol5 = s5;

        sol7 = s7;
        sol8 = s8;

        xBox = x;

        digi14 = d14;
        digi13 = d13;
        digi3 = d3;

        smart = sd;
        //readyShoot = rs;

        shooter = new Shoot(sol4, sol5, sol7, sol8);
        Load = new Load(victor, encoder);
        Unload = new Unload(victor, sol4, sol5);
        SuckUpBall = new SuckUpBall(victor, sol4, sol5);
    }

    public void setRun(boolean run) {
        running = run;
    }

    public void teleoperatedInit() {
        suctionOff();
        victor.set(0);
        shooting = false;
        loadingWithBall = false;
        loadingWithoutBall = false;
        endLoadCount = 0;
        unloading = false;
        ShootCount = 0;
        sucking = false;
        suckingCount = 0;
    }

    public void resetBooleans() {
        shooting = false;
        loadingWithBall = false;
        loadingWithoutBall = false;
        unloading = false;
        sucking = false;
        suctionOff();
        noShooty();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (Exception ex) {
            }
            while (running) {
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                }
                boolean xButton = xBox.getRawButton(3);
                boolean yButton = xBox.getRawButton(4);
                boolean leftBumper = xBox.getRawButton(5);
                boolean rightBumper = xBox.getRawButton(6);
                double leftRightTrigger = xBox.getRawAxis(3);
                double dPad = xBox.getRawAxis(6);

                if (dPad == -1) {
                    System.out.println("reset everything");
                    reset();
                }
                System.out.println(encoder.getAverageVoltage());

                //begin autosuction and manual suction
                if (xButton) { //toggle off
                    System.out.println("toggle suction off");
                    suctionOff();
                    sucking = false;
                }
                if (leftRightTrigger > .95 && !loadingWithBall && !loadingWithoutBall && !unloading && !shooting && !sucking && !digi3.get()) {
                    System.out.println("start suction");
                    sucking = true; //checks to see if ball is touching limit switch
                }
                if (sucking) { //autosuction occurs as long as this is true
                    System.out.println("suctioning");
                    suckingCount++;
                    SuckUpBall.suckItUp(); //suction command
                }
                if (suckingCount > SUCTION_COUNT_MAX) { //ends autosuction
                    System.out.println("end suction");
                    suckingCount = 0;
                    endSuctionMovement();
                }
                //end autosuction and manual suction

                //start end load
                if (digi3.get() && !unloading) {//stops the loading
                    endLoading();
                    setReadyShootLED();
                }
                // end end load

                //begin loading
                if (rightBumper && !loadingWithBall && !loadingWithoutBall && !unloading && !shooting) {
                    System.out.println("start loading");
                    //checks to see if suction is on or not
                    if (returnSuctionValue() == true) { //suctioning
                        System.out.println("start load w/ ball");
                        loadingWithBall = true;
                    }
                    if (returnSuctionValue() == false) { //not suctioning
                        System.out.println("start load w/out ball");
                        loadingWithoutBall = true;
                    }
                }
                if (loadingWithBall) { //runs load with ball
                    System.out.println("load w/ ball");
                    Load.loadWithBall(); //loadWithBall command
                }
                if (loadingWithoutBall) { //runs load without ball
                    System.out.println("load w/out ball");
                    Load.loadWithoutBall(); //loadWithoutBall command
                }
                //end loading

                //begin shooter
                if (leftRightTrigger < -.95 && !loadingWithBall && !loadingWithoutBall && !unloading && !shooting) {
                    System.out.println("start shooting");
                    setReadyShootLED();
                    shooting = true;
                    shooter.setCountToZero(); //resets the count in the class to zero
                    shooter.setShotTime(100);
                }
                System.out.println("Y Button: " + yButton);
                if (yButton == true && !loadingWithBall && !loadingWithoutBall && !unloading && !shooting) {
                    System.out.println("start shooting");
                    setReadyShootLED();
                    shooting = true;
                    shooter.setCountToZero(); //resets the count in the class to zero
                    shooter.setShotTime(4);
                }
                if (shooting) { //currently shooting
                    System.out.println("shooting");
                    ShootCount++;
                    shooter.Shoot(); //shoot command
                }
                if (ShootCount > SHOOT_COUNT_MAX) { //turns off the shoot
                    System.out.println("end shoot");
                    shooting = false;
                    ShootCount = 0;
                }
                //end shooter

                //begin unloading
                if (leftBumper && !loadingWithBall && !loadingWithoutBall && !unloading && !shooting) {
                    unloading = true;
                    System.out.println("start unload");
                }
                if (unloading) { //suction arm extends
                    System.out.println("unloading");
                    Unload.unload(); //unload command
                }
                if (encoder.getVoltage() >= LOADARM_UNLOADED_MIN_THESHOLD && encoder.getVoltage() <= LOADARM_UNLOADED_THESHOLD && (unloading)) { //ends unloading
                    //should add something to stop unloading the arm early
                    System.out.println("end unload");
                    endUnload();
                }
                //end Unload

                if (!loadingWithBall && !loadingWithoutBall && !sucking && !unloading && !digi3.get()) {
                    if (encoder.getAverageVoltage() < LOADARM_REST_ANGLE) {
                        victor.set(LOADARM_REST_ADJUSTMENT_SPEED);
                        System.out.println("adjusting");
                    } else if (encoder.getAverageVoltage() > LOADARM_REST_ANGLE2) {
                        victor.set(-LOADARM_REST_ADJUSTMENT_SPEED);
                        System.out.println("adjusting");
                    } else {
                        victor.set(0);
                        System.out.println("adjusted");
                    }
                }
            }
        }
    }

    public void endUnload() {
        unloading = false;
        victor.set(0); //stops the victor
    }

    public void reset() {
        loadingWithBall = false;
        loadingWithoutBall = false;
        unloading = false;
        shooting = false;
        sucking = false;
        sol7.set(true);
        sol8.set(false);
    }

    public void suctionOff() {
        sol4.set(false);
        sol5.set(true);
        victor.set(0);
    }

    public void noShooty() {
        sol7.set(true);
        sol8.set(false);
    }

    public void endLoading() {
        victor.set(0);
        suctionOff();
        loadingWithBall = false;
        loadingWithoutBall = false;
    }

    public boolean returnSuctionValue() {
        if (sol4.get() == true && sol5.get() == false) {
            return true;
        } else {
            return false;
        }
    }

    public void endSuctionMovement() {
        victor.set(0); //stops the victor movement
        sucking = false;
    }

    public void setReadyShootLED() {
        if (loadingWithBall) {
            //readyShoot.set(true);
        }
        if (shooting) {
            //readyShoot.set(false);
        }
    }
}
