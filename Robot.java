//Robot 2-11.java
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
import edu.wpi.first.wpilibj.GenericHID.RumbleType;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorMatch;
import edu.wpi.first.wpilibj.AnalogInput; //Ultrasonic Sensor


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

  private final I2C.Port cPort = I2C.Port.kOnboard;
  private final ColorSensorV3 cSensor = new ColorSensorV3(cPort);
  private final ColorMatch m_colorMatcher = new ColorMatch();
  private final Color BlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
  private final Color GreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
  private final Color RedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
  private final Color YellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

  private final AnalogInput ultrasonic = new AnalogInput(0);

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
    
    m_colorMatcher.addColorMatch(BlueTarget);
    m_colorMatcher.addColorMatch(GreenTarget);
    m_colorMatcher.addColorMatch(RedTarget);
    m_colorMatcher.addColorMatch(YellowTarget);
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
    int proximity = cSensor.getProximity();
    Color detectedColor = cSensor.getColor();
    String colorString;
    ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);
    
    if (match.color == BlueTarget) {
      colorString = "Blue" ;
    } else if (match.color == RedTarget) {
      colorString = "Red";
    } else if (match.color == GreenTarget) {
      colorString = "Green";
    } else if (match.color == YellowTarget) {
      colorString = "Yellow";
    } else {
      colorString = "Unknown";
    }
    //Scanner keyboard = new Scanner(System.in);
    //int myint = keyboard.nextInt();
    /**
     * The sensor returns a raw IR value of the infrared light detected.
     */
    double IR = cSensor.getIR();
    SmartDashboard.putNumber("Red", detectedColor.red);
    SmartDashboard.putNumber("Green", detectedColor.green);
    SmartDashboard.putNumber("Blue", detectedColor.blue);
    SmartDashboard.putNumber("IR", IR);
    SmartDashboard.putNumber("Proximity", proximity);
    SmartDashboard.putNumber("Confidence", match.confidence);
    SmartDashboard.putString("Detected Color", colorString);

    SmartDashboard.putNumber("UltraDistance", ultrasonic.getValue());
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

    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double a = ta.getDouble(0.0);

    double ratioX = x/27;
    double ratioY = y/20.5;
    double ratioA = 1-(a/20);

    double min = .3; //MIN_SOMETHING_SPEED
    double sineWithSignum = Math.signum(ratioX)*(1-min)*Math.sin(ratioX*Math.PI/2)+(1+min)/2; //sine curve of adjustment
    //double ogDrive = (ratioX*.7+min)+ratioA*.3;
    //double newDrive = (ratioX*.7)+ min + (ratioA*.3);

    double newNewDrive = sineWithSignum + min + (ratioA*.3);

    driveTrain.tankDrive(newNewDrive + min, 12/13 * newNewDrive + min); //minimum + adjustment/slow down

    //double correctionRatio = -ratioX + 1;
   // double correctionX = (Math.signum(-ratioX)*ratioX*ratioX)+(2*ratioX)+(Math.signum(ratioX)*.2);
    //double correctionX = Math.sin(1.2*ratioX + .2);
    //double correctionX = (Math.signum(ratioX) == 1) ? Math.sin(1.2*ratioX + .2): Math.sin(1.2*ratioX - .2);


    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightA", a);
    //SmartDashboard.putNumber("correctionX", correctionX);  
    SmartDashboard.putNumber("ratioX",ratioX);

    if (gamePad0.getRawButtonPressed(1)) {
      autoPilotStep = 1;
      autoPilotTimer.reset();
    }
    if (autoPilotStep == 1){
      if(autoPilotTimer.get()<.5){
        //driveTrain.tankDrive(-correctionX,correctionX*12/13);
      }
      else autoPilotStep = 2;
    }
    if (autoPilotStep == 2){
      autoPilotTimer.reset();
      if(autoPilotTimer.get()<3){
        //driveTrain.tankDrive(ratioY*.5, ratioY*.5);
      }
    }
    if(gamePad0.getRawButtonPressed(8)){ //start button is kill switch for autoPilot
      autoPilotStep=0;
    }
        
      //driveTrain.tankDrive(.65*ratioY,.65*ratioY*12/13);
    
    if(autoPilotStep==0){
      SmartDashboard.putNumber("leftStick",gamePad0.getRawAxis(1));
      SmartDashboard.putNumber("rightStick",gamePad0.getRawAxis(5));
      double leftStick = (-gamePad0.getRawAxis(1)*.6)*(gamePad0.getRawAxis(3)+1);
      double rightStick = (-gamePad0.getRawAxis(5)*.6)*(gamePad0.getRawAxis(3)+1);
      driveTrain.tankDrive(leftStick,rightStick*12/13);//12/13 is motor ratio for simon
     }
     arm.set(-gamePad0.getRawAxis(2));
     
     if(gamePad0.getRawButtonPressed(3)){
       //simon.set(.6);
       //vacuum.set(.5);
       gamePad0.setRumble(RumbleType.kRightRumble, 0.7);
       gamePad0.setRumble(RumbleType.kLeftRumble, 0.7);
     }
     else{
       //simon.set(.28);
       //vacuum.set(0);
       gamePad0.setRumble(RumbleType.kRightRumble, 0);
       gamePad0.setRumble(RumbleType.kLeftRumble, 0);
     }
 
   }
  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
