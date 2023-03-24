package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Telemetry {
    public void updateDriverMode(boolean driverMode){
        SmartDashboard.putBoolean("Driver Mode", driverMode);
        SmartDashboard.putBoolean("Operator Mode", !driverMode);
    }
}
