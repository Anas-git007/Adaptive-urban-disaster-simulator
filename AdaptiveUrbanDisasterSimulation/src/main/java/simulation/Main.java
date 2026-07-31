package simulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;


public class Main extends Application {

    
    private static final int MAP_COLS  = 15;
    private static final int MAP_ROWS  = 12;
    private static final int CELL_SIZE = 32;
    private static final int TICK_RATE = 5;

    private SimulationEngine engine;
    private RuleParser        ruleParser;
    private Canvas            canvas;
    private Label             tickLabel;
    private Label             ruleResultLabel;
    private TextField         ruleInput;
    private VBox              unitListBox;
    private VBox              hazardListBox;
    private VBox              rulesLogBox;
    private Label             stratDesc;
    private Button            btnStart;
    
    private final Label[]  statLabels = new Label[4];  
    private final Button[] stratBtns  = new Button[3]; 


    @Override
    public void start(Stage stage) {
        
        engine     = new SimulationEngine(MAP_COLS, MAP_ROWS);
        ruleParser = new RuleParser(engine);
        engine.showMultiTyping(); 

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#000000;");
        root.setTop(buildTopBar());
        root.setCenter(buildMainDashboard());

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        stage.setTitle("City Resilience Simulator — Emergency Operations Center");
        stage.setScene(new Scene(root,
                Math.min(1280, screen.getWidth()  * 0.98),
                Math.min(820,  screen.getHeight() * 0.95)));
        stage.setResizable(true);
        stage.show();

        startAnimationLoop();
    }


    private VBox buildTopBar() {
        Label title = monoLabel("🏙  Adaptive Urban Disaster SIMULATOR", 18, "#f8fafc", true);
        Label sub   = monoLabel("Tactical Operations Center — Operator Mode", 11, "#94a3b8", false);
        tickLabel   = monoLabel("Time: 0s", 12, "#38bdf8", true);

        btnStart            = actionBtn("▶  Start Simulation", "#10b981");
        Button btnHazard    = actionBtn("+ Add Hazard",        "#a855f7");
        Button btnReset     = actionBtn("↺  Reset Engine",     "#64748b");
        Button btnSpawnUnit = actionBtn("+ Add Unit",          "#06b6d4");

        btnSpawnUnit.setOnAction(e -> {
            String[] pool = {"ambulance","firetruck","police","citizen"};
            engine.addSpecificUnit(pool[(int)(Math.random()*pool.length)]);
        });
        btnStart.setOnAction(e -> {
            if (btnStart.getText().contains("Start")) {
                engine.start();
                btnStart.setText("⏸  Pause Simulation");
                btnStart.setStyle(btnStyle("#eab308","#0f172a"));
            } else {
                engine.pause();
                btnStart.setText("▶  Start Simulation");
                btnStart.setStyle(btnStyle("#10b981","white"));
            }
        });
        btnHazard.setOnAction(e -> engine.addRandomHazard());
        btnReset.setOnAction(e -> {
            engine.reset();
            highlightStrategy(engine.getStrategy());
            btnStart.setText("▶  Start Simulation");
            btnStart.setStyle(btnStyle("#10b981","white"));
        });

        HBox ctrlRow = new HBox(12, tickLabel, btnStart, btnHazard, btnSpawnUnit, btnReset);
        ctrlRow.setAlignment(Pos.CENTER_LEFT);

        HBox statsStrip = buildStatsStrip();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox middleRow = new HBox(0, ctrlRow, spacer, statsStrip);
        middleRow.setAlignment(Pos.CENTER_LEFT);
        middleRow.setPadding(new Insets(8,16,8,16));

        stratBtns[0] = stratBtn("Save Lives\nFirst");
        stratBtns[1] = stratBtn("Protect Assets\nFirst");
        stratBtns[2] = stratBtn("Nearest\nFirst");
        stratBtns[0].setOnAction(e -> { engine.setStrategy("saveLives");       highlightStrategy("saveLives"); });
        stratBtns[1].setOnAction(e -> { engine.setStrategy("protectProperty"); highlightStrategy("protectProperty"); });
        stratBtns[2].setOnAction(e -> { engine.setStrategy("nearest");         highlightStrategy("nearest"); });

        HBox stratRow = new HBox(8, stratBtns[0], stratBtns[1], stratBtns[2]);
        stratRow.setPadding(new Insets(4,16,0,16));

        stratDesc = monoLabel("", 11, "#34d399", false);
        stratDesc.setWrapText(true);
        stratDesc.setPadding(new Insets(6,16,8,16));
        highlightStrategy("saveLives");

        VBox top = new VBox(0);
        top.setStyle("-fx-background-color:#000000;-fx-border-color:#1e293b;-fx-border-width:0 0 1 0;");
        top.getChildren().addAll(padded(title,16,12,2), padded(sub,16,0,6),
                new Separator(), middleRow, stratRow, stratDesc);
        return top;
    }

    private void highlightStrategy(String s) {
        String on  = "-fx-background-color:#2563eb;-fx-text-fill:#ffffff;-fx-background-radius:4;"
                   + "-fx-font-family:Monospace;-fx-font-size:11;-fx-cursor:hand;-fx-font-weight:bold;"
                   + "-fx-border-color:#3b82f6;-fx-border-radius:4;";
        String off = "-fx-background-color:#1e293b;-fx-text-fill:#94a3b8;-fx-background-radius:4;"
                   + "-fx-font-family:Monospace;-fx-font-size:11;-fx-cursor:hand;-fx-border-color:transparent;";
        
        stratBtns[0].setStyle(s.equals("saveLives")       ? on : off);
        stratBtns[1].setStyle(s.equals("protectProperty") ? on : off);
        stratBtns[2].setStyle(s.equals("nearest")         ? on : off);
        switch (s) {
            case "saveLives"       -> stratDesc.setText("► Priority given to finding and treating injured individuals.");
            case "protectProperty" -> stratDesc.setText("► Priority given to stopping structural damage and chemical spreads.");
            case "nearest"         -> stratDesc.setText("► Emergency units head to the nearest incident to minimise travel time.");
        }
    }


    private HBox buildMainDashboard() {
        VBox leftPanel = buildRulePanel();
        leftPanel.setPrefWidth(400); leftPanel.setMinWidth(400); leftPanel.setMaxWidth(400);
        leftPanel.setStyle("-fx-background-color:#000000;-fx-border-color:#1e293b;-fx-border-width:0 1 0 0;");

        canvas = new Canvas(MAP_COLS * CELL_SIZE, MAP_ROWS * CELL_SIZE);
        canvas.setOnMouseClicked(e ->
            engine.addHazardAt((int)(e.getX()/CELL_SIZE),(int)(e.getY()/CELL_SIZE)));

        StackPane mapContainer = new StackPane(canvas);
        mapContainer.setStyle("-fx-background-color:#000000;");
        mapContainer.setPadding(new Insets(8));

        ScrollPane mapScroll = new ScrollPane(mapContainer);
        mapScroll.setFitToWidth(true); mapScroll.setFitToHeight(true);
        mapScroll.setStyle("-fx-background:#090d16;-fx-background-color:#000000;");

        ScrollPane sideScroll = new ScrollPane(buildSidePanel());
        sideScroll.setFitToWidth(true); sideScroll.setPrefWidth(290); sideScroll.setMinWidth(290);
        sideScroll.setStyle("-fx-background-color:#000000;-fx-background:#0f172a;"
                          + "-fx-border-color:#1e293b;-fx-border-width:0 0 0 1;");
        sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        HBox dashboard = new HBox(0, leftPanel, mapScroll, sideScroll);
        dashboard.setStyle("-fx-background-color:#000000;");
        HBox.setHgrow(mapScroll, Priority.ALWAYS);
        return dashboard;
    }

    private VBox buildSidePanel() {
        unitListBox   = new VBox(6);
        hazardListBox = new VBox(6);
        VBox panel = new VBox(10,
            monoLabel("EMERGENCY RESPONSE FLEET", 11, "#94a3b8", true), unitListBox,
            new Separator(),
            monoLabel("ACTIVE CRISIS SECTORS", 11, "#94a3b8", true), hazardListBox);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color:#000000;");
        return panel;
    }

    private HBox buildStatsStrip() {
        
        statLabels[0] = bigStat("0","#f87171");
        statLabels[1] = bigStat("0","#34d399");
        statLabels[2] = bigStat("0","#60a5fa");
        statLabels[3] = bigStat("0","#fbbf24");
        double w = 88;
        return new HBox(8,
            statBox(statLabels[0],"ACTIVE INC",   w),
            statBox(statLabels[1],"CONTAINED",    w),
            statBox(statLabels[2],"DISPATCHED",   w),
            statBox(statLabels[3],"UNITS ENGAGED",w));
    }

    private VBox statBox(Label num, String caption, double w) {
        Label c = monoLabel(caption, 8, "#94a3b8", true);
        c.setAlignment(Pos.CENTER);
        VBox b = new VBox(2, num, c);
        b.setAlignment(Pos.CENTER);
        b.setPadding(new Insets(6));
        b.setPrefWidth(w);
        b.setStyle("-fx-background-color:#000000;-fx-border-color:#334155;"
                 + "-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;");
        return b;
    }


    private VBox buildRulePanel() {
        Label header = monoLabel("TACTICAL COMMAND INPUT", 11, "#94a3b8", true);

        ruleInput = new TextField();
        ruleInput.setPromptText("Enter command here...");
        ruleInput.setStyle("-fx-background-color:#000000;-fx-text-fill:#f8fafc;"
                         + "-fx-prompt-text-fill:#475569;-fx-border-color:#334155;"
                         + "-fx-border-radius:4;-fx-background-radius:4;-fx-font-family:Monospace;");

        Button btnExec = actionBtn("Execute","#2563eb");
        Runnable exec = () -> {
            ruleResultLabel.setText(ruleParser.parse(ruleInput.getText()));
            ruleInput.clear();
        };
        btnExec.setOnAction(e -> exec.run());
        ruleInput.setOnAction(e -> exec.run());

        ruleResultLabel = new Label("Terminal Ready.");
        ruleResultLabel.setFont(Font.font("Monospace",11));
        ruleResultLabel.setTextFill(Color.web("#34d399"));
        ruleResultLabel.setWrapText(true);
        ruleResultLabel.setPrefWidth(350);

        HBox inputRow = new HBox(8, ruleInput, btnExec);
        HBox.setHgrow(ruleInput, Priority.ALWAYS);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        VBox staticTop = new VBox(8, header, inputRow, ruleResultLabel, new Separator());
        staticTop.setPadding(new Insets(14,14,0,14));

        Label cheatHdr = monoLabel("RULE REFERENCE:", 10, "#64748b", true);
        Label logHdr   = monoLabel("LIVE SYSTEM OVERRIDES:", 10, "#38bdf8", true);
        rulesLogBox = new VBox(2);

        VBox scrollLayout = new VBox(10, cheatHdr, buildCheatSheet(), new Separator(), logHdr, rulesLogBox);
        scrollLayout.setPadding(new Insets(6,14,14,14));
        scrollLayout.setStyle("-fx-background-color:#000000;");

        ScrollPane innerScroll = new ScrollPane(scrollLayout);
        innerScroll.setFitToWidth(true);
        innerScroll.setStyle("-fx-background:#090d16;-fx-background-color:#000000;-fx-border-color:transparent;");
        innerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        innerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox master = new VBox(0, staticTop, innerScroll);
        VBox.setVgrow(innerScroll, Priority.ALWAYS);
        master.setStyle("-fx-background-color:#000000;");
        return master;
    }

    private VBox buildCheatSheet() {
        String[][] rows = {
            {"set panic radius 5",       "Citizen panic zone radius"},
            {"set panic drain 0.8",      "Citizen panic health drain/tick"},
            {"set wander chance 0.6",    "Citizen random movement chance"},
            {"set heal amount 30",       "Ambulance health restored/tick"},
            {"set fuel cost 0.3",        "Fuel consumed per move"},
            {"set extinguish power 15",  "Firetruck extinguishing power"},
            {"set water drain 2.0",      "Firetruck water drain rate"},
            {"set clear power 12",       "Police clearing speed"},
            {"set response distance 8",  "Max dispatch search radius"},
            {"set growth rate 0.2",      "Hazard growth multiplier"},
            {"set fire spread 3",        "Fire spread tick interval"},
            {"set chem spread 6",        "Chem spread tick interval"},
            {"set refuel rate 0.5",      "Auto-refuel rate/tick"},
            {"enable / disable ambulance","Toggle ambulance dispatch"},
            {"enable / disable firetruck","Toggle firetruck dispatch"},
            {"enable / disable police",   "Toggle police dispatch"},
            {"stop / resume fire spread", "Freeze/thaw fire spread"},
            {"stop / resume chem spread", "Freeze/thaw chem spread"},
            {"stop / resume all spread",  "Freeze/thaw all spread"},
            {"block roads on / off",      "Hazards block BFS roads"},
            {"citizens flee on / off",    "Citizen flee behaviour"},
            {"auto refuel on / off",      "Vehicle auto-refuel"},
            {"strategy save lives",       "Prioritise civilian health"},
            {"strategy protect property", "Prioritise structures"},
            {"strategy nearest",          "Nearest-first dispatch"},
            {"add fire / chem / casualty","Spawn specific hazard"},
            {"add collapse / hazard",     "Spawn collapse or random"},
            {"clear hazards",             "Remove all active hazards"},
            {"reset rules",               "Revert all rules to defaults"},
            {"reset",                     "Full simulation hard-reset"},
        };
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(5);
        for (int i = 0; i < rows.length; i++) {
            Label cmd  = new Label(rows[i][0]);
            cmd.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            cmd.setTextFill(Color.web("#38bdf8"));
            Label desc = new Label("› " + rows[i][1]);
            desc.setWrapText(true); desc.setMaxWidth(130);
            desc.setFont(Font.font("SansSerif", 11));
            desc.setTextFill(Color.web("#94a3b8"));
            grid.add(cmd, 0, i); grid.add(desc, 1, i);
        }
        VBox container = new VBox(grid);
        container.setStyle("-fx-background-color:#000000;-fx-padding:10;");
        return container;
    }


    private void startAnimationLoop() {
        final long[] last  = {0};
        final long TICK_NS = 1_000_000_000L / TICK_RATE;
        new AnimationTimer() {
            @Override public void handle(long now) {
                if (now - last[0] >= TICK_NS) {
                    last[0] = now;
                    engine.tick();
                    render();
                    updateSidePanel();
                    updateStats();
                    updateRulesLog();
                    int avgFuel = (int)(engine.getAverageFuelRatio() * 100);
                    tickLabel.setText("Time: " + engine.getTickCount() + "s  |  Fleet fuel: " + avgFuel + "%");
                }
            }
        }.start();
    }

   
    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        CityMap map = engine.getMap();

        
        int ROAD     = CityMap.getCellTypeConstant("ROAD");
        int BUILDING = CityMap.getCellTypeConstant("BUILDING");
        int SAFEZONE = CityMap.getCellTypeConstant("SAFEZONE");

       
        for (int col = 0; col < MAP_COLS; col++) {
            for (int row = 0; row < MAP_ROWS; row++) {
                int px = col * CELL_SIZE, py = row * CELL_SIZE;
                int ct = map.getCellType(col, row);

                if (ct == ROAD) {
                    gc.setFill(Color.web("#0f172a"));
                    gc.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    gc.setStroke(Color.web("#1e293b",0.4));
                    gc.setLineWidth(1.0);
                    gc.strokeLine(px+CELL_SIZE/2.0,py, px+CELL_SIZE/2.0,py+CELL_SIZE);
                } else if (ct == BUILDING) {
                    gc.setFill(Color.web("#1e293b"));
                    gc.fillRect(px+1,py+1,CELL_SIZE-2,CELL_SIZE-2);
                    gc.setStroke(Color.web("#334155"));
                    gc.setLineWidth(0.75);
                    gc.strokeRect(px+1,py+1,CELL_SIZE-2,CELL_SIZE-2);
                } else if (ct == SAFEZONE) {
                    gc.setFill(Color.web("#022c22"));
                    gc.fillRect(px,py,CELL_SIZE,CELL_SIZE);
                    gc.setStroke(Color.web("#059669",0.3));
                    gc.strokeRect(px,py,CELL_SIZE,CELL_SIZE);
                } else {
                    gc.setFill(Color.web("#020617"));
                    gc.fillRect(px,py,CELL_SIZE,CELL_SIZE);
                }
            }
        }

    
        for (Entity e : List.copyOf(engine.getEntities())) {
            if (!e.isActive()) continue;
            e.render(gc, e.getX()*CELL_SIZE, e.getY()*CELL_SIZE, CELL_SIZE);
        }
    }

   
    private void updateSidePanel() {
        unitListBox.getChildren().clear();
        hazardListBox.getChildren().clear();

        for (Entity e : engine.getEntities()) {
            
            if (e.isFleetUnit()) {
                unitListBox.getChildren().add(unitCard(e));
            }
        }
        for (Entity e : engine.getEntities()) {
            if (e instanceof Hazard h && h.isActive()) {
                hazardListBox.getChildren().add(hazardCard(h));
            }
        }
        if (hazardListBox.getChildren().isEmpty()) {
            hazardListBox.getChildren().add(
                monoLabel("  ⚡ No active threats.", 10, "#10b981", false));
        }
    }

  
    private VBox unitCard(Entity e) {
        
        String emoji    = e.getUnitEmoji();      
        String resLabel = e.getResourceLabel();   
        double maxRes   = e.getMaxResource();     

        Label nm  = monoLabel(emoji + " " + e.getName(), 11, "#f1f5f9", true);
        boolean idle = e.getStatus().equals("IDLE") || e.getStatus().equals("PATROLLING");
        Label st  = monoLabel(e.getStatus(), 10, idle ? "#34d399" : "#fbbf24", true);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox hdr = new HBox();
        hdr.getChildren().addAll(nm, sp, st);
        hdr.setAlignment(Pos.CENTER_LEFT);

        HBox b1 = new HBox(6,
            monoLabel("FUEL", 9, "#94a3b8", false),
            pbar(e.getFuel()/e.getMaxFuel(), Color.web("#10b981"), Color.web("#ef4444"), 120));
        HBox b2 = new HBox(6,
            monoLabel(resLabel, 9, "#94a3b8", false),
            pbar(e.getSupplies()/maxRes, Color.web("#3b82f6"), Color.web("#fbbf24"), 120));

        VBox card = new VBox(4, hdr, b1, b2);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color:#1e293b;-fx-background-radius:4;"
                    + "-fx-border-color:#334155;-fx-border-width:1;-fx-border-radius:4;");
        return card;
    }

    private VBox hazardCard(Hazard h) {
        
        String emoji = switch (h.getHazardType()) {
            case "StructureFire" -> "🔥";
            case "ChemSpill"     -> "☣";
            case "MassCasualty"  -> "✚";
            case "RoadCollapse"  -> "⚠";
            default              -> "!";
        };
        Label nm  = monoLabel(emoji+"  "+h.getName(), 11, toHex(h.getColor()), true);
        Label pct = monoLabel((int)h.getSeverity()+"%", 10, "#f8fafc", true);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox hdr  = new HBox(); hdr.getChildren().addAll(nm,sp,pct);
        hdr.setAlignment(Pos.CENTER_LEFT);
        HBox bar = pbar(h.getSeverity()/100.0,
                h.getColor().deriveColor(0,1,1.1,1),
                h.getColor().deriveColor(0,1,0.6,1), 220);
        VBox card = new VBox(4, hdr, bar);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color:#1e293b;-fx-background-radius:6;"
                    + "-fx-border-color:#334155;-fx-border-width:1;");
        return card;
    }


    private void updateStats() {
       
        engine.getRules().logRule("Critical unit", engine.getCriticalUnitReport());
        
        statLabels[0].setText(String.valueOf(engine.getActiveHazardCount()));
        statLabels[1].setText(String.valueOf(engine.getContainedCount()));
        statLabels[2].setText(String.valueOf(engine.getDispatchCount()));
        statLabels[3].setText(String.valueOf(engine.getUnitsBusy()));
    }

    private void updateRulesLog() {
        rulesLogBox.getChildren().clear();
        Map<String,String> log = engine.getRules().getRuleLog();
        if (log.isEmpty()) {
            rulesLogBox.getChildren().add(
                monoLabel("  (Standard guidelines active)", 10, "#475569", false));
            return;
        }
        for (Map.Entry<String,String> entry : log.entrySet()) {
            Label l = new Label("  • " + entry.getKey() + " → " + entry.getValue());
            l.setWrapText(true); l.setMaxWidth(340);
            l.setFont(Font.font("Monospace",10));
            l.setTextFill(Color.web("#34d399"));
            rulesLogBox.getChildren().add(l);
        }
    }


    private Label monoLabel(String t, int sz, String hex, boolean bold) {
        Label l = new Label(t);
        l.setFont(bold ? Font.font("Monospace",FontWeight.BOLD,sz) : Font.font("Monospace",sz));
        l.setTextFill(Color.web(hex));
        return l;
    }

    private Label bigStat(String t, String hex) {
        Label l = new Label(t);
        l.setFont(Font.font("Monospace",FontWeight.BOLD,16));
        l.setTextFill(Color.web(hex));
        return l;
    }

    private Button actionBtn(String t, String hex) {
        Button b = new Button(t);
        b.setFont(Font.font("Monospace",FontWeight.BOLD,12));
        b.setStyle("-fx-background-color:"+hex+";-fx-text-fill:white;-fx-background-radius:4;"
                 + "-fx-cursor:hand;-fx-padding:6 14 6 14;");
        return b;
    }

    private Button stratBtn(String t) {
        Button b = new Button(t);
        b.setFont(Font.font("Monospace",11));
        b.setPrefWidth(120); b.setWrapText(true);
        b.setStyle("-fx-background-color:#1e293b;-fx-text-fill:#94a3b8;-fx-background-radius:4;"
                 + "-fx-cursor:hand;-fx-padding:5 8 5 8;-fx-border-color:#334155;-fx-border-radius:4;");
        return b;
    }

    private String btnStyle(String bg, String fg) {
        return "-fx-background-color:"+bg+";-fx-text-fill:"+fg+";-fx-background-radius:4;"
             + "-fx-cursor:hand;-fx-padding:6 14 6 14;-fx-font-family:Monospace;-fx-font-weight:bold;";
    }

    private HBox pbar(double ratio, Color ok, Color low, double maxW) {
        double w = Math.max(2, ratio * maxW);
        javafx.scene.shape.Rectangle track = new javafx.scene.shape.Rectangle(maxW, 5);
        track.setFill(Color.web("#334155")); track.setArcWidth(4); track.setArcHeight(4);
        javafx.scene.shape.Rectangle fill = new javafx.scene.shape.Rectangle(w, 5);
        fill.setFill(ratio > 0.35 ? ok : low); fill.setArcWidth(4); fill.setArcHeight(4);
        javafx.scene.layout.StackPane bar = new javafx.scene.layout.StackPane(track, fill);
        bar.setAlignment(Pos.CENTER_LEFT);
        return new HBox(bar);
    }

    private HBox padded(javafx.scene.Node n, int h, int top, int bot) {
        HBox b = new HBox(n); b.setPadding(new Insets(top,h,bot,h)); return b;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
            (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
    }

    public static void main(String[] args) {
        System.setProperty("glass.win.uiScale","1.0");
        System.setProperty("glass.mac.uiScale","1.0");
        System.setProperty("glass.gtk.uiScale","1.0");
        launch(args);
    }
}
