package pl.edu.agh.iisg.asimok.maneuvers.structure;

public interface StreamDataProvider<E> {
    E get(int idx);
    int size();
    int lastSeen();
}
