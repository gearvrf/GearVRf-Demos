package org.gearvrf.arpet.characterstates;

public interface IState {
    int id();
    void entry();
    void exit();
    void run();
}
