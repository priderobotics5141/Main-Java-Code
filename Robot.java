/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.kauailabs.navx.frc.AHRS;
import com.kauailabs.navx.frc.AHRS.SerialDataType;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Servo;

import edu.wpi.first.wpilibj.Timer;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  Joystick gamePad0 = new Joystick (0);
  VictorSP left0 = new VictorSP(0);
  VictorSP left1 = new VictorSP(1);
  SpeedControllerGroup leftDrive = new SpeedControllerGroup(left0,left1);
  VictorSP right0 = new VictorSP(2);
  VictorSP right1 = new VictorSP(3);
  SpeedControllerGroup rightDrive = new SpeedControllerGroup(right0,right1);
  DifferentialDrive driveTrain = new DifferentialDrive(leftDrive, rightDrive);
  VictorSP frontFoot = new VictorSP(4); //speed: 40 rotations per 30 seconds
  VictorSP arm = new VictorSP(5);
  VictorSP rearFoot = new VictorSP(6); //speed: 40.5 to 41 rotations per 30 seconds
  Servo simon = new Servo(7);
  AHRS navx;

  Timer autoPilotTimer = new Timer();

  int autoPilotStep = 0;
  int navxStep = 0;
  double targetAngle;
  int autoLiftStep = 0;
  boolean autoFace;
  double autoFaceTimeNeeded;
  boolean doAutoPilotNow = false;
  boolean b = true;
  double tempRatioX;
  boolean doubleAuto=false;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    navx = new AHRS(SerialPort.Port.kMXP, SerialDataType.kProcessedData, (byte)50);
    rightDrive.setInverted(false);// Set true for Flash, set false for Simon/Tank
    left0.setInverted(false);//false for simon and Dave
    left1.setInverted(true);// Set false for Dave, set true for Simon
    CameraServer.getInstance().startAutomaticCapture();
    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    teleopInit();
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    teleopPeriodic();
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }
  @Override
  public void teleopInit() {
    autoPilotTimer.start();
    navx.reset();
    navx.zeroYaw();
  }
  public static double normalizeAngle(double angle){
    return angle-360*Math.round(angle/360.0);
  }
  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    
    double yaw = navx.getYaw();
    
   
    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");
    NetworkTableEntry tv = table.getEntry("tv");

    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double a = ta.getDouble(0.0);
    double v = tv.getDouble(0.0);

    double ratioX = (x-6)/27;
    double ratioY = y/40.5;
    double ratioA = 1-(a/20);

    double min = .34;
    double sineWithSignum = Math.signum(ratioX)*(1-min)*Math.sin(ratioX*Math.PI/2)+(1+min)/2;
    double sineLeft = ((ratioA+min)-((ratioA+min)-min)/2)*Math.sin(ratioX*Math.PI/2)+((ratioA+min)+min)/2;
    //(ratioA+min) = max
    //min = min
    double sineRight = (min-((ratioA+min)-min)/2)*Math.sin(ratioX*Math.PI/2)+((ratioA+min)+min)/2;
    double newNewDrive = sineWithSignum + min + (ratioA*.3);



    //double correctionRatio = -ratioX + 1;
   // double correctionX = (Math.signum(-ratioX)*ratioX*ratioX)+(2*ratioX)+(Math.signum(ratioX)*.2);
    //double correctionX = Math.sin(1.2*ratioX + .2);
    double correctionX = (Math.signum(ratioX) == 1) ? Math.sin(1.2*ratioX + .2): Math.sin(1.2*ratioX - .2);


    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightA", a);
    SmartDashboard.putNumber("correctionX", correctionX);  
    SmartDashboard.putNumber("ratioX",ratioX);
    SmartDashboard.putNumber("D-Pad",gamePad0.getPOV());
    SmartDashboard.putNumber("Yaw",navx.getYaw());
    SmartDashboard.putNumber("NavxStep",navxStep);
    SmartDashboard.putBoolean("Auto",doAutoPilotNow);
    
    if (gamePad0.getRawButtonPressed(3)) {
      targetAngle = -90;
      autoFace = true;
      autoFaceTimeNeeded = Math.abs((targetAngle-navx.getYaw()))/100.; //determine amount of time needed to automatically turn
      autoPilotTimer.reset();
      autoPilotTimer.start();
      doubleAuto = true;
    }
    if (gamePad0.getRawButtonPressed(2)) {
      targetAngle = 90;
      autoFace = true;
      autoFaceTimeNeeded = Math.abs((targetAngle-navx.getYaw()))/100.; //determine amount of time needed to automatically turn
      autoPilotTimer.reset();
      autoPilotTimer.start();
      doubleAuto = true;
    }

    if ((gamePad0.getRawButtonPressed(1)) || doAutoPilotNow && v==1) {
      autoPilotStep = 1;}
    else{doAutoPilotNow=false;}
    if (autoPilotStep == 1){/*
      if (b) {tempRatioX = ratioX+(x/27);b=false;} //tempRatioX should change the 0
      //Change the 0 it must adjust to
      driveTrain.tankDrive(tempRatioX,tempRatioX);
      if (true) {autoPilotStep = 2;};
    }
    if (autoPilotStep == 2){*/
      if(v==1){driveTrain.tankDrive((sineLeft/*(ratioX*.5)+ratioA*.8*/)+.1,((sineRight*10/13/*(-(ratioX*.5))*12/13+ratioA*.8*/))+.1);}//.4
      if (a > 15){autoPilotStep = 0;doAutoPilotNow = false;b=true;}
    }
    if(gamePad0.getRawButtonPressed(8)){ //start button is kill switch for autoPilot, autoTurnLeft, and autoTurnRight
      autoPilotStep=0;
      doAutoPilotNow=false;
      doubleAuto=false;
      autoFace=false;
    }

      if(gamePad0.getRawButton(5)){simon.set(1);}
      else{simon.set(0);}
    
    if(autoPilotStep==0 && autoFace==false){
      SmartDashboard.putNumber("leftStick",gamePad0.getRawAxis(1));
      SmartDashboard.putNumber("rightStick",gamePad0.getRawAxis(5));
      double leftStick = (-gamePad0.getRawAxis(1)*.6)*(gamePad0.getRawAxis(3)+1);
      double rightStick = (-gamePad0.getRawAxis(5)*.6)*(gamePad0.getRawAxis(3)+1);
      driveTrain.tankDrive(leftStick,rightStick*12/13);//12/13 is motor ratio for simon
     }
     SmartDashboard.putNumber("normalized",normalizeAngle(navx.getYaw()+360));
    //arm negative is forward
    //rearFoot.set(gamePad0.getRawAxis(2)*(76./80));
    if(gamePad0.getRawButton(7)){frontFoot.set(-gamePad0.getRawAxis(2)*.7);}
    else frontFoot.set(gamePad0.getRawAxis(2)*.7);

/*     if(gamePad0.getRawButton(7)){rearFoot.set(-gamePad0.getRawAxis(2)*.7);}
    else rearFoot.set(gamePad0.getRawAxis(2)*.7);
    */
//trigger without button moves foot down, with button moves foot up
    if(gamePad0.getPOV()>180 && gamePad0.getPOV()<320 && gamePad0.getRawAxis(1) < .1 && gamePad0.getRawAxis(5) < .1){ //270 = D pad left
      
      navxStep += 1;
      if (navxStep == 1) {
//        navx.zeroYaw();
        targetAngle = -90;
        autoFace = true;
        autoFaceTimeNeeded = Math.abs((targetAngle-navx.getYaw()))/100.; //determine amount of time needed to automatically turn
        autoPilotTimer.reset();
        autoPilotTimer.start();
      }
    }
    else{navxStep = 0;};

    SmartDashboard.putNumber("targetAngle",targetAngle);
    SmartDashboard.putBoolean("autoFace",autoFace);
    SmartDashboard.putNumber("autoFaceTimeNeeded",autoFaceTimeNeeded);

    if(gamePad0.getPOV()>30 && gamePad0.getPOV()<150 && gamePad0.getRawAxis(1) < .1 && gamePad0.getRawAxis(5) < .1){ //90 = D pad right
      
      navxStep += 1;
      if (navxStep == 1) {
//        navx.zeroYaw();
        targetAngle = 90;
        autoFace = true;
        autoFaceTimeNeeded = Math.abs((targetAngle-navx.getYaw()))/100.; //determine amount of time needed to automatically turn
        autoPilotTimer.reset();
        autoPilotTimer.start();
      }
    }
    else{navxStep = 0;};

    SmartDashboard.putBoolean("autoFace",autoFace);

    if (autoFace){
      if(autoPilotTimer.get()<autoFaceTimeNeeded){
      double diff = (targetAngle-navx.getYaw())/70;
      driveTrain.tankDrive(diff*.5+Math.signum(diff)*min,-diff*.5-Math.signum(diff)*min);
      SmartDashboard.putNumber("diff",diff);
      }
      else {
        autoFace=false;
        if(doubleAuto){
          doAutoPilotNow = true;
        }
      }
    }

/*    if(gamePad0.getRawButtonPressed(4)){
      autoLiftStep=1;
    }
    */
    if(autoLiftStep==1){
      frontFoot.set(.5);
      rearFoot.set(.5);
    }

     
   }
  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
