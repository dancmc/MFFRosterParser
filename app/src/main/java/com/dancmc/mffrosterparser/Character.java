package com.dancmc.mffrosterparser;

import java.util.Arrays;

/**
 * Created by Daniel on 23/03/2017.
 */

public class Character {

    public String id;
    public String uniform = "";
    public Uniforms uniforms = new Uniforms();
    public int level = 0;
    public int tier = 1;
    public Attack attack = new Attack();
    public Defense defense = new Defense();
    public int hp = 0;
    public double dodge = 0;
    public double ignore_dodge = 0;
    public double defpen = 0;
    public double scd = 0;
    public double critrate = 0;
    public double critdamage = 0;
    public double atkspeed = 0;
    public double recorate = 0;
    public double movspeed = 0;
    public double debuff = 0;
    public final int[] skills = {1, 1, 1, 1, 1};
    public GearValue[][] gear = new GearValue[4][8];
    public long lastUpdate = 0;

    public Character() {
        for(int i = 0;i<gear.length;i++) {
            for(int j=0;j<gear[i].length;j++){
                gear[i][j] = new GearValue();
            }
        }
    }


    public class Uniforms {

    }

    public class Attack {
        public int physical;
        public int energy;

        public Attack() {
            physical = 0;
            energy = 0;
        }
    }

    public class Defense {
        public int physical;
        public int energy;

        public Defense() {
            physical = 0;
            energy = 0;
        }
    }

    public class GearValue {

        public String type = "";
        public double val = 0;
        public boolean pref = false;
        public double percent = 0;
    }


}
