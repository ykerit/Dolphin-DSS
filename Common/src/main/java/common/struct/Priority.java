package common.struct;

import java.io.Serializable;

public class Priority implements Comparable<Priority>, Serializable {

    public static final Priority UNDEFINED = newInstance(-1);

    private int priority;

    public static Priority newInstance(int p) {
        Priority priority = new Priority();
        priority.setPriority(p);
        return priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        final int prime = 517861;
        int result = 9511;
        result = prime * result + getPriority();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Priority other = (Priority) obj;
        if (getPriority() != other.getPriority())
            return false;
        return true;
    }

    @Override
    public int compareTo(Priority other) {
        return other.getPriority() - this.getPriority();
    }

    @Override
    public String toString() {
        return "{Priority: " + getPriority() + "}";
    }
}
