package nju.ics.Entity;

public class VehicleType {
    int type;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VehicleType))
            return false;
        VehicleType anotherVehicleType = (VehicleType) obj;
        return anotherVehicleType.type == this.type;
    }
}
