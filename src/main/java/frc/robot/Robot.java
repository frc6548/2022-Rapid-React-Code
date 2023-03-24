package frc.robot;

import edu.wpi.first.math.filter.SlewRateLimiter;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;

import edu.wpi.first.wpilibj.XboxController;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

public class Robot extends TimedRobot {

  // Motor Controllers
    public final PWMSparkMax m_leftMotor = new PWMSparkMax(0);
    public final PWMSparkMax m_rightMotor = new PWMSparkMax(1);
    public final PWMSparkMax m_roller = new PWMSparkMax(2);
    public final PWMSparkMax m_climberMain1 = new PWMSparkMax(3);
    public final PWMSparkMax m_climberSide = new PWMSparkMax(4);
    public final PWMSparkMax m_climberMain2 = new PWMSparkMax(5);

    // Differential Drive
    public final DifferentialDrive m_differentialDrive = new DifferentialDrive(m_leftMotor, m_rightMotor);

    // Xbox Controller and Slew Rate Limiter Filter for Joystick
    public final XboxController m_driver = new XboxController(0);
    public final XboxController m_operator = new XboxController(1);
    public final SlewRateLimiter filter = new SlewRateLimiter(3.0);

    // Pneumatic Bucket Double Solenoid, Climber Double Solenoid, and Compressor
    public final DoubleSolenoid m_bucketDS = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 2, 3);
    public final DoubleSolenoid m_climberDS = new DoubleSolenoid(PneumaticsModuleType.CTREPCM,0, 1);
    public final Compressor m_compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);

    // WPI Timer
    public final Timer m_timer = new Timer(); 

    // Shuffleboard Telemetry Subsystem
    public final Telemetry m_telemetry = new Telemetry();
    
    // Driver Mode Toggle of Drive < True = Driver Control : False = Operator Control >
    public boolean driverMode = true;

  @Override
  public void robotInit() {
    // Inverts One Side Of Drivetrain
    m_rightMotor.setInverted(true);

    // Inverts One Side of Main Climber
    m_climberMain2.setInverted(true);

    // Setting Pneumatics in the same direction
    m_bucketDS.set(Value.kReverse);
    m_climberDS.set(Value.kReverse);
  }

  @Override
  public void autonomousInit() {
    // Compressor on
    m_compressor.enableDigital();

    // Reset and Begin Timer
    m_timer.reset();
    m_timer.start();
  }

  @Override
  public void autonomousPeriodic() {
    if(m_timer.get() < 1 ){
      m_roller.set(-1);
    } else if(m_timer.get() < 3.5) {
      m_roller.set(0);
      m_differentialDrive.arcadeDrive(-0.6, 0);
    } else{
      m_bucketDS.set(Value.kForward);
      m_differentialDrive.stopMotor();
    }
  }

  @Override
  public void teleopPeriodic() {
    // Allow Driver to 'Tap In' and Take Over Control of Drive
    if(m_driver.getBackButtonPressed()){ 
      driverMode = true;
    }

    // Allow Operator to 'Tap In' and Take Over Control of Drive
    if(m_operator.getBackButtonPressed()){
      driverMode = false;
    }

    // True = Driver Has Control of Drive : False = Operator Has Control of Drive
    // < CONTROLED WITH XBOX LEFT JOYSTICK >
    if(driverMode){
      m_differentialDrive.arcadeDrive(-m_driver.getLeftY()/1.15, m_driver.getLeftX()/1.15);
       m_telemetry.updateDriverMode(driverMode); // Update Shuffleboard Telemetry
    } else {
      m_differentialDrive.arcadeDrive(-m_operator.getLeftY()/1.15, m_operator.getLeftX()/1.15);
      m_telemetry.updateDriverMode(driverMode); // Update Shuffleboard Telemetry
    }

      /*   \/ DRIVER CONTROLS \/
       * LEFT BUMPER COLLECTS CARGO
       * RIGHT BUMPER SHOOTS CARGO
       * 
       * X BUTTON TOGGLES BUCKET UP/DOWN
       */
 
      // Right Bumper Shoots Cargo
      if (m_driver.getRightBumper()){
        m_roller.set(1);
      } else if (!m_driver.getLeftBumper()) {
        m_roller.set(0);
      }

      // Toggles Arms Up and Down
      if(m_driver.getXButtonPressed()){
        m_bucketDS.toggle();
      } 

      /*  \/ OPERATOR CONTROLS \/
       * Y BUTTON MOVES MAIN CLIMBER UP
       * B BUTTON MOVES MAIN CLIMBER DOWN
       * 
       * X BUTTON MOVES SIDE CLIMBER UP
       * A BUTTON MOVES SIDE CLIMBER DOWN
       * 
       * START BUTTON TOGGLES SIDE CLIMBER FORWARD/REVERSE
       */

      // Y Button Moves Main Climber Up
      if(m_operator.getYButton()){
        m_climberMain1.set(-1);
        m_climberMain2.set(1);
      } else if (!m_operator.getBButton()){
        m_climberMain1.set(0.115);
        m_climberMain2.set(0.115);
      }

      // B Button Moves Main Climber Down
      if(m_operator.getBButton()){
        m_climberMain1.set(1);
        m_climberMain2.set(-1);
      } else if (!m_operator.getYButton()){
        m_climberMain1.set(0.115);
        m_climberMain2.set(0.115);
      }

      // X Moves Side Climber Up
      if(m_operator.getXButton()){
        m_climberSide.set(0.8);
      } else if (!m_operator.getAButton()) {
        m_climberSide.set(-0.115);
      }

      // A Moves Side Climber Down
      if(m_operator.getAButton()){
        m_climberSide.set(-0.75);
      } else if (!m_operator.getXButton()) {
        m_climberSide.set(-0.115);
      }

      // Toggles If Side Climber is In or Out
      if(m_operator.getStartButtonPressed()){
        m_climberDS.toggle();
      }
  }
}