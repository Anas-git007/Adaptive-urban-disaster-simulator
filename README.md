# Adaptive Urban Disaster Simulator

A Java-based emergency response simulation platform built with **JavaFX** that models dynamic urban disasters, autonomous emergency units, hazard propagation, and real-time dispatch strategies within a city environment.

The project was developed as an advanced Object-Oriented Programming (OOP) application and demonstrates the practical application of software engineering principles including abstraction, encapsulation, inheritance, polymorphism, composition, interface-driven design, generics, exception handling, and design modularity.

---

## Features

### Dynamic Urban Environment

* 15 × 12 city grid simulation
* Road, Building, and Safe Zone terrain types
* Interactive hazard placement
* Real-time visual rendering using JavaFX Canvas

### Emergency Response Units

* 🚑 Ambulances
* 🚒 Fire Trucks
* 🚓 Police Units
* 👥 Citizens

Each unit acts autonomously and makes independent decisions based on the current simulation state.

### Disaster Types

* 🔥 Structure Fires
* ☣️ Chemical Spills
* 🚧 Road Collapses
* 🏥 Mass Casualty Incidents

Hazards can grow, spread, and affect city operations dynamically.

### Intelligent Dispatch System

Three configurable response strategies:

* Save Lives
* Protect Property
* Nearest Incident

Responders automatically evaluate and prioritize incidents according to the selected strategy.

### BFS Pathfinding Engine

* Breadth-First Search navigation
* Shortest-path calculation on road networks
* Obstacle-aware routing
* Dynamic road blockage handling

### Runtime Rule Customization

Simulation parameters can be modified while the simulator is running through a command console.

Examples:

```text
set fireSpreadInterval 5
set ambulanceHealAmount 25
set autoRefuel true
set maxResponseDistance 50
```

### Exception Handling & Recovery

Custom exception hierarchy:

* SimulationConfigException
* OutOfBoundsRuleException

Invalid configuration values are automatically corrected through recovery logic without interrupting the simulation.

---

## OOP Concepts Demonstrated

### Encapsulation

Internal state is protected through controlled access methods and validation logic.

### Information Hiding

Private attributes, immutable fields, and controlled visibility enforcement.

### Inheritance

Structured class hierarchy built around the abstract `Entity` base class.

### Composition

Complex behaviors are achieved through object composition rather than excessive inheritance.

### Abstraction

Core behavior contracts are defined through abstract classes and interfaces.

### Polymorphism

Implementation includes:

* Inclusion Polymorphism
* Parametric Polymorphism (Generics)
* Method Overloading
* Coercion Polymorphism

### Interface-Based Design

The simulator uses multiple interfaces including:

* Movable
* Renderable
* IncidentResponder
* UnitCardProvider

---

## Project Architecture

```text
Entity
│
├── Citizen
├── Ambulance
├── FireTruck
├── Police
│
└── Hazard
    ├── StructureFire
    ├── ChemSpill
    ├── RoadCollapse
    └── MassCasualty
```

Core components:

```text
SimulationEngine
CityMap
SimulationRules
RuleParser
ScoredCandidate<T>
JavaFX GUI (Main)
```

---

## Technologies Used

* Java
* JavaFX
* Object-Oriented Programming
* Breadth-First Search (BFS)
* Generic Programming
* Custom Exception Handling

---

## Simulation Workflow

```text
Read State
    ↓
Update Entities
    ↓
Resolve Interactions
    ↓
Render Frame
    ↓
Repeat (5 Hz)
```

The simulation updates at a fixed rate of 5 ticks per second, ensuring deterministic behavior and predictable execution.

---

## Future Improvements

* A* Pathfinding
* Dijkstra-Based Routing
* AI Fleet Coordination
* Smart Resource Allocation
* Additional Disaster Types
* Weather and Traffic Systems
* Multi-City Simulations

---


## Academic Context

Developed as an Object-Oriented Programming project for the Bachelor of Computer Science (Data Analysis) program.

The project emphasizes practical application of advanced OOP principles through a realistic emergency management simulation.

---

## Author

**Muhammad Anas**

Bachelor of Computer Science (Data Analysis)

University of Messina (UNIME)

Italy

---

## License

This project is released under the MIT License.
