package simulation;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimulationRules {

    private int    citizenPanicRadius       = 3;
    private double citizenPanicDrain        = 0.3;
    private boolean citizensFlee            = true;
    private double citizenWanderChance      = 0.35;

    private boolean ambulanceDisabled       = false;
    private double  ambulanceHealAmount     = 15;
    private double  ambulanceFuelPerMove    = 0.8;

    private boolean fireTruckDisabled       = false;
    private double  fireTruckExtinguishPower = 8;
    private double  fireTruckWaterDrain     = 1.0;

    private boolean policeDisabled          = false;
    private double  policeClearPower        = 5;

    private boolean hazardSpreadEnabled     = true;
    private int     fireSpreadInterval      = 10;
    private int     chemSpreadInterval      = 14;
    private double  hazardGrowthRate        = 0.05;
    private boolean hazardsBlockRoads       = true;

    private int     maxResponseDistance     = 999;
    private boolean autoRefuel              = false;
    private double  autoRefuelRate          = 0.2;

 
    private final LinkedHashMap<String, String> ruleLog = new LinkedHashMap<>();

     
    public void logRule(String ruleName, String value) {
        ruleLog.put(ruleName, value);
        
        if (ruleLog.size() > 12) {
            ruleLog.remove(ruleLog.keySet().iterator().next());
        }
    }

    public Map<String, String> getRuleLog() { return ruleLog; }

    

    public void resetToDefaults() {
        citizenPanicRadius        = 3;
        citizenPanicDrain         = 0.3;
        citizensFlee              = true;
        citizenWanderChance       = 0.35;
        ambulanceDisabled         = false;
        ambulanceHealAmount       = 15;
        ambulanceFuelPerMove      = 0.8;
        fireTruckDisabled         = false;
        fireTruckExtinguishPower  = 8;
        fireTruckWaterDrain       = 1.0;
        policeDisabled            = false;
        policeClearPower          = 5;
        hazardSpreadEnabled       = true;
        fireSpreadInterval        = 10;
        chemSpreadInterval        = 14;
        hazardGrowthRate          = 0.05;
        hazardsBlockRoads         = true;
        maxResponseDistance       = 999;
        autoRefuel                = false;
        autoRefuelRate            = 0.2;
        ruleLog.clear();
    }
 
    public double calculateThreatScore(double severity, double distance) {
        return severity / (distance + 1.0);
    }

    public double calculateThreatScore(double severity, int urgencyMultiplier) {
        return severity * urgencyMultiplier;
    }
 
    public int  getCitizenPanicRadius() { return citizenPanicRadius; }
    public void setCitizenPanicRadius(int v) throws OutOfBoundsRuleException {
        if (v < 1 || v > 20)
            throw new OutOfBoundsRuleException(this, "Citizen Panic Radius", v, 1, 20);
        citizenPanicRadius = v;
    }

    public double getCitizenPanicDrain() { return citizenPanicDrain; }
    public void   setCitizenPanicDrain(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 10.0)
            throw new OutOfBoundsRuleException(this, "Citizen Panic Drain", v, 0.0, 10.0);
        citizenPanicDrain = v;
    }

    public boolean isCitizensFlee() { return citizensFlee; }
    public void    setCitizensFlee(boolean v) { citizensFlee = v; }

    public double getCitizenWanderChance() { return citizenWanderChance; }
    public void   setCitizenWanderChance(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 1.0)
            throw new OutOfBoundsRuleException(this, "Citizen Wander Chance", v, 0.0, 1.0);
        citizenWanderChance = v;
    }

    public boolean isAmbulanceDisabled() { return ambulanceDisabled; }
    public void    setAmbulanceDisabled(boolean v) { ambulanceDisabled = v; }

    public double getAmbulanceHealAmount() { return ambulanceHealAmount; }
    public void   setAmbulanceHealAmount(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 200.0)
            throw new OutOfBoundsRuleException(this, "Ambulance Heal Amount", v, 0.0, 200.0);
        ambulanceHealAmount = v;
    }

    public double getAmbulanceFuelPerMove() { return ambulanceFuelPerMove; }
    public void   setAmbulanceFuelPerMove(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 50.0)
            throw new OutOfBoundsRuleException(this, "Ambulance Fuel Per Move", v, 0.0, 50.0);
        ambulanceFuelPerMove = v;
    }

    public boolean isFireTruckDisabled() { return fireTruckDisabled; }
    public void    setFireTruckDisabled(boolean v) { fireTruckDisabled = v; }

    public double getFireTruckExtinguishPower() { return fireTruckExtinguishPower; }
    public void   setFireTruckExtinguishPower(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 200.0)
            throw new OutOfBoundsRuleException(this, "FireTruck Extinguish Power", v, 0.0, 200.0);
        fireTruckExtinguishPower = v;
    }

    public double getFireTruckWaterDrain() { return fireTruckWaterDrain; }
    public void   setFireTruckWaterDrain(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 50.0)
            throw new OutOfBoundsRuleException(this, "FireTruck Water Drain", v, 0.0, 50.0);
        fireTruckWaterDrain = v;
    }

    public boolean isPoliceDisabled() { return policeDisabled; }
    public void    setPoliceDisabled(boolean v) { policeDisabled = v; }

    public double getPoliceClearPower() { return policeClearPower; }
    public void   setPoliceClearPower(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 200.0)
            throw new OutOfBoundsRuleException(this, "Police Clear Power", v, 0.0, 200.0);
        policeClearPower = v;
    }

    public boolean isHazardSpreadEnabled() { return hazardSpreadEnabled; }
    public void    setHazardSpreadEnabled(boolean v) { hazardSpreadEnabled = v; }

    public int  getFireSpreadInterval() { return fireSpreadInterval; }
    public void setFireSpreadInterval(int v) throws OutOfBoundsRuleException {
        if (v < 1 || v > 999)
            throw new OutOfBoundsRuleException(this, "Fire Spread Interval", v, 1, 999);
        fireSpreadInterval = v;
    }

    public int  getChemSpreadInterval() { return chemSpreadInterval; }
    public void setChemSpreadInterval(int v) throws OutOfBoundsRuleException {
        if (v < 1 || v > 999)
            throw new OutOfBoundsRuleException(this, "Chemical Spread Interval", v, 1, 999);
        chemSpreadInterval = v;
    }

    public double getHazardGrowthRate() { return hazardGrowthRate; }
    public void   setHazardGrowthRate(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 10.0)
            throw new OutOfBoundsRuleException(this, "Hazard Growth Rate", v, 0.0, 10.0);
        hazardGrowthRate = v;
    }

    public boolean isHazardsBlockRoads() { return hazardsBlockRoads; }
    public void    setHazardsBlockRoads(boolean v) { hazardsBlockRoads = v; }

    public int  getMaxResponseDistance() { return maxResponseDistance; }
    
    
    public void setMaxResponseDistance(int v) throws OutOfBoundsRuleException {
        if (v < 1 || v > 999)
            throw new OutOfBoundsRuleException(this, "Max Response Distance", v, 1, 999);
        maxResponseDistance = v;
    }

    public boolean isAutoRefuel() { return autoRefuel; }
    public void    setAutoRefuel(boolean v) { autoRefuel = v; }

    public double getAutoRefuelRate() { return autoRefuelRate; }
    public void   setAutoRefuelRate(double v) throws OutOfBoundsRuleException {
        if (v < 0.0 || v > 20.0)
            throw new OutOfBoundsRuleException(this, "Auto Refuel Rate", v, 0.0, 20.0);
        autoRefuelRate = v;
    }
}
 
abstract class SimulationConfigException extends Exception {
    public SimulationConfigException(String message) {
        super(message);
    }
}
 
class OutOfBoundsRuleException extends SimulationConfigException {

    private final String ruleName;
    private final double invalidValue;
    private final double min;
    private final double max;

    public OutOfBoundsRuleException(SimulationRules rules, String ruleName,
                                    double invalidValue, double min, double max) {
        super("Assignment failed for '" + ruleName + "'. Value "
              + invalidValue + " violates bounds [" + min + ", " + max + "].");
        this.ruleName     = ruleName;
        this.invalidValue = invalidValue;
        this.min          = min;
        this.max          = max;
    }

    public String getRuleName()     { return ruleName; }
    public double getInvalidValue() { return invalidValue; }
    public double getMin()          { return min; }
    public double getMax()          { return max; }


    public void applyRecovery(SimulationRules rules) {
        double safeValue = (invalidValue < min) ? min : max;
        try {
            switch (ruleName) {
                case "Citizen Panic Radius"     -> rules.setCitizenPanicRadius((int) safeValue);
                case "Citizen Panic Drain"      -> rules.setCitizenPanicDrain(safeValue);
                case "Citizen Wander Chance"    -> rules.setCitizenWanderChance(safeValue);
                case "Ambulance Heal Amount"    -> rules.setAmbulanceHealAmount(safeValue);
                case "Ambulance Fuel Per Move"  -> rules.setAmbulanceFuelPerMove(safeValue);
                case "FireTruck Extinguish Power" -> rules.setFireTruckExtinguishPower(safeValue);
                case "FireTruck Water Drain"    -> rules.setFireTruckWaterDrain(safeValue);
                case "Police Clear Power"       -> rules.setPoliceClearPower(safeValue);
                case "Fire Spread Interval"     -> rules.setFireSpreadInterval((int) safeValue);
                case "Chemical Spread Interval" -> rules.setChemSpreadInterval((int) safeValue);
                case "Hazard Growth Rate"       -> rules.setHazardGrowthRate(safeValue);
                case "Max Response Distance"    -> rules.setMaxResponseDistance((int) safeValue);
                case "Auto Refuel Rate"         -> rules.setAutoRefuelRate(safeValue);
                default -> throw new RuntimeException(
                        "OutOfBoundsRuleException.applyRecovery: unhandled rule '" + ruleName + "'");
            }
        } catch (OutOfBoundsRuleException nested) {
            throw new RuntimeException(
                    "Recovery failed for rule '" + ruleName + "': "
                    + nested.getMessage(), nested);
        }
    }
}