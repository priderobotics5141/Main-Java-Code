/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.GenericHID;
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
//http://10.51.41.11:5801/ pipeline for camera
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  Joystick gamePad0 = new Joystick (0);
  boolean toggA = false;
  boolean toggB = false;
  boolean toggX= false;
  boolean toggY = false;
  VictorSP left1 = new VictorSP(1);
  SpeedControllerGroup leftDrive = new SpeedControllerGroup(left1);
  VictorSP right1 = new VictorSP(7);
  SpeedControllerGroup rightDrive = new SpeedControllerGroup(right1);
  DifferentialDrive driveTrain = new DifferentialDrive(rightDrive, leftDrive);
  VictorSP tinyArm = new VictorSP(9);
  boolean toggArm = false; 
  Servo simon = new Servo(0);
  DigitalInput limitSwitch0 = new DigitalInput(0);
  DigitalInput limitSwitch1 = new DigitalInput(1);
  Compressor c = new Compressor(0);
  Solenoid s0 = new Solenoid(0);
  Solenoid s1 = new Solenoid(1);
  Solenoid s2 = new Solenoid(2);
  Solenoid s3 = new Solenoid(3);

  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry ta = table.getEntry("ta");
  
  //read values periodically
  double x = tx.getDouble(0.0);
  double y = ty.getDouble(0.0);
  double area = ta.getDouble(0.0);
  
  Timer timer = new Timer();
  Timer timer2 = timer;
  public void solenoidAll(boolean aBoolean){
    s0.set(aBoolean);
    s1.set(aBoolean);
    s2.set(!aBoolean);
    s3.set(aBoolean);

  }
  
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
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
    System.out.println("helloHumans");
    s0.set(true);
    s1.set(true);
    s2.set(true);
    s3.set(true);
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

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopInit() {
    timer2.reset();
    timer2.start();
  }
  
  /**
   * This function is something
   */
  @Override
  public void teleopPeriodic() {
    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightArea", area);
  
    double rightStick = (gamePad0.getRawAxis(5)*.6)*(gamePad0.getRawAxis(3)+1);
    double leftStick = (gamePad0.getRawAxis(1)*.6)*(gamePad0.getRawAxis(3)+1); 
    driveTrain.tankDrive(leftStick,rightStick);
    
    //if(gamePad0.getRawButtonPressed(1)){toggA = !toggA;}
    //if(gamePad0.getRawButtonPressed(2)){toggB = !toggB;}
    if(gamePad0.getRawButtonPressed(3)){toggX = !toggX;}
    //if(gamePad0.getRawButtonPressed(4)){toggY = !toggY;}
    
    //if(toggA){solenoidAll(true);}
    //else{solenoidAll(false);}
    if(toggX){c.setClosedLoopControl(true);}
    else{c.setClosedLoopControl(false);}
    //if(toggY){simon.set(2);}
    //else{simon.set(0);}
    if(gamePad0.getRawButtonPressed(1)){toggA = true;}
    else{toggA = false;}
    if(gamePad0.getRawButtonPressed(2)){toggB = true;}
    else{toggB = false;}

    if(toggA == true && limitSwitch0.get() == true)
    {tinyArm.set(0.5);}
    if(toggB == true && limitSwitch1.get() == true)
    {tinyArm.set(-0.5);}
    
    
    if(timer2.get()<(2)){tinyArm.set(0.5);}
    if(timer2.get()>(2)){tinyArm.set(0);}
    if(limitSwitch0.get() == false && toggA == false){tinyArm.set(0);}
    if(limitSwitch1.get() == false && toggA == false){tinyArm.set(0);}
    
    if(gamePad0.getRawButtonPressed(4)){
      System.out.println(timer2.get());} 



     // System.out.println(timer.get());
      
    }
    
    //System.out.println(limitSwitch0.get());
  
    /** 
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
