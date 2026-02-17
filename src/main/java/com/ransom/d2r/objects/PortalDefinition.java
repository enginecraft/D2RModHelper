package com.ransom.d2r.objects;

public class PortalDefinition {
    public String name;
    public int normalLevel;
    public int nightmareLevel;
    public int hellLevel;
    public int numMonsters;
    public int monDen;

    public PortalDefinition(String name, int normalLevel, int nightmareLevel, int hellLevel) {
        this.name = name;
        this.normalLevel = normalLevel;
        this.nightmareLevel = nightmareLevel;
        this.hellLevel = hellLevel;
        this.numMonsters = -1;
        this.monDen = -1;
    }
}
