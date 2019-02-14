/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
  VictorSP arm = new VictorSP(5);

  Timer autoPilotTimer = new Timer();

  int autoPilotStep = 0;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    rightDrive.setInverted(false);// Set true for Flash, set false for Simon/Tank
    left0.setInverted(false);//false for simon and Dave
    left1.setInverted(true);// Set false for Dave, set true for Simon
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
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
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
  }
  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {    
   
    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");
    NetworkTableEntry tv = table.getEntry("tv");

    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double a = ta.getDouble(0.0);
    double v = tv.getDouble(0.0);

    double ratioX = x/27;
    double ratioY = y/40.5;
    double ratioA = 1-(a/20);

    double min = .3;
    double sineWithSignum = Math.signum(ratioX)*(1-min)*Math.sin(ratioX*Math.PI/2)+(1+min)/2;

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

    if (gamePad0.getRawButtonPressed(1) && v==1) {
      autoPilotStep = 1;}
    if (autoPilotStep == 1){
      if(v==1){driveTrain.tankDrive((ratioX*.4+.3)+ratioA*.3,(-(ratioX*.4)+.3)*12/13+ratioA*.3);}
       if (a > 14.5){autoPilotStep = 0;}
    }

    /*if (gamePad0.getRawButtonPressed(1) && v==1) {
      autoPilotStep = 1;
      autoPilotTimer.reset();
    }
    if (autoPilotStep == 1){
      if(v==1){
        driveTrain.tankDrive((ratioX+.3)+ratioA*.3,(-(ratioX)+.3)*12/13+ratioA*.3);
      }
      else autoPilotStep = 2;
    }
    if (autoPilotStep == 2){
      autoPilotTimer.reset();
      if(autoPilotTimer.get()<1.5){
        driveTrain.tankDrive(-.5,-.5*12/13);
      }
      else autoPilotStep=0;
    }
    if(gamePad0.getRawButtonPressed(8)){ //start button is kill switch for autoPilot
      autoPilotStep=0;
    }*/
        
      //driveTrain.tankDrive(.65*ratioY,.65*ratioY*12/13);
    
    if(autoPilotStep==0){
      SmartDashboard.putNumber("leftStick",gamePad0.getRawAxis(1));
      SmartDashboard.putNumber("rightStick",gamePad0.getRawAxis(5));
      double leftStick = (-gamePad0.getRawAxis(1)*.6)*(gamePad0.getRawAxis(3)+1);
      double rightStick = (-gamePad0.getRawAxis(5)*.6)*(gamePad0.getRawAxis(3)+1);
      driveTrain.tankDrive(leftStick,rightStick*12/13);//12/13 is motor ratio for simon
     }

    arm.set(-gamePad0.getRawAxis(2));
     
   }
  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
