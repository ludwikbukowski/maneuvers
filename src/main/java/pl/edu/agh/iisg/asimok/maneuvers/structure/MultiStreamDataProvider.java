package pl.edu.agh.iisg.asimok.maneuvers.structure;

public interface MultiStreamDataProvider<E> {
    Iterable<Object> getStreamTags();
    boolean containsStreamTag(Object tag);
    StreamDataProvider<E> getStream(Object tag);
}
