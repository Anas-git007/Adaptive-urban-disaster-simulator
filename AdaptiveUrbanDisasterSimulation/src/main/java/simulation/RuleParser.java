package simulation;

public class RuleParser {
 
    private final SimulationEngine engine;
    private final SimulationRules  rules;

     
    private String lastResult = "Ready. Type a command.";

    public RuleParser(SimulationEngine engine) {
        this.engine = engine;
        this.rules  = engine.getRules();
    }

 
    public String parse(String input) {
        if (input == null || input.isBlank()) {
            lastResult = "⚠ Empty command — type a command from the reference list.";
            return lastResult;
        }

        String raw    = input.trim();
        String cmd    = raw.toLowerCase();
        double numVal = extractNumber(cmd);
        boolean hasNum = !Double.isNaN(numVal);

        try {
             
            if (cmd.startsWith("set")) {

                if (cmd.contains("panic") && cmd.contains("radius") && hasNum) {
                    rules.setCitizenPanicRadius((int) numVal);
                    rules.logRule("panicRadius", String.valueOf((int) numVal));
                    lastResult = "✓ Citizen panic radius → " + (int) numVal;

                } else if (cmd.contains("panic") && cmd.contains("drain") && hasNum) {
                    rules.setCitizenPanicDrain(numVal);
                    rules.logRule("panicDrain", String.valueOf(numVal));
                    lastResult = "✓ Citizen panic drain → " + numVal + " /tick";

                } else if (cmd.contains("wander") && hasNum) {
                    rules.setCitizenWanderChance(numVal);
                    rules.logRule("wanderChance", String.valueOf(numVal));
                    lastResult = "✓ Citizen wander chance → " + numVal;

                } else if (cmd.contains("heal") && hasNum) {
                    rules.setAmbulanceHealAmount(numVal);
                    rules.logRule("healAmount", String.valueOf(numVal));
                    lastResult = "✓ Ambulance heal amount → " + numVal;

                } else if (cmd.contains("fuel") && cmd.contains("cost") && hasNum) {
                    rules.setAmbulanceFuelPerMove(numVal);
                    rules.logRule("fuelPerMove", String.valueOf(numVal));
                    lastResult = "✓ Fuel cost per move → " + numVal;

                } else if (cmd.contains("extinguish") && hasNum) {
                    rules.setFireTruckExtinguishPower(numVal);
                    rules.logRule("extPower", String.valueOf(numVal));
                    lastResult = "✓ Extinguish power → " + numVal;

                } else if (cmd.contains("water") && cmd.contains("drain") && hasNum) {
                    rules.setFireTruckWaterDrain(numVal);
                    rules.logRule("waterDrain", String.valueOf(numVal));
                    lastResult = "✓ Water drain rate → " + numVal;

                } else if (cmd.contains("clear") && cmd.contains("power") && hasNum) {
                    rules.setPoliceClearPower(numVal);
                    rules.logRule("clearPower", String.valueOf(numVal));
                    lastResult = "✓ Police clear power → " + numVal;

                } else if (cmd.contains("response") && cmd.contains("distance") && hasNum) {
                    rules.setMaxResponseDistance((int) numVal);
                    rules.logRule("maxResponseDist", String.valueOf((int) numVal));
                    lastResult = "✓ Max response distance → " + (int) numVal + " cells";

                } else if (cmd.contains("growth") && hasNum) {
                    rules.setHazardGrowthRate(numVal);
                    rules.logRule("growthRate", String.valueOf(numVal));
                    lastResult = "✓ Hazard growth rate → " + numVal + " /tick";

                } else if (cmd.contains("fire") && cmd.contains("spread") && hasNum) {
                    rules.setFireSpreadInterval((int) numVal);
                    rules.logRule("fireSpread", "every " + (int) numVal + " ticks");
                    lastResult = "✓ Fire spread interval → every " + (int) numVal + " ticks";

                } else if (cmd.contains("chem") && cmd.contains("spread") && hasNum) {
                    rules.setChemSpreadInterval((int) numVal);
                    rules.logRule("chemSpread", "every " + (int) numVal + " ticks");
                    lastResult = "✓ Chem spread interval → every " + (int) numVal + " ticks";

                } else if (cmd.contains("refuel") && cmd.contains("rate") && hasNum) {
                    rules.setAutoRefuelRate(numVal);
                    rules.logRule("refuelRate", String.valueOf(numVal));
                    lastResult = "✓ Auto-refuel rate → " + numVal + " /tick";

                } else {
                    rules.logRule("Error", raw);
                    lastResult = "✗ Unknown SET command or missing number: \"" + raw + "\"";
                }
 
            } else if (cmd.contains("disable") && cmd.contains("ambulance")) {
                rules.setAmbulanceDisabled(true);
                rules.logRule("ambulance", "DISABLED");
                lastResult = "✓ Ambulances disabled.";

            } else if (cmd.contains("enable") && cmd.contains("ambulance")) {
                rules.setAmbulanceDisabled(false);
                rules.logRule("ambulance", "enabled");
                lastResult = "✓ Ambulances enabled.";

            } else if (cmd.contains("disable")
                    && (cmd.contains("firetruck") || cmd.contains("fire truck"))) {
                rules.setFireTruckDisabled(true);
                rules.logRule("fireTruck", "DISABLED");
                lastResult = "✓ Fire trucks disabled.";

            } else if (cmd.contains("enable")
                    && (cmd.contains("firetruck") || cmd.contains("fire truck"))) {
                rules.setFireTruckDisabled(false);
                rules.logRule("fireTruck", "enabled");
                lastResult = "✓ Fire trucks enabled.";

            } else if (cmd.contains("disable") && cmd.contains("police")) {
                rules.setPoliceDisabled(true);
                rules.logRule("police", "DISABLED");
                lastResult = "✓ Police disabled.";

            } else if (cmd.contains("enable") && cmd.contains("police")) {
                rules.setPoliceDisabled(false);
                rules.logRule("police", "enabled");
                lastResult = "✓ Police enabled.";
 
            } else if ((cmd.contains("stop") || cmd.contains("disable"))
                    && cmd.contains("all") && cmd.contains("spread")) {
                rules.setHazardSpreadEnabled(false);
                rules.logRule("spread", "ALL STOPPED");
                lastResult = "✓ All hazard spreading stopped.";

            } else if ((cmd.contains("resume") || cmd.contains("enable"))
                    && cmd.contains("all") && cmd.contains("spread")) {
                rules.setHazardSpreadEnabled(true);
                rules.logRule("spread", "resumed");
                lastResult = "✓ All hazard spreading resumed.";

            } else if ((cmd.contains("stop") || cmd.contains("disable"))
                    && cmd.contains("fire") && cmd.contains("spread")) {
                rules.setFireSpreadInterval(999);
                rules.logRule("fireSpread", "STOPPED");
                lastResult = "✓ Fire spreading stopped.";

            } else if ((cmd.contains("resume") || cmd.contains("start"))
                    && cmd.contains("fire") && cmd.contains("spread")) {
                rules.setFireSpreadInterval(10);
                rules.logRule("fireSpread", "resumed (10)");
                lastResult = "✓ Fire spreading resumed.";

            } else if ((cmd.contains("stop") || cmd.contains("disable"))
                    && cmd.contains("chem") && cmd.contains("spread")) {
                rules.setChemSpreadInterval(999);
                rules.logRule("chemSpread", "STOPPED");
                lastResult = "✓ Chem spreading stopped.";

            } else if ((cmd.contains("resume") || cmd.contains("start"))
                    && cmd.contains("chem") && cmd.contains("spread")) {
                rules.setChemSpreadInterval(14);
                rules.logRule("chemSpread", "resumed (14)");
                lastResult = "✓ Chem spreading resumed.";

          

            } else if (cmd.contains("citizens") && cmd.contains("flee")
                    && cmd.contains("off")) {
                rules.setCitizensFlee(false);
                rules.logRule("citizensFlee", "OFF");
                lastResult = "✓ Citizens no longer flee hazards.";

            } else if (cmd.contains("citizens") && cmd.contains("flee")
                    && cmd.contains("on")) {
                rules.setCitizensFlee(true);
                rules.logRule("citizensFlee", "on");
                lastResult = "✓ Citizens will flee hazards.";

            } else if (cmd.contains("block") && cmd.contains("road")
                    && cmd.contains("off")) {
                rules.setHazardsBlockRoads(false);
                rules.logRule("blockRoads", "OFF");
                lastResult = "✓ Hazards no longer block roads.";

            } else if (cmd.contains("block") && cmd.contains("road")
                    && cmd.contains("on")) {
                rules.setHazardsBlockRoads(true);
                rules.logRule("blockRoads", "on");
                lastResult = "✓ Hazards block roads (default).";

            } else if (cmd.contains("auto") && cmd.contains("refuel")
                    && cmd.contains("on")) {
                rules.setAutoRefuel(true);
                rules.logRule("autoRefuel", "ON");
                lastResult = "✓ Auto-refuel enabled.";

            } else if (cmd.contains("auto") && cmd.contains("refuel")
                    && cmd.contains("off")) {
                rules.setAutoRefuel(false);
                rules.logRule("autoRefuel", "off");
                lastResult = "✓ Auto-refuel disabled.";

         

            } else if (cmd.contains("strategy") && cmd.contains("save")) {
                engine.setStrategy("saveLives");
                rules.logRule("strategy", "saveLives");
                lastResult = "✓ Strategy: Save Lives First.";

            } else if (cmd.contains("strategy") && cmd.contains("protect")) {
                engine.setStrategy("protectProperty");
                rules.logRule("strategy", "protectProperty");
                lastResult = "✓ Strategy: Protect Property.";

            } else if (cmd.contains("strategy") && cmd.contains("nearest")) {
                engine.setStrategy("nearest");
                rules.logRule("strategy", "nearest");
                lastResult = "✓ Strategy: Nearest First.";

            

            } else if (cmd.contains("add") && cmd.contains("fire")) {
                engine.addSpecificHazard("fire");
                lastResult = "✓ Structure Fire spawned.";

            } else if (cmd.contains("add") && cmd.contains("chem")) {
                engine.addSpecificHazard("chem");
                lastResult = "✓ Chemical Spill spawned.";

            } else if (cmd.contains("add") && cmd.contains("casualty")) {
                engine.addSpecificHazard("casualty");
                lastResult = "✓ Mass Casualty event spawned.";

            } else if (cmd.contains("add") && cmd.contains("collapse")) {
                engine.addSpecificHazard("collapse");
                lastResult = "✓ Road Collapse spawned.";

            } else if (cmd.contains("add") && cmd.contains("hazard")) {
                engine.addRandomHazard();
                lastResult = "✓ Random hazard added.";

             

            } else if (cmd.contains("clear") && cmd.contains("hazard")) {
                clearAllHazards();
                lastResult = "✓ All hazards cleared.";

            } else if (cmd.equals("reset rules")) {
                rules.resetToDefaults();
                lastResult = "✓ All rules reset to defaults.";

            } else if (cmd.equals("reset")) {
                engine.reset();
                lastResult = "✓ Simulation fully reset.";

          

            } else if (cmd.equals("pause")) {
                engine.pause();
                lastResult = "✓ Paused.";

            } else if (cmd.equals("start")) {
                engine.start();
                lastResult = "✓ Started.";

            } else {
                rules.logRule("Error", raw);
                lastResult = "✗ Unknown command: \"" + raw + "\"  (see reference list below)";
            }

        } catch (OutOfBoundsRuleException e) {

            e.applyRecovery(rules);
            lastResult = "✗ Out-of-range value rejected. "
                       + e.getRuleName() + " auto-clamped to nearest safe limit. "
                       + "Valid range: [" + e.getMin() + ", " + e.getMax() + "].";
            rules.logRule("Auto-Recovery", e.getRuleName() + " clamped.");
        }

        rules.logRule("Last Command", raw);
        return lastResult;
    }

    private double extractNumber(String cmd) {
        String[] tokens = cmd.split("\\s+");
        for (int i = tokens.length - 1; i >= 0; i--) {
            try {
                return Double.parseDouble(tokens[i]);
            } catch (NumberFormatException ignored) {
               
            }
        }
        return Double.NaN;
    }

  
    private void clearAllHazards() {
        for (Entity e : engine.getEntities()) {
            if (e instanceof Hazard) e.setActive(false);
        }
    }

    public String getLastResult() { return lastResult; }
}