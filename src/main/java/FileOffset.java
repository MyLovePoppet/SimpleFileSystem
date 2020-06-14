import java.util.Objects;

public class FileOffset{
    public final long start, end, size;

    public FileOffset(long start, long end) {
        this.start = start;
        this.end = end;
        this.size = this.end - this.start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileOffset that = (FileOffset) o;
        return start == that.start &&
                end == that.end &&
                size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, size);
    }

    @Override
    public String toString() {
        return "FileOffset{" +
                "start=" + start +
                ", end=" + end +
                ", size=" + size +
                '}';
    }
}
