package nju.ics.Entity;

public class TollUnit {
    String index;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TollUnit)) {
            return false;
        }
        TollUnit anotherTollUnit = (TollUnit) obj;
        return index!= null && anotherTollUnit.index != null && index.equals(anotherTollUnit.index);
    }
}
