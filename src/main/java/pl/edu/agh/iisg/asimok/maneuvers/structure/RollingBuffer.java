package pl.edu.agh.iisg.asimok.maneuvers.structure;

public interface RollingBuffer<E extends TimelineItem> {
    void add(E item);
    void addAll(Iterable<? extends E> items);
    E get(int idx);
    int size();
}
