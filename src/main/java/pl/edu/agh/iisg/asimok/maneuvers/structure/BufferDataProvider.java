package pl.edu.agh.iisg.asimok.maneuvers.structure;


class BufferDataProvider<E extends TimelineItem> implements StreamDataProvider<E> {
    private final RollingBuffer<E> buffer;
    private final int offset;
    
    BufferDataProvider(RollingBuffer<E> buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    @Override
    public E get(int idx) {
        return buffer.get(idx);
    }

    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public int lastSeen() {
        return buffer.size() - offset - 1;
    }
}
