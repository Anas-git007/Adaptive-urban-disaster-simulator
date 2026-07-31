package simulation;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.ArrayList;



interface Movable {
    int    getX();
    int    getY();
    double getFuel();
    double getMaxFuel();
}

interface IncidentResponder {
    void   setStrategy(String strategy);
    String getStatus();
    int    getIncidentCount();
}

interface Renderable {
    void render(GraphicsContext gc, int px, int py, int cellSize);
}

interface UnitCardProvider {
    
    String getUnitEmoji();
    String getResourceLabel();
    double getMaxResource();
    boolean isFleetUnit();
}
 
class ScoredCandidate<T extends Entity> {

    private final T      target;
    private final double score;

    public ScoredCandidate(T target, double score) {
        this.target = target;
        this.score  = score;
    }

    public T      getTarget() { return target; }
    public double getScore()  { return score; }

    public boolean isBetterThan(ScoredCandidate<T> other) {
        return other == null || this.score < other.score;
    }
}


public abstract class Entity implements Movable, Renderable, UnitCardProvider {

    private int             x;
    private int             y;
    private double          fuel;
    private double          supplies;
    private String          status;
    private boolean         active;
    private int             incidentCount;
    
    private Entity          currentTarget;
    
    
    private SimulationRules rules;

    private final double maxFuel;
    private final String name;
   

    public Entity(int x, int y, double fuel, double supplies, String name) {
        this.x             = x;
        this.y             = y;
        this.fuel          = fuel;
        this.maxFuel       = fuel;      
        this.supplies      = supplies;
        this.name          = name;     
        this.status        = "IDLE";
        this.active        = true;
        this.incidentCount = 0;
        this.currentTarget = null;
        this.rules         = null;
    }

    public abstract void update(CityMap map, List<Entity> allEntities);
    public abstract Color getColor();
    public abstract String getLabel();
    @Override
    public abstract void render(GraphicsContext gc, int px, int py, int cellSize);
    @Override public abstract String  getUnitEmoji();
    @Override public abstract String  getResourceLabel();
    @Override public abstract double  getMaxResource();
    @Override public abstract boolean isFleetUnit();

    public    void            setRules(SimulationRules r) { this.rules = r; }
    protected SimulationRules getRules()                   { return rules; }
    public    boolean         hasRules()                   { return rules != null; }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    protected void setFuel(double f)           { fuel    = Math.max(0, Math.min(maxFuel, f)); }
    protected void setSupplies(double s)        { supplies = Math.max(0, s); }
    protected void setStatus(String s)          { status   = s; }
    protected void incrementIncidentCount()     { incidentCount++; }

    public Entity getTarget()         { return currentTarget; }
    public void   setTarget(Entity t) { currentTarget = t; }

    public void heal(double amount)       { setFuel(getFuel() + amount); }
    public void takeDamage(double amount) { setFuel(getFuel() - amount); }

    protected void moveToward(int tx, int ty, CityMap map,
                              List<Entity> allEntities, double fuelCost) {
        if (x == tx && y == ty) return;
        int[] next = map.bfsNextStep(x, y, tx, ty);
        if (next == null) return;

        boolean occupied = false;
        Entity  blocker  = null;

        for (Entity other : allEntities) {
            
            if (other != this && other.isActive()
                    && other.getX() == next[0] && other.getY() == next[1]) {
                
                
                if (!(other instanceof Hazard)) {   // hazards are terrain, not blockers
                    occupied = true;
                    blocker  = other;
                    break;
                }
            }
        }

        if (occupied && blocker != null) {
            if (blocker.getStatus().equals("IDLE") || blocker instanceof Citizen) {
                blocker.stepAside(map, allEntities);
                if (blocker.getX() != next[0] || blocker.getY() != next[1])
                    occupied = false;
            }
           
            if (occupied && (this instanceof IncidentResponder))
                occupied = false;
        }

        if (!occupied) {
            setPosition(next[0], next[1]);
            takeDamage(fuelCost);
        }
    }

    protected void stepAside(CityMap map, List<Entity> allEntities) {
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            if (!map.isRoad(nx, ny) || map.isBlocked(nx, ny)) continue;
            boolean taken = false;
            for (Entity o : allEntities)
                if (o != this && o.isActive() && o.getX() == nx && o.getY() == ny)
                    { taken = true; break; }
            if (!taken) { setPosition(nx, ny); return; }
        }
    }

    protected void ensureNoIdleOverlap(CityMap map, List<Entity> allEntities) {
        for (Entity o : allEntities)
            if (o != this && o.isActive() && o.getX() == x && o.getY() == y)
                { stepAside(map, allEntities); return; }
    }

    protected void applyAutoRefuel() {
        if (rules != null && rules.isAutoRefuel()) {
            heal(rules.getAutoRefuelRate());
            setSupplies(getSupplies() + rules.getAutoRefuelRate() * 0.5);
        }
    }
    
    protected int dist(Entity e)       { return Math.abs(e.getX()-x) + Math.abs(e.getY()-y); }
    protected int dist(int tx, int ty) { return Math.abs(tx-x)       + Math.abs(ty-y); }

  
    protected void drawFuelBar(GraphicsContext gc, int px, int py, int cellSize) {
        double ratio = Math.max(0, Math.min(1, getFuel() / getMaxFuel()));
        gc.setFill(Color.web("#020617"));
        gc.fillRect(px + 4, py + cellSize - 4, cellSize - 8, 3);
        gc.setFill(ratio > 0.35 ? Color.web("#10b981") : Color.web("#ef4444"));
        gc.fillRect(px + 4, py + cellSize - 4, (cellSize - 8) * ratio, 3);
    }
 
    @Override public int     getX()           { return x; }
    @Override public int     getY()           { return y; }
    @Override public double  getFuel()        { return fuel; }
    @Override public double  getMaxFuel()     { return maxFuel; }
    public    double         getSupplies()    { return supplies; }
    public    String         getStatus()      { return status; }
    public    boolean        isActive()       { return active; }
    public    void           setActive(boolean a) { active = a; }
    public    String         getName()        { return name; }
    public    int            getIncidentCount() { return incidentCount; }
}


class Citizen extends Entity {

     
    private boolean panicking = false;

    public Citizen(int x, int y, String name) { super(x, y, 100, 100, name); }

     
    @Override public String  getUnitEmoji()     { return "👤"; }
    @Override public String  getResourceLabel() { return "HEALTH"; }
    @Override public double  getMaxResource()   { return 100.0; }
    @Override public boolean isFleetUnit()      { return false; }  

    @Override
    public void update(CityMap map, List<Entity> allEntities) {
        if (getFuel() <= 0) { setActive(false); return; }

        int     panicRadius  = hasRules() ? getRules().getCitizenPanicRadius()  : 3;
        double  panicDrain   = hasRules() ? getRules().getCitizenPanicDrain()   : 0.3;
        boolean flee         = !hasRules() || getRules().isCitizensFlee();
        double  wanderChance = hasRules() ? getRules().getCitizenWanderChance() : 0.35;

        panicking = false;
        Hazard closestHazard = null;
        int    minHazardDist = Integer.MAX_VALUE;

        for (Entity e : allEntities) {
            if (e instanceof Hazard h && e.isActive()) {
                int d = dist(e);
                if (d <= panicRadius && d < minHazardDist) {
                    panicking    = true;
                    minHazardDist = d;
                    closestHazard = h;
                }
            }
        }

        if (panicking) {
            setStatus("PANICKING");
            takeDamage(panicDrain);
            if (closestHazard != null) {
                if      (minHazardDist == 0) takeDamage(2.5);
                else if (minHazardDist == 1) takeDamage(0.8);
            }
            if (flee && closestHazard != null) {
                int awayX = Math.max(0, Math.min(map.getWidth()  - 1,
                        getX() + (getX() - closestHazard.getX())));
                int awayY = Math.max(0, Math.min(map.getHeight() - 1,
                        getY() + (getY() - closestHazard.getY())));
                 
                moveToward(awayX, awayY, map, allEntities, 0.2f);
            }
        } else {
            setStatus("CALM");
            if (Math.random() < wanderChance) {
                int rx = Math.max(0, Math.min(map.getWidth()  - 1, getX() + (int)(Math.random()*5)-2));
                int ry = Math.max(0, Math.min(map.getHeight() - 1, getY() + (int)(Math.random()*5)-2));
             
                moveToward(rx, ry, map, allEntities, 0.1f);
            }
        }
    }

    public boolean isPanicking() { return panicking; }

    @Override public Color  getColor() { return panicking ? Color.ORANGE : Color.web("#44cc88"); }
    @Override public String getLabel() { return "C"; }

    
    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        double cx = px + cellSize / 2.0;
        gc.setFill(panicking ? Color.web("#fb923c") : Color.web("#a7f3d0"));
        gc.fillOval(cx - 4, py + 5, 8, 8);
        gc.setStroke(panicking ? Color.web("#fb923c") : Color.web("#a7f3d0"));
        gc.setLineWidth(2.5);
        gc.strokeLine(cx, py+13, cx, py+24);
        gc.strokeLine(cx-6, py+16, cx+6, py+16);
        gc.strokeLine(cx, py+24, cx-5, py+31);
        gc.strokeLine(cx, py+24, cx+5, py+31);
        drawFuelBar(gc, px, py, cellSize);
    }
}


class Ambulance extends Entity implements IncidentResponder {

    private String strategy = "saveLives";

    public Ambulance(int x, int y, String name) { super(x, y, 160, 100, name); }
 
    @Override public void   setStrategy(String s) { strategy = s; }
    @Override public String getStatus()            { return super.getStatus(); }
    @Override public int    getIncidentCount()     { return super.getIncidentCount(); }
 
    @Override public String  getUnitEmoji()     { return "🚑"; }
    @Override public String  getResourceLabel() { return "SUPPLIES"; }
    @Override public double  getMaxResource()   { return 100.0; }
    @Override public boolean isFleetUnit()      { return true; }

    @Override
    public void update(CityMap map, List<Entity> allEntities) {
        applyAutoRefuel();

        boolean disabled    = hasRules() && getRules().isAmbulanceDisabled();
        double  fuelPerMove = hasRules() ? getRules().getAmbulanceFuelPerMove() : 0.8;
        double  healAmount  = hasRules() ? getRules().getAmbulanceHealAmount()  : 15;
        int     maxDist     = hasRules() ? getRules().getMaxResponseDistance()  : 999;

        if (disabled || getFuel() <= 0) {
            setStatus(getFuel() <= 0 ? "NO FUEL" : "DISABLED");
            setTarget(null);
            return;
        }
 
        ScoredCandidate<Entity> best = null;

        for (Entity e : allEntities) {
            if (!e.isActive() || dist(e) > maxDist) continue;

            boolean isPanickingCitizen = (e instanceof Citizen c && c.isPanicking());
            boolean isMassCasualty     = (e instanceof Hazard h && h.getHazardType().equals("MassCasualty"));
            if (!isPanickingCitizen && !isMassCasualty) continue;
 
            boolean alreadyTargeted = false;
            for (Entity other : allEntities) {
                if (other != this && other instanceof Ambulance amb && amb.getTarget() == e) {
                    alreadyTargeted = true; break;
                }
            }
            if (alreadyTargeted) continue;

            double severity = (e instanceof Hazard h2) ? h2.getSeverity() : (100.0 - e.getFuel());
 
            int urgencyModifier = (isMassCasualty && strategy.equalsIgnoreCase("saveLives")) ? 3 : 1;
            double priorityBoost = hasRules()
                    ? getRules().calculateThreatScore(severity, urgencyModifier)
                    : severity * urgencyModifier;
            double score = dist(e) - priorityBoost * 0.1;

            ScoredCandidate<Entity> candidate = new ScoredCandidate<>(e, score);
            if (candidate.isBetterThan(best)) best = candidate;
        }

        if (best != null) {
            Entity target = best.getTarget();
            setStatus("RESPONDING");
            setTarget(target);
            moveToward(target.getX(), target.getY(), map, allEntities, fuelPerMove);
            setSupplies(getSupplies() - 0.3);
            if (dist(target) <= 1) {
                if (target instanceof Citizen) target.heal(healAmount);
                incrementIncidentCount();
            }
        } else {
            setStatus("IDLE");
            setTarget(null);
            ensureNoIdleOverlap(map, allEntities);
        }
    }

    @Override public Color  getColor() { return getFuel() <= 30 ? Color.GRAY : Color.WHITE; }
    @Override public String getLabel() { return "A"; }

    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(px + 5, py + 9, 24, 14);
        gc.setStroke(Color.web("#ef4444"));
        gc.setLineWidth(2.5);
        gc.strokeLine(px+17, py+12, px+17, py+20);
        gc.strokeLine(px+13, py+16, px+21, py+16);
        drawFuelBar(gc, px, py, cellSize);
    }
}
 
class FireTruck extends Entity implements IncidentResponder {

    private String strategy = "saveLives";

    public FireTruck(int x, int y, String name) { super(x, y, 180, 100, name); }

    @Override public void   setStrategy(String s) { strategy = s; }
    @Override public String getStatus()            { return super.getStatus(); }
    @Override public int    getIncidentCount()     { return super.getIncidentCount(); }
 
    @Override public String  getUnitEmoji()     { return "🚒"; }
    @Override public String  getResourceLabel() { return "WATER"; }
    @Override public double  getMaxResource()   { return 100.0; }
    @Override public boolean isFleetUnit()      { return true; }

    @Override
    public void update(CityMap map, List<Entity> allEntities) {
        applyAutoRefuel();

        boolean disabled   = hasRules() && getRules().isFireTruckDisabled();
        double fuelPerMove = hasRules() ? getRules().getAmbulanceFuelPerMove()     : 0.8;
        double extPower    = hasRules() ? getRules().getFireTruckExtinguishPower() : 8;
        double waterDrain  = hasRules() ? getRules().getFireTruckWaterDrain()      : 1.0;
        int    maxDist     = hasRules() ? getRules().getMaxResponseDistance()      : 999;

        if (disabled || getFuel() <= 0 || getSupplies() <= 0) {
            setStatus(getFuel() <= 0 ? "NO FUEL" : getSupplies() <= 0 ? "NO WATER" : "DISABLED");
            setTarget(null);
            return;
        }

        ScoredCandidate<Entity> best = null;

        for (Entity e : allEntities) {
            if (!e.isActive() || !(e instanceof Hazard h) || dist(e) > maxDist) continue;
            String type = h.getHazardType();
            if (!type.equals("StructureFire") && !type.equals("ChemSpill")) continue;

            boolean alreadyTargeted = false;
            for (Entity other : allEntities)
                if (other != this && other instanceof FireTruck ft && ft.getTarget() == e)
                    { alreadyTargeted = true; break; }
            if (alreadyTargeted) continue;
 
            double threatScore = hasRules()
                    ? getRules().calculateThreatScore(h.getSeverity(), dist(e))
                    : h.getSeverity() / (dist(e) + 1.0);

            double score = -threatScore + (100.0-getSupplies())*1.5 + (getMaxFuel()-getFuel())*0.3;
            if (strategy.equalsIgnoreCase("protectProperty") && type.equals("StructureFire")) score -= 25;
            else if (strategy.equalsIgnoreCase("saveLives")  && type.equals("ChemSpill"))     score -= 25;

            ScoredCandidate<Entity> candidate = new ScoredCandidate<>(e, score);
            if (candidate.isBetterThan(best)) best = candidate;
        }

        if (best != null) {
            Entity target = best.getTarget();
            setStatus("EN ROUTE");
            setTarget(target);
            moveToward(target.getX(), target.getY(), map, allEntities, fuelPerMove);
            if (getX() == target.getX() && getY() == target.getY()) {
                target.takeDamage(extPower);
                setSupplies(Math.max(0, getSupplies() - waterDrain));
                if (target.getFuel() <= 0) {
                    target.setActive(false);
                    incrementIncidentCount();
                    setStatus("CONTAINED");
                    setTarget(null);
                }
            }
        } else {
            setStatus("IDLE");
            setTarget(null);
            ensureNoIdleOverlap(map, allEntities);
        }
    }

    @Override public Color  getColor() { return getSupplies() <= 25 ? Color.DARKRED : Color.RED; }
    @Override public String getLabel() { return "F"; }

    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        gc.setFill(Color.web("#ef4444"));
        gc.fillRect(px+4, py+11, 20, 12);
        gc.setFill(Color.web("#fca5a5"));
        gc.fillRect(px+24, py+13, 5, 10);
        gc.setStroke(Color.web("#e2e8f0"));
        gc.setLineWidth(1.5);
        gc.strokeLine(px+6,  py+7,  px+22, py+7);
        gc.strokeLine(px+10, py+6,  px+10, py+10);
        gc.strokeLine(px+18, py+6,  px+18, py+10);
        drawFuelBar(gc, px, py, cellSize);
    }
}
 
class Police extends Entity implements IncidentResponder {

    private String strategy = "saveLives";

    public Police(int x, int y, String name) { super(x, y, 140, 80, name); }

    @Override public void   setStrategy(String s) { strategy = s; }
    @Override public String getStatus()            { return super.getStatus(); }
    @Override public int    getIncidentCount()     { return super.getIncidentCount(); }
 
    @Override public String  getUnitEmoji()     { return "🚓"; }
    @Override public String  getResourceLabel() { return "EQUIPMENT"; }
    @Override public double  getMaxResource()   { return 80.0; }
    @Override public boolean isFleetUnit()      { return true; }

    @Override
    public void update(CityMap map, List<Entity> allEntities) {
        applyAutoRefuel();

        boolean disabled   = hasRules() && getRules().isPoliceDisabled();
        double fuelPerMove = hasRules() ? getRules().getAmbulanceFuelPerMove() : 0.8;
        double clearPower  = hasRules() ? getRules().getPoliceClearPower()     : 5;
        int    maxDist     = hasRules() ? getRules().getMaxResponseDistance()  : 999;

        if (disabled || getFuel() <= 0) {
            setStatus(getFuel() <= 0 ? "NO FUEL" : "DISABLED");
            setTarget(null);
            return;
        }

        ScoredCandidate<Entity> best = null;

        for (Entity e : allEntities) {
            if (!e.isActive() || !(e instanceof Hazard h) || dist(e) > maxDist) continue;
            String type = h.getHazardType();
            if (!type.equals("RoadCollapse") && !type.equals("MassCasualty")) continue;

            boolean alreadyTargeted = false;
            for (Entity other : allEntities)
                if (other != this && other instanceof Police p && p.getTarget() == e)
                    { alreadyTargeted = true; break; }
            if (alreadyTargeted) continue;

            double score = dist(e);
            if      (strategy.equalsIgnoreCase("protectProperty") && type.equals("RoadCollapse")) score -= 25;
            else if (strategy.equalsIgnoreCase("saveLives")        && type.equals("MassCasualty")) score -= 25;

            ScoredCandidate<Entity> candidate = new ScoredCandidate<>(e, score);
            if (candidate.isBetterThan(best)) best = candidate;
        }

        if (best != null) {
            Entity target = best.getTarget();
            setStatus("RESPONDING");
            setTarget(target);
            moveToward(target.getX(), target.getY(), map, allEntities, fuelPerMove);
            if (getX() == target.getX() && getY() == target.getY()) {
                target.takeDamage(clearPower);
                if (target.getFuel() <= 0) {
                    target.setActive(false);
                    incrementIncidentCount();
                    setStatus("CLEARED");
                    setTarget(null);
                }
            }
        } else {
            setStatus("IDLE");
            setTarget(null);
            ensureNoIdleOverlap(map, allEntities);
        }
    }

    @Override public Color  getColor() { return Color.web("#4488ff"); }
    @Override public String getLabel() { return "P"; }

    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        gc.setFill(Color.web("#1e3a8a"));
        gc.fillRect(px+4,  py+13, 12, 10);
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(px+16, py+13, 12, 10);
        gc.fillOval(px+6,  py+21, 5, 5);
        gc.fillOval(px+20, py+21, 5, 5);
        gc.setFill(Color.web("#38bdf8"));
        gc.fillRect(px+14, py+8,  5, 5);
        drawFuelBar(gc, px, py, cellSize);
    }
}


abstract class Hazard extends Entity {

    private int     spreadTimer   = 0;
    private boolean spreadEnabled = true;

    private final String hazardType;

    public Hazard(int x, int y, String name, String type, double initSeverity) {
        super(x, y, initSeverity, 0, name);
        this.hazardType = type;
        setStatus("ACTIVE");
    }

    protected abstract int   defaultSpreadInterval();

    protected abstract Hazard createChild(int x, int y);

    @Override public String  getUnitEmoji()     { return getLabel(); }
    @Override public String  getResourceLabel() { return "SEVERITY"; }
    @Override public double  getMaxResource()   { return 100.0; }
    @Override public boolean isFleetUnit()      { return false; }

    @Override
    public void update(CityMap map, List<Entity> allEntities) {
        if (!isActive()) return;

        boolean globalSpread = !hasRules() || getRules().isHazardSpreadEnabled();
        boolean blockRoads   = !hasRules() || getRules().isHazardsBlockRoads();
        double  growthRate   = hasRules()  ? getRules().getHazardGrowthRate() : 0.05;
        int     interval     = defaultSpreadInterval();

        if (blockRoads) map.blockCell(getX(), getY());

        spreadTimer++;
        if (globalSpread && spreadEnabled && spreadTimer >= interval) {
            spreadTimer = 0;
            trySpread(map, allEntities);
        }
        heal(growthRate);
    }

    protected void trySpread(CityMap map, List<Entity> allEntities) {
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        List<int[]> candidates = new ArrayList<>();
        for (int[] d : dirs) {
            int nx = getX()+d[0], ny = getY()+d[1];
            if (map.isRoad(nx, ny) && !map.isBlocked(nx, ny))
                candidates.add(new int[]{nx, ny});
        }
        if (!candidates.isEmpty()) {
            int[] c = candidates.get((int)(Math.random()*candidates.size()));
            Hazard child = createChild(c[0], c[1]);
            child.setRules(getRules());
            allEntities.add(child);
        }
    }

    public void   setSpreadEnabled(boolean e) { spreadEnabled = e; }
    public String getHazardType()             { return hazardType; }
    public double getSeverity()               { return getFuel(); }

    protected void renderSeverityLabel(GraphicsContext gc, int px, int py) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 8));
        gc.fillText((int) getSeverity() + "%", px+4, py+10);
    }

    protected void renderHalo(GraphicsContext gc, int px, int py, int cellSize) {
        gc.setFill(getColor().deriveColor(0, 1, 1, 0.12));
        gc.fillOval(px-4, py-4, cellSize+8, cellSize+8);
        gc.setStroke(getColor().deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(1.0);
        gc.strokeOval(px-4, py-4, cellSize+8, cellSize+8);
    }
}

class StructureFire extends Hazard {
    public StructureFire(int x, int y) {
        super(x, y, "Structure Fire", "StructureFire", 30 + Math.random()*40);
    }
    @Override protected int   defaultSpreadInterval() { return hasRules() ? getRules().getFireSpreadInterval() : 10; }
    @Override protected Hazard createChild(int x,int y) { return new StructureFire(x, y); }
    @Override public    Color  getColor()               { return Color.ORANGERED; }
    @Override public    String getLabel()               { return "🔥"; }
    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        double cx = px + cellSize/2.0;
        renderHalo(gc, px, py, cellSize);
        gc.setFill(Color.web("#f97316"));
        double[] fx = {cx, px+6, px+12, cx+2, px+cellSize-10, px+cellSize-6};
        double[] fy = {py+4, py+cellSize-6, py+cellSize-10, py+10, py+cellSize-12, py+cellSize-6};
        gc.fillPolygon(fx, fy, 6);
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(px+10, py+12, cellSize-20, cellSize-18);
        renderSeverityLabel(gc, px, py);
    }
}

class ChemSpill extends Hazard {
    public ChemSpill(int x, int y) {
        super(x, y, "Chem Spill", "ChemSpill", 20 + Math.random()*35);
    }
    @Override protected int   defaultSpreadInterval() { return hasRules() ? getRules().getChemSpreadInterval() : 14; }
    @Override protected Hazard createChild(int x,int y) { return new ChemSpill(x, y); }
    @Override public    Color  getColor()               { return Color.web("#aaff00"); }
    @Override public    String getLabel()               { return "☣"; }
    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        renderHalo(gc, px, py, cellSize);
        gc.setFill(Color.web("#22c55e"));
        gc.fillOval(px+5,  py+10, cellSize-10, cellSize-16);
        gc.fillOval(px+12, py+5,  10, 10);
        gc.setFill(Color.web("#15803d"));
        gc.fillOval(px+9,  py+12, 4, 4);
        gc.fillOval(px+18, py+8,  3, 3);
        renderSeverityLabel(gc, px, py);
    }
}

class MassCasualty extends Hazard {
    public MassCasualty(int x, int y) {
        super(x, y, "Mass Casualty", "MassCasualty", 35 + Math.random()*30);
        setSpreadEnabled(false);
    }
    @Override protected int   defaultSpreadInterval()    { return 999; }
    @Override protected Hazard createChild(int x,int y)  { return new MassCasualty(x, y); }
    @Override public    Color  getColor()                 { return Color.web("#ffaa00"); }
    @Override public    String getLabel()                 { return "✚"; }
    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        double cx = px + cellSize/2.0;
        renderHalo(gc, px, py, cellSize);
        gc.setFill(Color.web("#ef4444"));
        double[] dx = {cx, px+cellSize-6, cx, px+6};
        double[] dy = {py+4, py+cellSize/2.0, py+cellSize-6, py+cellSize/2.0};
        gc.fillPolygon(dx, dy, 4);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeLine(cx, py+9, cx, py+cellSize-13);
        renderSeverityLabel(gc, px, py);
    }
}

class RoadCollapse extends Hazard {
    public RoadCollapse(int x, int y) {
        super(x, y, "Road Collapse", "RoadCollapse", 45 + Math.random()*30);
        setSpreadEnabled(false);
    }
    @Override protected int   defaultSpreadInterval()    { return 999; }
    @Override protected Hazard createChild(int x,int y)  { return new RoadCollapse(x, y); }
    @Override public    Color  getColor()                 { return Color.web("#888888"); }
    @Override public    String getLabel()                 { return "⚠"; }
    @Override
    public void render(GraphicsContext gc, int px, int py, int cellSize) {
        double cx = px + cellSize/2.0, cy = py + cellSize/2.0;
        renderHalo(gc, px, py, cellSize);
        gc.setFill(Color.web("#6b7280"));
        double[] dx = {cx, px+cellSize-4, cx, px+4};
        double[] dy = {py+4, cy, py+cellSize-4, cy};
        gc.fillPolygon(dx, dy, 4);
        gc.setStroke(Color.web("#111827"));
        gc.setLineWidth(1.0);
        gc.strokeLine(cx-4, cy-4, cx+4, cy+4);
        gc.strokeLine(cx+2, cy-6, cx-2, cy+6);
        renderSeverityLabel(gc, px, py);
    }
}