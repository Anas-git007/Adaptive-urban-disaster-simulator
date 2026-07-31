package simulation;

import java.util.ArrayList;
import java.util.List;
 
public class SimulationEngine {

    private final CityMap                map;
    private final SimulationRules        rules;
    private final List<Entity>           entities;    
    private final List<IncidentResponder>responders;  
    private final List<Movable>          movables;    

    private Ambulance[] ambulances;
    private FireTruck[] fireTrucks;
    private Police[]    policeUnits;
    private Citizen[]   citizens;

    private int     tickCount      = 0;
    private boolean running        = false;
    private String  strategy       = "saveLives";
    private int     containedCount = 0;
    private int     dispatchCount  = 0;

  

    public SimulationEngine(int mapWidth, int mapHeight) {
        this.map        = new CityMap(mapWidth, mapHeight);
        this.entities   = new ArrayList<>();
        this.responders = new ArrayList<>();
        this.movables   = new ArrayList<>();
        this.rules      = new SimulationRules();
        spawnInitialEntities();
    }

    

    private void spawnInitialEntities() {
        ambulances = new Ambulance[]{
            new Ambulance(0, 0, "Ambulance α"),
            new Ambulance(0, 0, "Ambulance β")
        };
        fireTrucks = new FireTruck[]{
            new FireTruck(0, 0, "FireTruck 1"),
            new FireTruck(0, 0, "FireTruck 2")
        };
        policeUnits = new Police[]{
            new Police(0, 0, "Police 1")
        };
        citizens = new Citizen[]{
            new Citizen(0, 0, "Citizen 1"),
            new Citizen(0, 0, "Citizen 2"),
            new Citizen(0, 0, "Citizen 3"),
            new Citizen(0, 0, "Citizen 4"),
            new Citizen(0, 0, "Citizen 5")
        };

        for (Ambulance a : ambulances)  spawn(a);
        for (FireTruck f : fireTrucks)  spawn(f);
        for (Police    p : policeUnits) spawn(p);
        for (Citizen   c : citizens)    spawn(c);

        int[] pos = map.randomRoadCell();
        StructureFire sf = new StructureFire(pos[0], pos[1]);
        sf.setRules(rules);
        entities.add(sf);
    }

    
    private void spawn(Entity e) {
        int[] pos = map.randomRoadCell();
        e.setPosition(pos[0], pos[1]);
        e.setRules(rules);
        entities.add(e);                           

        if (e instanceof IncidentResponder r)
            responders.add(r);                     

        movables.add(e);                           
    }
 

    public void tick() {
        if (!running) return;
        tickCount++;
        map.clearBlocks();

        long busyBefore = countBusy();

        List<Entity> snapshot = new ArrayList<>(entities);
        List<Entity> activeHazardsBefore = snapshot.stream()
            .filter(e -> e instanceof Hazard && e.isActive())
            .toList();

         
        for (Entity e : snapshot) {
            if (e.isActive()) e.update(map, entities);
        }

        for (Entity e : entities) {
            if (!e.hasRules()) e.setRules(rules);
        }

        for (Entity h : activeHazardsBefore) {
            if (!h.isActive()) containedCount++;
        }

        long busyAfter = countBusy();
        if (busyAfter > busyBefore) dispatchCount += (int)(busyAfter - busyBefore);

        entities.removeIf(e -> !e.isActive());
        responders.removeIf(r -> (r instanceof Entity e) && !e.isActive());
        movables.removeIf(m -> (m instanceof Entity e) && !e.isActive());
    }
 
    private long countBusy() {
        return responders.stream()
            .filter(r -> r.getStatus().equals("RESPONDING")
                      || r.getStatus().equals("EN ROUTE"))
            .count();
    }

 
    public void setStrategy(String s) {
        this.strategy = s;
        for (IncidentResponder r : responders) {
            r.setStrategy(s);                      
        }
    }
 
    public double getAverageFuelRatio() {
        if (movables.isEmpty()) return 0.0;
        double total = 0.0;
        for (Movable m : movables) {              
            total += m.getFuel() / m.getMaxFuel();
        }
        return total / movables.size();
    }
 
    public String getCriticalUnitReport() {
        Movable lowest = null;
        for (Movable m : movables) {             
            if (lowest == null || m.getFuel() < lowest.getFuel())
                lowest = m;
        }
        if (lowest == null) return "No units";
       
        String unitName = (lowest instanceof Entity e) ? e.getName() : "Unknown";
        return unitName + " — fuel: " + String.format("%.0f%%",
               (lowest.getFuel() / lowest.getMaxFuel()) * 100);
    }
 
 
    public void showMultiTyping() {
        
        Ambulance liveAmbulance = ambulances[0];

        Entity            asEntity    = liveAmbulance;  
        Movable           asMovable   = liveAmbulance;  
        IncidentResponder asResponder = liveAmbulance;  
        Renderable        asRenderable= liveAmbulance;  

   
        boolean isDeployable = asEntity.isActive() && asEntity.getFuel() > 20;
        double fuelRatio    = asMovable.getFuel() / asMovable.getMaxFuel();
        int    travelRange  = (int)(fuelRatio * 20); 
        asResponder.setStrategy(this.strategy);   
        String currentStatus   = asResponder.getStatus();
        int    incidentsServed = asResponder.getIncidentCount();
 
        boolean renderReady = (asRenderable != null);
 
        System.out.println("\n");
        System.out.println("   MULTI-TYPING TRIAGE REPORT (startup check) ");
        System.out.println("\n");
        System.out.printf( "  Unit        (via Entity)           : %-8s%n", asEntity.getName());
        System.out.printf( "  Deployable  (via Entity.isActive)  : %-8s%n", isDeployable);
        System.out.printf( "  Fuel ratio  (via Movable)          : %-8s%n",
                           String.format("%.0f%%", fuelRatio * 100));
        System.out.printf( "  Travel range(via Movable)          : %-5d cells%n", travelRange);
        System.out.printf( "  Status      (via IncidentResponder): %-8s%n", currentStatus);
        System.out.printf( "  Incidents   (via IncidentResponder): %-8d%n", incidentsServed);
        System.out.printf( "  Render ready(via Renderable)       : %-8s%n", renderReady);
        System.out.println("\n");
    } 
    
    public void addRandomHazard() {
        int[] p = map.randomRoadCell();
        addHazardAt(p[0], p[1]);
    }

    public void addHazardAt(int x, int y) {
        if (!map.isRoad(x, y)) return;
        int type = (int)(Math.random() * 4);
        Hazard h = switch (type) {
            case 0  -> new StructureFire(x, y);
            case 1  -> new ChemSpill(x, y);
            case 2  -> new MassCasualty(x, y);
            default -> new RoadCollapse(x, y);
        };
        h.setRules(rules);
        entities.add(h);
    }

    public void addSpecificHazard(String type) {
        int[] p = map.randomRoadCell();
        Hazard h = switch (type.toLowerCase()) {
            case "fire"     -> new StructureFire(p[0], p[1]);
            case "chem"     -> new ChemSpill(p[0], p[1]);
            case "casualty" -> new MassCasualty(p[0], p[1]);
            case "collapse" -> new RoadCollapse(p[0], p[1]);
            default         -> new StructureFire(p[0], p[1]);
        };
        h.setRules(rules);
        entities.add(h);
    }

 
    public void addSpecificUnit(String type) {
        Entity e = switch (type.toLowerCase()) {
            case "ambulance" -> new Ambulance(0, 0, "Ambulance Extra");
            case "firetruck" -> new FireTruck(0, 0, "FireTruck Extra");
            case "police"    -> new Police(0, 0, "Police Extra");
            case "citizen"   -> new Citizen(0, 0, "Citizen Extra");
            default          -> null;
        };
        if (e != null) spawn(e);
    }

   
    public void start() { running = true; }
    public void pause() { running = false; }

    public void reset() {
        running  = false;
        entities.clear();
        responders.clear();
        movables.clear();
        map.clearBlocks();
        tickCount      = 0;
        containedCount = 0;
        dispatchCount  = 0;
        rules.resetToDefaults();
        spawnInitialEntities();
        setStrategy(strategy);
    }

  
    public CityMap         getMap()            { return map; }
    public List<Entity>    getEntities()       { return entities; }
    public SimulationRules getRules()          { return rules; }
    public boolean         isRunning()         { return running; }
    public int             getTickCount()      { return tickCount; }
    public int             getContainedCount() { return containedCount; }
    public int             getDispatchCount()  { return dispatchCount; }
    public String          getStrategy()       { return strategy; }

    public long getActiveHazardCount() {
        return entities.stream()
            .filter(e -> e instanceof Hazard && e.isActive())
            .count();
    }

    public long   getUnitsBusy()       { return countBusy(); }

 
    public Ambulance[] getAmbulances()  { return ambulances; }
    public FireTruck[] getFireTrucks()  { return fireTrucks; }
    public Police[]    getPoliceUnits() { return policeUnits; }
    public Citizen[]   getCitizens()    { return citizens; }
}